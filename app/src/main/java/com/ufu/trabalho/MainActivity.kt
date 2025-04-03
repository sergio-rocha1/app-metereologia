package com.ufu.trabalho

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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ufu.trabalho.database.LocationEntity
import com.ufu.trabalho.mapper.WeatherExtras
import com.ufu.trabalho.model.HourlyModel
import com.ufu.trabalho.ui.components.CurrentWeatherUiState
import com.ufu.trabalho.ui.components.DailyForecastUiState
import com.ufu.trabalho.ui.components.HourlyForecastUiState
import com.ufu.trabalho.viewmodel.WeatherViewModel
import com.uilover.trabalho.ui.components.ErrorMessage
import com.uilover.trabalho.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Constantes de layout utilizadas em toda a aplicação
 */
private val STANDARD_PADDING = 16.dp
private val STANDARD_PADDING_SMALL = 8.dp
private val STANDARD_PADDING_LARGE = 24.dp
private val ICON_SIZE_SMALL = 30.dp
private val ICON_SIZE_MEDIUM = 40.dp
private val ICON_SIZE_LARGE = 150.dp
private val CORNER_RADIUS = 25.dp

/**
 * Atividade principal do aplicativo de meteorologia.
 * 
 * Responsável por gerenciar a interface do usuário principal, solicitar permissões de localização
 * e coordenar a obtenção de dados meteorológicos com base na localização do usuário.
 */
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val weatherViewModel: WeatherViewModel by viewModels()

    /**
     * Inicializa a atividade, configura a interface do usuário e solicita permissões necessárias.
     *
     * @param savedInstanceState Estado salvo da atividade, caso tenha sido restaurada
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
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

    /**
     * Solicita permissão de localização ao usuário.
     * 
     * Verifica se a permissão já foi concedida e, caso contrário, inicia o fluxo de solicitação.
     */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocationAndRefreshWeather()
        }
    }

    /**
     * Launcher para processar o resultado da solicitação de permissão de localização.
     * 
     * Se a permissão for concedida, obtém a localização atual e atualiza os dados meteorológicos.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocationAndRefreshWeather()
            }
        }

    /**
     * Obtém a localização atual do usuário e atualiza os dados meteorológicos.
     * 
     * Esta função requer que a permissão de localização já tenha sido concedida.
     */
    private fun getLocationAndRefreshWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                weatherViewModel.refreshWeatherData(it.latitude, it.longitude)
            }
        }
    }
}

/**
 * Composable principal que exibe a tela de previsão do tempo.
 *
 * Por padrão, apresenta os dados meteorológicos da cidade da localização atual.
 * Possui um botão de busca (no canto superior direito) e um botão para exibir as cidades já pesquisadas (no canto superior esquerdo).
 * Ao clicar no botão da esquerda, é exibido um diálogo com as cidades salvas, cada uma mostrando sua última data de atualização.
 * Em cada opção, há um botão para excluir a cidade ou clicar nela para buscar a previsão atualizada.
 *
 * @param viewModel ViewModel que gerencia os dados meteorológicos e as localizações salvas
 * @param onRefresh Callback para atualizar os dados meteorológicos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    onRefresh: () -> Unit = {}
) {
    // Estados para controle da busca e do diálogo de localizações salvas
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showLocationsDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Coleta os estados do ViewModel
    val currentState by viewModel.currentWeatherState.collectAsState()
    val hourlyState by viewModel.hourlyForecastState.collectAsState()
    val dailyState by viewModel.dailyForecastState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState(initial = emptyList())
    // Novo estado para os dados extras
    val extras by viewModel.weatherExtrasState.collectAsState()

    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF59469D), Color(0xFF643D67))
                        )
                    )
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = STANDARD_PADDING_SMALL),
                    verticalArrangement = Arrangement.spacedBy(STANDARD_PADDING_SMALL)
                ) {
                    // Se estiver em modo busca, insere a barra de pesquisa como primeiro item
                    if (isSearching) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Pesquisar cidade...", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onSearch = {
                                        viewModel.searchAndRefresh(searchQuery)
                                        isSearching = false
                                        focusManager.clearFocus()
                                    }
                                ),
                                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = STANDARD_PADDING)
                            )
                        }
                    }
                    // Exibe os dados do clima atual
                    item {
                        when (currentState) {
                            is CurrentWeatherUiState.Loading ->
                                LoadingIndicator(modifier = Modifier.padding(top = 100.dp))
                            is CurrentWeatherUiState.Success -> {
                                val current = (currentState as CurrentWeatherUiState.Success).data
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = STANDARD_PADDING_LARGE)
                                        .padding(horizontal = STANDARD_PADDING_LARGE),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = current.locationName.uppercase(),
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = current.condition,
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                        textAlign = TextAlign.Center
                                    )
                                    Image(
                                        painter = painterResource(
                                            id = getDrawableResourceIdForTime(current.condition, current.dateTime)
                                        ),
                                        contentDescription = "Ícone do clima",
                                        modifier = Modifier
                                            .size(ICON_SIZE_LARGE)
                                            .padding(top = STANDARD_PADDING_SMALL),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = "${current.temperature}°",
                                        fontSize = 63.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = formatDateTime(current.dateTime),
                                        fontSize = 19.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = STANDARD_PADDING_SMALL),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "L:${current.lowTemp} H:${current.highTemp}",
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
                    // Exibe os dados extras (se disponíveis)
                    extras?.let {
                        item {
                            WeatherExtrasView(extras = it)
                        }
                    }
                    // Previsão horária
                    item {
                        when (hourlyState) {
                            is HourlyForecastUiState.Loading ->
                                LoadingIndicator(modifier = Modifier.height(150.dp).fillMaxWidth())
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
                    // Previsão diária
                    item {
                        when (dailyState) {
                            is DailyForecastUiState.Loading ->
                                LoadingIndicator(modifier = Modifier.height(300.dp).fillMaxWidth())
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
                                                iconRes = getDrawableResourceIdForTime(item.status, item.day)
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
                    // Botão de atualizar centralizado
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
                                modifier = Modifier.widthIn(max = 250.dp)
                            ) {
                                Text(text = "Atualizar", color = Color.White)
                            }
                        }
                    }
                }
                // Botão de busca no canto superior direito
                if (!isSearching) {
                    IconButton(
                        onClick = { isSearching = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .background(Color.White, shape = RoundedCornerShape(50))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Pesquisar",
                            tint = Color(0xFF643D67)
                        )
                    }
                }
                // Botão para exibir as cidades salvas no canto superior esquerdo
                if (!isSearching) {
                    IconButton(
                        onClick = { showLocationsDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(36.dp)
                            .background(Color.White, shape = RoundedCornerShape(50))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_list),
                            contentDescription = "Cidades Salvas",
                            tint = Color(0xFF643D67)
                        )
                    }
                }
                // Diálogo com as cidades salvas
                if (showLocationsDialog) {
                    SavedLocationsDialog(
                        locations = savedLocations,
                        onDismiss = { showLocationsDialog = false },
                        onLocationSelected = { location ->
                            // Atualiza o timestamp e busca dados atualizados para a localização selecionada
                            viewModel.updateLocationAccessed(location.id)
                            viewModel.refreshWeatherData(location.latitude.toDouble(), location.longitude.toDouble())
                            showLocationsDialog = false
                        },
                        onDelete = { location ->
                            viewModel.deleteLocation(location.id)
                        }
                    )
                }
            }
        }
    )
}

/**
 * Composable que exibe um diálogo com a lista de cidades salvas.
 *
 * Cada item mostra o nome da cidade e a última data de atualização.
 * Possui um botão para excluir a cidade ou clicar nela para buscar a previsão atualizada.
 *
 * @param locations Lista de localizações salvas
 * @param onDismiss Ação ao fechar o diálogo
 * @param onLocationSelected Ação ao selecionar uma localização (busca a previsão atualizada)
 * @param onDelete Ação para excluir a localização do banco de dados
 */
@Composable
fun SavedLocationsDialog(
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onLocationSelected: (LocationEntity) -> Unit,
    onDelete: (LocationEntity) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cidades Salvas") },
        text = {
            LazyColumn {
                items(locations) { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onLocationSelected(location) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = location.displayName,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Atualizado: ${formatTimestamp(location.lastAccessed)}",
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { onDelete(location) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Excluir"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Composable que exibe um item de previsão diária.
 *
 * @param day Dia da semana
 * @param tempMax Temperatura máxima do dia
 * @param tempMin Temperatura mínima do dia
 * @param iconRes ID do recurso de ícone para a condição meteorológica
 */
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
            text = "$tempMin° / $tempMax°",
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Composable que exibe um modelo de previsão horária.
 *
 * @param model Modelo de dados contendo informações meteorológicas horárias
 */
@Composable
fun FutureModelViewHolder(model: HourlyModel) {
    Column(
        modifier = Modifier.padding(STANDARD_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = model.hour, color = Color.White, fontSize = 16.sp)
        // Aqui usamos a função para previsão horária, que verifica se o horário é noturno
        val iconId = getHourlyDrawableResourceId(model.picPath, model.hour)
        Image(
            painter = painterResource(id = iconId),
            contentDescription = "Ícone para ${model.hour}",
            modifier = Modifier.size(ICON_SIZE_MEDIUM)
        )
        Text(text = "${model.temp}°", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun WeatherExtrasView(extras: WeatherExtras) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = STANDARD_PADDING, vertical = STANDARD_PADDING_SMALL),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeatherExtraItem(
            iconRes = R.drawable.rainy, // Ícone para probabilidade de chuva
            label = "Chuva",
            value = "${extras.precipitationProbability}%"
        )
        WeatherExtraItem(
            iconRes = R.drawable.wind, // Ícone para velocidade do vento
            label = "Vento",
            value = "${extras.windSpeed} km/h"
        )
        WeatherExtraItem(
            iconRes = R.drawable.humidity, // Ícone para umidade
            label = "Umidade",
            value = "${extras.humidity}%"
        )
        WeatherExtraItem(
            iconRes = R.drawable.ic_uv, // Ícone para UV
            label = "UV",
            value = extras.uvIndex.toString()
        )
    }
}

@Composable
fun WeatherExtraItem(iconRes: Int, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(40.dp)
        )
        Text(text = label, fontSize = 14.sp, color = Color.White)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

/**
 * Obtém o ID do recurso de ícone diurno com base na condição meteorológica.
 *
 * @param condition Condição meteorológica (ex: "clear", "cloudy", etc)
 * @return ID do recurso de ícone correspondente à condição
 */
fun getDayIcon(condition: String): Int {
    return when {
        condition.contains("ensolarado", ignoreCase = true) || condition.contains("céu limpo", ignoreCase = true) -> R.drawable.sunny
        condition.contains("nublado", ignoreCase = true) -> R.drawable.cloudy_sunny
        condition.contains("chuva", ignoreCase = true) -> R.drawable.storm
        else -> R.drawable.cloudy
    }
}

/**
 * Obtém o ID do recurso de ícone com base na condição meteorológica e período do dia.
 *
 * @param condition Condição meteorológica
 * @param isNight Indica se é período noturno
 * @return ID do recurso de ícone correspondente à condição e período
 */
fun getIconForCondition(condition: String, isNight: Boolean): Int {
    val dayIcon = getDayIcon(condition)
    return if (!isNight) {
        dayIcon
    } else {
        when (dayIcon) {
            R.drawable.sunny -> R.drawable.moon         // Para céu limpo à noite
            R.drawable.cloudy_sunny -> R.drawable.moon_cloudy  // Para nuvens à noite
            R.drawable.storm -> R.drawable.rain          // Para chuva à noite (pode ser ícone de chuva noturna)
            else -> R.drawable.moon
        }
    }
}

/**
 * Verifica se um determinado horário ISO representa um período noturno.
 *
 * @param dateTime Data e hora no formato ISO "yyyy-MM-dd'T'HH:mm"
 * @return true se for período noturno, false caso contrário
 */
fun isNight(dateTime: String): Boolean {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
        }
        val date = parser.parse(dateTime)
        val calendar = Calendar.getInstance().apply { time = date }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        hour >= 18 || hour < 6
    } catch (e: Exception) {
        false
    }
}

/**
 * Verifica se uma hora específica representa um período noturno.
 *
 * @param hour Hora no formato "HH:mm"
 * @return true se for período noturno, false caso contrário
 */
fun isNightHour(hour: String): Boolean {
    return try {
        val hourInt = hour.substring(0, 2).toInt()
        hourInt >= 18 || hourInt < 6
    } catch (e: Exception) {
        false
    }
}

/**
 * Obtém o ID do recurso de ícone com base na condição e data/hora.
 *
 * @param condition Condição meteorológica
 * @param dateTime Data e hora no formato ISO
 * @return ID do recurso de ícone correspondente
 */
fun getDrawableResourceIdForTime(condition: String, dateTime: String): Int {
    val night = isNight(dateTime)
    return getIconForCondition(condition, night)
}

/**
 * Obtém o ID do recurso de ícone para previsão horária.
 *
 * @param picPath Caminho/chave da condição meteorológica
 * @param hour Hora no formato "HH:mm"
 * @return ID do recurso de ícone correspondente
 */
fun getHourlyDrawableResourceId(picPath: String, hour: String): Int {
    val dayIcon = getDrawableResourceId(picPath)
    val night = isNightHour(hour)
    return if (!night) {
        dayIcon
    } else {
        // Mapeia o ícone diurno para sua versão noturna
        when (dayIcon) {
            R.drawable.sunny -> R.drawable.moon
            R.drawable.cloudy_sunny -> R.drawable.moon_cloudy
            R.drawable.storm -> R.drawable.rain
            else -> R.drawable.moon
        }
    }
}

/**
 * Mapeia a chave para um drawable diurno simples
 *
 * @param picPath Caminho/chave da condição meteorológica
 * @return ID do recurso de ícone correspondente
 */
fun getDrawableResourceId(picPath: String): Int {
    return when (picPath) {
        "sunny" -> R.drawable.sunny
        "cloudy_sunny" -> R.drawable.cloudy_sunny
        "storm" -> R.drawable.storm
        else -> R.drawable.cloudy
    }
}

/**
 * Formata uma string de data/hora ISO para um formato mais legível.
 *
 * @param isoString Data e hora no formato ISO "yyyy-MM-dd'T'HH:mm"
 * @return Data e hora formatada como "EEE MMM dd | HH:mm"
 */
fun formatDateTime(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("EEE MMM dd | HH:mm", Locale("pt", "BR"))
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}

/**
 * Helper para formatar o timestamp (em milissegundos) para uma string legível.
 */
fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
    return formatter.format(date)
}

/** Windsurf
 * Prompt: "Baseado em todo o projeto, como posso deixar todos os icones, textos etc alinhados?"
 * private val STANDARD_PADDING = 16.dp
 * private val STANDARD_PADDING_SMALL = 8.dp
 * private val STANDARD_PADDING_LARGE = 24.dp
 * private val ICON_SIZE_SMALL = 30.dp
 * private val ICON_SIZE_MEDIUM = 40.dp
 * private val ICON_SIZE_LARGE = 150.dp
 * private val CORNER_RADIUS = 25.dp
 */

/** Cursor
 * Prompt: "Crie as classes de dados e entidades de banco de dados para este projeto."
 * WeatherEntity, DailyForecastEntity, HourlyForecastEntity, LocationEntity
 * OpenMeteoResponse, CurrentWeather, DailyForecast, HourlyForecast
 * FutureModel, HourlyModel, LocationResult, ReverseGeocodeResponse
 */

/** Cursor
 * Prompt: "Faça a documentação usando padrão KDoc para este projeto."
 * Documentação KDoc
 */
