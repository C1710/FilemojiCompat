package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.NoEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.structures.EmojiPack

const val SYSTEM_DEFAULT = "emoji_system_default"

class SystemDefaultEmojiPack private constructor(
    context: Context
): EmojiPack(
    SYSTEM_DEFAULT,
    context.resources.getString(R.string.system_default),
    context.resources.getString(R.string.system_default_description),
    Version(IntArray(0))
) {



    companion object {
        private var systemDefaultEmojiPack: SystemDefaultEmojiPack? = null

        fun getSystemDefaultPack(context: Context): SystemDefaultEmojiPack {
            if (systemDefaultEmojiPack == null) {
                systemDefaultEmojiPack = SystemDefaultEmojiPack(context)
            }
            return systemDefaultEmojiPack!!
        }
    }

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        Log.d("FilemojiCompat", "init: Using system default")
        return DefaultEmojiCompatConfig.create(context) ?: NoEmojiCompatConfig(context)
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean = true
    override fun getIcon(context: Context): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, R.drawable.ic_default_emojis, context.theme)
    }
}
