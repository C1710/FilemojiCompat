package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.interfaces.EmojiPackSelectionListener
import de.c1710.filemojicompat_ui.versions.Version
import de.c1710.filemojicompat_ui.versions.VersionProvider

/**
 * A data structure representing an emoji pack.
 * There are many types of packs, based on their origin or the way they need to be handled.
 * @param id A unique name for the pack. It will be used internally at different places
 *           (e.g. when setting the preference or determining the file name). It should never change
 *           or get translated, etc.
 * @param name The user-facing name of the pack. May be translated or changed
 * @param description The (short) user-facing description that is visible in the normal emoji picker
 * @param version The current version of the pack.
 *                It needs to be changed to a larger value, when the font should be updated.
 *                May be retrieved from the web, see [de.c1710.filemojicompat_ui.structures.VersionOnline.versionOnline],
 *                therefore an interface, [VersionProvider] is used. [Version] already implements this
 *                interface, therefore it can be directly used as an argument.
 * @param website The URL of the website/repository for the emoji pack
 * @param license The URL of the license for the emoji pack
 *                (This might be auto-downloaded in the future, so it should point to a rather small/plaintext file, if possible)
 * @param descriptionLong A longer description that is shown when the user expands the item for the emoji pack.
 *                        It may contain additional information like a copyright notice, etc.
 */
abstract class EmojiPack(
    var id: String,
    var name: String,
    var description: String,
    private val version: VersionProvider?,
    var website: Uri? = null,
    var license: Uri? = null,
    var descriptionLong: String? = null
) {

    private val selectionListeners: ArrayList<EmojiPackSelectionListener> = ArrayList(3)

    /**
     * Selects an emoji pack (i.e. stores the preference and resets [EmojiCompat]) and deselects the
     * currently selected emoji pack
     */
    fun select(context: Context, previousSelection: EmojiPack? = selectedPack) {
        if (previousSelection != this) {
            previousSelection?.selectionListeners?.forEach {
                it.onDeSelected(context, previousSelection)
            }

            EmojiPreference.setSelected(context, this.id)
            selectedPack = this
            selectionListeners.forEach {
                it.onSelected(context, this)
            }
        } else {
            Log.d("FilemojiCompat", "Pack %s is already selected".format(id))
        }
    }

    /**
     * Loads the actual [EmojiCompat.Config] associated with this pack to use in [EmojiCompat.init]/[EmojiCompat.reset].
     * This should only be done in conjunction with [select].
     */
    internal abstract fun load(context: Context, list: EmojiPackList): EmojiCompat.Config

    /**
     * Returns the icon for this emoji pack.
     * Note: This is done as a function, because at the time the pack object is created, the [Context.getTheme]
     * might not be initialized yet, so a pre-set icon might be wrongly themed.
     * However, if the icon is theme-independent, it may be stored permanently
     */
    abstract fun getIcon(context: Context): Drawable?

    /**
     * Returns the current version of this pack. Should be called as late as possible.
     */
    fun getVersion(): Version? {
        return version?.getVersion()
    }

    fun addSelectionListener(selectionListener: EmojiPackSelectionListener) {
        selectionListeners.add(selectionListener)
    }

    fun removeSelectionListener(selectionListener: EmojiPackSelectionListener) {
        selectionListeners.remove(selectionListener)
    }

    override fun toString(): String = this.id

    companion object {
        /**
         * The currently selected EmojiPack (should reflect the status from [EmojiPreference.getSelected])
         */
        var selectedPack: EmojiPack? = null
            internal set
    }
}

