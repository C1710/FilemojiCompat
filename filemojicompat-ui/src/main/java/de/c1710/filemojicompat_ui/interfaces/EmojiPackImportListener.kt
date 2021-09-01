package de.c1710.filemojicompat_ui.interfaces

/**
 * Used when a custom emoji pack is being imported
 */
interface EmojiPackImportListener {
    /**
     * Called when a custom emoji pack has been successfully loaded/imported.
     * @param customEmoji The file hash of the successfully imported pack
     */
    fun onLoaded(customEmoji: String)

    /**
     * Called when loading failed.
     * @param error The reason, loading/importing failed
     */
    fun onFailed(error: Throwable) {}
}