package com.rel.languager

import android.content.SharedPreferences
import com.rel.languager.Constants.DEFAULT_LANGUAGE
import java.util.Locale

/**
 * Utility class to handle language preferences for apps
 */
object LanguageUtils {

    fun getLanguageForPackage(packageName: String, prefs: SharedPreferences): String {
        return prefs.getString(packageName, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguageForPackage(packageName: String, languageCode: String, prefs: SharedPreferences) {
        prefs.edit().putString(packageName, languageCode).apply()
    }

    fun getAllLanguageMappings(prefs: SharedPreferences): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val allPrefs = prefs.all

        for ((key, value) in allPrefs) {
            if (value is String && key != com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP) {
                map[key] = value
            }
        }

        return map
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            Pair(DEFAULT_LANGUAGE, "Default"),
            Pair("en", "English"),
            Pair("he", "Hebrew"),
            Pair("fr", "French"),
            Pair("de", "German"),
            Pair("es", "Spanish"),
            Pair("it", "Italian"),
            Pair("pt", "Portuguese"),
            Pair("ru", "Russian"),
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
            Pair("sv", "Swedish"),
            Pair("zh-CN", "Chinese-CN"),
            Pair("zh-TW", "Chinese-TW")
        )
    }

    fun getLocaleForLanguage(languageCode: String): Locale {
        return when (languageCode) {
            "en" -> Locale("en")
            "fr" -> Locale("fr")
            "de" -> Locale("de")
            "it" -> Locale("it")
            "ja" -> Locale("ja")
            "ko" -> Locale("ko")
            "zh-CN" -> Locale("zh", "CN")
            "zh-TW" -> Locale("zh", "TW")
            "he" -> Locale("he")
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
