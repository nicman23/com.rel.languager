# Languager - Language Spoofing Xposed Module

## Overview
Languager is an Xposed module that allows you to change the language of specific Android applications without changing your system language. This is particularly useful for apps that enforce system language settings or for users who want different languages for different apps.

## Features
- **Per-App Language Selection**: Choose a different language for each app
- **Comprehensive Language Support**: 26+ languages available
- **User-Friendly Interface**: Easy-to-use UI with app search functionality
- **Verbose Logging**: Optional detailed logging for debugging purposes
- **Compatibility**: Works on Android 5.0+ (API 21+)

## Technical Details
- **Language Spoofing Mechanism**:
  - Uses Xposed framework for method hooking
  - Targets all locale-related methods:
    - `Configuration.getLocales()` (API 24+)
    - `Configuration.getLocale()` (API < 24)
    - `Configuration.locale` property
    - `Resources.getConfiguration().locale`
    - `Locale.getDefault()`
    - `Locale.getDefault(Category)` (API 24+)
  - Dynamically replaces system locale with user-selected language

- **Shared Preferences**:
  - Stores configuration using XSharedPreferences
  - JSON-based storage for app-language mappings
  - Supports verbose logging toggle

## Requirements
- Android device with Xposed framework installed (LSPosed, EdXposed, etc.)
- Android 5.0+ (API 21+)

## Installation
1. Install the Languager APK
2. Enable the module in your Xposed framework manager
3. Select the apps you want to apply language spoofing to
4. Reboot your device
5. Open the Languager app to configure language settings for each app

## Usage
1. Open the Languager app
2. Use the search bar to find specific apps
3. Select your preferred language for each app from the dropdown menu
4. Click "Save Settings" to apply changes
5. Reboot or restart the target apps for changes to take effect

## Default Target Apps
- LinkedIn
- Instagram
- Facebook
- Twitter
- WhatsApp
- Snapchat
- Spotify
- YouTube
- Gmail
- Amazon Shopping
- Netflix
- Google Maps
- Uber
- Pinterest
- Reddit

You can add more apps in the Xposed framework manager's module settings.

## Supported Languages
- English
- French (Français)
- German (Deutsch)
- Italian (Italiano)
- Japanese (日本語)
- Korean (한국어)
- Chinese (中文)
- Spanish (Español)
- Portuguese (Português)
- Russian (Русский)
- Arabic (العربية)
- Hindi (हिन्दी)
- Turkish (Türkçe)
- Dutch (Nederlands)
- Polish (Polski)
- Thai (ไทย)
- Czech (Čeština)
- Swedish (Svenska)
- Danish (Dansk)
- Finnish (Suomi)
- Norwegian (Norsk)
- Greek (Ελληνικά)
- Hebrew (עברית)
- Indonesian (Bahasa Indonesia)
- Malay (Bahasa Melayu)
- Vietnamese (Tiếng Việt)

## Building from Source

### Prerequisites
- Android Studio 4.0+
- JDK 11+
- Gradle 7.0+

### Local Build (debug-mode)
1. Clone the repository:
   ```
   git clone https://github.com/yourusername/languager.git
   cd languager
   ```

1. Or build manually:
   ```
   ./gradlew clean assembleDebug
   ```

1. Find the APK at `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions
The project includes a GitHub workflow that automatically builds and releases the app when a new tag is pushed:

1. Tag a new version:
   ```
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```

2. The workflow will:
   - Build the release APK
   - Sign the APK
   - Create a GitHub release
   - Attach the APK to the release

## Troubleshooting
- **Module not working**: Make sure it's enabled in LSPosed/EdXposed Manager
- **Language not changing**: Try restarting the app or rebooting your device
- **Crashes**: Enable verbose logging and check the Xposed logs

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License
This project is open source and available under the [MIT License](LICENSE).

## Acknowledgements
- [Xposed Framework](https://github.com/rovo89/XposedBridge)
- [LSPosed](https://github.com/LSPosed/LSPosed)
- [EdXposed](https://github.com/ElderDrivers/EdXposed)
