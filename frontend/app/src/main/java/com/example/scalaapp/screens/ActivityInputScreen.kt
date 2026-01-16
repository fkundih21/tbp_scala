package com.example.scalaapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.scalaapp.AktivnostRequest
import com.example.scalaapp.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun ActivityInputScreen() {
    var naziv by remember { mutableStateOf("") }
    var trajanje by remember { mutableStateOf("") }
    var kalorije by remember { mutableStateOf("") }

    var danasnjeAktivnosti by remember { mutableStateOf(listOf<AktivnostRequest>()) }

    val ukupnoKalorija = danasnjeAktivnosti.sumOf { it.kalorije }
    val ukupnoMinuta = danasnjeAktivnosti.sumOf { it.trajanje }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fiksniId = "11111111-1111-1111-1111-111111111111"

    fun osvjeziAktivnosti() {
        scope.launch {
            try {
                danasnjeAktivnosti = RetrofitInstance.api.dohvatiDanasnjeAktivnosti(fiksniId)
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        osvjeziAktivnosti()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            "Tjelesna Aktivnost",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "DANAŠNJI TRENING",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Potrošeno", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$ukupnoKalorija", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(" kcal", modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Aktivnost", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$ukupnoMinuta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(" min", modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Popis aktivnosti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(danasnjeAktivnosti) { trening ->
                ListItem(
                    headlineContent = { Text(trening.naziv, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("${trening.trajanje} min") },
                    trailingContent = {
                        Text(
                            "-${trening.kalorije} kcal",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Divider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Dodaj novu aktivnost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = naziv,
            onValueChange = { naziv = it },
            label = { Text("Vrsta vježbe (npr. Trčanje)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = trajanje,
                onValueChange = { trajanje = it },
                label = { Text("Minuta") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = kalorije,
                onValueChange = { kalorije = it },
                label = { Text("Kcal") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val req = AktivnostRequest(
                            korisnikId = fiksniId,
                            naziv = naziv,
                            trajanje = trajanje.toIntOrNull() ?: 0,
                            kalorije = kalorije.toIntOrNull() ?: 0
                        )
                        RetrofitInstance.api.unesiAktivnost(req)
                        Toast.makeText(context, "Aktivnost spremljena!", Toast.LENGTH_SHORT).show()

                        naziv = ""; trajanje = ""; kalorije = ""

                        osvjeziAktivnosti()

                    } catch (e: Exception) {
                        Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("UPIŠI TRENING")
        }
    }
}