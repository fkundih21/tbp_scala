package com.example.scalaapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scalaapp.Namirnica
import com.example.scalaapp.PrehranaRequest
import com.example.scalaapp.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun FoodInputScreen() {
    var nazivHrane by remember { mutableStateOf("") }
    var kalorije by remember { mutableStateOf("") }
    var listaTrenutniObrok by remember { mutableStateOf(listOf<Namirnica>()) }

    var danasUkupnoKcal by remember { mutableIntStateOf(0) }
    var danasListaSvihNamirnica by remember { mutableStateOf(listOf<Namirnica>()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val kalorijeTrenutniObrok = listaTrenutniObrok.sumOf { it.kalorije }
    val fiksniId = "11111111-1111-1111-1111-111111111111"

    fun osvjeziDanasnjeStanje() {
        scope.launch {
            try {
                val resp = RetrofitInstance.api.dohvatiDanasnjuPrehranu(fiksniId)
                danasUkupnoKcal = resp.ukupnoKcal
                danasListaSvihNamirnica = resp.namirnice
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        osvjeziDanasnjeStanje()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Prehrana", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DANAŠNJI STATUS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$danasUkupnoKcal", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text(" kcal", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                }

                if (danasListaSvihNamirnica.isNotEmpty()) {
                    Text(
                        "Pojedeno: " + danasListaSvihNamirnica.joinToString(", ") { it.naziv },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                } else {
                    Text("Još ništa niste unijeli danas.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text("Dodaj novi obrok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nazivHrane,
                onValueChange = { nazivHrane = it },
                label = { Text("Namirnica") },
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
            OutlinedTextField(
                value = kalorije,
                onValueChange = { kalorije = it },
                label = { Text("kcal") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (nazivHrane.isNotEmpty() && kalorije.isNotEmpty()) {
                        val nova = Namirnica(nazivHrane, kalorije.toInt())
                        listaTrenutniObrok = listaTrenutniObrok + nova
                        nazivHrane = ""; kalorije = ""
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(listaTrenutniObrok) { hrana ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(hrana.naziv)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${hrana.kalorije} kcal", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { listaTrenutniObrok = listaTrenutniObrok - hrana }) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        Button(
            enabled = listaTrenutniObrok.isNotEmpty(),
            onClick = {
                scope.launch {
                    try {
                        val request = PrehranaRequest(
                            korisnikId = fiksniId,
                            ukupnoKcal = kalorijeTrenutniObrok,
                            namirnice = listaTrenutniObrok
                        )
                        RetrofitInstance.api.unesiPrehranu(request)
                        Toast.makeText(context, "Obrok spremljen!", Toast.LENGTH_SHORT).show()
                        listaTrenutniObrok = emptyList()
                        osvjeziDanasnjeStanje()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("SPREMI OBROK (${kalorijeTrenutniObrok} kcal)")
        }
    }
}