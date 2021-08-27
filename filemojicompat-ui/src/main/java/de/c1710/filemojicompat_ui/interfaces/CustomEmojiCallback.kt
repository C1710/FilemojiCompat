package de.c1710.filemojicompat_ui.interfaces

interface CustomEmojiCallback {
    fun onLoaded(customEmoji: String)

    fun onFailed(error: Throwable) {}
}