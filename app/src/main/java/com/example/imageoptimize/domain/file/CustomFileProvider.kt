package com.example.imageoptimize.domain.file

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.imageoptimize.R
import java.io.File

class CustomFileProvider: FileProvider(
    R.xml.image_path
) {
    companion object {
        fun getImageUri(context: Context): ImageUri {
            val directory = File(context.cacheDir, "photo")
            directory.mkdirs()
            val file = File.createTempFile(
                "image_",
                ".jpg",
                directory
            )

            val authority = context.packageName + ".fileprovider"

            val uri = getUriForFile(
                context,
                authority,
                file,
            )

            return ImageUri(uri, file)
        }

        fun clearCache(context: Context) {
            val directory = context.cacheDir
            if(directory.isDirectory) {
                directory.deleteRecursively()
            }
        }
    }
}

data class ImageUri(
    val uri: Uri,
    val file: File
)