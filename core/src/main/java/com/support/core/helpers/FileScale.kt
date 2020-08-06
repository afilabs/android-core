package com.support.core.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.support.core.Inject
import com.support.core.extension.tryCall
import java.io.FileNotFoundException


@Inject(true)
class FileScale(
        private val context: Context,
        private val fileCache: FileCache,
        private val fileBitmap: FileBitmap
) {
    companion object {
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1080
    }

    fun execute(uri: Uri, cacheInGallery: Boolean = false, removeOriginal: Boolean = false): String {
        val bitmap = fileBitmap.getBitmapFrom(uri) ?: error("Can not decode ${uri.path}")
        val bmp = scale(bitmap)
        val newPath = if (cacheInGallery) fileCache.saveToGallery(bmp) else fileCache.saveToCache(bmp)
        bmp.recycle()
        bitmap.recycle()
        if (removeOriginal) tryCall {
            context.contentResolver.delete(uri, null, null)
        }
        return newPath
    }

    fun execute(bitmap: Bitmap, recycle: Boolean = false, cacheInGallery: Boolean = false): String {
        val bmp = scale(bitmap)
        val newPath = if (cacheInGallery) fileCache.saveToGallery(bmp) else fileCache.saveToCache(bmp)
        bmp.recycle()
        if (recycle) bitmap.recycle()
        return newPath
    }

    fun scale(bitmap: Bitmap): Bitmap {
        val size = bitmap.getExpectSize()
        return Bitmap.createScaledBitmap(bitmap, size.width(), size.height(), false)
    }

    private fun Bitmap.getExpectSize(): Rect {
        var bmpWidth: Int = width
        var bmpHeight: Int = height

        var maxWidth = MAX_WIDTH
        var maxHeight = MAX_HEIGHT

        val ratio = width.toFloat() / height
        if (ratio < 1) {
            maxWidth = MAX_HEIGHT
            maxHeight = MAX_WIDTH
        }
        if (width > maxWidth) {
            bmpWidth = maxWidth
            bmpHeight = (bmpWidth / ratio).toInt()
        } else if (height > maxHeight) {
            bmpHeight = maxHeight
            bmpWidth = (bmpHeight * ratio).toInt()
        }
        return Rect(0, 0, bmpWidth, bmpHeight)
    }

}