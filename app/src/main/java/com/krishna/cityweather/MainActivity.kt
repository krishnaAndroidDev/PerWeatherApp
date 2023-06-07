package com.krishna.cityweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CityWeatherTheme {
        SearchBarSample()
    }
}

@Composable
fun WeatherScreen() {
    Scaffold(
        topBar = { SearchBarSample() }
    ) {
        Box(modifier = Modifier.padding(it)) {
            WeatherView()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSample(
    viewModel: WeatherViewModel = viewModel(),
) {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    val cities = viewModel.citySearchResults.collectAsState().value

    Box(
        Modifier
            .semantics { isContainer = true }
            .zIndex(4f)
            .padding(top = 6.dp)
            .fillMaxWidth()) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            query = text,
            onQueryChange = {
                text = it
                viewModel.searchForCity(it)
            },
            onSearch = {
                active = false
            },
            active = active,
            onActiveChange = {
                active = it
            },
            placeholder = { Text("Search city") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (active)
                    IconButton(onClick = {
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(cities.data) {
                            ListItem(
                                headlineContent = { Text(it.name) },
                                supportingContent = { Text(it.state + ", " + it.country) },
                                modifier = Modifier.clickable {
                                    viewModel.getWeather(it.lat, it.lon)
                                    text = it.name
                                    active = false
                                }
                            )
                        }
                    }

                }

                else -> {
                    ListItem(headlineContent = { Text("Not found") })
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

                AsyncImage(
                    modifier = Modifier
                        .width(300.dp)
                        //.background(Color.Cyan)
                        .height(300.dp),
                    model = IMAGE_URL.replace("$", weather.data.weather.first().icon),
                    contentDescription = "Weather icon",
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = weather.data.main.temp.toInt().toString().uppercase() + "°",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 9.dp),
                        text = "F",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    text = weather.data.weather.first().main,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 24.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = weather.data.main.temp_min.toInt().toString() + "°F",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = "Min Temp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = weather.data.main.temp_max.toInt().toString() + "°F",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = "Max Temp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = weather.data.main.humidity.toString(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 9.dp),
                            text = "Humidity",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                }

                Spacer(modifier = Modifier.weight(1f))

            }
        }

        else -> {

            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                contentAlignment = Alignment.Center) {
                Text(
                    text = "Search with city name"
                )
            }

        }
    }

}