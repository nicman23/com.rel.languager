package com.rel.languager

import java.util.Locale
import android.content.SharedPreferences
import com.rel.languager.Constants.DEFAULT_LANGUAGE

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

    fun getAvailableLanguages(): List<Locale> {
        return listOf(
            Locale.getDefault(),
            Locale("en"),
            Locale("he"),
            Locale("fr"),
            Locale("de"),
            Locale("es"),
            Locale("it"),
            Locale("pt"),
            Locale("ru"),
            Locale("ja"),
            Locale("ko"),
            Locale("ar"),
            Locale("hi"),
            Locale("bn"),
            Locale("pa"),
            Locale("ta"),
            Locale("te"),
            Locale("ml"),
            Locale("th"),
            Locale("vi"),
            Locale("id"),
            Locale("ms"),
            Locale("tr"),
            Locale("nl"),
            Locale("pl"),
            Locale("sv"),
            Locale("zh", "CN"),
            Locale("zh", "TW")
        )
    }
}
