package de.c1710.filemojicompat_ui.interfaces

import android.content.Context
import de.c1710.filemojicompat_ui.structures.EmojiPack

interface EmojiPackListener {
    fun onSelected(context: Context, pack: EmojiPack)

    fun onUnSelected(context: Context, pack: EmojiPack)
}