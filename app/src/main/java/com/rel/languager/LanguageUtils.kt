package com.rel.languager

import android.content.SharedPreferences
import com.rel.languager.Constants.DEFAULT_LANGUAGE
import com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP
import org.json.JSONObject
import java.util.Locale

/**
 * Utility class to handle language preferences for apps
 */
object LanguageUtils {

    /**
     * Get the language code for a specific package
     * @param packageName The package name of the app
     * @param prefs The shared preferences
     * @return The language code (e.g., "en", "fr", "es")
     */
    fun getLanguageForPackage(packageName: String, prefs: SharedPreferences): String {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        return try {
            val languageMap = JSONObject(languageMapJson ?: "{}")
            if (languageMap.has(packageName)) {
                languageMap.getString(packageName)
            } else {
                DEFAULT_LANGUAGE
            }
        } catch (e: Exception) {
            DEFAULT_LANGUAGE
        }
    }

    /**
     * Set the language for a specific package
     * @param packageName The package name of the app
     * @param languageCode The language code to set (e.g., "en", "fr", "es")
     * @param prefs The shared preferences
     */
    fun setLanguageForPackage(packageName: String, languageCode: String, prefs: SharedPreferences) {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        try {
            val languageMap = JSONObject(languageMapJson ?: "{}")
            languageMap.put(packageName, languageCode)
            prefs.edit().putString(PREF_APP_LANGUAGE_MAP, languageMap.toString()).apply()
        } catch (e: Exception) {
            // Fallback to a new map if there's an error
            val newMap = JSONObject()
            newMap.put(packageName, languageCode)
            prefs.edit().putString(PREF_APP_LANGUAGE_MAP, newMap.toString()).apply()
        }
    }

    /**
     * Get all app-language mappings
     * @param prefs The shared preferences
     * @return A map of package names to language codes
     */
    fun getAllLanguageMappings(prefs: SharedPreferences): Map<String, String> {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        val result = mutableMapOf<String, String>()
        
        try {
            val languageMap = JSONObject(languageMapJson ?: "{}")
            val keys = languageMap.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = languageMap.getString(key)
            }
        } catch (e: Exception) {
            // Return empty map on error
        }
        
        return result
    }

    /**
     * Get a locale for the specified language code
     * @param languageCode The language code (e.g., "en", "fr", "es")
     * @return The corresponding Locale object
     */
    fun getLocaleForLanguage(languageCode: String): Locale {
        // Check if the language code includes a country code (e.g., "en-US")
        if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            if (parts.size >= 2) {
                val language = parts[0]
                val country = parts[1]
                return Locale(language, country)
            }
        }
        
        // Handle standard language codes
        return when (languageCode) {
            "en" -> Locale.US // Use US English as default
            "en_US" -> Locale.US
            "en_GB" -> Locale.UK
            "fr" -> Locale.FRANCE
            "fr_FR" -> Locale.FRANCE
            "fr_CA" -> Locale("fr", "CA")
            "de" -> Locale.GERMANY
            "de_DE" -> Locale.GERMANY
            "it" -> Locale.ITALY
            "it_IT" -> Locale.ITALY
            "ja" -> Locale.JAPAN
            "ja_JP" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "ko_KR" -> Locale.KOREA
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "zh_CN" -> Locale.SIMPLIFIED_CHINESE
            "zh_TW" -> Locale.TRADITIONAL_CHINESE
            "es" -> Locale("es", "ES")
            "es_ES" -> Locale("es", "ES")
            "es_MX" -> Locale("es", "MX")
            "pt" -> Locale("pt", "PT")
            "pt_BR" -> Locale("pt", "BR")
            "pt_PT" -> Locale("pt", "PT")
            "ru" -> Locale("ru", "RU")
            "ar" -> Locale("ar", "SA") // Saudi Arabia as default
            "hi" -> Locale("hi", "IN")
            "tr" -> Locale("tr", "TR")
            "nl" -> Locale("nl", "NL")
            "pl" -> Locale("pl", "PL")
            "th" -> Locale("th", "TH")
            "cs" -> Locale("cs", "CZ")
            "sv" -> Locale("sv", "SE")
            "da" -> Locale("da", "DK")
            "fi" -> Locale("fi", "FI")
            "no" -> Locale("no", "NO")
            "el" -> Locale("el", "GR")
            "he" -> Locale("he", "IL")
            "id" -> Locale("id", "ID")
            "ms" -> Locale("ms", "MY")
            "vi" -> Locale("vi", "VN")
            else -> {
                // For any other language code, try to create a Locale directly
                try {
                    Locale(languageCode)
                } catch (e: Exception) {
                    Locale.US // Default to US English if invalid code
                }
            }
        }
    }

    /**
     * Get a list of available languages with their codes and display names
     * @return A list of Pair<String, String> where first is the language code and second is the display name
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "fr" to "French (Français)",
            "de" to "German (Deutsch)",
            "it" to "Italian (Italiano)",
            "ja" to "Japanese (日本語)",
            "ko" to "Korean (한국어)",
            "zh" to "Chinese (中文)",
            "es" to "Spanish (Español)",
            "pt" to "Portuguese (Português)",
            "ru" to "Russian (Русский)",
            "ar" to "Arabic (العربية)",
            "hi" to "Hindi (हिन्दी)",
            "tr" to "Turkish (Türkçe)",
            "nl" to "Dutch (Nederlands)",
            "pl" to "Polish (Polski)",
            "th" to "Thai (ไทย)",
            "cs" to "Czech (Čeština)",
            "sv" to "Swedish (Svenska)",
            "da" to "Danish (Dansk)",
            "fi" to "Finnish (Suomi)",
            "no" to "Norwegian (Norsk)",
            "el" to "Greek (Ελληνικά)",
            "he" to "Hebrew (עברית)",
            "id" to "Indonesian (Bahasa Indonesia)",
            "ms" to "Malay (Bahasa Melayu)",
            "vi" to "Vietnamese (Tiếng Việt)"
        )
    }
}
