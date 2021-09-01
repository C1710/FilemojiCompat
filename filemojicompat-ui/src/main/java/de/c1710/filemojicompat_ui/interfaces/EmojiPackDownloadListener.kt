package de.c1710.filemojicompat_ui.interfaces

import java.io.IOException

/**
 * Used for handling the download of [de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack]
 */
interface EmojiPackDownloadListener {
    /**
     * Called throughout the download to update the current download progress.
     * @param bytesRead How many bytes have already been downloaded (in total).
     * @param contentLength How many bytes in total need to be downloaded.
     */
    fun onProgress(bytesRead: Long, contentLength: Long)

    /**
     * Called when the download went wrong.
     */
    fun onFailure(e: IOException)

    /**
     * Called when the download has been cancelled
     */
    fun onCancelled()

    /**
     * Called when the download has been successfully executed.
     * Might not be called at all, if the download has been cancelled.
     */
    fun onDone()
}