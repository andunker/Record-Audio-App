package com.thegeekchief.recordaudioapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var startRecordingButton: Button
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private val requestCode = 1

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
            isRecording = !isRecording
        }
    }

    private fun startRecording() {
        val outputFile = createOutputFile()
        if (outputFile != null) {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setOutputFile(outputFile.absolutePath)

            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            release()
        }
    }

    private fun createOutputFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File.createTempFile("AUDIO_${timeStamp}_", ".3gp", storageDir)
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
