package com.krishna.cityweather

import com.squareup.moshi.Json

class SearchCityResponse {
}

data class City(
    @Json val name: String,
    @Json val lat: Double,
    @Json val lon: Double,
    @Json val country: String,
    @Json val state: String,
)

data class WeatherData(
    @Json val weather: List<Weather>,
    @Json val main: Main,
)

data class Main(
    @Json val temp: Double,
    @Json val temp_max: Double,
    @Json val temp_min: Double,
    @Json val pressure: Int,
    @Json val humidity: Int,
)

data class Weather(
    @Json val main: String,
    @Json val icon: String,
)
