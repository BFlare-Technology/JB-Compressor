package com.bflaretechnology.jbcompressor

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class BackgroundBitmapToBytes{

    fun convertBitmapToBytes(mBitmap: Bitmap, imageSize: Double, getBytes: (bytes: ByteArray) -> Unit){
        GlobalScope.launch(Dispatchers.IO) {
            val bytes= convertBitmapToByteArray(mBitmap, imageSize)
            withContext(Dispatchers.Main) {
               getBytes(bytes)
            }
        }
    }

    private fun convertBitmapToByteArray(mBitmap: Bitmap, imageSize: Double): ByteArray  {
        val mainImageQuality = when{
            imageSize < 50 -> 85
            imageSize in 50.0..100.0 -> 80
            imageSize in 100.0..200.0 -> 75
            imageSize in 200.0..300.0 -> 75
            imageSize in 300.0..1024.0 -> 75
            else -> 75
        }
        val resizedMainImage = Bitmap.createScaledBitmap(mBitmap, mBitmap.width, mBitmap.height, true)
        val stream = ByteArrayOutputStream()
        resizedMainImage.compress(Bitmap.CompressFormat.JPEG, mainImageQuality, stream)
        return stream.toByteArray()
    }

}