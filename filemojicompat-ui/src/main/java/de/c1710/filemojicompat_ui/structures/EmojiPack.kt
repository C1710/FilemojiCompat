package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat_ui.helpers.EmojiPackListener
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version

abstract class EmojiPack (
    var id: String,
    var name: String,
    var description: String,
    var version: Version?,
    var website: Uri? = null,
    var license: Uri? = null,
    var descriptionLong: String? = null
) {
    private val listeners: ArrayList<EmojiPackListener> = ArrayList(3)

    fun select(context: Context, previousSelection: EmojiPack? = selectedPack) {
        previousSelection?.listeners?.forEach {
            it.onUnSelected(context, previousSelection)
        }

        EmojiPreference.setSelected(context, this.id)
        selectedPack = this
        listeners.forEach {
            it.onSelected(context, this)
        }
    }

    abstract fun load(context: Context, list: EmojiPackList): EmojiCompat.Config

    abstract fun isCurrentVersion(list: EmojiPackList): Boolean

    abstract fun getIcon(context: Context): Drawable?


    fun addListener(listener: EmojiPackListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EmojiPackListener) {
        listeners.remove(listener)
    }

    companion object {
        var selectedPack: EmojiPack? = null
    }
}

