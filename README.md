# Discord Joined Servers Search for Android

Quickly find and open **Discord servers you've already joined** using a floating search window on Android.

**Joined servers** means servers that are already part of your Discord account. This is **not** a public server discovery tool, server directory, or tool for joining new servers.

This is the Android companion project to the Discord Joined Servers Search Chrome extension. It is unofficial and not affiliated with Discord.

## How it works

- A draggable floating search box lets you filter your **joined servers** by name.
- The search window remembers its position.
- Expand the search box to view the matching server list.
- If the Discord Android app is installed and Accessibility is enabled, the popup can appear when Discord is in the foreground and hide when you leave it.
- If Discord is not installed, you can launch the search popup manually from this app.
- You can also open it manually when Discord is installed.
- Tap **×** to close the popup. When dismissed during a Discord session, it stays closed until Discord is reopened or you launch the popup again.
- Entries with server IDs can be opened through Android's link handling; whether they open in a browser or Discord depends on device settings.

## Current development status: v1.1.0 prototype

**Implemented in source:** floating overlay, foreground detection, manual opening, close/dismiss behavior, saved position, expandable search, filtering of manually entered server entries, and a GitHub Actions debug APK build workflow.

**Not yet implemented:** automatic retrieval of your joined servers using Discord OAuth2, verified navigation to every server, configurable browser-versus-app opening preferences, and a persistently signed release APK.

**Important:** For now, you must enter server names and IDs manually. Automatic synchronization of joined servers is a planned feature, not something this version already provides.

## Install and test

1. Open **Actions** in this repository and download the APK artifact from a successful Android build workflow run.
2. Install the debug APK.
3. Grant **Display over other apps** permission. Enable the Accessibility service if you want automatic appearance when Discord is open. Android 13+ may require **Allow restricted settings** in App info for sideloaded Accessibility services.
4. Add one server per line in the form `My Joined Server | 123456789012345678`. Entries without IDs can be searched but not opened.
5. Open Discord to test automatic appearance, or launch this app to open the popup manually. Use **×** to dismiss it.

## Privacy and limitations

- Accessibility is configured not to retrieve screen content; it is used for foreground-window events.
- Server entries are stored locally in the app's private preferences.
- This prototype does not request Discord credentials or account tokens and does not make Discord API calls.
- Server links use `https://discord.com/channels/SERVER_ID`; actual routing depends on Android and installed applications.
- Window events from keyboards or system dialogs may temporarily affect visibility.
- This is experimental source code; a successful device build and behavior across Android versions have not yet been verified.
- GitHub Actions debug signing may change between runs, so updates are not guaranteed to install over previous builds.

## Distribution

The source is maintained here, and Android builds are distributed through GitHub Actions. This project is separate from the PC Chrome extension.
