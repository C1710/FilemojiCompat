package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.structures.DownloadStatus
import de.c1710.filemojicompat_ui.structures.Version
import okhttp3.Call
import java.io.IOException
import java.net.URL

class DownloadableEmojiPack(
    id: String,
    name: String,
    internal val source: URL,
    description: String,
    private val icon: Drawable?,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null,
    var downloadedVersion: Version? = null
) : FileBasedEmojiPack(id, name, description, version, website, license, descriptionLong) {
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

        status.addListener(object : EmojiPackDownloadListener {
            override fun onProgress(bytesRead: Long, contentLength: Long) {}

            override fun onFailure(e: IOException) {}

            override fun onDone() {
                downloadedVersion = version
            }

        })
    }

    fun getDownloadStatus(): DownloadStatus? = downloadStatus

    fun isDownloaded(): Boolean {
        return downloadedVersion != null
    }

    fun isDownloading(): Boolean {
        return downloadStatus != null && !downloadStatus!!.done
    }

    fun cancelDownload() {
        call?.cancel()
        downloadStatus = null
    }

    override fun getFileName(): String {
        return if (downloadedVersion != null && !downloadedVersion!!.isZero()) {
            "%s-%s.ttf".format(id, downloadedVersion!!.version.joinToString("."))
        } else {
            "%s.ttf".format(id)
        }
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean {
        return downloadedVersion == version
    }

    override fun getIcon(context: Context): Drawable? = icon

    override fun deleteImpl(context: Context, list: EmojiPackList): Int {
        super.deleteImpl(context, list)

        downloadedVersion = null

        return -1
    }
}