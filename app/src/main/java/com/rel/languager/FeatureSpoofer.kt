package com.rel.languager

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.rel.languager.Constants.SHARED_PREF_FILE_NAME
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.Locale
import java.util.Locale.Category

class FeatureSpoofer: IXposedHookLoadPackage {
    private fun log(message: String) {
        XposedBridge.log("[Languager] $message")
    }

    /**
     * To read preference of user.
     */
    private val pref by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, SHARED_PREF_FILE_NAME).apply {
            reload() // Ensure we have the latest preferences

            // Check if preferences are readable
            if (!file.canRead()) {
                log("ERROR: Preference file is not readable!")
                makeWorldReadable()
            }
        }
    }

    /**
     * Attempt to make the preferences file world-readable
     */
    private fun XSharedPreferences.makeWorldReadable() {
        try {
            val chmod = Runtime.getRuntime().exec("chmod 664 ${file.absolutePath}")
            chmod.waitFor()
            reload()
        } catch (e: Exception) {
            log("Failed to make preferences readable: ${e.message}")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        lpparam?.packageName?.let { packageName ->
            // Skip our own package
            if (packageName == BuildConfig.APPLICATION_ID) {
                return
            }

            log("Loaded languager for $packageName")

            // Force reload preferences to get the latest settings
            pref.reload()

            // Get the preferred language for this package
            val languageCode = LanguageUtils.getLanguageForPackage(packageName, pref)

            // Only proceed if a non-default language is selected
            if (languageCode != Constants.DEFAULT_LANGUAGE) {
                val locale = LanguageUtils.getLocaleForLanguage(languageCode)

                log("Using language $languageCode for package $packageName")

                // Hook all relevant locale methods
                hookLocaleAPIs(lpparam, locale, languageCode, packageName)
            } else {
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
        try {
            // Common hooks for all API levels
            hookCommonLocaleAPIs(lpparam, locale, languageCode, packageName)

            // API level specific hooks
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hookApi24PlusLocaleAPIs(lpparam, locale, languageCode, packageName)
            } else {
                hookPreApi24LocaleAPIs(lpparam, locale, languageCode, packageName)
            }

            log("Successfully hooked all locale APIs for $packageName to use $languageCode")
        } catch (e: Exception) {
            log("Error during hooking process: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun hookCommonLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // 1. Hook Locale.getDefault() for all API levels
        try {
            XposedHelpers.findAndHookMethod(
                Locale::class.java.name,
                lpparam.classLoader,
                "getDefault",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = locale
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Locale.getDefault(): ${e.message}")
        }

        // 2. Hook Resources.updateConfiguration for all API levels
        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "updateConfiguration",
                Configuration::class.java,
                "android.util.DisplayMetrics",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val config = param.args[0] as Configuration
                        try {
                            XposedHelpers.setObjectField(config, "locale", locale)
                        } catch (e: Throwable) {
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.updateConfiguration: ${e.message}")
        }

        // 3. Hook Configuration.setLocale for API 17+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                XposedHelpers.findAndHookMethod(
                    Configuration::class.java.name,
                    lpparam.classLoader,
                    "setLocale",
                    Locale::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            XposedHelpers.setObjectField(param.thisObject, "locale", locale)
                        }
                    }
                )
            } catch (e: Throwable) {
                log("Error hooking Configuration.setLocale: ${e.message}")
            }
        }
    }

    private fun hookApi24PlusLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // 1. Hook Locale.getDefault(Locale.Category) for API 24+
        try {
            XposedHelpers.findAndHookMethod(
                Locale::class.java.name,
                lpparam.classLoader,
                "getDefault",
                Locale.Category::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = locale
                        log("Spoofed Locale.getDefault(Category) to $languageCode for $packageName")
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Locale.getDefault(Category): ${e.message}")
        }

        // 2. Hook Resources.getConfiguration().getLocales() for API 24+
        try {
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam.classLoader,
                "getLocales",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val localeList = LocaleList(locale)
                        param.result = localeList
                    }
                }
            )
        } catch (e: Throwable) {
        }

        // 3. Hook Configuration.setLocales for API 24+
        try {
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam.classLoader,
                "setLocales",
                LocaleList::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[0] = LocaleList(locale)
                        log("Spoofed Configuration.setLocales to $languageCode for $packageName")
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Configuration.setLocales: ${e.message}")
        }
    }

    private fun hookPreApi24LocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // For pre-API 24, most hooks are covered by the common methods
        // But we can add additional hooks specific to older APIs if needed

        // Hook Configuration constructor that takes a Configuration parameter
        try {
            XposedHelpers.findAndHookConstructor(
                Configuration::class.java.name,
                lpparam.classLoader,
                Configuration::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val newConfig = param.thisObject as Configuration
                        XposedHelpers.setObjectField(newConfig, "locale", locale)
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Configuration constructor: ${e.message}")
        }
    }
}