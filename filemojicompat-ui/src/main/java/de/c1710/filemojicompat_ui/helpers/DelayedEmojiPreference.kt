package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import de.c1710.filemojicompat_ui.helpers.DelayedEmojiPreference.commitSelection
import de.c1710.filemojicompat_ui.interfaces.EmojiPreferenceInterface
import de.c1710.filemojicompat_ui.structures.EmojiPack

/**
 * A "proxy" for [EmojiPreference] that does not immediately store changed preferences,
 * but only when [commitSelection] is called.
 */
object DelayedEmojiPreference: EmojiPreferenceInterface {
    var selected: String? = null

    override fun getSelected(context: Context): String {
        return selected ?: run {
            selected = EmojiPreference.getSelected(context)
            selected!!
        }
    }

    override fun setSelected(context: Context, value: String) {
        selected = value
    }

    override fun getDefault(context: Context): String = EmojiPreference.getDefault(context)

    override fun setDefault(context: Context, value: String) = EmojiPreference.setDefault(context, value)

    override fun getNameForCustom(context: Context, hash: String): String? = EmojiPreference.getNameForCustom(context, hash)

    override fun setNameForCustom(context: Context, name: String, hash: String) = EmojiPreference.setNameForCustom(context, name, hash)

    /**
     * Actually stores the preference (independently of whether it is different to the previous one or not)
     */
    fun commitSelection(context: Context) {
        selected?.let { EmojiPreference.setSelected(context, it) }
    }

    fun dismissSelection(context: Context, list: EmojiPackList = EmojiPackList.defaultList) {
        // Reset there as well
        EmojiPack.selectedPack = list[EmojiPreference.getSelected(context)]
        selected = null
    }
}