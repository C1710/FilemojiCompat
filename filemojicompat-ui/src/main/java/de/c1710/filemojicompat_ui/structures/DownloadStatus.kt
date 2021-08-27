package de.c1710.filemojicompat_ui.structures

import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import java.io.IOException

class DownloadStatus : EmojiPackDownloadListener {
    var bytesRead: Long = 0
        private set
    var size: Long = 0
        private set
    var error: IOException? = null
        private set
    var done: Boolean = false
        private set
    private val listeners: ArrayList<EmojiPackDownloadListener> = ArrayList(1)

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

    fun addListener(listener: EmojiPackDownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EmojiPackDownloadListener) {
        listeners.remove(listener)
    }
}