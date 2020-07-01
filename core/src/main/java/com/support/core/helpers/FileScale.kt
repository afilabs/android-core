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
        private val fileCache: FileCache
) {
    companion object {
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1080
    }

    fun execute(uri: Uri, cacheInGallery: Boolean = false, removeOriginal: Boolean = false): String {
        val bitmap = getBitmapFrom(uri) ?: error("Can not decode ${uri.path}")
        val bmp = scale(bitmap)
        val newPath = if (cacheInGallery) fileCache.saveToGallery(bmp) else fileCache.saveToCache(bmp)
        bmp.recycle()
        bitmap.recycle()
        if (removeOriginal) tryCall {
            context.contentResolver.delete(uri, null, null)
        }
        return newPath
    }

    private fun getBitmapFrom(uri: Uri): Bitmap? {
        val bitmap: Bitmap
        val exif: ExifInterface?

        if (Build.VERSION.SDK_INT > 23) {
            bitmap = try {
                val ims = context.contentResolver.openInputStream(uri) ?: return null
                BitmapFactory.decodeStream(ims)
            } catch (e: FileNotFoundException) {
                return null
            }
            exif = context.contentResolver.openInputStream(uri)?.let { ExifInterface(it) }
        } else {
            bitmap = FileUtils.getPath(context, uri)?.let { BitmapFactory.decodeFile(it) }
                    ?: return null
            exif = uri.path?.let { ExifInterface(it) }
        }

        val orientation: Int = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED) ?: ExifInterface.ORIENTATION_NORMAL

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }

    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, true).also { source.recycle() }
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