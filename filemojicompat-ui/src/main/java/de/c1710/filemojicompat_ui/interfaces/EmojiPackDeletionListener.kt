package de.c1710.filemojicompat_ui.interfaces

import android.content.Context
import de.c1710.filemojicompat_ui.packs.DeletableEmojiPack

/**
 * Used when an emoji pack should be deleted
 */
interface EmojiPackDeletionListener {
    /**
     * Called when the timeout is over and the emoji pack actually needs to be deleted.
     * @param pack The pack to be deleted
     * @param oldIndex The index the pack had in the [de.c1710.filemojicompat_ui.helpers.EmojiPackList], or -1 if it hasn't been removed from there
     */
    fun onDeleted(context: Context, pack: DeletableEmojiPack, oldIndex: Int)

    /**
     * Called when the given emoji pack is about to be deleted, although deletion may still be cancelled.
     * @param timeToDeletion The time in ms how long it will take until the pack gets deleted (i.e., [onDeleted] will be called)
     */
    fun onDeletionScheduled(context: Context, pack: DeletableEmojiPack, timeToDeletion: Long)

    /**
     * Called when deletion for the given emoji pack is canceled.
     * In this case, [onDeleted] will not be called
     */
    fun onDeleteCancelled(context: Context, pack: DeletableEmojiPack)
}