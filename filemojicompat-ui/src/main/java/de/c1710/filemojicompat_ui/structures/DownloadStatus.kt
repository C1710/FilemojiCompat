package de.c1710.filemojicompat_ui.structures

import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import java.io.IOException

class DownloadStatus: EmojiPackDownloader.DownloadCallback {
    private var bytesRead: Long = 0
    private var size: Long = 0
    private var error: IOException? = null
    private var done: Boolean = false
    private val callbacks: ArrayList<EmojiPackDownloader.DownloadCallback> = ArrayList(1)

    override fun onProgress(bytesRead: Long, contentLength: Long) {
        this.bytesRead = bytesRead
        this.size = contentLength
        callbacks.forEach { callback -> callback.onProgress(bytesRead, size) }
    }

    override fun onFailure(e: IOException) {
        this.error = e
        callbacks.forEach { callback -> callback.onFailure(error!!) }
    }

    override fun onDone() {
        this.done = true
        callbacks.forEach { callback -> callback.onDone() }
    }

    fun addCallback(callback: EmojiPackDownloader.DownloadCallback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: EmojiPackDownloader.DownloadCallback) {
        callbacks.remove(callback)
    }

    fun getBytesRead(): Long = bytesRead
    fun getSize(): Long = size
    fun getProgress(): Double = bytesRead.toDouble() / size.toDouble()
    fun getError(): IOException? = error
    fun isDone(): Boolean = done
}