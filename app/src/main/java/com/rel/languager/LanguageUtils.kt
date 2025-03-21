package com.rel.languager
import com.rel.languager.Constants.DEFAULT_LANGUAGE

import java.util.Locale
import android.content.SharedPreferences

object LanguageUtils {
    fun getLanguageForPackage(packageName: String, prefs: SharedPreferences): String {
        return prefs.getString(packageName, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun getAllLanguageMappings(prefs: SharedPreferences): Map<String, String> {
        val map = mutableMapOf<String, String>()

        for ((key, value) in prefs.all) {
            if (value is String && key != com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP) {
                map[key] = value
            }
        }

        return map
    }

    fun getAvailableLanguages(): List<Locale> {
        val localeList = Locale.getAvailableLocales().toMutableList()

        localeList.sortBy { it.getDisplayLanguage(Locale.getDefault()) }
        localeList.add(0, Locale.getDefault())

        return localeList.distinct().toMutableList()
    }
}
