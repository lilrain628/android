package com.example.mycalculator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File


class MusicActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private val musicList = mutableListOf<String>()
    private var currentTrackIndex = 0
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_activity)

        val play = findViewById<Button>(R.id.play_Button)
        val stop = findViewById<Button>(R.id.stop_button)
        val next = findViewById<Button>(R.id.next_button)
        val prev = findViewById<Button>(R.id.prev_button)
        val cycle = findViewById<Button>(R.id.cycle_button)
        val exitButton = findViewById<Button>(R.id.exit_button)
        seekBar = findViewById(R.id.seekBar)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                    playMusic()
                } else {
                    Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
                }
            }

        requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)

        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        play.setOnClickListener {
            if (!::mediaPlayer.isInitialized || !mediaPlayer.isPlaying) {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    play.text = "Pause"
                }
            } else {
                mediaPlayer.pause()
                play.text = "Play"
            }
        }

        stop.setOnClickListener {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                play.text = "Play"
            }
        }

        next.setOnClickListener {
            currentTrackIndex = (currentTrackIndex + 1) % musicList.size
            loadTrack(currentTrackIndex)
        }

        prev.setOnClickListener {
            currentTrackIndex = (currentTrackIndex - 1 + musicList.size) % musicList.size
            loadTrack(currentTrackIndex)
        }

        cycle.setOnClickListener {
            mediaPlayer.isLooping = !mediaPlayer.isLooping
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun playMusic() {
        val rootPath = Environment.getExternalStorageDirectory().path
        Log.d("MusicActivity", "Loading music from: $rootPath")
        val rootDir = File(rootPath)

        rootDir.walkTopDown().forEach { file ->
            if (file.extension in listOf("mp3")) {
                musicList.add(file.absolutePath)
                Log.d("vovan", "added track: ${file.name}")
                Log.d("MusicList", musicList.joinToString("\n"))
            }
        }
        if (musicList.isNotEmpty()) {
            loadTrack(currentTrackIndex)
        } else {
            Toast.makeText(this, "муз файлы не найдены", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadTrack(index: Int) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        val trackPath = musicList[index]
        mediaPlayer = MediaPlayer().apply {
            setDataSource(trackPath)
            prepare()
        }

        val musicName = File(trackPath).name
        val musicTitle = findViewById<TextView>(R.id.musictitle)
        musicTitle.text = musicName

        seekBar.max = mediaPlayer.duration
        updateSeekBar()

        mediaPlayer.setOnCompletionListener {
            currentTrackIndex = (currentTrackIndex + 1) % musicList.size
            loadTrack(currentTrackIndex)
        }
    }

    private fun updateSeekBar() {
        if (::mediaPlayer.isInitialized) {
            seekBar.progress = mediaPlayer.currentPosition
            seekBar.postDelayed({ updateSeekBar() }, 1000)
        }
    }

}