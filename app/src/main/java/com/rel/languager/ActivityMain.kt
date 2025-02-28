package com.rel.languager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP
import com.rel.languager.Constants.PREF_ENABLE_VERBOSE_LOGS
import com.rel.languager.Constants.SHARED_PREF_FILE_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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

    private lateinit var appListRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var noAppsText: TextView
    private lateinit var saveButton: Button
    private lateinit var verboseLogsSwitch: Switch
    private lateinit var statusText: TextView

    private val enabledApps = mutableListOf<ApplicationInfo>()
    private val languageMappings = mutableMapOf<String, String>()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        initializeViews()
        setupListeners()
        loadLanguageMappings()
        loadEnabledApps()
    }

    private fun initializeViews() {
        statusText = findViewById(R.id.status_text)
        verboseLogsSwitch = findViewById(R.id.verbose_logs_switch)
        appListRecyclerView = findViewById(R.id.app_list)
        searchView = findViewById(R.id.search_view)
        loadingProgress = findViewById(R.id.loading_progress)
        noAppsText = findViewById(R.id.no_apps_text)
        saveButton = findViewById(R.id.save_button)

        // Set up RecyclerView
        appListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set initial values
        statusText.text = getString(R.string.module_status_active)
        verboseLogsSwitch.isChecked = pref?.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false) ?: false
    }

    private fun setupListeners() {
        // Set listeners for verbose logs switch
        verboseLogsSwitch.setOnCheckedChangeListener { _, isChecked ->
            pref?.edit()?.putBoolean(PREF_ENABLE_VERBOSE_LOGS, isChecked)?.apply()
        }

        // Set up search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText)
                return true
            }
        })

        // Set up save button
        saveButton.setOnClickListener {
            saveLanguageMappings()
        }
    }

    private fun loadLanguageMappings() {
        val languageMapJson = pref?.getString(PREF_APP_LANGUAGE_MAP, "{}")
        try {
            val jsonObject = JSONObject(languageMapJson ?: "{}")
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                languageMappings[key] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            // If there's an error, we'll start with an empty map
        }
    }

    private fun saveLanguageMappings() {
        try {
            val jsonObject = JSONObject()
            for ((packageName, languageCode) in languageMappings) {
                jsonObject.put(packageName, languageCode)
            }
            pref?.edit()?.putString(PREF_APP_LANGUAGE_MAP, jsonObject.toString())?.apply()
            
            Snackbar.make(
                findViewById(R.id.root_view_for_snackbar),
                R.string.settings_saved,
                Snackbar.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Snackbar.make(
                findViewById(R.id.root_view_for_snackbar),
                R.string.error_saving_settings,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadEnabledApps() {
        loadingProgress.visibility = View.VISIBLE
        appListRecyclerView.visibility = View.GONE
        noAppsText.visibility = View.GONE

        mainScope.launch {
            val enabledAppsList = withContext(Dispatchers.IO) {
                getEnabledApps()
            }

            enabledApps.clear()
            enabledApps.addAll(enabledAppsList)

            if (enabledApps.isEmpty()) {
                loadingProgress.visibility = View.GONE
                noAppsText.visibility = View.VISIBLE
            } else {
                loadingProgress.visibility = View.GONE
                appListRecyclerView.visibility = View.VISIBLE
                
                // Set up adapter
                val adapter = AppLanguageAdapter(
                    this@ActivityMain,
                    enabledApps,
                    languageMappings
                ) { packageName, languageCode ->
                    // This is called when a language is selected for an app
                    languageMappings[packageName] = languageCode
                }
                appListRecyclerView.adapter = adapter
            }
        }
    }

    private fun getEnabledApps(): List<ApplicationInfo> {
        val packageManager = packageManager
        val result = mutableListOf<ApplicationInfo>()

        try {
            // Get all installed applications that have a launcher icon (user-visible apps)
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                // Only include non-system apps that can be launched
                if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0 || 
                    packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                    result.add(app)
                }
            }
            
            // Sort apps by name
            result.sortWith { app1, app2 ->
                val name1 = app1.loadLabel(packageManager).toString()
                val name2 = app2.loadLabel(packageManager).toString()
                name1.compareTo(name2, ignoreCase = true)
            }
        } catch (e: Exception) {
            // Return empty list on error
        }

        return result
    }

    private fun filterApps(query: String?) {
        if (query.isNullOrEmpty()) {
            (appListRecyclerView.adapter as? AppLanguageAdapter)?.let { adapter ->
                // Reset to original list
                adapter.updateList(enabledApps)
            }
            return
        }

        val filteredList = enabledApps.filter { app ->
            val appName = app.loadLabel(packageManager).toString().lowercase()
            val packageName = app.packageName.lowercase()
            val searchQuery = query.lowercase()
            
            appName.contains(searchQuery) || packageName.contains(searchQuery)
        }

        (appListRecyclerView.adapter as? AppLanguageAdapter)?.let { adapter ->
            adapter.updateList(filteredList)
            
            if (filteredList.isEmpty()) {
                noAppsText.visibility = View.VISIBLE
                appListRecyclerView.visibility = View.GONE
            } else {
                noAppsText.visibility = View.GONE
                appListRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
