# Still

**A quiet launcher for Android.**

Still is a minimalist, privacy-first Android launcher intended for a Pixel running GrapheneOS. It is monochrome, OLED-first, text-first, and designed to make a modern Android phone feel closer to a beautiful dumb phone.

## Project identity

- App name: Still
- Repo/package name: `still-launcher`
- Android package: `dev.chuds.still`
- Tagline: “A quiet launcher for Android.”
- Design direction: pure black, soft white text, gray secondary text, no colorful icons, no feeds, no default app drawer

## Privacy promise

Still is built to be boring in the best way:

- No `INTERNET` permission
- No Firebase
- No Google Play Services dependency
- No analytics SDK
- No telemetry
- No ads
- Settings stored locally with Preferences DataStore
- Package visibility scoped to launchable apps with `<queries>`
- No `QUERY_ALL_PACKAGES`

## MVP behavior

- Declares a HOME activity so Android can offer Still as the default Home app
- Displays title, live clock, date, primary text menu, secondary tools, and footer hint
- Launches selected installed apps
- Long-press background to open hidden all-apps/tools screen
- Still settings maps these slots to installed apps:
  - Phone
  - Messages
  - Signal
  - Maps
  - Browser
  - Camera
  - Settings
- Browser opens through an intentional friction screen:
  - “Browser”
  - “Use this intentionally.”
  - Open / Cancel

## Architecture

```text
MainActivity
└── StillApp
    ├── HomeViewModel
    │   ├── AppRepository
    │   │   ├── PackageScanner
    │   │   └── PreferencesRepository
    │   └── AppLauncher
    └── Compose screens
        ├── HomeScreen
        ├── AllAppsScreen
        ├── SettingsScreen
        ├── AppPickerScreen
        └── FrictionScreen
```

### Important files

- `AndroidManifest.xml` — declares Still as HOME/DEFAULT and adds scoped package visibility for launchable apps.
- `PackageScanner.kt` — queries launchable apps via `ACTION_MAIN` + `CATEGORY_LAUNCHER`.
- `AppLauncher.kt` — launches explicit package/class component selections.
- `PreferencesRepository.kt` — persists slot mappings with Preferences DataStore.
- `HomeViewModel.kt` — combines installed apps and settings into UI state.
- `HomeScreen.kt` — minimalist clock/menu home UI.
- `AppPickerScreen.kt` — app picker and hidden all-apps screen.
- `FrictionScreen.kt` — intentional browser gate.

## Build instructions

1. Open the project folder in Android Studio.
2. Use JDK 17 or newer.
3. Let Android Studio sync Gradle.
4. Install on a Pixel or emulator.
5. Open Android Settings → Apps → Default apps → Home app → choose **Still**.

This package targets:

- Kotlin
- Jetpack Compose
- Gradle Kotlin DSL
- Preferences DataStore
- Android package `dev.chuds.still`

## Notes for GrapheneOS

Still does not depend on Google Play Services. On a fresh GrapheneOS setup, some default slot heuristics may be empty until you choose apps from Still settings. Long-press the home screen background, open **Still settings**, and map each word to the apps installed on your profile.

## Font direction

The MVP uses system font fallbacks so it builds immediately. For the intended premium visual identity, add open-source fonts under `app/src/main/res/font/` and update `StillTypography.kt`.

Recommended pairings:

- Clock: Cormorant Garamond or Instrument Serif
- Menus: Space Grotesk, IBM Plex Mono, or Instrument Sans

Do not bundle fonts unless their licenses are compatible with your distribution plan.

## Testing checklist

- App installs successfully.
- Still appears in Android's Home app picker.
- Setting Still as default Home returns to Still when pressing/swiping Home.
- Home screen has black background and no icons.
- Clock updates every second.
- Date displays correctly.
- Tapping Phone/Messages/Signal/Maps/Camera/Settings launches the mapped app.
- Browser first opens the friction screen.
- Browser friction Open launches the mapped browser.
- Browser friction Cancel returns home.
- Long-pressing the home background opens All apps.
- All apps list shows launchable apps.
- All apps → Still settings opens local settings.
- App picker persists mappings after process restart.
- Manifest contains no `android.permission.INTERNET`.
- Manifest contains no `android.permission.QUERY_ALL_PACKAGES`.

## Current limitations

- No icon pack support by design.
- No notification listener.
- No accessibility service.
- No kiosk/device-owner mode.
- No widgets.
- No search.
- No multiple profiles UX yet.
- No custom font files bundled yet.

## License

Choose a license before publishing. MIT or Apache-2.0 are straightforward options for an open-source launcher.
