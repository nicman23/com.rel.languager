package com.rel.languager

import android.content.SharedPreferences
import com.rel.languager.Constants.DEFAULT_LANGUAGE
import java.util.Locale

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
        val localeList =
                Locale.getAvailableLocales().toList().distinctBy { it.language }.toMutableList()
        // replace chinese with simplified chinese
        localeList.removeIf { it.language == "zh" }

        localeList.add(Locale("zh", "CN"))
        localeList.add(Locale("zh", "TW"))

        return localeList.sortedBy { it.getDisplayLanguage(Locale.ENGLISH) }
    }
}
