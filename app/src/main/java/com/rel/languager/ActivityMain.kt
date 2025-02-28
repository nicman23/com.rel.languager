package com.rel.languager

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rel.languager.Constants.PREF_ENABLE_VERBOSE_LOGS
import com.rel.languager.Constants.SHARED_PREF_FILE_NAME

class ActivityMain : AppCompatActivity() {
    /**
     * Normally [MODE_WORLD_READABLE] causes a crash.
     * But if "xposedsharedprefs" flag is present in AndroidManifest,
     * then the file is accordingly taken care by lsposed framework.
     *
     * If an exception is thrown, means module is not enabled,
     * hence Android throws a security exception.
     */
    private val pref by lazy {
        try {
            getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_WORLD_READABLE)
        } catch (_: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if module is enabled
        if (pref == null) {
            AlertDialog.Builder(this)
                .setMessage(R.string.module_not_enabled)
                .setPositiveButton(R.string.close) { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        }

        // Initialize UI elements
        val statusText = findViewById<TextView>(R.id.status_text)
        val verboseLogsSwitch = findViewById<Switch>(R.id.verbose_logs_switch)

        // Set initial values
        statusText.text = getString(R.string.module_status_active)
        verboseLogsSwitch.isChecked = pref?.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false) ?: false

        // Set listeners
        verboseLogsSwitch.setOnCheckedChangeListener { _, isChecked ->
            pref?.edit()?.putBoolean(PREF_ENABLE_VERBOSE_LOGS, isChecked)?.apply()
        }
    }
}
