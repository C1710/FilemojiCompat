package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.structures.DownloadStatus
import de.c1710.filemojicompat_ui.versions.Version
import de.c1710.filemojicompat_ui.versions.VersionProvider
import okhttp3.Call
import java.io.File
import java.io.IOException
import java.net.URI

/**
 * An emoji pack that can be downloaded through the UI.
 * These packs are (usually) manually created and added to the [EmojiPackList] to offer a set of
 * options to choose from as a used.
 * @param id A unique name for the pack. It will be used internally at different places
 *           (e.g. when setting the preference or determining the file name). It should never change
 *           or get translated, etc.
 * @param name The user-facing name of the pack. May be translated or changed
 * @param source The URL to download the TTF-file from.
 *               For files from googlesource.com, the downloaded file will be Base64-decoded.
 * @param description The (short) user-facing description that is visible in the normal emoji picker
 * @param icon An icon for the pack, e.g. an emoji from it or the logo
 * @param version The current version of the pack. It needs to be changed to a larger value, when the
 *                font should be updated. May be retrieved from the web, see [de.c1710.filemojicompat_ui.structures.VersionOnline.versionOnline]
 * @param website The URL of the website/repository for the emoji pack
 * @param license The URL of the license for the emoji pack
 *                (This might be auto-downloaded in the future, so it should point to a rather small/plaintext file, if possible)
 * @param descriptionLong A longer description that is shown when the user expands the item for the emoji pack.
 *                        It may contain additional information like a copyright notice, etc.
 */
class DownloadableEmojiPack(
    id: String,
    name: String,
    internal val source: URI,
    description: String,
    private val icon: Drawable?,
    version: VersionProvider?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
) : FileBasedEmojiPack(id, name, description, version, website, license, descriptionLong) {
    var downloadedVersion: Version? = null

    /**
     * @param id A unique name for the pack. It will be used internally at different places
     *           (e.g. when setting the preference or determining the file name). It should never change
     *           or get translated, etc.
     * @param name The user-facing name of the pack. May be translated or changed
     * @param source The URL to download the TTF-file from.
     *               For files from googlesource.com, the downloaded file will be Base64-decoded.
     * @param description The (short) user-facing description that is visible in the normal emoji picker
     * @param icon An icon for the pack, e.g. an emoji from it or the logo
     * @param version The current version of the pack. It needs to be changed to a larger value, when the
     *                font should be updated. May be retrieved from the web, see [de.c1710.filemojicompat_ui.structures.VersionOnline.versionOnline]
     * @param website The URL of the website/repository for the emoji pack
     * @param license The URL of the license for the emoji pack
     *                (This might be auto-downloaded in the future, so it should point to a rather small/plaintext file, if possible)
     * @param descriptionLong A longer description that is shown when the user expands the item for the emoji pack.
     *                        It may contain additional information like a copyright notice, etc.
     */
    constructor(
        id: String,
        name: String,
        source: URI,
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

    /**
     * @param id A unique name for the pack. It will be used internally at different places
     *           (e.g. when setting the preference or determining the file name). It should never change
     *           or get translated, etc.
     * @param name The user-facing name of the pack. May be translated or changed
     * @param source The URL to download the TTF-file from.
     *               For files from googlesource.com, the downloaded file will be Base64-decoded.
     * @param description The (short) user-facing description that is visible in the normal emoji picker
     * @param icon An icon for the pack, e.g. an emoji from it or the logo
     * @param version The current version of the pack. It needs to be changed to a larger value, when the
     *                font should be updated. May be retrieved from the web, see [de.c1710.filemojicompat_ui.structures.VersionOnline.versionOnline]
     * @param website The URL of the website/repository for the emoji pack
     * @param license The URL of the license for the emoji pack
     *                (This might be auto-downloaded in the future, so it should point to a rather small/plaintext file, if possible)
     * @param descriptionLong A longer description that is shown when the user expands the item for the emoji pack.
     *                        It may contain additional information like a copyright notice, etc.
     */
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
        URI(source),
        description,
        icon,
        version?.let { Version(it) },
        website?.let { Uri.parse(it) },
        license?.let { Uri.parse(it) },
        descriptionLong
    )

    private var call: Call? = null
    private var downloadStatus: DownloadStatus? = null

    /**
     * Downloads the pack
     * @param emojiStorage The directory to download the pack into (usually retrieved through [EmojiPackList.emojiStorage])
     */
    fun download(emojiStorage: File) {
        val status = DownloadStatus()
        EmojiPackDownloader(this, emojiStorage)
            .download(status)
        this.downloadStatus = status

        status.addListener(object : EmojiPackDownloadListener {
            override fun onProgress(bytesRead: Long, contentLength: Long) {}

            override fun onFailure(e: IOException) {
                downloadStatus = null
            }
            override fun onCancelled() {
                downloadStatus = null
            }

            override fun onDone() {
                val oldFile = File(emojiStorage, getFileName())
                downloadedVersion = getVersion()
                oldFile.delete()
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
        downloadStatus?.onCancelled()
    }

    override fun getFileName(): String {
        return getFileName(true)
    }

    /**
     * Returns the file name (i.e. font.ttf or with a version) for the font.
     * @param forDownloadedVersion Whether or not to use [downloadedVersion] as the version to generate
     *                             the file name (otherwise, [version] is used).
     *                             The file for the downloaded version should exist (in some directory).
     */
    fun getFileName(forDownloadedVersion: Boolean = true): String {
        val versionForFileName = if (forDownloadedVersion) {
            downloadedVersion
        } else {
            getVersion()
        }

        return if (versionForFileName != null && !versionForFileName.isZero()) {
            "%s-%s.ttf".format(id, versionForFileName.toString())
        } else {
            "%s.ttf".format(id)
        }
    }

    /**
     * Returns whether the version currently downloaded is the current one.
     */
    fun isCurrentVersion(): Boolean {
        Log.d("FilemojiCompat", "isCurrentVersion: %s, %s".format( downloadedVersion, getVersion()))
        return (downloadedVersion ?: Version(IntArray(0))) >= (getVersion() ?: Version(IntArray(0)))
    }

    override fun getIcon(context: Context): Drawable? = icon

    override fun deleteImpl(context: Context, list: EmojiPackList): Int {
        super.deleteImpl(context, list)

        downloadedVersion = null

        return -1
    }
}