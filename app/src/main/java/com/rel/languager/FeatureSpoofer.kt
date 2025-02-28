package com.rel.languager

import android.content.res.Configuration
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
            log("Preference location: ${file.canonicalPath}")
        }
    }

    private val verboseLog: Boolean by lazy {
        pref.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        log("Loaded languager for ${lpparam?.packageName}")

        // Hook locale methods to force English locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Hook the getLocales method for API 24+
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam?.classLoader,
                "getLocales",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val englishLocaleList = LocaleList(Locale.ENGLISH)
                        param.result = englishLocaleList
                        if (verboseLog) {
                            log("Spoofed getLocales to English")
                        }
                    }
                }
            )
        } else {
            // Hook the getLocale method for API < 24
            XposedHelpers.findAndHookMethod(
                Configuration::class.java.name,
                lpparam?.classLoader,
                "getLocale",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = Locale.ENGLISH
                        if (verboseLog) {
                            log("Spoofed getLocale to English")
                        }
                    }
                }
            )
        }
    }
}