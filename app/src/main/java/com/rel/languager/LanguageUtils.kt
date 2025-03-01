package com.rel.languager

import android.content.SharedPreferences
import com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP
import org.json.JSONObject
import java.util.Locale

/**
 * Utility class to handle language preferences for apps
 */
object LanguageUtils {

    /**
     * Get the language code for a specific package
     */
    fun getLanguageForPackage(packageName: String, prefs: SharedPreferences): String {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        return try {
            val jsonObject = JSONObject(languageMapJson)
            jsonObject.optString(packageName, "")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Set the language for a specific package
     */
    fun setLanguageForPackage(packageName: String, languageCode: String, prefs: SharedPreferences) {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        try {
            val jsonObject = JSONObject(languageMapJson)
            jsonObject.put(packageName, languageCode)
            prefs.edit().putString(PREF_APP_LANGUAGE_MAP, jsonObject.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get all app-language mappings
     */
    fun getAllLanguageMappings(prefs: SharedPreferences): Map<String, String> {
        val languageMapJson = prefs.getString(PREF_APP_LANGUAGE_MAP, "{}")
        return try {
            val jsonObject = JSONObject(languageMapJson)
            val map = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Get a list of available languages with their codes and display names
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

    /**
     * Get a locale for the specified language code
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
}
