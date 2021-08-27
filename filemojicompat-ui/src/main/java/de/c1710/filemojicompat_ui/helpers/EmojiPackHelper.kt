package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack

class EmojiPackHelper {
    companion object {
        @JvmStatic
        fun init(context: Context) {
            if (EmojiPackList.defaultList == null) {
                EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = ArrayList())
                Log.w("FilemojiCompat", "init: No Emoji Pack list created. Using empty one")
            }

            val config: EmojiCompat.Config = getCurrentConfig(context);

            EmojiCompat.init(config)
        }

        @JvmStatic
        fun init(context: Context, emojiPacks: ArrayList<EmojiPack>) {
            EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = emojiPacks)

            init(context)
        }


        fun reset(context: Context) {
            val config = getCurrentConfig(context)

            EmojiCompat.reset(config)
        }

        private fun getCurrentConfig(context: Context): EmojiCompat.Config {
            val list = EmojiPackList.defaultList!!

            val emojiPack = list[EmojiPreference.getSelected(context)]
            val selectedPack = if (emojiPack != null) {
                emojiPack
            } else {
                Log.e(
                    "FilemojiCompat", "generateCurrentConfig: selected emoji pack %s not in list"
                        .format(EmojiPreference.getSelected(context))
                )
                Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_SHORT).show()
                SystemDefaultEmojiPack.getSystemDefaultPack(context)
            }

            return selectedPack.load(context, list)
        }
    }
}