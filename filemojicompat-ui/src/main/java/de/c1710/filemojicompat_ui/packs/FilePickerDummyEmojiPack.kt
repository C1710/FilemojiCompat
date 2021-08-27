package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.structures.EmojiPack

const val EXTERNAL_FILE = "emoji_load_external_file"

class FilePickerDummyEmojiPack(
    context: Context
) : EmojiPack(
    EXTERNAL_FILE,
    context.resources.getString(R.string.import_pack),
    context.resources.getString(R.string.import_pack_description),
    Version(IntArray(0)),
    null,
    null,
    context.resources.getString(R.string.import_pack_description_long)
) {
    companion object {
        private var filePickerDummyEmojiPack: FilePickerDummyEmojiPack? = null

        fun setAndGetFilePickerPack(context: Context): FilePickerDummyEmojiPack {
            filePickerDummyEmojiPack = FilePickerDummyEmojiPack(context)
            return filePickerDummyEmojiPack!!
        }

        fun getFilePickerPack(): FilePickerDummyEmojiPack {
            return filePickerDummyEmojiPack!!
        }
    }

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        Log.wtf(
            "FilemojiCompat",
            "External file dummy selected as emoji pack. This should not happen"
        )
        return SystemDefaultEmojiPack.getSystemDefaultPack(context).load(context, list)
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean = true
    override fun getIcon(context: Context): Drawable? =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_file, context.theme)
}
