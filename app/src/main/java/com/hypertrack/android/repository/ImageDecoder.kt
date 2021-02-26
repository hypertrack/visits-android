package com.hypertrack.android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 *  Image related utilities
 */
interface ImageDecoder {
    /**@return Bitmap scaled to fit 210x160 square */
    fun readBitmap(imagePath: String, maxSideLength: Int): Bitmap
}

class SimpleImageDecoder : ImageDecoder {

    override fun readBitmap(imagePath: String, maxSideLength: Int): Bitmap {
        // Log.v(TAG, "fetchIcon $imagePath with size $maxSideLength")
        val options = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(imagePath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight
            // Log.v(TAG, "captured size ${outWidth}x${outHeight} downscaling to $maxSideLength")

            // Determine how much to scale down the image
            val scaleFactor: Int = listOf(1, photoW / maxSideLength, photoH / maxSideLength).maxOf { it }

            // Log.v(TAG, "Computed scale factor $scaleFactor")

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor

        }

        return BitmapFactory.decodeFile(imagePath, options)
    }

    companion object {
        const val TAG = "ImageDecoder"
    }

}

