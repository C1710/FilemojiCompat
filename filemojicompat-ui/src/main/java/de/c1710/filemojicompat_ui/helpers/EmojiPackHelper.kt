package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import de.c1710.filemojicompat_ui.packs.createFileName
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import java.io.File

class EmojiPackHelper {
    companion object {
        @JvmStatic
        fun init(context: Context) {
            if (EmojiPackList.defaultList == null) {
                EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = ArrayList())
                Log.w("FilemojiCompat", "init: No Emoji Pack list created. Using empty one")
            }

            val config: EmojiCompat.Config = generateCurrentConfig(context);

            EmojiCompat.init(config)
        }

        fun reset(context: Context) {
            val config = generateCurrentConfig(context)

            EmojiCompat.reset(config)
        }

        private fun generateCurrentConfig(context: Context): EmojiCompat.Config {
            val list = EmojiPackList.defaultList!!

            val emojiPack = list[EmojiPreference.getSelected(context)]
            val selectedPack = if (emojiPack != null) {
                emojiPack
            } else {
                Log.e("FilemojiCompat", "generateCurrentConfig: selected emoji pack %s not in list"
                    .format(EmojiPreference.getSelected(context)))
                Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_SHORT).show()
                SystemDefaultEmojiPack.getSystemDefaultPack()
            }

            return selectedPack.load(context, list)
        }

        @JvmStatic
        fun init(context: Context, emojiPacks: ArrayList<EmojiPack>) {
            EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = emojiPacks)

            init(context)
        }

        private fun loadDownloadedPack(context: Context, selected: String, list: EmojiPackList): EmojiCompat.Config {
            val downloadedVersion = list.downloadedVersion(selected)
            val fileName = createFileName(selected, downloadedVersion)
            Log.d("FilemojiCompat", "loadDownloadedPack: File path: %s".format(fileName))
            return loadFromEmojiStorage(context, list, fileName)
        }

        private fun loadFromEmojiStorage(context: Context, list: EmojiPackList, fileName: String): EmojiCompat.Config {
            val file = File(list.emojiStorage, fileName)
            val config = FileEmojiCompatConfig.init(context, file)
            if (config.fallbackEnabled.get()) {
                Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_LONG).show()
                return DefaultEmojiCompatConfig.create(context) ?: config
            }
            return config
        }
    }
}