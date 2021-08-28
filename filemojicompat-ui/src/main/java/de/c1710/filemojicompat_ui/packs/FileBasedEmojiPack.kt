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
    fun getFileName(): String {
        return getFileName(this.id, this.version)
    }

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        val downloadedVersion = if (this is DownloadableEmojiPack) {
            downloadedVersion
        } else {
            null
        }
        Log.d(
            "FilemojiCompat",
            "load: Loading %s with version %s".format(this.id, downloadedVersion.toString())
        )

        // Here we need the _actual_ version we have
        val fileName = getFileName(this.id, downloadedVersion)
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

private fun getFileName(packId: String, version: Version?): String {
    return if (!(version ?: Version(IntArray(0))).isZero()) {
        // We have already checked that the version is not zero. If it was null, the default
        // version _would_ be zero, therefore we can safely assume that that is not the case
        "%s-%s.ttf".format(packId, version!!.version.joinToString("."))
    } else {
        "%s.ttf".format(packId)
    }
}