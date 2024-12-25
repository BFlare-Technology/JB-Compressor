package com.bflaretechnology.jbcompressor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns

class ImageCompressionHandler(private val context: Context, val getCompressedBitmap:(compressedImage:Bitmap, compressedImageBytes:ByteArray )->Unit) {

    fun setImageUriString(uriString: String){
        val compressImage = CompressImage(context){ compressedImage ->
            val backgroundBitmapToBytes = BackgroundBitmapToBytes()
            backgroundBitmapToBytes.convertBitmapToBytes(compressedImage, getFileSizeFromUri(context, Uri.parse(uriString))){ compressedImageBytes ->
                getCompressedBitmap(compressedImage, compressedImageBytes)
            }
        }
        compressImage.startCompression(uriString)

    }

    private fun getFileSizeFromUri(context: Context, uri: Uri): Double {
        var size: Long = -1
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (!it.isNull(sizeIndex)) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size.toDouble()
    }

}