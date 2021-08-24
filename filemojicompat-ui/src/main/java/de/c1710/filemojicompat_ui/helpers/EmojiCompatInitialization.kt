package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat.NoEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.structures.CUSTOM_PACK
import de.c1710.filemojicompat_ui.structures.EmojiPack.Companion.createFileName
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import de.c1710.filemojicompat_ui.structures.SYSTEM_DEFAULT
import java.io.File

class EmojiCompatInitialization {
    companion object {
        fun init(context: Context, list: EmojiPackList) {
            val config: EmojiCompat.Config = when (val selected = EmojiPreference.getSelected(context)) {
                SYSTEM_DEFAULT -> {
                    Log.d("FilemojiCompat", "init: Using system default")
                    DefaultEmojiCompatConfig.create(context) ?: NoEmojiCompatConfig(context)
                }
                CUSTOM_PACK -> {
                    Log.d("FilemojiCompat", "init: Loading from file")
                    loadCustomPack(context, list)
                }
                else -> {
                    Log.d("FilemojiCompat", "init: Loading from downloaded pack")
                    loadDownloadedPack(context, selected, list)
                }
            }

            EmojiCompat.init(config)
        }

        private fun loadDownloadedPack(context: Context, selected: String, list: EmojiPackList): EmojiCompat.Config {
            val downloadedVersion = list.downloadedVersion(selected)
            val fileName = createFileName(selected, downloadedVersion)
            Log.d("FilemojiCompat", "loadDownloadedPack: File path: %s".format(fileName))
            return loadFromEmojiStorage(context, list, fileName)
        }

        private fun loadCustomPack(context: Context, list: EmojiPackList): EmojiCompat.Config {
            val customName = EmojiPreference.getCustom(context)
            Log.d("FilemojiCompat", "loadCustomPack: Hash: %s".format(customName.toString()))
            if (customName != null) {
                val fileName = createFileName(customName, Version(IntArray(0)))
                return loadFromEmojiStorage(context, list, fileName)
            } else {
                Toast.makeText(context, R.string.loadingFailed, Toast.LENGTH_LONG).show()
                return DefaultEmojiCompatConfig.create(context) ?: NoEmojiCompatConfig(context)
            }
        }

        private fun loadFromEmojiStorage(context: Context, list: EmojiPackList, fileName: String): EmojiCompat.Config {
            val file = File(list.emojiStorage, fileName)
            val config = FileEmojiCompatConfig.init(context, file)
            if (config.fallbackEnabled.get()) {
                Toast.makeText(context, R.string.loadingFailed, Toast.LENGTH_LONG).show()
                return DefaultEmojiCompatConfig.create(context) ?: config
            }
            return config
        }
    }
}