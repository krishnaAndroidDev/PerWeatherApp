package com.krishna.cityweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
) : ViewModel() {

    val citySearchResults: MutableStateFlow<NetworkResult<List<City>>> =
        MutableStateFlow(NetworkResult.Loading())

    val cityWeatherResults: MutableStateFlow<NetworkResult<WeatherData>> =
        MutableStateFlow(NetworkResult.Loading())

    fun searchForCity(query: String) = viewModelScope.launch(Dispatchers.IO) {
        citySearchResults.value = repository.getLatLonByCityName(query)
    }

    fun getWeather(lat: Double, lon: Double) = viewModelScope.launch(Dispatchers.IO) {
        cityWeatherResults.value = repository.getWeatherData(lat, lon)
    }

}