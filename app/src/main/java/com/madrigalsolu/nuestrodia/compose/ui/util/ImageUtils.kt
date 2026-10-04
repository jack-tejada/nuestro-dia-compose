package com.madrigalsolu.nuestrodia.compose.ui.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Memory-safe and Exif-aware image decoding utility.
 * // ponytail: Standard library and native Android platform APIs without heavy image-loading libraries.
 */
fun decodeSampledBitmapFromUri(
  context: Context,
  uri: Uri,
  maxDimension: Int = 1920,
): ImageBitmap? = runCatching {
  val contentResolver = context.contentResolver

  // Step 1: Decode image bounds without allocating memory for pixels
  val options = BitmapFactory.Options().apply {
    inJustDecodeBounds = true
  }
  contentResolver.openInputStream(uri)?.use { stream ->
    BitmapFactory.decodeStream(stream, null, options)
  }

  val origWidth = options.outWidth
  val origHeight = options.outHeight
  if (origWidth <= 0 || origHeight <= 0) return null

  // Step 2: Compute inSampleSize as power of 2 to avoid OutOfMemoryError on 12MP/48MP captures
  var inSampleSize = 1
  val largestDimension = maxOf(origWidth, origHeight)
  while ((largestDimension / inSampleSize) > maxDimension) {
    inSampleSize *= 2
  }

  // Step 3: Decode downsampled bitmap
  val decodeOptions = BitmapFactory.Options().apply {
    this.inSampleSize = inSampleSize
    inPreferredConfig = Bitmap.Config.ARGB_8888
  }
  val rawBitmap = contentResolver.openInputStream(uri)?.use { stream ->
    BitmapFactory.decodeStream(stream, null, decodeOptions)
  } ?: return null

  // Step 4: Correct Exif rotation so vertical photos are never displayed sideways
  val rotatedBitmap = applyExifOrientation(contentResolver, uri, rawBitmap)
  rotatedBitmap.asImageBitmap()
}.getOrNull()

/**
 * Convenience overload accepting a URI string.
 */
fun decodeSampledBitmapFromUri(
  context: Context,
  uriString: String,
  maxDimension: Int = 1920,
): ImageBitmap? = runCatching {
  decodeSampledBitmapFromUri(context, Uri.parse(uriString), maxDimension)
}.getOrNull()

private fun applyExifOrientation(
  contentResolver: ContentResolver,
  uri: Uri,
  bitmap: Bitmap,
): Bitmap {
  val orientation = runCatching {
    contentResolver.openInputStream(uri)?.use { stream ->
      val exif = ExifInterface(stream)
      exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }
  }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

  val matrix = Matrix()
  when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
    ExifInterface.ORIENTATION_TRANSPOSE -> {
      matrix.postRotate(90f)
      matrix.postScale(-1f, 1f)
    }
    ExifInterface.ORIENTATION_TRANSVERSE -> {
      matrix.postRotate(270f)
      matrix.postScale(-1f, 1f)
    }
    else -> return bitmap
  }

  val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  if (transformed != bitmap) {
    bitmap.recycle()
  }
  return transformed
}
