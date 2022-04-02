package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.versions.Version

/**
 * Represents a custom emoji pack, that has been imported through a file picker.
 * Usually, you don't create an object of this class manually.
 */
class CustomEmojiPack constructor(
    context: Context,
    hash: String
) : FileBasedEmojiPack(
    hash,
    EmojiPreference.getNameForCustom(context, hash) ?: hash,
    "",
    null as Version?,
    null as Uri?,
    null as Uri?,
    context.resources.getString(R.string.custom_emoji_description_long)
) {
    override fun getIcon(context: Context): Drawable? =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_custom_emojis, context.theme)

    // Completely remove the pack from the pack list
    override fun deleteImpl(context: Context, list: EmojiPackList): Int {
        super.deleteImpl(context, list)

        // It may be possible, that the pack has already been removed
        return if (this in list) {
            val index = list.indexOf(this)
            list.removePack(this)
            index
        } else {
            -1
        }
    }
}