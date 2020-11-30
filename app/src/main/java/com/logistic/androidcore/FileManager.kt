package com.logistic.androidcore

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.support.core.extension.tryCall
import com.support.core.helpers.BitmapLoader
import com.support.core.helpers.FileSaver
import com.support.core.helpers.ImageScale
import java.io.File

class FileManager(private val context: Context) {

    companion object {
        const val TEMP_SIGNATURE = "temp_signature"
        const val APP_FOLDER_PHOTOS = "JTMS Photos"
        private const val TEMP_PHOTOS = "temp_photos"
    }

    private val tempCapturedFolder: File
        get() = (context.getExternalFilesDir(TEMP_PHOTOS)
                ?: File(context.filesDir, TEMP_PHOTOS)).apply { mkdirs() }

    private val imageScale = ImageScale(BitmapLoader(context))
    private val fileSaver = FileSaver(context)

    fun createTempCapturedFile(): Uri {
        return FileProvider.getUriForFile(context,
                context.getString(R.string.file_provider_auth),
                File(tempCapturedFolder, "${System.currentTimeMillis()}.jpg"))
    }

    fun delete(uri: Uri) {
        tryCall { context.contentResolver.delete(uri, null, null) }
    }

    fun delete(uri: String) {
        delete(File(uri))
    }

    fun delete(file: File) {
        tryCall { file.delete() }
    }

    fun savePhotoToGallery(uri: Uri): String {
        val bitmap = imageScale.scale(uri)
        val path = fileSaver.saveToGallery(bitmap, APP_FOLDER_PHOTOS)
        bitmap.recycle()
        return path
    }

    fun cacheSignature(bitmap: Bitmap): String {
        val newBitmap = imageScale.scale(bitmap)
        val path = fileSaver.saveToCache(newBitmap, TEMP_SIGNATURE)
        newBitmap.recycle()
        return path
    }

    fun recycleTempImages() {
        tempCapturedFolder.listFiles()?.forEach {
            delete(it)
        }
    }
}