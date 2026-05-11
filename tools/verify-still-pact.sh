#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 - "$root" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

ANDROID_NAME = "{http://schemas.android.com/apk/res/android}name"
USE_PERMISSION_TAGS = {
    "uses-permission",
    "uses-permission-sdk-23",
    "uses-permission-sdk-m",
}
DYNAMIC_RECEIVER_SUFFIX = ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"

EXPECTED_SOURCE_PERMISSIONS = {
    "still-launcher": set(),
    "still-notes": set(),
    "still-cal": {
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.SCHEDULE_EXACT_ALARM",
        "android.permission.RECEIVE_BOOT_COMPLETED",
    },
    "still-clock": {
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.SCHEDULE_EXACT_ALARM",
        "android.permission.USE_EXACT_ALARM",
        "android.permission.RECEIVE_BOOT_COMPLETED",
        "android.permission.USE_FULL_SCREEN_INTENT",
        "android.permission.WAKE_LOCK",
        "android.permission.VIBRATE",
    },
}
FORBIDDEN_SDK_TOKENS = [
    "com.google.firebase",
    "com.google.android.gms",
    "google-services",
    "firebase",
    "crashlytics",
    "analytics",
    "mixpanel",
    "amplitude",
]
NUMBER_WORDS = {
    "no": 0,
    "zero": 0,
    "one": 1,
    "two": 2,
    "three": 3,
    "four": 4,
    "five": 5,
    "six": 6,
    "seven": 7,
    "eight": 8,
    "nine": 9,
    "ten": 10,
}
SKIP_CONFIG_DIRS = {".git", ".gradle", "build"}


def local_name(tag):
    return tag.rsplit("}", 1)[-1] if tag.startswith("{") else tag


def read_xml(path):
    try:
        return ET.fromstring(path.read_text(encoding="utf-8"))
    except ET.ParseError as exc:
        raise ValueError(f"{path}: invalid XML: {exc}") from exc


def manifest_names(path, tag_names):
    root_element = read_xml(path)
    names = []
    for element in root_element.iter():
        if local_name(element.tag) in tag_names:
            name = element.attrib.get(ANDROID_NAME) or element.attrib.get("android:name")
            if name:
                names.append(name)
    return names


def manifest_package(path):
    return read_xml(path).attrib.get("package")


def source_manifests(root):
    src = root / "app/src"
    if not src.is_dir():
        return []
    return sorted(path for path in src.rglob("AndroidManifest.xml") if path.is_file())


def format_names(names) -> str:
    ordered = sorted(names)
    return ", ".join(ordered) if ordered else "none"


def compare_exact(errors, label, actual, expected):
    missing = expected - actual
    unexpected = actual - expected
    if missing:
        errors.append(f"{label}: missing permissions: {format_names(missing)}")
    if unexpected:
        errors.append(f"{label}: unexpected permissions: {format_names(unexpected)}")


def is_app_dynamic_receiver_permission(permission, package_name):
    if not permission.endswith(DYNAMIC_RECEIVER_SUFFIX):
        return False
    prefix = permission[: -len(DYNAMIC_RECEIVER_SUFFIX)]
    if package_name:
        return prefix == package_name
    return bool(re.fullmatch(r"[A-Za-z][A-Za-z0-9_.]*", prefix))


def readme_permission_count(path):
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip().lower()
        if "permissions" not in line:
            continue
        if "|" in line and "what it guarantees" in line:
            continue
        if "no `internet` permission" in line or "no internet permission" in line:
            continue
        match = re.search(
            r"\b(no|zero|one|two|three|four|five|six|seven|eight|nine|ten|\d+)"
            r"\b(?:\s+\w+){0,4}\s+permissions\b",
            line,
        )
        if match:
            token = match.group(1)
            return int(token) if token.isdigit() else NUMBER_WORDS[token]
    return None


def config_files(root):
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(root)
        if any(part in SKIP_CONFIG_DIRS for part in rel.parts[:-1]):
            continue
        name = path.name
        if (
            name.endswith(".gradle")
            or name.endswith(".gradle.kts")
            or name in {"settings.gradle", "settings.gradle.kts"}
            or name.endswith(".versions.toml")
        ):
            yield path


def latest_input_mtime(root):
    paths = source_manifests(root)
    return max((path.stat().st_mtime for path in paths if path.is_file()), default=0.0)


def strip_config_comments(path, text):
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    marker = "#" if path.name.endswith(".toml") else "//"
    return "\n".join(line.split(marker, 1)[0] for line in text.splitlines())


def merged_manifests(root):
    intermediates = root / "app/build/intermediates"
    if not intermediates.is_dir():
        return []

    manifests = []
    for path in intermediates.rglob("AndroidManifest.xml"):
        rel = path.relative_to(root)
        parts = rel.parts
        if any("merged" in part for part in parts):
            manifests.append(path)
    return sorted(set(manifests))


root = Path(sys.argv[1])
name = root.name
manifest = root / "app/src/main/AndroidManifest.xml"
readme = root / "README.md"
expected_permissions = EXPECTED_SOURCE_PERMISSIONS.get(name)

errors = []
if expected_permissions is None:
    errors.append(f"{name}: no expected permission allowlist configured")

for required in (manifest, readme):
    if not required.is_file():
        errors.append(f"{name}: missing {required.relative_to(root)}")

source_permission_names = []
if expected_permissions is not None and manifest.is_file():
    try:
        source_permission_names = manifest_names(manifest, USE_PERMISSION_TAGS)
        source_permissions = set(source_permission_names)
        compare_exact(errors, f"{name}: source manifest", source_permissions, expected_permissions)

        duplicates = [item for item, count in Counter(source_permission_names).items() if count > 1]
        if duplicates:
            errors.append(f"{name}: source manifest duplicate permissions: {format_names(duplicates)}")
    except ValueError as exc:
        errors.append(f"{name}: {exc}")

    for source_manifest in source_manifests(root):
        if source_manifest == manifest:
            continue
        rel = source_manifest.relative_to(root)
        try:
            variant_permissions = manifest_names(source_manifest, USE_PERMISSION_TAGS)
            duplicate_variant = [item for item, count in Counter(variant_permissions).items() if count > 1]
            if duplicate_variant:
                errors.append(f"{name}: {rel} duplicate permissions: {format_names(duplicate_variant)}")
            unexpected_variant = set(variant_permissions) - expected_permissions
            if unexpected_variant:
                errors.append(
                    f"{name}: {rel} unexpected source permissions: "
                    f"{format_names(unexpected_variant)}"
                )
            variant_declarations = set(manifest_names(source_manifest, {"permission"}))
            if variant_declarations:
                errors.append(
                    f"{name}: {rel} unexpected permission declarations: "
                    f"{format_names(variant_declarations)}"
                )
        except ValueError as exc:
            errors.append(f"{name}: {exc}")

if manifest.is_file() and readme.is_file():
    stated_count = readme_permission_count(readme)
    manifest_count = len(source_permission_names)
    if stated_count is None:
        errors.append(f"{name}: could not find stated README permission count")
    elif stated_count != manifest_count:
        errors.append(
            f"{name}: README states {stated_count} permissions, "
            f"source manifest declares {manifest_count}"
        )

if expected_permissions is not None:
    merged_manifest_paths = merged_manifests(root)
    if not merged_manifest_paths:
        errors.append(f"{name}: no merged manifests found; run :app:assembleDebug before verifier")
    input_mtime = latest_input_mtime(root)
    stale_merged = [
        str(path.relative_to(root))
        for path in merged_manifest_paths
        if path.stat().st_mtime < input_mtime
    ]
    if stale_merged:
        errors.append(
            f"{name}: merged manifests are older than source manifests; "
            f"run :app:assembleDebug before verifier: {format_names(stale_merged)}"
        )
    for merged_manifest in merged_manifest_paths:
        rel = merged_manifest.relative_to(root)
        try:
            package_name = manifest_package(merged_manifest)
            merged_uses = set(manifest_names(merged_manifest, USE_PERMISSION_TAGS))
            merged_without_dynamic = {
                permission
                for permission in merged_uses
                if not is_app_dynamic_receiver_permission(permission, package_name)
            }
            compare_exact(
                errors,
                f"{name}: {rel} merged uses-permission",
                merged_without_dynamic,
                expected_permissions,
            )

            permission_declarations = set(manifest_names(merged_manifest, {"permission"}))
            unexpected_declarations = {
                permission
                for permission in permission_declarations
                if not is_app_dynamic_receiver_permission(permission, package_name)
            }
            if unexpected_declarations:
                errors.append(
                    f"{name}: {rel} unexpected merged permission declarations: "
                    f"{format_names(unexpected_declarations)}"
                )
        except ValueError as exc:
            errors.append(f"{name}: {exc}")

for config in config_files(root):
    rel = config.relative_to(root)
    text = strip_config_comments(config, config.read_text(encoding="utf-8")).lower()
    for token in FORBIDDEN_SDK_TOKENS:
        if token.lower() in text:
            errors.append(f"{name}: forbidden Gradle/settings/catalog token '{token}' in {rel}")

if errors:
    for error in errors:
        print(f"verify-still-pact: {error}", file=sys.stderr)
    sys.exit(1)

print(f"verify-still-pact: {name} clean")
PY
