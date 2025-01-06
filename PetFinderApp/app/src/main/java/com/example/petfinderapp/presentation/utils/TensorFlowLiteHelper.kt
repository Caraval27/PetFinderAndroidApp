package com.example.petfinderapp.utils

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TensorFlowLiteHelper(context: Context) {
    private val interpreter: Interpreter

    /*
    init {
        //Laddar in den valda modellen från assetes
        val assetManager = context.assets
        val model = assetManager.open("tflitemodel.tflite").use { inputStream ->
            val modelBytes = ByteArray(inputStream.available())
            inputStream.read(modelBytes)
            ByteBuffer.wrap(modelBytes).order(ByteOrder.nativeOrder())
        }
        interpreter = Interpreter(model)
    }*/

    init {
        // Open the model file from assets
        val assetManager = context.assets
        val modelFileDescriptor = assetManager.openFd("trained_model_dog_photos_40_epochs.tflite")
        val modelFileInputStream = modelFileDescriptor.createInputStream()

        // Use MappedByteBuffer to load the model
        val model = modelFileInputStream.channel.map(FileChannel.MapMode.READ_ONLY, modelFileDescriptor.startOffset, modelFileDescriptor.length)

        // Initialize interpreter with the model
        interpreter = Interpreter(model)
    }

    //formaterar en bild till rätt format
    /*
    fun preprocessImage(bitmap: Bitmap, imageSize: Int = 224): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        resizedBitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }*/

    fun preprocessImage(bitmap: Bitmap, imageSize: Int = 224): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        resizedBitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }

    /*
    fun runModel(inputBuffer: ByteBuffer, outputSize: Int): FloatArray {
        val output = Array(1) { FloatArray(outputSize) }
        interpreter.run(inputBuffer, output)
        return output[0]
    }*/
    fun runModel(inputBuffer: ByteBuffer, outputSize: Int): FloatArray {
        val output = Array(1) { FloatArray(outputSize) }
        interpreter.run(inputBuffer, output)
        return output[0]
    }
}
