package com.ufu.trabalho

/**
 * Modificações de alinhamento e padronização de UI
 * Realizadas por: Cascade - Assistente de IA Codeium
 * Data: 30/03/2025
 * 
 * Melhorias implementadas:
 * - Padronização de dimensões com constantes
 * - Alinhamento consistente de textos e elementos
 * - Espaçamento uniforme entre componentes
 * - Aprimoramento da acessibilidade com descrições de conteúdo
 * - Distribuição equilibrada de elementos na interface
 */
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ufu.trabalho.model.HourlyModel
import com.ufu.trabalho.ui.components.CurrentWeatherUiState
import com.ufu.trabalho.ui.components.DailyForecastUiState
import com.ufu.trabalho.ui.components.HourlyForecastUiState
import com.ufu.trabalho.viewmodel.WeatherViewModel
import com.uilover.trabalho.ui.components.ErrorMessage
import com.uilover.trabalho.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Locale

// MODIFICAÇÃO: Adicionadas constantes para padronização de dimensões em toda a UI
// Isso garante consistência visual e facilita futuras alterações
private val STANDARD_PADDING = 16.dp
private val STANDARD_PADDING_SMALL = 8.dp
private val STANDARD_PADDING_LARGE = 24.dp
private val ICON_SIZE_SMALL = 30.dp
private val ICON_SIZE_MEDIUM = 40.dp
private val ICON_SIZE_LARGE = 150.dp
private val CORNER_RADIUS = 25.dp

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            // Passa o viewModel e a função onRefresh para o composable
            WeatherScreen(
                viewModel = weatherViewModel,
                onRefresh = { getLocationAndRefreshWeather() }
            )
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocationAndRefreshWeather()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocationAndRefreshWeather()
            } else {
                // Trate o caso de permissão negada se necessário
            }
        }

    private fun getLocationAndRefreshWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                weatherViewModel.refreshWeatherData(it.latitude, it.longitude)
            }
        }
    }
}

@Preview
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    onRefresh: () -> Unit = {}
) {
    // Coleta os três estados do ViewModel
    val currentState by viewModel.currentWeatherState.collectAsState()
    val hourlyState by viewModel.hourlyForecastState.collectAsState()
    val dailyState by viewModel.dailyForecastState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF59469D),
                        Color(0xFF643D67)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Removemos o padding horizontal e deixamos só um padding vertical pequeno
            contentPadding = PaddingValues(vertical = STANDARD_PADDING_SMALL),
            verticalArrangement = Arrangement.spacedBy(STANDARD_PADDING_SMALL)
        ) {
            // Seção do clima atual
            item {
                when (currentState) {
                    is CurrentWeatherUiState.Loading -> {
                        LoadingIndicator(modifier = Modifier.padding(top = 100.dp))
                    }
                    is CurrentWeatherUiState.Success -> {
                        val current = (currentState as CurrentWeatherUiState.Success).data

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp) // espaço superior
                                .padding(horizontal = STANDARD_PADDING_LARGE),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Nome da cidade
                            Text(
                                text = current.locationName.uppercase(),
                                fontSize = 20.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            // Condição do clima
                            Text(
                                text = current.condition,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                textAlign = TextAlign.Center
                            )

                            // Ícone grande
                            Image(
                                painter = painterResource(
                                    id = getDrawableResourceIdFromCondition(current.condition)
                                ),
                                contentDescription = "Ícone do clima",
                                modifier = Modifier
                                    .size(ICON_SIZE_LARGE)
                                    .padding(top = STANDARD_PADDING_SMALL),
                                contentScale = ContentScale.Fit
                            )

                            // Temperatura atual
                            Text(
                                text = "${current.temperature}°",
                                fontSize = 63.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                textAlign = TextAlign.Center
                            )

                            // Data/hora formatada
                            Text(
                                text = formatDateTime(current.dateTime),
                                fontSize = 19.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                textAlign = TextAlign.Center
                            )

                            // Altas e baixas
                            Text(
                                text = "H:${current.highTemp} L:${current.lowTemp}",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is CurrentWeatherUiState.Error -> {
                        val errorMessage = (currentState as CurrentWeatherUiState.Error).message
                        ErrorMessage(message = errorMessage)
                    }
                }
            }

            // Seção da previsão horária
            item {
                when (hourlyState) {
                    is HourlyForecastUiState.Loading -> {
                        LoadingIndicator(modifier = Modifier.height(150.dp).fillMaxWidth())
                    }
                    is HourlyForecastUiState.Success -> {
                        val hourlyItems = (hourlyState as HourlyForecastUiState.Success).data
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = STANDARD_PADDING_LARGE),
                            horizontalArrangement = Arrangement.spacedBy(STANDARD_PADDING_SMALL)
                        ) {
                            items(hourlyItems) { item ->
                                FutureModelViewHolder(item)
                            }
                        }
                    }
                    is HourlyForecastUiState.Error -> {
                        val errorMessage = (hourlyState as HourlyForecastUiState.Error).message
                        ErrorMessage(
                            message = errorMessage,
                            modifier = Modifier.height(150.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Seção da previsão diária (usando Column para evitar altura infinita)
            item {
                when (dailyState) {
                    is DailyForecastUiState.Loading -> {
                        LoadingIndicator(modifier = Modifier.height(300.dp).fillMaxWidth())
                    }
                    is DailyForecastUiState.Success -> {
                        val dailyItems = (dailyState as DailyForecastUiState.Success).data
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = STANDARD_PADDING_LARGE)
                        ) {
                            Text(
                                text = "Previsão para os próximos dias",
                                fontSize = 20.sp,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(STANDARD_PADDING_SMALL)
                            ) {
                                dailyItems.forEach { item ->
                                    DailyForecastItem(
                                        day = item.day,
                                        tempMax = item.highTemp.toDouble(),
                                        tempMin = item.lowTemp.toDouble(),
                                        iconRes = when (item.picPath) {
                                            "sunny" -> R.drawable.sunny
                                            "cloudy_sunny" -> R.drawable.cloudy_sunny
                                            "storm" -> R.drawable.storm
                                            else -> R.drawable.cloudy
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is DailyForecastUiState.Error -> {
                        val errorMessage = (dailyState as DailyForecastUiState.Error).message
                        ErrorMessage(
                            message = errorMessage,
                            modifier = Modifier.height(300.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Botão de Atualizar centralizado e com largura máxima limitada
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple)
                        ),
                        shape = RoundedCornerShape(CORNER_RADIUS / 2),
                        modifier = Modifier.widthIn(max = 250.dp) // Limita a largura máxima
                    ) {
                        Text(text = "Atualizar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(day: String, tempMax: Double, tempMin: Double, iconRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.purple).copy(alpha = 0.3f),
                shape = RoundedCornerShape(CORNER_RADIUS)
            )
            .padding(vertical = STANDARD_PADDING_SMALL, horizontal = STANDARD_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Ícone do clima",
            modifier = Modifier.size(ICON_SIZE_SMALL)
        )
        Text(
            text = "$tempMax° / $tempMin°",
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun FutureModelViewHolder(model: HourlyModel) {
    // Layout para exibir previsão horária
    Column(
        modifier = Modifier.padding(STANDARD_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = model.hour, color = Color.White, fontSize = 16.sp)
        Image(
            painter = painterResource(id = getDrawableResourceId(model.picPath)),
            contentDescription = "Ícone para ${model.hour}",
            modifier = Modifier.size(ICON_SIZE_MEDIUM)
        )
        Text(text = "${model.temp}°", color = Color.White, fontSize = 16.sp)
    }
}

// Função auxiliar para mapear o nome do ícone para um recurso drawable
fun getDrawableResourceId(picPath: String): Int {
    return when (picPath) {
        "sunny" -> R.drawable.sunny
        "cloudy_sunny" -> R.drawable.cloudy_sunny
        "storm" -> R.drawable.storm
        else -> R.drawable.cloudy
    }
}

/**
 * Função auxiliar que retorna um ícone de acordo com a condição textual.
 * Ajuste conforme a lógica que preferir.
 */
fun getDrawableResourceIdFromCondition(condition: String): Int {
    return when {
        condition.contains("ensolarado", ignoreCase = true) -> R.drawable.sunny
        condition.contains("nublado", ignoreCase = true) -> R.drawable.cloudy_sunny
        condition.contains("chuva", ignoreCase = true) -> R.drawable.storm
        else -> R.drawable.cloudy
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        // Parser para o formato ISO recebido
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = parser.parse(isoString)
        // Formatter para o formato desejado. "EEE" fornece o dia da semana abreviado
        val formatter = SimpleDateFormat("EEE MMM dd | HH:mm", Locale("pt", "BR"))
        formatter.format(date)
    } catch (e: Exception) {
        isoString // fallback caso ocorra erro
    }
}