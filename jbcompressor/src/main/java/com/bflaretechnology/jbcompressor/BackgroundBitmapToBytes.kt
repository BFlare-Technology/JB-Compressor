package com.bflaretechnology.jbcompressor

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class BackgroundBitmapToBytes{

    fun convertBitmapToBytes(mBitmap: Bitmap, imageSize: Double, compressAmount: Int, getBytes: (bytes: ByteArray) -> Unit){
        GlobalScope.launch(Dispatchers.IO) {
            val bytes= convertBitmapToByteArray(mBitmap, imageSize, compressAmount)
            withContext(Dispatchers.Main) {
               getBytes(bytes)
            }
        }
    }

    private fun convertBitmapToByteArray(mBitmap: Bitmap, imageSize: Double, compressAmount: Int): ByteArray  {
        val resizedMainImage = Bitmap.createScaledBitmap(mBitmap, mBitmap.width, mBitmap.height, true)
        val stream = ByteArrayOutputStream()
        resizedMainImage.compress(Bitmap.CompressFormat.JPEG, compressAmount, stream)
        return stream.toByteArray()
    }

}