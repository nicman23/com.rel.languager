package com.rel.languager

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
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
            if (packageName == BuildConfig.APPLICATION_ID) {
                return
            }

            pref.reload()
            val languageCode = LanguageUtils.getLanguageForPackage(packageName, pref)

            if (languageCode == Constants.DEFAULT_LANGUAGE) {
                return
            }

            val locale = LanguageUtils.getLocaleForLanguage(languageCode)
            hookLocaleAPIs(lpparam, locale, languageCode, packageName)
        }
    }

    private fun hookLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale,
        languageCode: String,
        packageName: String
    ) {
        try {
            hookCommonLocaleAPIs(lpparam, locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hookApi24PlusLocaleAPIs(lpparam, locale)
            } else {
                hookPreApi24LocaleAPIs(lpparam, locale)
            }

        } catch (e: Exception) {
            log("Error during hooking process: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun hookCommonLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale
    ) {
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

        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "getConfiguration",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val config = param.result as Configuration
                        XposedHelpers.setObjectField(config, "locale", locale)
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.getConfiguration(): ${e.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "updateConfiguration",
                Configuration::class.java,
                android.util.DisplayMetrics::class.java,
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

        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java.name,
                lpparam.classLoader,
                "getSystem",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val resources = param.result as Resources
                        val config = resources.configuration
                        XposedHelpers.setObjectField(config, "locale", locale)
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Resources.getSystem(): ${e.message}")
        }

        try {
            XposedHelpers.findAndHookConstructor(
                Configuration::class.java.name,
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedHelpers.setObjectField(param.thisObject, "locale", locale)
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Configuration constructor: ${e.message}")
        }
    }

    private fun hookApi24PlusLocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale
    ) {
        try {
            XposedHelpers.findAndHookMethod(
                Locale::class.java.name,
                lpparam.classLoader,
                "getDefault",
                Locale.Category::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = locale
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Locale.getDefault(Category): ${e.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.content.res.Configuration", lpparam.classLoader,
                "getLocales", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Use XposedHelpers to create LocaleList to avoid direct API call
                        val localeListClass = XposedHelpers.findClass(
                            "android.os.LocaleList",
                            lpparam.classLoader
                        )
                        val localeList = try {
                            XposedHelpers.newInstance(
                                localeListClass,
                                arrayOf<Any>(locale)
                            )
                        } catch (e: Throwable) {
                            try {
                                // If array constructor fails, try the varargs constructor
                                XposedHelpers.callStaticMethod(
                                    localeListClass,
                                    "create",
                                    locale
                                )
                            } catch (e2: Throwable) {
                                try {
                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "getDefault"
                                    )
                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "forLanguageTags",
                                        locale.toLanguageTag()
                                    )
                                } catch (e3: Throwable) {
                                    log("Failed to create LocaleList: ${e3.message}")
                                    null
                                }
                            }
                        }
                        param.result = localeList
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Configuration.getLocales(): ${e.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.content.res.Configuration", lpparam.classLoader,
                "setLocales",
                "android.os.LocaleList",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Use XposedHelpers to create LocaleList to avoid direct API call
                        val localeListClass = XposedHelpers.findClass(
                            "android.os.LocaleList",
                            lpparam.classLoader
                        )
                        // Create a LocaleList using reflection to support all API levels
                        val localeList = try {
                            XposedHelpers.newInstance(
                                localeListClass,
                                arrayOf<Any>(locale)
                            )
                        } catch (e: Throwable) {
                            try {
                                XposedHelpers.callStaticMethod(
                                    localeListClass,
                                    "create",
                                    locale
                                )
                            } catch (e2: Throwable) {
                                try {
                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "getDefault"
                                    )

                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "forLanguageTags",
                                        locale.toLanguageTag()
                                    )
                                } catch (e3: Throwable) {
                                    log("Failed to create LocaleList: ${e3.message}")
                                    null
                                }
                            }
                        }
                        param.args[0] = localeList
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking Configuration.setLocales: ${e.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.os.LocaleList", lpparam.classLoader,
                "getDefault", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val localeListClass = XposedHelpers.findClass(
                            "android.os.LocaleList",
                            lpparam.classLoader
                        )
                        val localeList = try {
                            XposedHelpers.newInstance(
                                localeListClass,
                                arrayOf<Any>(locale)
                            )
                        } catch (e: Throwable) {
                            try {
                                XposedHelpers.callStaticMethod(
                                    localeListClass,
                                    "create",
                                    locale
                                )
                            } catch (e2: Throwable) {
                                try {
                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "forLanguageTags",
                                        locale.toLanguageTag()
                                    )
                                } catch (e3: Throwable) {
                                    log("Failed to create LocaleList: ${e3.message}")
                                    null
                                }
                            }
                        }
                        param.result = localeList
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking LocaleList.getDefault(): ${e.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.os.LocaleList", lpparam.classLoader,
                "getAdjustedDefault", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val localeListClass = XposedHelpers.findClass(
                            "android.os.LocaleList",
                            lpparam.classLoader
                        )
                        val localeList = try {
                            XposedHelpers.newInstance(
                                localeListClass,
                                arrayOf<Any>(locale)
                            )
                        } catch (e: Throwable) {
                            try {
                                XposedHelpers.callStaticMethod(
                                    localeListClass,
                                    "create",
                                    locale
                                )
                            } catch (e2: Throwable) {
                                try {
                                    XposedHelpers.callStaticMethod(
                                        localeListClass,
                                        "forLanguageTags",
                                        locale.toLanguageTag()
                                    )
                                } catch (e3: Throwable) {
                                    log("Failed to create LocaleList: ${e3.message}")
                                    null
                                }
                            }
                        }
                        param.result = localeList
                    }
                }
            )
        } catch (e: Throwable) {
            log("Error hooking LocaleList.getAdjustedDefault(): ${e.message}")
        }
    }

    private fun hookPreApi24LocaleAPIs(
        lpparam: XC_LoadPackage.LoadPackageParam,
        locale: Locale
    ) {
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
