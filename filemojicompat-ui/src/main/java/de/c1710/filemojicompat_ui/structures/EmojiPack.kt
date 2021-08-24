package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version
import okhttp3.Call
import java.net.URL

// Picked a number from random.org
const val PICK_EMOJI_FONT = 0x31996763

class EmojiPack(
    var id: String,
    var name: String,
    var source: URL?,
    var description: String,
    // Assume it's svg
    var icon: Drawable?,
    var version: Version?,
    var website: Uri? = null,
    var license: Uri? = null,
    var descriptionLong: String? = null
) {
    private var downloadStatus: DownloadStatus? = null
    private var call: Call? = null

    constructor(
        id: String,
        name: String,
        source: URL?,
        description: String,
        icon: Drawable?,
        version: IntArray?,
        website: Uri? = null,
        license: Uri? = null,
        descriptionLong: String? = null
    ) : this(
        id,
        name,
        source,
        description,
        icon,
        version?.let { Version(it) },
        website,
        license,
        descriptionLong
    )

    constructor(
        id: String,
        name: String,
        source: String?,
        description: String,
        icon: Drawable?,
        version: IntArray?,
        website: String? = null,
        license: String? = null,
        descriptionLong: String? = null
    ) : this(
        id,
        name,
        source?.let { URL(it) },
        description,
        icon,
        version?.let { Version(it) },
        website?.let { Uri.parse(it) },
        license?.let { Uri.parse(it) },
        descriptionLong
    )

    fun select(context: Context) {
        EmojiPreference.setSelected(context, this.id)
    }

    fun download(list: EmojiPackList) {
        val status = DownloadStatus()
        EmojiPackDownloader(this, list)
            .download(status)
        this.downloadStatus = status
    }

    fun getDownloadStatus(): DownloadStatus? = downloadStatus

    fun isDownloaded(list: EmojiPackList): Boolean {
        return this == list.systemDefault
                || this == list.externalFile
                || list.downloadedPacks.containsKey(this.id)
    }

    fun isCurrentVersion(list: EmojiPackList): Boolean {
        return this == list.systemDefault
                || this == list.externalFile
                || list.downloadedPacks[this.id] ?: Version(IntArray(0)) >= this.version ?: Version(IntArray(0))
    }

    fun cancelDownload() {
        call?.cancel()
    }

    fun createFileName(): String {
        return Companion.createFileName(this.id, this.version)
    }



    companion object {
        fun createFileName(packId: String, version: Version?): String {
            return if (!(version ?: Version(IntArray(0))).isZero()) {
                // We have already checked that the version is not zero. If it was null, the default
                // version _would_ be zero, therefore we can safely assume that that is not the case
                "%s-%s.ttf".format(packId, version!!.version.joinToString("."))
            } else {
                "%s.ttf".format(packId)
            }
        }
    }
}