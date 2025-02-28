package com.rel.languager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppLanguageAdapter(
    private val context: Context,
    private var appList: List<ApplicationInfo>,
    private val languageMappings: MutableMap<String, String>,
    private val onLanguageSelected: (String, String) -> Unit
) : RecyclerView.Adapter<AppLanguageAdapter.AppViewHolder>() {

    private val packageManager: PackageManager = context.packageManager
    private val availableLanguages = LanguageUtils.getAvailableLanguages()

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val languageSpinner: Spinner = view.findViewById(R.id.language_spinner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_language, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = appList[position]
        val packageName = app.packageName

        // Set app icon and name
        holder.appIcon.setImageDrawable(app.loadIcon(packageManager))
        holder.appName.text = app.loadLabel(packageManager)
        holder.packageName.text = packageName

        // Create language spinner adapter
        val spinnerAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            availableLanguages.map { it.second }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        holder.languageSpinner.adapter = spinnerAdapter

        // Set the selected language
        val currentLanguage = languageMappings[packageName] ?: Constants.DEFAULT_LANGUAGE
        val languageIndex = availableLanguages.indexOfFirst { it.first == currentLanguage }
        if (languageIndex >= 0) {
            holder.languageSpinner.setSelection(languageIndex)
        }

        // Set listener for language selection
        holder.languageSpinner.setOnItemSelectedListener { position ->
            val selectedLanguageCode = availableLanguages[position].first
            languageMappings[packageName] = selectedLanguageCode
            onLanguageSelected(packageName, selectedLanguageCode)
        }
    }

    override fun getItemCount() = appList.size

    // Method to update the list for filtering
    fun updateList(newList: List<ApplicationInfo>) {
        appList = newList
        notifyDataSetChanged()
    }

    // Extension function to simplify setting item selected listener
    private fun Spinner.setOnItemSelectedListener(onItemSelected: (Int) -> Unit) {
        this.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onItemSelected(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        })
    }
}
