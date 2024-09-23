package com.krishna.cityweather

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.krishna.cityweather.ui.theme.CityWeatherTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CityWeatherTheme {
                val context = LocalContext.current
                Scaffold(topBar = {
                    SearchBarSample(context = context)
                }) { dp ->
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(dp)
                    ) {
                        WeatherView()
                        LocationScreen(context = context)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchBarSample(
        viewModel: WeatherViewModel = viewModel(),
        context: Context
    ) {
        var text by rememberSaveable { mutableStateOf("") }
        var active by rememberSaveable { mutableStateOf(false) }

        val cities = viewModel.citySearchResults.collectAsState().value
        val doesHavePermission = viewModel.hasLocationPermission.collectAsState().value


        // Fetch the last saved city name when the composable is first launched
        LaunchedEffect(Unit) {
            text = SharedPreferencesHelper.getSavedCityName(context) ?: ""
        }

        Column(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(all = PADDING_8.dp)
                .semantics { isContainer = true }) {

            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = text,
                onQueryChange = {
                    text = it
                    if (doesHavePermission) {
                        viewModel.searchForCity(it)
                    } else {
                        Toast.makeText(context,  R.string.no_search_perm, Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onSearch = {
                    viewModel.searchForCity(it)
                    if (cities is NetworkResult.Success && cities.data.isNotEmpty()) {
                        val selectedCity = cities.data.first()
                        viewModel.getWeather(
                            context,
                            selectedCity.lat,
                            selectedCity.lon,
                            selectedCity.name
                        )
                    }
                },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = { Text(stringResource(id = R.string.city_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (active)
                        IconButton(
                            onClick = {
                                text = ""
                                active = false
                            }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                },
            ) {

                when (cities) {
                    is NetworkResult.Success -> {

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(PADDING_16.dp),
                            verticalArrangement = Arrangement.spacedBy(PADDING_4.dp)
                        ) {
                            items(cities.data) {
                                ListItem(
                                    headlineContent = { Text(it.name) },
                                    supportingContent = { Text(it.state + ", " + it.country) },
                                    modifier = Modifier.clickable {
                                        viewModel.getWeather(context, it.lat, it.lon, it.name)
                                        text = it.name
                                        active = false
                                    })
                            }
                        }
                    }

                    else -> {
                        ListItem(headlineContent = { Text(stringResource(id = R.string.not_found)) })
                    }
                }

            }
        }
    }


    @Composable
    fun WeatherView(
        viewModel: WeatherViewModel = viewModel(),
    ) {

        val weather = viewModel.cityWeatherResults.collectAsState().value

        when (weather) {

            is NetworkResult.Success -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PADDING_24.dp, vertical = PADDING_8.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .width(PADDING_60.dp)
                                .height(PADDING_60.dp),
                            model = IMAGE_URL.replace("$", weather.data.weather.first().icon),
                            contentDescription = "Weather icon",
                        )

                        Text(
                            text = weather.data.main.temp.toInt().toString().uppercase() + "°",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            modifier = Modifier.padding(bottom = PADDING_8.dp),
                            text = "F",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            modifier = Modifier.padding(all = PADDING_8.dp),
                            text = weather.data.weather.first().main,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PADDING_100.dp)
                            .padding(horizontal = PADDING_24.dp, vertical = PADDING_8.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_8.dp),
                                text = weather.data.main.temp_min.toInt().toString() + "°F",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_8.dp),
                                text = stringResource(id = R.string.min_temp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .height(PADDING_80.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_8.dp),
                                text = weather.data.main.temp_max.toInt().toString() + "°F",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_8.dp),
                                text = stringResource(id = R.string.max_temp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .height(PADDING_80.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_10.dp),
                                text = weather.data.main.humidity.toString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                modifier = Modifier.padding(bottom = PADDING_10.dp),
                                text = stringResource(id = R.string.humidity),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    }

                    Spacer(modifier = Modifier.weight(1f))

                }
            }

            else -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = PADDING_10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = stringResource(id = R.string.city_name_search)
                    )
                }
            }
        }
    }

    @Composable
    fun LocationScreen(viewModel: WeatherViewModel = viewModel(), context: Context) {

        val latitude by viewModel.latitude.collectAsState()
        val longitude by viewModel.longitude.collectAsState()
        val cityName by viewModel.cityName.collectAsState()
        val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            viewModel.updateLocationPermission(isGranted)

            if (isGranted) {
                viewModel.fetchLocation(context)
            } else {
                Toast.makeText(context, R.string.perm_denied, Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.checkLocationPermission(context)

            if (!hasLocationPermission) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PADDING_24.dp, vertical = PADDING_12.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(PADDING_8.dp),
                text = stringResource(id = R.string.your_location),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(PADDING_16.dp))

            if (latitude != null && longitude != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.latitude))
                        }
                        append("$latitude")
                    },
                    modifier = Modifier.padding(PADDING_8.dp)
                )

                Text(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.longitude))
                    }
                    append("$longitude")
                }, modifier = Modifier.padding(PADDING_8.dp))

                Text(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.city_name))
                    }
                    append("$cityName")
                }, modifier = Modifier.padding(PADDING_8.dp))
            } else {
                Text(
                    text = stringResource(R.string.no_permission),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    companion object {
        private const val PADDING_8 = 8
        private const val PADDING_4 = 4
        private const val PADDING_10 = 10
        private const val PADDING_16 = 16
        private const val PADDING_24 = 24
        private const val PADDING_12 = 12
        private const val PADDING_80 = 80
        private const val PADDING_60 = 60
        private const val PADDING_100 = 100
    }
}
