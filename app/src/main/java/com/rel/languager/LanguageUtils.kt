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
        return when (languageCode) {
            "en" -> Locale.ENGLISH
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "zh" -> Locale.CHINESE
            "es" -> Locale("es")
            "pt" -> Locale("pt")
            "ru" -> Locale("ru")
            "ar" -> Locale("ar")
            "hi" -> Locale("hi")
            "tr" -> Locale("tr")
            "nl" -> Locale("nl")
            "pl" -> Locale("pl")
            "th" -> Locale("th")
            "cs" -> Locale("cs")
            "sv" -> Locale("sv")
            "da" -> Locale("da")
            "fi" -> Locale("fi")
            "no" -> Locale("no")
            "el" -> Locale("el")
            "he" -> Locale("he")
            "id" -> Locale("id")
            "ms" -> Locale("ms")
            "vi" -> Locale("vi")
            else -> Locale.ENGLISH // Default to English for unknown codes
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
