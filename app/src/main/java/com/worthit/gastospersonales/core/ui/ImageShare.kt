package com.worthit.gastospersonales.core.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Shares images (and only images) with other apps through the FileProvider declared in the manifest.
object ImageShare {

    private const val SHARE_DIR = "shared"

    // Writes the PNG files; call it off the main thread. Only the images of the last share are kept.
    fun save(context: Context, pages: List<Bitmap>, baseName: String): List<Uri> {
        val dir = File(context.cacheDir, SHARE_DIR).apply {
            deleteRecursively()
            mkdirs()
        }
        return pages.mapIndexed { index, page ->
            val file = File(dir, "${baseName}_${index + 1}.png")
            file.outputStream().use { page.compress(Bitmap.CompressFormat.PNG, 100, it) }
            page.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }

    // The chooser for the saved images. It carries no text.
    fun chooser(uris: List<Uri>, title: String): Intent {
        val send = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
        send.setType("image/png").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = ClipData.newRawUri(null, uris.first()).also { clip ->
            uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
        }
        return Intent.createChooser(send, title)
    }
}
