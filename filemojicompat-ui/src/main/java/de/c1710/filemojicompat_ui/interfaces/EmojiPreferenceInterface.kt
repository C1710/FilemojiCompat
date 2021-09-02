package de.c1710.filemojicompat_ui.interfaces

import android.content.Context
import de.c1710.filemojicompat_ui.helpers.EmojiPreference.setSelected
import de.c1710.filemojicompat_ui.packs.SYSTEM_DEFAULT

/**
 * Used to store and retrieve preferences with possibly different backends
 */
interface EmojiPreferenceInterface {
    /**
     * Returns the id/hash of the currently selected emoji pack, or a default value if there is none
     */
    fun getSelected(context: Context): String

    /**
     * Stores the id/hash of the currently/newly selected emoji pack
     */
    fun setSelected(context: Context, value: String)


    /**
     * Returns the name of the default pack for this application.
     * Note: Usually, this should be [SYSTEM_DEFAULT], but it may be overridden, if the application e.g.
     * comes with an Asset-based default pack.
     */
    fun getDefault(context: Context): String

    /**
     * Sets the id/hash of the default emoji pack for this application.
     * Note: You should <i>never</i> use a [de.c1710.filemojicompat_ui.packs.FileBasedEmojiPack] here, because it may be deleted by
     * the user. If you want to update their choice, use [setSelected] instead.
     * Usually you would use a [de.c1710.filemojicompat_ui.packs.AssetEmojiPack] here.
     */
    fun setDefault(context: Context, value: String)


    /**
     * The preference name/key for the custom names preference
     */
    fun getNameForCustom(context: Context, hash: String): String?

    /**
     * Returns the name assigned for a certain custom/imported emoji pack.
     * Note: You should only use this for display, internally, you should use the [de.c1710.filemojicompat_ui.structures.EmojiPack.id]/hash of the pack.
     * @param hash The file hash of the pack
     * @return A user-assigned name of the pack (or null if it doesn't exist)
     */
    fun setNameForCustom(context: Context, name: String, hash: String)
}