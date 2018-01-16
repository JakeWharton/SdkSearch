package com.jakewharton.sdksearch.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    } else {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        setBounds(0, 0, bitmap.width, bitmap.height)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}
