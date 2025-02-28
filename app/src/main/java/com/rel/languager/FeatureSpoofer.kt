package com.rel.languager

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.rel.languager.Constants.PREF_ENABLE_VERBOSE_LOGS
import com.rel.languager.Constants.SHARED_PREF_FILE_NAME
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.Locale

class FeatureSpoofer: IXposedHookLoadPackage {
    private fun log(message: String) {
        XposedBridge.log("Languager: $message")
        Log.d("Languager", message)
    }

    /**
     * To read preference of user.
     */
    private val pref by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, SHARED_PREF_FILE_NAME).apply {
            reload() // Ensure we have the latest preferences
            log("Preference location: ${file.canonicalPath}")
        }
    }

    private val verboseLog: Boolean by lazy {
        pref.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        lpparam?.packageName?.let { packageName ->
            log("Loaded languager for $packageName")
            
            // Get the preferred language for this package
            val languageCode = LanguageUtils.getLanguageForPackage(packageName, pref)
            
            // Only proceed if a non-default language is selected
            if (languageCode != Constants.DEFAULT_LANGUAGE) {
                val locale = LanguageUtils.getLocaleForLanguage(languageCode)
                
                if (verboseLog) {
                    log("Using language $languageCode for package $packageName")
                }

                // Hook all relevant locale methods
                hookLocaleAPIs(lpparam, locale, languageCode, packageName)
            } else if (verboseLog) {
                log("Using default language for package $packageName")
            }
        }
    }
    
    private fun hookLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // 1. Hook Configuration.getLocales() (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam.classLoader,
                "getLocales",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val localeList = LocaleList(locale)
                        param.result = localeList
                        if (verboseLog) {
                            log("Spoofed getLocales to $languageCode for $packageName")
                        }
                    }
                }
            )
            
            // 2. Hook Configuration.locale property (all API levels)
            try {
                val configClass = XposedHelpers.findClass(Configuration::class.java.name, lpparam.classLoader)
                XposedHelpers.findAndHookMethod(
                    configClass,
                    "getLocale",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = locale
                            if (verboseLog) {
                                log("Spoofed Configuration.getLocale() to $languageCode for $packageName")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                log("Error hooking Configuration.getLocale(): ${e.message}")
            }
            
            // 3. Hook Resources.getConfiguration().getLocales() (API 24+)
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "getConfiguration",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val configuration = param.result as Configuration
                        val localeList = LocaleList(locale)
                        try {
                            XposedHelpers.setObjectField(configuration, "mLocales", localeList)
                        } catch (e: Exception) {
                            log("Error setting mLocales field: ${e.message}")
                        }
                        try {
                            XposedHelpers.setObjectField(configuration, "locale", locale)
                        } catch (e: Exception) {
                            log("Error setting locale field: ${e.message}")
                        }
                        if (verboseLog) {
                            log("Spoofed Resources.getConfiguration() locales to $languageCode for $packageName")
                        }
                    }
                }
            )
        } else {
            // 4. Hook Configuration.getLocale() (API < 24)
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam.classLoader,
                "getLocale",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = locale
                        if (verboseLog) {
                            log("Spoofed getLocale to $languageCode for $packageName")
                        }
                    }
                }
            )
            
            // 5. Hook Resources.getConfiguration().locale (API < 24)
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "getConfiguration",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val configuration = param.result as Configuration
                        try {
                            XposedHelpers.setObjectField(configuration, "locale", locale)
                        } catch (e: Exception) {
                            log("Error setting locale field: ${e.message}")
                        }
                        if (verboseLog) {
                            log("Spoofed Resources.getConfiguration().locale to $languageCode for $packageName")
                        }
                    }
                }
            )
        }
        
        // 6. Hook Locale.getDefault() for all API levels
        XposedHelpers.findAndHookMethod(
            Locale::class.java.name,
            lpparam.classLoader,
            "getDefault",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = locale
                    if (verboseLog) {
                        log("Spoofed Locale.getDefault() to $languageCode for $packageName")
                    }
                }
            }
        )
        
        // 7. For API 24+, also hook the category-specific getDefault
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                XposedHelpers.findAndHookMethod(
                    Locale::class.java.name,
                    lpparam.classLoader,
                    "getDefault",
                    Locale.Category::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = locale
                            if (verboseLog) {
                                log("Spoofed Locale.getDefault(Category) to $languageCode for $packageName")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                log("Error hooking Locale.getDefault(Category): ${e.message}")
            }
        }
    }
}