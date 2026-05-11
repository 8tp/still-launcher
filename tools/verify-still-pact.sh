#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 - "$root" <<'PY'
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
name = root.name
manifest = root / "app/src/main/AndroidManifest.xml"
gradle = root / "app/build.gradle.kts"
readme = root / "README.md"

manifest_forbidden = [
    "android.permission.INTERNET",
    "ACCESS_NETWORK_STATE",
    "ACCESS_WIFI_STATE",
    "QUERY_ALL_PACKAGES",
    "READ_EXTERNAL_STORAGE",
    "WRITE_EXTERNAL_STORAGE",
    "MANAGE_EXTERNAL_STORAGE",
]
gradle_forbidden = [
    "com.google.firebase",
    "com.google.android.gms",
    "crashlytics",
    "analytics",
    "mixpanel",
    "amplitude",
]
number_words = {
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


def strip_xml_comments(text: str) -> str:
    return re.sub(r"<!--.*?-->", "", text, flags=re.S)


def strip_kotlin_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return "\n".join(line.split("//", 1)[0] for line in text.splitlines())


def readme_permission_count(path: Path):
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
            return int(token) if token.isdigit() else number_words[token]
    return None


errors = []
for required in (manifest, readme):
    if not required.is_file():
        errors.append(f"{name}: missing {required.relative_to(root)}")

if manifest.is_file():
    manifest_text = strip_xml_comments(manifest.read_text(encoding="utf-8"))
    for token in manifest_forbidden:
        if token in manifest_text:
            errors.append(f"{name}: forbidden manifest token: {token}")

    manifest_count = len(re.findall(r"<uses-permission\b", manifest_text))
    stated_count = readme_permission_count(readme) if readme.is_file() else None
    if stated_count is None:
        errors.append(f"{name}: could not find stated README permission count")
    elif stated_count != manifest_count:
        errors.append(
            f"{name}: README states {stated_count} permissions, "
            f"manifest declares {manifest_count}"
        )

if gradle.is_file():
    gradle_text = strip_kotlin_comments(gradle.read_text(encoding="utf-8"))
    for token in gradle_forbidden:
        if token in gradle_text:
            errors.append(f"{name}: forbidden gradle token: {token}")

if errors:
    for error in errors:
        print(f"verify-still-pact: {error}", file=sys.stderr)
    sys.exit(1)

print(f"verify-still-pact: {name} clean")
PY
