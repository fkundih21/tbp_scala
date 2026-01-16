package com.example.scalaapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scalaapp.Biljeska
import com.example.scalaapp.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotesScreen() {
    var naslov by remember { mutableStateOf("") }
    var sadrzaj by remember { mutableStateOf("") }
    var listaBiljeski by remember { mutableStateOf(listOf<Biljeska>()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fiksniId = "11111111-1111-1111-1111-111111111111"

    fun osvjeziBiljeske() {
        scope.launch {
            try {
                listaBiljeski = RetrofitInstance.api.dohvatiBiljeske(fiksniId)
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        osvjeziBiljeske()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Moje Bilješke", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listaBiljeski) { biljeska ->
                val datumPrikaz = try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    formatter.format(parser.parse(biljeska.datum!!.split(".")[0])!!)
                } catch (e: Exception) { "" }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(biljeska.naslov, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(datumPrikaz, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(biljeska.sadrzaj, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Nova bilješka", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = naslov,
            onValueChange = { naslov = it },
            label = { Text("Naslov") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = sadrzaj,
            onValueChange = { sadrzaj = it },
            label = { Text("Sadržaj") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val nova = Biljeska(
                            korisnikId = fiksniId,
                            naslov = naslov,
                            sadrzaj = sadrzaj
                        )
                        RetrofitInstance.api.unesiBiljesku(nova)
                        Toast.makeText(context, "Bilješka dodana!", Toast.LENGTH_SHORT).show()
                        naslov = ""; sadrzaj = ""
                        osvjeziBiljeske()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Greška!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SPREMI BILJEŠKU")
        }
    }
}