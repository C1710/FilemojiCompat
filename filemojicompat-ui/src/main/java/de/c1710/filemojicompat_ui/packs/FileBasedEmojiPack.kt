package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.structures.Version
import java.io.File

/**
 * An emoji pack that is stored in some file
 */
abstract class FileBasedEmojiPack(
    id: String,
    name: String,
    description: String,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
) : DeletableEmojiPack(
    id, name, description, version, website, license, descriptionLong
) {
    /**
     * Returns the bare file name for a pack.
     * To actually get the (maybe not even existing) file, you need to prefix it with a directory
     */
    open fun getFileName(): String {
        return "%s.ttf".format(id)
    }

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        Log.d(
            "FilemojiCompat",
            "load: Loading %s".format(this.id)
        )

        // Here we need the _actual_ version we have
        val fileName = getFileName()
        Log.d("FilemojiCompat", "Loading file based pack: File path: %s".format(fileName))

        val file = File(list.emojiStorage, fileName)
        val config = FileEmojiCompatConfig.init(context, file)

        if (config.fallbackEnabled.get()) {
            Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_LONG).show()
            return DefaultEmojiCompatConfig.create(context) ?: config
        }

        return config
    }

    override fun deleteImpl(context: Context, list: EmojiPackList): Int {
        val file = File(list.emojiStorage, getFileName())

        file.delete()

        return -1
    }
}
