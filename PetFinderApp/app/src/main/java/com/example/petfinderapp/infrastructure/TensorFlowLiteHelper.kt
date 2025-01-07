package com.example.petfinderapp.infrastructure

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TensorFlowLiteHelper(context: Context, modelName: String) {
    private val interpreter: Interpreter

    init {
        val assetManager = context.assets
        val modelFileDescriptor = assetManager.openFd(modelName)
        val modelFileInputStream = modelFileDescriptor.createInputStream()

        val model = modelFileInputStream.channel.map(FileChannel.MapMode.READ_ONLY, modelFileDescriptor.startOffset, modelFileDescriptor.length)

        interpreter = Interpreter(model)
    }

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

    fun runModel(inputBuffer: ByteBuffer, outputSize: Int): FloatArray {
        val output = Array(1) { FloatArray(outputSize) }
        interpreter.run(inputBuffer, output)
        return output[0]
    }
}