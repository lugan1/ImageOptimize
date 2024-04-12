package com.example.imageoptimize.domain.file

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtil {
    fun crop(origin: Bitmap, width: Int, height: Int) : Bitmap {
        val originWidth = origin.width
        val originHeight = origin.height
        if(originWidth < width || originHeight < height) {
            return origin
        }

        val x = if(originWidth > width) (originWidth - width) / 2 else 0
        val y = if(originHeight > height) (originHeight - height) / 2 else 0

        val cropWith = if(originWidth > width) width else originWidth
        val cropHeight = if(originHeight > height) height else originHeight


        val cropBitmap = Bitmap.createBitmap(origin, x, y, cropWith, cropHeight)
        origin.recycle()
        return cropBitmap
    }

    fun resize(origin: Bitmap): Bitmap {
        val quality = when {
            origin.width > 2048 && origin.height > 2048 -> 0.2
            origin.width > 1024 && origin.height > 1024 -> 0.5
            else -> 0.6
        }

        val scaledBitmap = Bitmap.createScaledBitmap(
            origin,
            (origin.width * quality).toInt(),
            (origin.height * quality).toInt(),
            true
        )
        origin.recycle()
        return scaledBitmap
    }

    fun resize(origin: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var width = origin.width
        var height = origin.height

        if(width <= maxWidth && height <= maxHeight) return origin

        val ratio: Float = width.toFloat() / height.toFloat()

        if (ratio > 1) {
            width = maxWidth
            height = (maxWidth / ratio).toInt()
        }
        else {
            height = maxHeight
            width = (maxHeight * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(
            origin,
            width,
            height,
            true
        )
        origin.recycle()
        return resizedBitmap
    }

    fun write(bitmap: Bitmap, targetFile: File): File? {
        return try {
            FileOutputStream(targetFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            targetFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}