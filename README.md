# Languager - Language Spoofing Xposed Module

## Overview
Languager is an Xposed module designed to force specific apps to use English language regardless of the system language settings. This is particularly useful for apps that enforce system language restrictions.

## Features
- **Language Spoofing**: Forces English locale for targeted applications
- **Verbose Logging**: Optional detailed logging for debugging purposes
- **Simple UI**: Easy-to-use interface to manage module settings

## Technical Details
- **Language Spoofing Mechanism**:
  - Uses Xposed framework for method hooking
  - Targets `Configuration.getLocales()` and `Configuration.getLocale()`
  - Replaces system locale with English

- **Shared Preferences**:
  - Stores configuration using XSharedPreferences
  - Supports verbose logging toggle

## Requirements
- Android device with Xposed framework installed (LSPosed, EdXposed, etc.)
- Android 5.0+ (API 21+)

## Installation
1. Install the Languager APK
2. Enable the module in your Xposed framework manager
3. Select the apps you want to apply the language spoofing to
4. Reboot your device
5. Open the Languager app to verify it's working

## Usage
1. Open the Languager app
2. Toggle "Enable verbose logs" if you want detailed logging
3. The module will automatically force English language in the selected apps

## Default Target Apps
- LinkedIn
- Instagram

You can add more apps in the Xposed framework manager's module settings.

## Building from Source
1. Clone the repository
2. Open the project in Android Studio
3. Build the project using Gradle

## License
This project is open source and available under the [MIT License](LICENSE).

## Acknowledgements
- [Xposed Framework](https://github.com/rovo89/XposedBridge)
- [LSPosed](https://github.com/LSPosed/LSPosed)
