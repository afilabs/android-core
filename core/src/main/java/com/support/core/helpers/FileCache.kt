package com.support.core.helpers

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.support.core.extension.tryCall
import java.io.File
import java.io.FileOutputStream

class FileCache(private val context: Context, private val folderName: String) {
    fun delete(path: String) {
        File(path).delete()
    }

    fun delete(uri: Uri) {
        tryCall { context.contentResolver.delete(uri, null, null) }
    }

    fun saveToCache(it: Bitmap, quality: Int = 80): String {
        val folder = getOrCreateCache(folderName)
        return doSave(it, quality, folder)
    }

    fun saveToGallery(it: Bitmap, quality: Int = 80): String {
        val folder = getOrCreateExternal(folderName)
        return doSave(it, quality, folder)
    }

    private fun doSave(it: Bitmap, quality: Int, folder: File): String {
        val currentTime = System.currentTimeMillis()
        val file = File(folder, "$currentTime.jpg")
        FileOutputStream(file).use { out ->
            it.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        return file.path
    }

    private fun getOrCreateExternal(folderName: String): File {
        val file = onCreateGalleryFolder(folderName)
        val exist = file.exists()
        file.mkdirs()
        if (!exist) refresh(file)
        return file
    }

    private fun refresh(folder: File) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(folder.path),
            arrayOf("image/*")
        ) { _, _ -> }
    }

    private fun onCreateGalleryFolder(folderName: String): File {
        val folder = File("${context.cacheDir}/$folderName")
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            return try {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
            } catch (e: Throwable) {
                context.getExternalFilesDir(folderName) ?: folder
            }
        }
        return folder
    }

    private fun getOrCreateCache(folderName: String): File {
        val folder = if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) File("${context.externalCacheDir}/$folderName")
        else File("${context.cacheDir}/$folderName")
        folder.mkdirs()
        return folder
    }

}