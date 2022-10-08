package ru.ivos.weather_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import ru.ivos.weather_app.data.WeatherModel
import ru.ivos.weather_app.screens.MainCard
import ru.ivos.weather_app.screens.TabLayout

const val API_KEY = "a8c5801cd9e84ab296684107220610"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val daysList = remember {
                mutableStateOf(listOf<WeatherModel>())
            }
            val currentDay = remember {
                mutableStateOf(WeatherModel(
                    "", "", "0.0", "", "", "0.0", "0.0", ""
                ))
            }

            getData("Saint-Petersburg", this, daysList, currentDay)
            Image(
                painter = painterResource(id = R.drawable.clear_sky),
                contentDescription = "clear_sky_theme",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.9f),
                contentScale = ContentScale.FillBounds
            )
            Column {
                MainCard(currentDay)
                TabLayout(daysList)
            }

        }
    }
}

private fun getData(city: String,
                    context: Context,
                    daysList: MutableState<List<WeatherModel>>,
                    currentDay: MutableState<WeatherModel>) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=3" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        {
            Log.d("LogFromGetData", "Volley error: $it")
        }
    )
    queue.add(stringRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()

    val list = ArrayList<WeatherModel>()
    val mainObj = JSONObject(response)
    val city = mainObj.getJSONObject("location").getString("name")
    val days = mainObj.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObj.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObj.getJSONObject("current").getString("temp_c")
    )
    return list
}

private fun getWeatherByHours(item: WeatherModel): List<WeatherModel> {

    val hoursArray = JSONArray(item.hours)
    val list = ArrayList<WeatherModel>()

    for(i in 0 until hoursArray.length()){
        val wItem = WeatherModel(
            "",
            (hoursArray[i] as JSONObject).getString("time"),
            (hoursArray[i] as JSONObject).getString("temp_c"),
            (hoursArray[i] as JSONObject).getJSONObject("condition").getString("text"),
            (hoursArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
            "",
            "",
            ""
        )
        list.add(wItem)
    }
    return list
}

