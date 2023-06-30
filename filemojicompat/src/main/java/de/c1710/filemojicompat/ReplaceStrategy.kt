package de.c1710.filemojicompat

enum class ReplaceStrategy {
    /**
     * Never replace all emojis, but only those that are not supported.
     * Corresponds to [androidx.emoji2.text.EmojiCompat.Config.setReplaceAll] with false.
     *
     * Not recommended.
     */
    NEVER,

    /**
     * Replaces all emojis if and only if an emoji font file is present; default behavior.
     */
    NORMAL,

    /**
     * Replaces all emojis, even if the fallback/integrated font is used.
     * Recommended if you provide a bundled emoji font.
     */
    ALWAYS
}