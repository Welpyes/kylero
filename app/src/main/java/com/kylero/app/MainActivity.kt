package com.kylero.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickFile: Button
    private lateinit var btnPlay: Button
    private lateinit var tvStatus: TextView

    private var currentChart: Chart? = null
    private var currentChartJson: String? = null

    private val json = Json { ignoreUnknownKeys = true }

    private val pickFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) loadChart(uri)
    }

    private val pickAudio = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && currentChartJson != null) {
            launchGame(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPickFile = findViewById(R.id.btnPickFile)
        btnPlay = findViewById(R.id.btnPlay)
        tvStatus = findViewById(R.id.tvStatus)

        btnPickFile.setOnClickListener {
            pickFile.launch(arrayOf("application/json", "text/plain"))
        }

        btnPlay.setOnClickListener {
            val chart = currentChart
            if (chart == null) {
                Toast.makeText(this, "No chart loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pickAudio.launch(arrayOf("audio/*"))
        }
    }

    private fun loadChart(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: return

            val chart = json.decodeFromString<Chart>(text)
            currentChart = chart
            currentChartJson = text

            tvStatus.text = buildString {
                appendLine("Loaded: ${chart.meta.title}")
                appendLine("BPM: ${chart.meta.bpm}  Keys: ${chart.meta.keys}")
                appendLine("Notes: ${chart.notes.size}")
                appendLine("Offset: ${chart.meta.offset_ms}ms")
            }

            btnPlay.isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to parse chart: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchGame(audioUri: Uri) {
        val chartJson = currentChartJson ?: return
        GameActivity.start(this, chartJson, audioUri)
    }
}
