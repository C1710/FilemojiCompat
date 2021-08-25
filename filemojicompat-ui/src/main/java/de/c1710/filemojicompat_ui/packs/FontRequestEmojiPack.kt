package de.c1710.filemojicompat_ui.packs

import android.content.Context

class FontRequestEmojiPack {
}

// TODO: Implement

// Copied from the private fields in DefaultEmojiCompatConfigFactory
private const val INTENT_LOAD_EMOJI_FONT = "androidx.content.action.LOAD_EMOJI_FONT"
private const val DEFAULT_EMOJI_QUERY = "emojicompat-emoji-font"

fun collectFontProviders(context: Context): ArrayList<FontRequestEmojiPack> {
    // This is mostly adapted from what DefaultEmojiCompatConfigFactory does
    TODO()
}