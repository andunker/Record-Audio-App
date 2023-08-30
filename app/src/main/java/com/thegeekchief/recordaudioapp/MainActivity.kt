package com.thegeekchief.recordaudioapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var startRecordingButton: Button
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording: Boolean = false
    private lateinit var outputFile: File

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private val requestCode = 1

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startRecordingButton = findViewById(R.id.startRecordingButton)
        startRecordingButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                startRecordingButton.text = "Start Recording"
            } else {
                if (checkPermissions()) {
                    startRecording()
                    startRecordingButton.text = "Stop Recording"
                } else {
                    requestPermissions()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        outputFile = createOutputFile()

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setOutputFile(outputFile.absolutePath)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            if (isRecording) {
                mediaRecorder.stop()
                mediaRecorder.reset()
                mediaRecorder.release()
                saveRecordingToMediaStore()
                isRecording = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveRecordingToMediaStore() {
        try {
            val contentResolver = contentResolver
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "AUDIO_$timeStamp.mp4")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            }

            val contentUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

            val outputStream = contentUri?.let { contentResolver.openOutputStream(it) }

            val audioData = outputFile.readBytes()

            if (audioData.isNotEmpty()) {
                outputStream?.write(audioData)
                outputStream?.close()
            }

            val result = contentUri.toString()

            Toast.makeText(
                this@MainActivity,
                "File saved successfully: $result",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createOutputFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile("AUDIO_${timeStamp}_", ".mp4")
    }

    private fun checkPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}
