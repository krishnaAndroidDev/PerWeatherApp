package com.krishna.cityweather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("data/2.5/weather?units=imperial&appid=$API_KEY")
    suspend fun getWeatherData(@Query("lat") lat: Double, @Query("lon") lon: Double) :WeatherData

    @GET("geo/1.0/direct?limit=5&appid=$API_KEY")
    suspend fun getGeoLocation(@Query("q") cityName: String) :List<City>

}
