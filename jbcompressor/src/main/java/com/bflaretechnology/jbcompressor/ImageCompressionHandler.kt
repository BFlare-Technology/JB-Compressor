package com.bflaretechnology.jbcompressor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns

class ImageCompressionHandler(private val context: Context, val getCompressedBitmap:(compressedImage:Bitmap, compressedImageBytes:ByteArray )->Unit) {

    fun setImageUriString(uriString: String, retainQuality: Int? = null){

        val imageSize = getFileSizeFromUri(context, Uri.parse(uriString))
        val compressAmount = retainQuality ?: when {
            imageSize < 100 -> 85   // Small image, preserve quality
            imageSize < 500 -> 75   // Medium image, moderate compression
            imageSize < 1024 -> 70  // Larger image, compress more
            imageSize < 2048 -> 65  // Very large
            else -> 60              // Huge images, prioritize size reduction
        }

        val compressImage = CompressImage(context){ compressedImage ->
            val backgroundBitmapToBytes = BackgroundBitmapToBytes()
            backgroundBitmapToBytes.convertBitmapToBytes(compressedImage, imageSize, compressAmount){ compressedImageBytes ->
                getCompressedBitmap(compressedImage, compressedImageBytes)
            }
        }
        compressImage.startCompression(uriString)

    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Double {
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