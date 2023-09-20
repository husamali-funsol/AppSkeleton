package com.android.ar.ruler.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MyPreferences {

    private var instance: MyPreferences? = null

    fun getPrefInstance(): MyPreferences? {
        if (instance == null) {
            instance = MyPreferences()
        }
        return instance
    }

    fun setStringPreferences(context: Context, key: String?, result: String?) {
        CoroutineScope(IO).launch {
            val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString(key, result)
            editor.apply()
        }
    }

    fun getIntPreferences(context: Context, key: String, default: Int = 0): Int {
        val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
        return preferences.getInt(key, default)
    }

    fun setIntPreferences(context: Context, key: String, result: Int) {
        CoroutineScope(IO).launch {
            val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putInt(key, result)
            editor.apply()
        }
    }

    fun getLongPreferences(context: Context, key: String): Long {
        val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
        return preferences.getLong(key, 0)
    }

    fun setLongPreferences(context: Context, key: String, result: Long) {
        CoroutineScope(IO).launch {
            val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putLong(key, result)
            editor.apply()
        }
    }

    fun getStringPreferences(context: Context, key: String?,defaultValue: String = "English"): String? {
        val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
        return preferences.getString(key, defaultValue)
    }

    fun setBooleanPreferences(context: Context, key: String?, result: Boolean) {
        CoroutineScope(IO).launch {
            val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putBoolean(key, result)
            editor.apply()
        }
    }

    fun getBooleanPreferences(context: Context, key: String?): Boolean {
        val preferences = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
        return preferences.getBoolean(key, false)
    }

}