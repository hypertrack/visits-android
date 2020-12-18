package com.hypertrack.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.max
import kotlin.math.min

/**
 *  Image related utilities
 */
interface ImageDecoder {
    /**@return Bitmap scaled to fit 210x160 square */
    fun fetchIcon(imagePath: String, maxSideLength: Int): Bitmap
}

class SimpleImageDecoder(private val context: Context) : ImageDecoder {

    override fun fetchIcon(imagePath: String, maxSideLength: Int) : Bitmap {
        val options = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(imagePath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = max(1, min(photoW / maxSideLength, photoH / maxSideLength))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor

        }

        return BitmapFactory.decodeFile(imagePath, options)
    }

}

