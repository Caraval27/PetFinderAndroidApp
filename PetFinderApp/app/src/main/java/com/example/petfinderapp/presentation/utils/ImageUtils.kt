package com.example.petfinderapp.presentation.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher

object ImageUtils {
    fun openGallery(
        getPictureLauncher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        getPictureLauncher.launch(intent)
    }

    fun handleGalleryResult(
        resultCode: Int,
        data: Intent?,
        existingImages: List<String>
    ): List<String> {
        return if (resultCode == Activity.RESULT_OK) {
            val uris = data?.clipData
            val singleUri = data?.data

            when {
                uris != null -> {
                    existingImages + (0 until uris.itemCount).map { i -> uris.getItemAt(i).uri.toString() }
                }
                singleUri != null -> {
                    existingImages + singleUri.toString()
                }
                else -> existingImages
            }
        } else {
            existingImages
        }
    }
}