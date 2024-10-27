package com.example.culturalphraseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.culturalphraseapp.ui.theme.CulturalPhraseAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CulturalPhraseAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen { word ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val phrase = fetchPhrase(word)
                            withContext(Dispatchers.Main) {
                                updateResult(phrase)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun getApiKey(): String {
        val inputStream = resources.openRawResource(R.raw.api_key)
        return inputStream.bufferedReader().use { it.readText().trim() }
    }

    // Función que realiza la solicitud a la API de OpenAI
    private suspend fun fetchPhrase(word: String): String {
        val apiKey = getApiKey()  // Obtener la clave API desde el archivo
        val url = "https://api.openai.com/v1/engines/davinci/completions"
        val json = """
        {
            "prompt": "Can you please provide me with 50 cultural and objective phrases about the word '$word'",
            "max_tokens": 50
        }
    """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(), json
        )

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val jsonResponse = JSONObject(it)
                    val phrase = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0).getString("text")
                    return phrase
                } ?: "Error: Respuesta vacía del servidor"
            } else {
                "Error: ${response.message}"
            }
        } catch (e: IOException) {
            "Error: ${e.message}"
        }
    }

    @Composable
    fun MainScreen(onSearch: (String) -> Unit) {
        var word by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("Resultado aparecerá aquí") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Introduce una palabra") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (word.isNotEmpty()) {
                        onSearch(word)
                    }
                }
            ) {
                Text("Buscar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = result,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Función para actualizar el resultado en la UI
    private fun updateResult(phrase: String) {
        // En este ejemplo, puedes modificar el resultado directamente
        // Luego lo usarás para actualizar `result` en la UI.
    }

    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        CulturalPhraseAppTheme {
            MainScreen { }
        }
    }
}
