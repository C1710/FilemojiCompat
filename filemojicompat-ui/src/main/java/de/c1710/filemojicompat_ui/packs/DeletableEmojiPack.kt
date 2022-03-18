package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.net.Uri
import android.os.Handler
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDeletionListener
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.VersionProvider

/**
 * An emoji pack that can be deleted (somehow)
 */
abstract class DeletableEmojiPack(
    id: String,
    name: String,
    description: String,
    version: VersionProvider?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
) : EmojiPack(id, name, description, version, website, license, descriptionLong) {
    private val listeners: ArrayList<EmojiPackDeletionListener> = ArrayList(3)

    /**
     * Returns the index of the emoji in the list, if it is removed from the list or -1 otherwise.
     */
    protected abstract fun deleteImpl(context: Context, list: EmojiPackList): Int
    // The pending deletion
    private var deletion: Runnable? = null
    // ...and the handler it's been posted to
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

    /**
     * Prevents the actual deletion from happening
     */
    fun cancelDeletion(context: Context) {
        this.deletion?.let { callback ->
            this.deletionHandler!!.removeCallbacks(callback)
            this.deletion = null
            this.deletionHandler = null
            listeners.forEach { it.onDeleteCancelled(context, this) }
        }
    }

    /**
     * Plans a deletion for some time in the future (until then, it can still be cancelled)
     * @param timeToDelete The time in ms to wait with the deletion
     * @param handler The Handler to run the deletion on
     * @param list The list to (depending on the actual type of pack) remove the pack from later on
     */
    fun scheduleDeletion(
        context: Context,
        timeToDelete: Long,
        handler: Handler,
        list: EmojiPackList,
        selectDefaultAllowed: (String) -> Boolean = { _ -> true }
    ) {
        if(selectDefaultAllowed(SYSTEM_DEFAULT) || selectedPack != this) {
            if (selectedPack == this) {
                // Here, we need to immediately select another pack, since deleting is not something
                // that can be dismissed once a dialog is closed.
                list.getDefaultPack(context).select(context, this, preference = EmojiPreference)
            }

            listeners.forEach {
                it.onDeletionScheduled(context, this, timeToDelete)
            }

            val deletion = Runnable { delete(context, list) }
            handler.postDelayed(deletion, timeToDelete)

            this.deletion = deletion
            this.deletionHandler = handler
        } // else: The deletion/selection of the default pack was not allowed
    }

    fun isGettingDeleted(): Boolean = this.deletion != null

    fun addDeletionListener(listener: EmojiPackDeletionListener) {
        listeners.add(listener)
    }

    fun removeDeletionListener(listener: EmojiPackDeletionListener) {
        listeners.remove(listener)
    }
}