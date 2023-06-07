package com.krishna.cityweather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService,
) {

    suspend fun getLatLonByCityName(searchQuery: String) = withContext(Dispatchers.IO) {
        try {
            val results = weatherService.getGeoLocation(searchQuery)

            Log.v("hiiiiiii", "results: $results")

            NetworkResult.Success(results)
        } catch (e: Exception) {
            Log.v("hiiiiiii", "results: ${e.message}")

            NetworkResult.Error("Not found")
        }
    }

    suspend fun getWeatherData(lat:Double,lon:Double) = withContext(Dispatchers.IO) {
        try {
            val results = weatherService.getWeatherData(lat,lon)

            Log.v("hiiiiiii", "results: $results")

            NetworkResult.Success(results)
        } catch (e: Exception) {
            Log.v("hiiiiiii", "results: ${e.message}")

            NetworkResult.Error("Not found")
        }
    }

}