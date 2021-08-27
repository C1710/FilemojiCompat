package de.c1710.filemojicompat_ui.structures

import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import java.io.IOException

class DownloadStatus: EmojiPackDownloader.DownloadListener {
    var bytesRead: Long = 0
        private set
    var size: Long = 0
        private set
    var error: IOException? = null
        private set
    var done: Boolean = false
        private set
    private val listeners: ArrayList<EmojiPackDownloader.DownloadListener> = ArrayList(1)

    override fun onProgress(bytesRead: Long, contentLength: Long) {
        this.bytesRead = bytesRead
        this.size = contentLength
        listeners.forEach { callback -> callback.onProgress(bytesRead, size) }
    }

    override fun onFailure(e: IOException) {
        this.error = e
        listeners.forEach { callback -> callback.onFailure(error!!) }
    }

    override fun onDone() {
        this.done = true
        listeners.forEach { callback -> callback.onDone() }
    }

    fun addListener(listener: EmojiPackDownloader.DownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EmojiPackDownloader.DownloadListener) {
        listeners.remove(listener)
    }
}