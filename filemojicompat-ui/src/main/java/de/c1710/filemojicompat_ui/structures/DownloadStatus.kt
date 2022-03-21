package de.c1710.filemojicompat_ui.structures

import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import java.io.IOException

/**
 * A data structure to collect/store updates from an [de.c1710.filemojicompat_ui.pack_helpers.EmojiPackDownloader]
 * and pass them on to other listeners
 */
class DownloadStatus : EmojiPackDownloadListener {
    /**
     * How many bytes have been downloaded in total
     */
    var bytesRead: Long = 0
        private set

    /**
     * How many bytes the download will have
     */
    var size: Long = 0
        private set

    /**
     * An error that prevented the download from succeeding
     */
    var error: IOException? = null
        private set

    /**
     * Whether the download has finished successfully
     */
    var done: Boolean = false
        private set

    /**
     * Whether the download has been cancelled
     */
    var cancelled: Boolean = false
        private set

    private val listeners: ArrayList<EmojiPackDownloadListener> = ArrayList(1)

    override fun onProgress(bytesRead: Long, contentLength: Long) {
        this.bytesRead = bytesRead
        this.size = contentLength
        listeners.forEach { callback -> callback.onProgress(bytesRead, size) }
    }

    override fun onFailure(e: IOException?) {
        this.error = e
        listeners.forEach { callback -> callback.onFailure(error) }
    }

    override fun onCancelled() {
        this.cancelled = true
        listeners.forEach { callback -> callback.onCancelled() }
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