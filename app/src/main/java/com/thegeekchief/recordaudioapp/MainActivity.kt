package com.thegeekchief.recordaudioapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
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
        outputFile = createOutputFile(".mp3") // Specify the file extension

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
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
                val contentUri = saveRecordingToMediaStore()
                val filePath = contentUri?.let { getFilePathFromContentUri(it) }
                isRecording = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveRecordingToMediaStore(): Uri? {
        try {
            val contentResolver = contentResolver
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put(
                    MediaStore.Audio.Media.DISPLAY_NAME, "AUDIO_$timeStamp.mp3"
                ) // Specify the file extension
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3") // Use MP3 MIME type
            }

            val contentUri =
                contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

            val outputStream = contentUri?.let { contentResolver.openOutputStream(it) }

            val audioData = outputFile.readBytes()

            if (audioData.isNotEmpty()) {
                outputStream?.write(audioData)
                outputStream?.close()
            }

            val result = contentUri.toString()

            Toast.makeText(
                this@MainActivity, "File saved successfully: $result", Toast.LENGTH_SHORT
            ).show()

            return contentUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFilePathFromContentUri(contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun createOutputFile(fileExtension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile("AUDIO_${timeStamp}_", fileExtension)
    }

    private fun checkPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}
