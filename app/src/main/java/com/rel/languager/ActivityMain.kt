package com.rel.languager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.rel.languager.Constants.PREF_APP_LANGUAGE_MAP
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
    private lateinit var saveButton: MaterialButton
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
        appListRecyclerView = findViewById(R.id.app_list)
        searchView = findViewById(R.id.search_view)
        loadingProgress = findViewById(R.id.loading_progress)
        noAppsText = findViewById(R.id.no_apps_text)
        saveButton = findViewById(R.id.save_button)

        // Set up RecyclerView
        appListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        // Set up search functionality
        searchView.apply {
            isSubmitButtonEnabled = false
            isFocusable = true
            isIconified = false
            clearFocus() // Clear initial focus to prevent keyboard from showing automatically

            // Set text colors for the SearchView
            val searchText = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchText?.apply {
                setTextColor(ContextCompat.getColor(this@ActivityMain, android.R.color.white))
                setHintTextColor(ContextCompat.getColor(this@ActivityMain, R.color.teal_200))
                // Change cursor color to white
                try {
                    // Get the cursor drawable field
                    val cursorDrawableField = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                    cursorDrawableField.isAccessible = true
                    // Set cursor color to white
                    cursorDrawableField.set(this, R.drawable.white_cursor)
                } catch (e: Exception) {
                    // Ignore if we can't change the cursor color
                }
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterApps(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterApps(newText)
                    return true
                }
            })
        }

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
            try {
                val enabledAppsList = withContext(Dispatchers.IO) {
                    getEnabledApps()
                }

                enabledApps.clear()
                enabledApps.addAll(enabledAppsList)

                if (enabledApps.isEmpty()) {
                    loadingProgress.visibility = View.GONE
                    noAppsText.visibility = View.VISIBLE
                    noAppsText.text = getString(R.string.no_apps_found)
                } else {
                    // Create and set the adapter
                    val adapter = AppLanguageAdapter(
                        this@ActivityMain,
                        enabledApps,
                        languageMappings,
                        LanguageUtils.getAvailableLanguages()
                    ) { packageName, languageCode ->
                        // Update language mapping when spinner selection changes
                        languageMappings[packageName] = languageCode
                    }

                    appListRecyclerView.adapter = adapter
                    loadingProgress.visibility = View.GONE
                    appListRecyclerView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                loadingProgress.visibility = View.GONE
                noAppsText.visibility = View.VISIBLE
                noAppsText.text = getString(R.string.error_loading_apps)
            }
        }
    }

    private suspend fun getEnabledApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        val pm = packageManager
        @Suppress("DEPRECATION")
        val installedApps = pm.getInstalledApplications(0)

        return@withContext installedApps.filter { app ->
            app.enabled && !app.packageName.equals(packageName)
        }.sortedBy {
            pm.getApplicationLabel(it).toString().lowercase()
        }
    }

    private fun filterApps(query: String?) {
        val adapter = appListRecyclerView.adapter as? AppLanguageAdapter ?: return

        if (query.isNullOrBlank()) {
            adapter.updateList(enabledApps)
            return
        }

        val filteredApps = enabledApps.filter { appInfo ->
            val appName = packageManager.getApplicationLabel(appInfo).toString().lowercase()
            val packageName = appInfo.packageName.lowercase()
            val searchQuery = query.lowercase()

            appName.contains(searchQuery) || packageName.contains(searchQuery)
        }

        adapter.updateList(filteredApps)
    }
}
