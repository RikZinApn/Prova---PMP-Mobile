package com.example.temperatureapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.temperatureapp.ui.theme.TemperatureAppTheme
import com.google.firebase.database.*

data class Leitura(
    val temperatura: String,
    val umidade: String,
    val data: String,
    val hora: String
)

class MainActivity : ComponentActivity() {

    // Referência para o Firebase
    private val database = FirebaseDatabase.getInstance()
    private val leiturasRef = database.getReference("leituras")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemperatureAppTheme {
                // Definindo a UI
                TemperatureScreen()
            }
        }
    }

    @Composable
    fun TemperatureScreen() {
        var leituras by remember { mutableStateOf<List<Leitura>>(emptyList()) }

        // Função para pegar os dados do Firebase Realtime Database
        LaunchedEffect(Unit) {
            fetchDataFromFirebase { dados ->
                leituras = dados
            }
        }

        // Exibir os dados na tela
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Leituras de Temperatura e Umidade",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // LazyColumn para exibir as leituras
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(leituras.size) { index ->
                    val leitura = leituras[index]
                    ReadingItem(leitura)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // Atualiza os dados quando o botão for pressionado
                fetchDataFromFirebase { dados ->
                    leituras = dados
                }
            }) {
                Text(text = "Refresh")
            }
        }
    }

    // Função para buscar todas as leituras
    private fun fetchDataFromFirebase(onDataFetched: (List<Leitura>) -> Unit) {
        leiturasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leiturasList = mutableListOf<Leitura>()

                // Corrigido para iterar sobre todos os filhos e adicionar cada leitura à lista
                snapshot.children.forEach { childSnapshot ->
                    val temperatura = childSnapshot.child("temperatura").getValue(Long::class.java)?.toString() ?: "N/A"
                    val umidade = childSnapshot.child("umidade").getValue(Long::class.java)?.toString() ?: "N/A"
                    val data = childSnapshot.child("data").getValue(String::class.java) ?: "N/A"
                    val hora = childSnapshot.child("hora").getValue(String::class.java) ?: "N/A"
                    leiturasList.add(Leitura(temperatura, umidade, data, hora))
                }

                onDataFetched(leiturasList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    // Composable para exibir cada item da leitura
    @Composable
    fun ReadingItem(leitura: Leitura) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Data: ${leitura.data}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Hora: ${leitura.hora}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Temperatura: ${leitura.temperatura}°C", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Umidade: ${leitura.umidade}%", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
