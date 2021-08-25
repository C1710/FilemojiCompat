package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version

abstract class EmojiPack (
    var id: String,
    var name: String,
    var description: String,
    var icon: Drawable?,
    var version: Version?,
    var website: Uri? = null,
    var license: Uri? = null,
    var descriptionLong: String? = null
) {
    fun select(context: Context) {
        EmojiPreference.setSelected(context, this.id)
    }

    abstract fun load(context: Context, list: EmojiPackList): EmojiCompat.Config

    abstract fun isCurrentVersion(list: EmojiPackList): Boolean
}

