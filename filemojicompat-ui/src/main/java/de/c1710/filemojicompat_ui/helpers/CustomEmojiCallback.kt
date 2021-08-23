package de.c1710.filemojicompat_ui.helpers

import java.io.File

interface CustomEmojiCallback {
    fun onLoaded(customEmoji: String)

    fun onFailed(error: Throwable) {}
}