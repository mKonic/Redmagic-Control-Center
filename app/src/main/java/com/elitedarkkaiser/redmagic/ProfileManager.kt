
package com.elitedarkkaiser.redmagic

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ProfileManager {

    private const val KEY = "profiles"

    fun saveProfiles(context: Context, profiles: List<Profile>) {
        val arr = JSONArray()
        profiles.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("fanEnabled", it.fanEnabled)
            obj.put("fanLevel", it.fanLevel)
            obj.put("pumpEnabled", it.pumpEnabled)
            obj.put("pumpProfile", it.pumpProfile)
            obj.put("autoFan", it.autoFan)
            arr.put(obj)
        }
        context.getSharedPreferences("profiles", 0)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun loadProfiles(context: Context): MutableList<Profile> {
        val str = context.getSharedPreferences("profiles", 0).getString(KEY, null)
        val list = mutableListOf<Profile>()
        if (str != null) {
            val arr = JSONArray(str)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Profile(
                        o.getString("name"),
                        o.getBoolean("fanEnabled"),
                        o.getInt("fanLevel"),
                        o.getBoolean("pumpEnabled"),
                        o.getString("pumpProfile"),
                        o.getBoolean("autoFan")
                    )
                )
            }
        }
        return list
    }
}
