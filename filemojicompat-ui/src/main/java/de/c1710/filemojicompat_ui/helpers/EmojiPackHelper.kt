package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat.NoEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.structures.EXTERNAL_FILE
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import de.c1710.filemojicompat_ui.structures.SYSTEM_DEFAULT
import java.io.File

class EmojiPackHelper {
    companion object {
        @JvmStatic
        fun init(context: Context) {
            val config: EmojiCompat.Config = generateCurrentConfig(context);

            EmojiCompat.init(config)
        }

        fun reset(context: Context) {
            val config = generateCurrentConfig(context)

            EmojiCompat.reset(config)
        }

        private fun generateCurrentConfig(context: Context): EmojiCompat.Config {
            val list = EmojiPackList.defaultList!!

            return when (val selected = EmojiPreference.getSelected(context)) {
                SYSTEM_DEFAULT -> {
                    Log.d("FilemojiCompat", "init: Using system default")
                    DefaultEmojiCompatConfig.create(context) ?: NoEmojiCompatConfig(context)
                }
                else -> {
                    Log.d("FilemojiCompat", "init: Loading from downloaded pack")
                    loadDownloadedPack(context, selected, list)
                }
            }
        }

        @JvmStatic
        fun init(context: Context, emojiPacks: ArrayList<EmojiPack>) {
            EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = emojiPacks)

            init(context)
        }

        private fun loadDownloadedPack(context: Context, selected: String, list: EmojiPackList): EmojiCompat.Config {
            val downloadedVersion = list.downloadedVersion(selected)
            val fileName = EmojiPack.createFileName(selected, downloadedVersion)
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