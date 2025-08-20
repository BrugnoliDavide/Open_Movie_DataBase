package com.example.openvideodatabase.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun isFirstTime(): Boolean {
        return sharedPreferences.getString("user_name", null) == null
    }

    fun clearUserData() {
        sharedPreferences.edit().remove("user_name").apply()
    }
}