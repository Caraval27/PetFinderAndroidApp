package com.example.petfinderapp.presentation.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {
    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        )
    }

    fun openCamera(
        context: Context,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        onImageUriCreated: (Uri) -> Unit
    ) {
        try {
            val file = createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "com.example.petfinderapp.provider",
                file
            )
            onImageUriCreated(uri)
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}