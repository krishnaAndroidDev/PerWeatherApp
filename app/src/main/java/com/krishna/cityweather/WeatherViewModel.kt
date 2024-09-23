package com.krishna.cityweather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    val citySearchResults: MutableStateFlow<NetworkResult<List<City>>> =
        MutableStateFlow(NetworkResult.Loading())

    val cityWeatherResults: MutableStateFlow<NetworkResult<WeatherData>> =
        MutableStateFlow(NetworkResult.Loading())

    // StateFlow to hold latitude, longitude and city name
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName: StateFlow<String?> = _cityName

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    fun searchForCity(query: String) = viewModelScope.launch(Dispatchers.IO) {
        citySearchResults.value = repository.getLatLonByCityName(query)
    }

    // Function to get weather data
    fun getWeather(context: Context, lat: Double, lon: Double, cityName: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.getWeatherData(lat, lon)
        cityWeatherResults.value = result

        if (result is NetworkResult.Success) {
            // Save the city name in SharedPreferences
            SharedPreferencesHelper.saveCityName(context, cityName)
        }
    }

    fun checkLocationPermission(context: Context) {
        _hasLocationPermission.value =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Fetch location from FusedLocationProviderClient
    fun fetchLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _latitude.value = location.latitude
                _longitude.value = location.longitude
                getCityName(context, location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    // Get city name using Geocoder
    private fun getCityName(context: Context, latitude: Double, longitude: Double) {
        val geocoder = Geocoder(context, Locale.getDefault())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    _cityName.value = addresses[0].locality
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Update permission state
    fun updateLocationPermission(isGranted: Boolean) {
        _hasLocationPermission.value = isGranted
    }

}