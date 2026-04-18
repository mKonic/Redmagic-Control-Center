package com.elitedarkkaiser.redmagic

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AppProfileRuleManager {

    private const val PREFS = "app_profile_rules"
    private const val KEY = "rules"

    fun saveRules(context: Context, rules: List<AppProfileRule>) {
        val arr = JSONArray()
        rules.forEach {
            val obj = JSONObject()
            obj.put("packageName", it.packageName)
            obj.put("profileName", it.profileName)
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, arr.toString())
            .commit()
    }

    fun loadRules(context: Context): MutableList<AppProfileRule> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return mutableListOf()

        val arr = JSONArray(raw)
        val list = mutableListOf<AppProfileRule>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                AppProfileRule(
                    packageName = o.getString("packageName"),
                    profileName = o.getString("profileName")
                )
            )
        }
        return list
    }
}
