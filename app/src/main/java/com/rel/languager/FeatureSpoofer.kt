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
            
            // Check if preferences are readable
            if (!file.canRead()) {
                log("ERROR: Preference file is not readable!")
                makeWorldReadable()
            } else {
                log("Preference file is readable")
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
            log("Attempted to make preferences readable, success: ${file.canRead()}")
        } catch (e: Exception) {
            log("Failed to make preferences readable: ${e.message}")
        }
    }

    private val verboseLog: Boolean by lazy {
        val enabled = pref.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false)
        log("Verbose logging is ${if (enabled) "enabled" else "disabled"}")
        enabled
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        lpparam?.packageName?.let { packageName ->
            // Skip our own package
            if (packageName == BuildConfig.APPLICATION_ID) {
                log("Skipping our own package")
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
                        if (verboseLog) {
                            log("Spoofed Locale.getDefault() to $languageCode for $packageName")
                        }
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
                            if (verboseLog) {
                                log("Spoofed updateConfiguration locale to $languageCode for $packageName")
                            }
                        } catch (e: Throwable) {
                            log("Error setting locale in updateConfiguration: ${e.message}")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.updateConfiguration: ${e.message}")
        }
        
        // 3. Hook Resources.getConfiguration() for all API levels
        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "getConfiguration",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val configuration = param.result as Configuration
                        try {
                            XposedHelpers.setObjectField(configuration, "locale", locale)
                            if (verboseLog) {
                                log("Spoofed Resources.getConfiguration().locale to $languageCode for $packageName")
                            }
                        } catch (e: Throwable) {
                            log("Error setting locale in getConfiguration: ${e.message}")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.getConfiguration: ${e.message}")
        }
    }
    
    private fun hookApi24PlusLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // 1. Hook Configuration.getLocales() (API 24+)
        try {
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
        } catch (e: Throwable) {
            log("Error hooking Configuration.getLocales(): ${e.message}")
        }
        
        // 2. Hook Resources.getConfiguration().getLocales() (API 24+)
        try {
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
                            if (verboseLog) {
                                log("Spoofed Resources.getConfiguration() mLocales to $languageCode for $packageName")
                            }
                        } catch (e: Throwable) {
                            log("Error setting mLocales field: ${e.message}")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.getConfiguration for mLocales: ${e.message}")
        }
        
        // 3. Hook Resources.updateConfiguration to ensure changes stick for API 24+
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
                            val localeList = LocaleList(locale)
                            XposedHelpers.setObjectField(config, "mLocales", localeList)
                            if (verboseLog) {
                                log("Spoofed updateConfiguration mLocales to $languageCode for $packageName")
                            }
                        } catch (e: Throwable) {
                            log("Error setting mLocales in updateConfiguration: ${e.message}")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.updateConfiguration for mLocales: ${e.message}")
        }
        
        // 4. Hook for category-specific Locale.getDefault (API 24+)
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
        } catch (e: Throwable) {
            log("Error hooking Locale.getDefault(Category): ${e.message}")
        }
    }
    
    private fun hookPreApi24LocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        // 1. Hook Configuration.getLocale() (API < 24)
        try {
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
        } catch (e: Throwable) {
            log("Error hooking Configuration.getLocale(): ${e.message}")
        }
    }
}