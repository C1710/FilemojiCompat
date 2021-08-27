package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.structures.DownloadStatus
import okhttp3.Call
import java.io.IOException
import java.net.URL

class DownloadableEmojiPack(
    id: String,
    name: String,
    val source: URL,
    description: String,
    private val icon: Drawable?,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
): FileBasedEmojiPack(id, name, description, version, website, license, descriptionLong) {
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

        status.addListener(object: EmojiPackDownloader.DownloadListener {
            override fun onProgress(bytesRead: Long, contentLength: Long) {}

            override fun onFailure(e: IOException) {}

            override fun onDone() {
                version?.let { list.downloadedVersions[id] = version!! }
            }

        })
    }

    fun getDownloadStatus(): DownloadStatus? = downloadStatus

    fun isDownloaded(list: EmojiPackList): Boolean {
        // We assume that an Emoji Pack without a source is always downloaded.
        // At least it _can't_ be downloaded anyway...
        return list.downloadedVersions.containsKey(this.id)
    }

    fun isDownloading(): Boolean {
        return downloadStatus != null && !downloadStatus!!.done
    }

    fun cancelDownload() {
        call?.cancel()
        downloadStatus = null
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean {
        return list.downloadedVersions[this.id] ?: Version(IntArray(0)) >=
                this.version ?: Version(IntArray(0))
    }

    override fun getIcon(context: Context): Drawable? = icon

    override fun deleteImpl(context: Context, list: EmojiPackList): Int {
        super.deleteImpl(context, list)

        if (this.id in list.downloadedVersions) {
            list.downloadedVersions.remove(this.id)
        }

        return -1
    }
}