# Discord Joined Servers Search for Android

A **rootless**, personal-use floating search overlay for the official Discord Android app. Separate project from the PC Chrome extension.

## Status: experimental v1.0.0 scaffold

**Implemented:** Discord foreground detection via Accessibility events, overlay show/hide, draggable position with persistence, compact search box, expandable/filterable server list, manually entered server names and IDs, Android build workflow.

**Not implemented:** Discord OAuth2, automatic joined-server synchronization, verified Discord deep-link navigation, signed release APK. **Do not mistake this for full feature parity with the Chrome extension.** The manual server list is an interim implementation, not a substitute for OAuth.

## Setup

1. Create a new repository named `discord-joined-servers-search-android` under your GitHub account.
2. Push these files to `main`. The workflow builds a debug APK under Actions → latest run → Artifacts.
3. Install the APK, open the app, grant **Draw over other apps** and enable the Accessibility service. On Android 13+, sideloaded apps may require enabling **Allow restricted settings** in the app's system App info menu before Accessibility can be enabled.
4. Add entries like `My Server | 123456789012345678` (one per line). Only server names without IDs can be searched, not opened.
5. Open Discord. The search overlay should appear. Leave Discord and it should disappear.

## Limitations and privacy

- Accessibility is configured with `canRetrieveWindowContent=false`; no Discord screen content is read.
- No Discord credentials, account tokens, API calls, root, or client hooks.
- Manual server names/IDs are saved locally in app-private preferences.
- Opening a server uses `https://discord.com/channels/SERVER_ID` through Android's ACTION_VIEW; actual app routing must be tested.
- Accessibility window events from keyboards/system dialogs may cause temporary overlay visibility changes. Further device testing is needed.
- Android overlay permission may need to be granted again after reinstalling.
- Debug APK signing is for testing; do not assume it provides stable update compatibility across GitHub Actions runs. A persistent signing key is needed for releases.

This project is unofficial and is not affiliated with Discord. No guarantee is made about Discord's policies or app compatibility.

## v1.1.0: App detection and manual popup

- Detects whether the official Discord Android package (`com.discord`) is installed, without root.
- When Discord is installed and Accessibility is enabled, the popup follows Discord foreground state.
- **Open search popup** in the launcher opens the overlay manually, including on devices without Discord installed.
- The **×** button closes the popup. After closing during a Discord session, it stays dismissed until Discord is reopened or the launcher is used again.
- If Accessibility isn't enabled, manual launch uses a foreground service and a persistent notification while the popup is displayed. Closing the popup stops that service.
- Android's overlay permission is required. Sideloaded Accessibility services may require enabling *Allow restricted settings* in Android's app-info screen.

**Current limitations:** This is a source prototype, not a verified APK. Automatic OAuth2 guild sync and deep-link preference settings are not implemented. Server entries still require manual `Name | ServerID` input. Browser handling of Discord links depends on Android's verified-link settings.
