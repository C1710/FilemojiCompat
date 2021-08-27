package de.c1710.filemojicompat_ui.interfaces

import java.io.IOException

interface EmojiPackDownloadListener {
    fun onProgress(bytesRead: Long, contentLength: Long)

    fun onFailure(e: IOException)

    fun onDone()
}