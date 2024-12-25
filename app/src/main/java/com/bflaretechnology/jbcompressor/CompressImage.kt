package com.bflaretechnology.jbcompressor

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CompressImage(private val context: Context, val onImageCompressed: (Bitmap)->Unit ) {

    fun startCompression(imageUri: String){
        val realPath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getPathFromDocuments(context, Uri.parse(imageUri))
        } else {
            getImageFromGallery(imageUri).toString()
        }
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(realPath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        //      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth + 0.0f) / (actualHeight + 0.0f)
        val maxRatio = maxWidth / maxHeight

        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)


        //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false


        //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(realPath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp, middleX - ((bmp.width + 0.0f) / 2), middleY - (bmp.height + 0.0f) / 2, Paint(
                Paint.FILTER_BITMAP_FLAG
            )
        )


        //      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(realPath)

            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap!!,
                0,
                0,
                scaledBitmap!!.width,
                scaledBitmap!!.height,
                matrix,
                true
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val out = ByteArrayOutputStream()
        scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)

        val compressedImage  = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
        onImageCompressed(compressedImage)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }


    private fun getPathFromDocuments(context: Context, uri: Uri): String {
        try {
            val id = DocumentsContract.getDocumentId(uri)
            var inputStream: InputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            val file = File(context.cacheDir.absolutePath + "/" + id)
            inputStream?.let {
                writeFile(inputStream, file)
            }
            return file.absolutePath
        } catch (e: Exception) {
            return getImageFromGallery( uri.toString()).toString()
        }
    }

    private fun writeFile(inputStream: InputStream, file: File) {
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while ((inputStream.read(buf).also { len = it }) > 0) {
                out.write(buf, 0, len)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getImageFromGallery(contentURI: String): String? {
        try {
            val contentUri = Uri.parse(contentURI)
            val cursor: Cursor? = context.getContentResolver().query(contentUri, null, null, null, null)
            if (cursor == null) {
                return contentUri.path
            } else {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val s = cursor.getString(index)
                cursor.close()
                return s
            }
        } catch (e: java.lang.Exception) {
            return getPathFromDocuments(context, Uri.parse(contentURI))
        }
    }

}