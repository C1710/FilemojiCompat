package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version

class CustomEmojiPack(
    context: Context,
    hash: String
): FileBasedEmojiPack (
    hash,
    EmojiPreference.getNameForCustom(context, hash) ?: hash,
    "",
    null as Version?,
    null as Uri?,
    null as Uri?,
    context.resources.getString(R.string.custom_emoji_description_long)
) {
    override fun isCurrentVersion(list: EmojiPackList): Boolean = true
    override fun getIcon(context: Context): Drawable? = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_custom_emojis, context.theme)

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