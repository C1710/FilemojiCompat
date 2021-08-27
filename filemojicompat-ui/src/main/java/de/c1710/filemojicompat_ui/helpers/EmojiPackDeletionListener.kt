package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import de.c1710.filemojicompat_ui.packs.DeletableEmojiPack

interface EmojiPackDeletionListener {
    fun onDeleted(context: Context, pack: DeletableEmojiPack, oldIndex: Int)

    fun onDeletionScheduled(context: Context, pack: DeletableEmojiPack, timeToDeletion: Long)

    fun onDeleteCancelled(context: Context, pack: DeletableEmojiPack)
}