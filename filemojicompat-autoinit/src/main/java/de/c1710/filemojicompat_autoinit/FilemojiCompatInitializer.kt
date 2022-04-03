package de.c1710.filemojicompat_autoinit

import android.content.Context
import androidx.startup.Initializer
import de.c1710.filemojicompat_defaults.DefaultEmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPackHelper

/**
 * An automatic initializer for FilemojiCompat with the default list.
 * Based on EmojiCompatInitializer.
 * @see androidx.emoji2.text.EmojiCompatInitializer
 */
class FilemojiCompatInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        EmojiPackHelper.init(context, DefaultEmojiPackList.get(context))
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return ArrayList()
    }
}