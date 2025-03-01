package com.rel.languager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
    private var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (pref == null) {
            AlertDialog.Builder(this)
                .setMessage(R.string.module_not_enabled)
                .setPositiveButton(R.string.close) { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        }

        initializeViews()
        setupListeners()
        loadLanguageMappings()
        loadEnabledApps()
        
        setupBackPressHandling()
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges) {
                    AlertDialog.Builder(this@ActivityMain)
                        .setTitle(R.string.unsaved_changes_title)
                        .setMessage(R.string.unsaved_changes_message)
                        .setPositiveButton(R.string.save_and_exit) { _, _ ->
                            saveLanguageMappings()
                            finish()
                        }
                        .setNegativeButton(R.string.discard_and_exit) { _, _ ->
                            finish()
                        }
                        .setNeutralButton(R.string.cancel, null)
                        .show()
                } else {
                    finish()
                }
            }
        })
    }

    private fun initializeViews() {
        appListRecyclerView = findViewById(R.id.app_list)
        searchView = findViewById(R.id.search_view)
        loadingProgress = findViewById(R.id.loading_progress)
        noAppsText = findViewById(R.id.no_apps_text)
        saveButton = findViewById(R.id.save_button)

        appListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        searchView.apply {
            isSubmitButtonEnabled = false
            isFocusable = true
            isIconified = false
            clearFocus() 

            val searchText = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchText?.apply {
                setTextColor(ContextCompat.getColor(this@ActivityMain, android.R.color.white))
                setHintTextColor(ContextCompat.getColor(this@ActivityMain, R.color.teal_200))
                try {
                    val cursorDrawableField = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                    cursorDrawableField.isAccessible = true
                    cursorDrawableField.set(this, R.drawable.white_cursor)
                } catch (e: Exception) {
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

        saveButton.setOnClickListener {
            saveLanguageMappings()
        }
    }

    private fun loadLanguageMappings() {
        languageMappings.clear()
        pref?.let { preferences ->
            val mappings = LanguageUtils.getAllLanguageMappings(preferences)
            languageMappings.putAll(mappings)
        }
        hasUnsavedChanges = false
    }

    private fun saveLanguageMappings() {
        try {
            pref?.let { preferences ->
                val editor = preferences.edit()
                
                val allPrefs = preferences.all
                for (key in allPrefs.keys) {
                    if (key != Constants.PREF_APP_LANGUAGE_MAP) {
                        editor.remove(key)
                    }
                }
                
                for ((packageName, languageCode) in languageMappings) {
                    if (languageCode != Constants.DEFAULT_LANGUAGE) {
                        editor.putString(packageName, languageCode)
                    }
                }
                
                editor.apply()
            }

            Snackbar.make(
                findViewById(R.id.root_view_for_snackbar),
                R.string.settings_saved,
                Snackbar.LENGTH_SHORT
            ).show()
            
            hasUnsavedChanges = false
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
                    val adapter = AppLanguageAdapter(
                        this@ActivityMain,
                        enabledApps,
                        languageMappings,
                        LanguageUtils.getAvailableLanguages()
                    ) { packageName, languageCode ->
                        languageMappings[packageName] = languageCode
                        hasUnsavedChanges = true
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
