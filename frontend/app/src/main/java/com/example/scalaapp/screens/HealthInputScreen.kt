package com.example.scalaapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scalaapp.ParametriRequest
import com.example.scalaapp.ParametriResponse
import com.example.scalaapp.RetrofitInstance
import com.example.scalaapp.Upozorenje
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HealthInputScreen() {
    var tezina by remember { mutableStateOf("") }
    var visina by remember { mutableStateOf("") }
    var tlakSys by remember { mutableStateOf("") }
    var tlakDia by remember { mutableStateOf("") }

    var zadnjiPodaci by remember { mutableStateOf<ParametriResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var listaUpozorenja by remember { mutableStateOf(listOf<Upozorenje>()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val fiksniId = "11111111-1111-1111-1111-111111111111"

    fun osvjeziSve() {
        scope.launch {
            try {
                isLoading = true

                val lista = RetrofitInstance.api.dohvatiParametre()
                zadnjiPodaci = lista.firstOrNull()

                listaUpozorenja = RetrofitInstance.api.dohvatiUpozorenja(fiksniId)

            } catch (e: Exception) {
                println("Greška: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        osvjeziSve()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Praćenje Stanja",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { osvjeziSve() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Osvježi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val zadnjeUpozorenje = listaUpozorenja.firstOrNull()

        if (zadnjeUpozorenje != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ALARM IZ BAZE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = zadnjeUpozorenje.poruka,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Vrijeme: ${zadnjeUpozorenje.datum.replace("T", " ").substringBefore(".")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (zadnjiPodaci != null) {
            LastMeasurementCard(data = zadnjiPodaci!!)
        } else if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Text("Nema prijašnjih mjerenja.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Unesi novo mjerenje",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tezina,
                        onValueChange = { tezina = it },
                        label = { Text("Težina (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = visina,
                        onValueChange = { visina = it },
                        label = { Text("Visina (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tlakSys,
                        onValueChange = { tlakSys = it },
                        label = { Text("Sys Tlak") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = tlakDia,
                        onValueChange = { tlakDia = it },
                        label = { Text("Dia Tlak") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val request = ParametriRequest(
                            korisnikId = fiksniId,
                            tezina = tezina.toDoubleOrNull() ?: 0.0,
                            visina = visina.toDoubleOrNull() ?: 0.0,
                            tlakSys = tlakSys.toIntOrNull() ?: 0,
                            tlakDia = tlakDia.toIntOrNull() ?: 0
                        )
                        RetrofitInstance.api.unesiParametre(request)
                        Toast.makeText(context, "Spremljeno!", Toast.LENGTH_SHORT).show()
                        tezina = ""; visina = ""; tlakSys = ""; tlakDia = ""
                        osvjeziSve()

                    } catch (e: Exception) {
                        Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.AccountBox, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SPREMI NOVE PODATKE")
        }
    }
}

@Composable
fun LastMeasurementCard(data: ParametriResponse) {
    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd.MM.yyyy u HH:mm", Locale.getDefault())
        val cleanDate = data.datum.split(".")[0]
        val dateObj = parser.parse(cleanDate)
        formatter.format(dateObj!!)
    } catch (e: Exception) {
        data.datum
    }

    val bmi = if (data.visina > 0) data.tezina / ((data.visina / 100) * (data.visina / 100)) else 0.0

    val (bmiColor, bmiText) = when {
        bmi == 0.0 -> Pair(Color.Gray, "-")
        bmi < 18.5 -> Pair(Color.Blue, "Pothranjenost")
        bmi < 25.0 -> Pair(Color(0xFF006400), "Idealna težina")
        bmi < 30.0 -> Pair(Color(0xFFFF8C00), "Prekomjerna")
        else -> Pair(Color.Red, "Pretilost")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ZADNJE MJERENJE ($formattedDate)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Težina", style = MaterialTheme.typography.bodySmall)
                    Text("${data.tezina} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                val tlakColor = if (data.tlakSys > 140 || data.tlakDia > 90) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tlak", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${data.tlakSys}/${data.tlakDia}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tlakColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("BMI", style = MaterialTheme.typography.bodySmall)
                    if (bmi > 0) {
                        Text(
                            String.format("%.1f", bmi),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = bmiColor
                        )
                        Text(
                            bmiText,
                            style = MaterialTheme.typography.labelSmall,
                            color = bmiColor,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}