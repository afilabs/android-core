package com.support.core.helpers

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri

class ImageScale(private val fileBitmap: BitmapLoader) {
    companion object {
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1080
    }

    fun scale(uri: Uri): Bitmap {
        val bitmap = fileBitmap.getBitmapFrom(uri)
        val newBitmap = scale(bitmap)
        if (bitmap != newBitmap) bitmap.recycle()
        return newBitmap
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