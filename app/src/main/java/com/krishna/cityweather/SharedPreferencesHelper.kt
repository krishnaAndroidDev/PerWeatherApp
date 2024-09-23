package com.krishna.cityweather

import android.content.Context

object SharedPreferencesHelper {
    private const val PREFS_NAME = "city_weather_prefs"
    private const val KEY_CITY_NAME = "city_name"

    fun saveCityName(context: Context, cityName: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_CITY_NAME, cityName)
            apply()
        }
    }

    fun getSavedCityName(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_CITY_NAME, null)
    }
}
