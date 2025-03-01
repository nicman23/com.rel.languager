package com.rel.languager

import android.content.SharedPreferences
import com.rel.languager.Constants.DEFAULT_LANGUAGE
import java.util.Locale

/**
 * Utility class to handle language preferences for apps
 */
object LanguageUtils {

    /**
     * Get the language code for a specific package
     */
    fun getLanguageForPackage(packageName: String, prefs: SharedPreferences): String {
        return prefs.getString(packageName, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Set the language for a specific package
     */
    fun setLanguageForPackage(packageName: String, languageCode: String, prefs: SharedPreferences) {
        prefs.edit().putString(packageName, languageCode).apply()
    }

    /**
     * Get all app-language mappings
     */
    fun getAllLanguageMappings(prefs: SharedPreferences): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val allPrefs = prefs.all
        
        for ((key, value) in allPrefs) {
            // Skip any non-string values or the PREF_APP_LANGUAGE_MAP key if it still exists
            if (value is String && key != com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP) {
                map[key] = value
            }
        }
        
        return map
    }

    /**
     * Get a list of available languages with their codes and display names
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            Pair(DEFAULT_LANGUAGE, "System Default"),
            Pair("en", "English"),
            Pair("fr", "French"),
            Pair("de", "German"),
            Pair("es", "Spanish"),
            Pair("it", "Italian"),
            Pair("pt", "Portuguese"),
            Pair("ru", "Russian"),
            Pair("zh", "Chinese"),
            Pair("ja", "Japanese"),
            Pair("ko", "Korean"),
            Pair("ar", "Arabic"),
            Pair("hi", "Hindi"),
            Pair("bn", "Bengali"),
            Pair("pa", "Punjabi"),
            Pair("ta", "Tamil"),
            Pair("te", "Telugu"),
            Pair("ml", "Malayalam"),
            Pair("th", "Thai"),
            Pair("vi", "Vietnamese"),
            Pair("id", "Indonesian"),
            Pair("ms", "Malay"),
            Pair("tr", "Turkish"),
            Pair("nl", "Dutch"),
            Pair("pl", "Polish"),
            Pair("sv", "Swedish")
        )
    }

    /**
     * Get a locale for the specified language code
     */
    fun getLocaleForLanguage(languageCode: String): Locale {
        // If default language (SYS), return system default locale
        if (languageCode == DEFAULT_LANGUAGE) {
            return Locale.getDefault()
        }
        
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
            "bn" -> Locale("bn")
            "pa" -> Locale("pa")
            "ta" -> Locale("ta")
            "te" -> Locale("te")
            "ml" -> Locale("ml")
            "th" -> Locale("th")
            "vi" -> Locale("vi")
            "id" -> Locale("id")
            "ms" -> Locale("ms")
            "tr" -> Locale("tr")
            "nl" -> Locale("nl")
            "pl" -> Locale("pl")
            "sv" -> Locale("sv")
            else -> Locale.getDefault()
        }
    }
}
