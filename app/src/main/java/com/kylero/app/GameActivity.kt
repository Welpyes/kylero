package com.kylero.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kylero.app.audio.AudioPlayer
import com.kylero.app.engine.GameEngine
import com.kylero.app.engine.TimingEngine
import com.kylero.app.input.InputHandler
import com.kylero.app.view.GameView
import kotlinx.serialization.json.Json

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var audioPlayer: AudioPlayer

    private val json = Json { ignoreUnknownKeys = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chartJson = intent.getStringExtra(EXTRA_CHART_JSON)
        val audioUriStr = intent.getStringExtra(EXTRA_AUDIO_URI)

        if (chartJson == null) {
            Toast.makeText(this, "No chart data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val chart = try {
            json.decodeFromString<Chart>(chartJson)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid chart: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        gameView = GameView(this)
        setContentView(gameView)

        val timing = TimingEngine()
        val input = InputHandler()
        val engine = GameEngine(chart, timing, input)

        gameView.setup(engine) {
            audioPlayer.release()
            Toast.makeText(this, "Song finished! Score: ${engine.state.score}", Toast.LENGTH_LONG).show()
            finish()
        }

        audioPlayer = AudioPlayer(this)
        audioPlayer.prepare(Uri.parse(audioUriStr),
            onReady = {
                audioPlayer.start()
                engine.start()
            },
            onError = { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                engine.start()
            }
        )

        hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
        audioPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }

    companion object {
        const val EXTRA_CHART_JSON = "chart_json"
        const val EXTRA_AUDIO_URI = "audio_uri"

        fun start(context: Context, chartJson: String, audioUri: Uri) {
            context.startActivity(Intent(context, GameActivity::class.java).apply {
                putExtra(EXTRA_CHART_JSON, chartJson)
                putExtra(EXTRA_AUDIO_URI, audioUri.toString())
            })
        }
    }
}
