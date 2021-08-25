package de.c1710.filemojicompat_ui.packs

import android.graphics.drawable.Drawable
import android.net.Uri
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.structures.DownloadStatus
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import okhttp3.Call
import java.net.URL

class DownloadableEmojiPack(
    id: String,
    name: String,
    val source: URL,
    description: String,
    icon: Drawable?,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
): FileBasedEmojiPack(id, name, description, icon, version, website, license, descriptionLong) {
    constructor(
        id: String,
        name: String,
        source: URL,
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
        source: String,
        description: String,
        icon: Drawable?,
        version: IntArray?,
        website: String? = null,
        license: String? = null,
        descriptionLong: String? = null
    ) : this(
        id,
        name,
        URL(source),
        description,
        icon,
        version?.let { Version(it) },
        website?.let { Uri.parse(it) },
        license?.let { Uri.parse(it) },
        descriptionLong
    )

    private var call: Call? = null
    private var downloadStatus: DownloadStatus? = null

    fun download(list: EmojiPackList) {
        val status = DownloadStatus()
        EmojiPackDownloader(this, list)
            .download(status)
        this.downloadStatus = status
    }

    fun getDownloadStatus(): DownloadStatus? = downloadStatus

    fun isDownloaded(list: EmojiPackList): Boolean {
        // We assume that an Emoji Pack without a source is always downloaded.
        // At least it _can't_ be downloaded anyway...
        return list.downloadedPacks.containsKey(this.id)
    }

    fun isDownloading(): Boolean {
        return downloadStatus != null && !downloadStatus!!.isDone()
    }

    fun cancelDownload() {
        call?.cancel()
        downloadStatus = null
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean {
        return list.downloadedPacks[this.id] ?: Version(IntArray(0)) >=
                this.version ?: Version(IntArray(0))
    }
}