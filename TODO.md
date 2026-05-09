# Still TODO / GitHub issue list

## MVP hardening

1. **Add instrumented smoke test for Home declaration**
   - Verify `MainActivity` resolves for `ACTION_MAIN` + `CATEGORY_HOME`.
   - Verify no INTERNET permission is declared.

2. **Add default app mapping quality pass for GrapheneOS**
   - Test default package labels on Pixel 8a / Pixel 9a.
   - Improve local fallback heuristics for Vanadium, AOSP Dialer, AOSP Messaging, Camera, and Settings.

3. **Handle uninstalled mapped apps gracefully**
   - Current behavior falls back to heuristics if stored component is missing.
   - Add UI copy in settings showing “missing” vs “not set”.

4. **Add empty-state copy for app picker**
   - Show a calm explanation if no launchable apps are visible.
   - Include a note about Android package visibility.

5. **Add optional 12-hour / 24-hour setting**
   - Current MVP uses 24-hour time.
   - DataStore setting should remain local.

## Design polish

6. **Bundle licensed open-source fonts**
   - Candidate clock fonts: Cormorant Garamond, Instrument Serif.
   - Candidate menu fonts: Space Grotesk, IBM Plex Mono, Instrument Sans.
   - Confirm OFL or compatible license before bundling.

7. **Tune type scale on Pixel 8a and Pixel 9a**
   - Check readability at default, large, and maximum system font scales.
   - Keep the interface calm and spacious.

8. **Add subtle screen transitions**
   - Fade only.
   - No bouncy motion, no colorful animations.

9. **Add optional reduced UI mode**
   - Hide date.
   - Hide footer hint after setup.
   - Show only 3 primary actions.

## Launcher features

10. **Add app search on hidden all-apps screen**
    - Text-only.
    - No fuzzy dopamine ranking.
    - No telemetry.

11. **Add per-slot friction rules**
    - Browser is the MVP friction slot.
    - Later: Maps, Messages, custom delay, or confirm phrase.

12. **Add profile-aware app listing**
    - Investigate `LauncherApps` for managed/work profile support.
    - Keep package visibility minimal.

13. **Add optional favorites-only mode**
    - Hide all-apps list behind a stronger gesture.
    - Still no kiosk mode yet.

14. **Add import/export local settings**
    - Plain JSON.
    - User-triggered only.
    - No network.

## Future, non-MVP

15. **Device-owner / kiosk exploration**
    - Not in MVP.
    - Only for users who explicitly provision it.

16. **Notification minimalism**
    - Not in MVP.
    - Avoid notification listener unless absolutely necessary.

17. **Accessibility review**
    - Ensure TalkBack labels are meaningful.
    - Ensure touch targets are large enough.
    - Preserve high contrast without harsh white.
