package com.lisitede.preset.preset.album

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SavedAlbumImage(val uri: Uri, val name: String)

/** Copies the selected image's original bytes into a new MediaStore image. */
class AlbumWriter(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    suspend fun copyImage(source: Uri): SavedAlbumImage = withContext(Dispatchers.IO) {
        val mimeType = resolver.getType(source)?.takeIf { it.startsWith("image/") }
            ?: throw IOException("Selected media is not an image")
        val originalName = resolver.query(
            source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }.orEmpty()
        val originalExtension = originalName.substringAfterLast('.', "")
        val extension = if (originalExtension.matches(Regex("[a-zA-Z0-9]{1,16}"))) {
            originalExtension
        } else {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                ?: throw IOException("Unable to determine original image extension")
        }
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val name = newName(collection, extension)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.IS_PENDING, 1)
            // Let MediaStore choose its default; no DATA or RELATIVE_PATH is specified.
        }
        val coroutineContext = currentCoroutineContext()
        var destination: Uri? = null
        try {
            val input = resolver.openInputStream(source) ?: throw IOException("Unable to read image")
            input.use {
                coroutineContext.ensureActive()
                val target = resolver.insert(collection, values) ?: throw IOException("Unable to add image")
                destination = target
                val output = resolver.openOutputStream(target, "w") ?: throw IOException("Unable to write image")
                output.use {
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        total += count
                    }
                    if (total == 0L) throw IOException("Empty source image")
                }
            }
            coroutineContext.ensureActive()
            val target = checkNotNull(destination)
            if (Build.VERSION.SDK_INT >= 29) {
                val published = resolver.update(target, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
                if (published != 1) throw IOException("Unable to publish image")
            }
            SavedAlbumImage(target, name)
        } catch (failure: Exception) {
            // Also remove an unfinished insert when its page/job is cancelled.
            destination?.let { target ->
                try {
                    resolver.delete(target, null, null)
                } catch (cleanupFailure: Exception) {
                    failure.addSuppressed(cleanupFailure)
                }
            }
            throw failure
        }
    }

    private suspend fun newName(collection: Uri, extension: String): String {
        repeat(20) {
            currentCoroutineContext().ensureActive()
            val timestamp = LocalDateTime.now().format(FILE_TIME)
            val suffix = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000))
            val name = "$timestamp-$suffix.$extension"
            val exists = resolver.query(
                collection, arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.DISPLAY_NAME} = ?", arrayOf(name), null
            )?.use { it.moveToFirst() } ?: throw IOException("Unable to check image name")
            if (!exists) return name
        }
        throw IOException("Unable to generate an unused image name")
    }

    private companion object {
        val FILE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss", Locale.ROOT)
        val RANDOM = SecureRandom()
    }
}
