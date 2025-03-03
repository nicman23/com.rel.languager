package com.rel.languager

import java.util.Locale
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import android.content.Context
import android.widget.ImageView
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import androidx.recyclerview.widget.RecyclerView

class AppLanguageAdapter(
    private val context: Context,
    private var appList: List<ApplicationInfo>,
    private val languageMappings: MutableMap<String, String>,
    private val availableLanguages: List<Locale> = LanguageUtils.getAvailableLanguages(),
    private val onLanguageSelected: (String, String) -> Unit
) : RecyclerView.Adapter<AppLanguageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val languageSpinner: Spinner = view.findViewById(R.id.language_spinner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        val packageManager = context.packageManager

        holder.appIcon.setImageDrawable(app.loadIcon(packageManager))
        holder.appName.text = app.loadLabel(packageManager)
        holder.packageName.text = app.packageName

        val spinnerAdapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            availableLanguages.map { it.displayName }
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                textView.text = availableLanguages[position].language
                textView.textSize = 14f
                textView.setPadding(8, 0, 8, 0)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                textView.text = availableLanguages[position].displayName
                return view
            }
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.languageSpinner.adapter = spinnerAdapter

        val currentLanguage = languageMappings[app.packageName] ?: ""
        val languageIndex = availableLanguages.indexOfFirst { it.language == currentLanguage }
        if (languageIndex >= 0) {
            holder.languageSpinner.setSelection(languageIndex)
        }

        holder.languageSpinner.setOnItemSelectedListener { pos ->
            val selectedLanguageCode = availableLanguages[pos].language
            val currentLanguageCode = languageMappings[app.packageName] ?: Constants.DEFAULT_LANGUAGE

            if (selectedLanguageCode != currentLanguageCode) {
                languageMappings[app.packageName] = selectedLanguageCode
                onLanguageSelected(app.packageName, selectedLanguageCode)
            }
        }
    }

    override fun getItemCount() = appList.size

    fun updateList(newList: List<ApplicationInfo>) {
        appList = newList
        notifyDataSetChanged()
    }

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
                return
            }
        })
    }
}
