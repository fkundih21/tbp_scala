package com.example.scalaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scalaapp.ui.theme.ScalaAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HealthInputScreen()
            }
        }
    }
}

@Composable
fun HealthInputScreen() {
    // State varijable za unos
    var tezina by remember { mutableStateOf("") }
    var visina by remember { mutableStateOf("") }
    var tlakSys by remember { mutableStateOf("") }
    var tlakDia by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Za asinkroni poziv

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Unos Zdravstvenih Parametara", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = tezina,
            onValueChange = { tezina = it },
            label = { Text("Težina (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = visina,
            onValueChange = { visina = it },
            label = { Text("Visina (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedTextField(
                value = tlakSys,
                onValueChange = { tlakSys = it },
                label = { Text("Sys Tlak") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = tlakDia,
                onValueChange = { tlakDia = it },
                label = { Text("Dia Tlak") },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Poziv API-ja
                scope.launch {
                    try {
                        val request = ParametriRequest(
                            korisnikId = "fe5a645e-571f-412a-8f88-6fd8f1807d51", //test
                            tezina = tezina.toDoubleOrNull() ?: 0.0,
                            visina = visina.toDoubleOrNull() ?: 0.0,
                            tlakSys = tlakSys.toIntOrNull() ?: 0,
                            tlakDia = tlakDia.toIntOrNull() ?: 0
                        )

                        // Mrežni poziv
                        val response = RetrofitInstance.api.unesiParametre(request)
                        Toast.makeText(context, "Uspjeh: $response", Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SPREMI U BAZU")
        }
    }
}