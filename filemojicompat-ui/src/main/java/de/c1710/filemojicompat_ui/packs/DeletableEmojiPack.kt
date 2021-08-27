package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.net.Uri
import android.os.Handler
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDeletionListener
import de.c1710.filemojicompat_ui.structures.EmojiPack

abstract class DeletableEmojiPack(
    id: String,
    name: String,
    description: String,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
): EmojiPack(id, name, description, version, website, license, descriptionLong) {
    private val listeners: ArrayList<EmojiPackDeletionListener> = ArrayList(3)
    // Returns the index of the emoji in the list, if it is removed from the emojiList or -1 otherwise.
    protected abstract fun deleteImpl(context: Context, list: EmojiPackList): Int
    private var deletion: Runnable? = null
    private var deletionHandler: Handler? = null

    // MUST call select for a new one before
    private fun delete(context: Context, list: EmojiPackList) {
        this.deletion = null
        this.deletionHandler = null
        val index = deleteImpl(context, list)
        listeners.forEach {
            it.onDeleted(context, this, index)
        }
    }

    fun cancelDeletion(context: Context) {
        this.deletion?.let { callback ->
            this.deletionHandler!!.removeCallbacks(callback)
            this.deletion = null
            this.deletionHandler = null
            listeners.forEach { it.onDeleteCancelled(context, this) }
        }
    }

    fun scheduleDeletion(context: Context, timeToDelete: Long, handler: Handler, list: EmojiPackList) {
        list.getDefaultPack(context).select(context, this)

        listeners.forEach {
            it.onDeletionScheduled(context, this, timeToDelete)
        }

        val deletion = Runnable { delete(context, list) }
        handler.postDelayed(deletion, timeToDelete)

        this.deletion = deletion
        this.deletionHandler = handler
    }

    fun isGettingDeleted(): Boolean = this.deletion != null

    fun addDeletionListener(listener: EmojiPackDeletionListener) {
        listeners.add(listener)
    }

    fun removeDeletionListener(listener: EmojiPackDeletionListener) {
        listeners.remove(listener)
    }
}