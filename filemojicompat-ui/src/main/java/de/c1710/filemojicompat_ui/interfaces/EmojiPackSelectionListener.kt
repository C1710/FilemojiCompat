package de.c1710.filemojicompat_ui.interfaces

import android.content.Context
import de.c1710.filemojicompat_ui.structures.EmojiPack

/**
 * Used to track the selection status of an emoji pack
 */
interface EmojiPackSelectionListener {
    fun onSelected(context: Context, pack: EmojiPack)

    fun onDeSelected(context: Context, pack: EmojiPack)
}