package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import java.io.File

abstract class FileBasedEmojiPack(
    id: String,
    name: String,
    description: String,
    icon: Drawable?,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
): EmojiPack(
    id, name, description, icon, version, website, license, descriptionLong
) {
    fun createFileName(): String {
        return createFileName(this.id, this.version)
    }

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        val downloadedVersion = list.downloadedVersion(this.id)

        // Here we need the _actual_ version we have
        val fileName = createFileName(this.id, downloadedVersion)
        Log.d("FilemojiCompat", "Loading file based pack: File path: %s".format(fileName))

        val file = File(list.emojiStorage, fileName)
        val config = FileEmojiCompatConfig.init(context, file)

        if (config.fallbackEnabled.get()) {
            Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_LONG).show()
            return DefaultEmojiCompatConfig.create(context) ?: config
        }

        return config
    }

    override fun isDeletable(): Boolean = true
}

fun createFileName(packId: String, version: Version?): String {
    return if (!(version ?: Version(IntArray(0))).isZero()) {
        // We have already checked that the version is not zero. If it was null, the default
        // version _would_ be zero, therefore we can safely assume that that is not the case
        "%s-%s.ttf".format(packId, version!!.version.joinToString("."))
    } else {
        "%s.ttf".format(packId)
    }
}