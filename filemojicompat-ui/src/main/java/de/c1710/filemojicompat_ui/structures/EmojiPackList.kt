package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version
import java.io.File
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

const val SYSTEM_DEFAULT = "emoji_system_default"
const val EXTERNAL_FILE = "emoji_load_external_file"

class EmojiPackList(
    context: Context,
    storageDirectory: String = "emoji",
    private var emojiPacks: ArrayList<EmojiPack>
) {

    val systemDefault: EmojiPack = EmojiPack(
        SYSTEM_DEFAULT,
        context.resources.getString(R.string.system_default),
        null,
        context.resources.getString(R.string.system_default_description),
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_default_emojis, context.theme),
        Version(IntArray(0))
    )

    val externalFile: EmojiPack = EmojiPack(
        EXTERNAL_FILE,
        context.resources.getString(R.string.external_file),
        null,
        context.resources.getString(R.string.external_file_description),
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_file, context.theme),
        Version(IntArray(0)),
        null,
        null,
        context.resources.getString(R.string.external_file_description_long)
    )

    val size: Int
        get() { return emojiPacks.size }
    val emojiStorage: File = File(context.getExternalFilesDir(null), storageDirectory)

    // Only store the IDs of the downloaded packs
    var downloadedPacks: HashMap<String, Version> = HashMap()

    init {
        emojiPacks.add(0, systemDefault)
        loadStoredPacks(context)
        emojiPacks.add(externalFile)
    }

    private fun loadStoredPacks(context: Context) {
        if (emojiStorage.exists()) {
            if (emojiStorage.isDirectory) {
                // This cannot be null as we have already checked that we have a directory
                Arrays.stream(emojiStorage.listFiles()!!)
                    .filter { file: File -> file.extension == "ttf" }
                    .map { file: File -> file.nameWithoutExtension }
                    .map { file: String -> file.split('-', ignoreCase = true, limit = 2) }
                    .map { nameVersion: List<String> ->
                        val name = nameVersion[0]
                        val version = if (nameVersion.size > 1) {
                            nameVersion[1]
                        } else {
                            "0"
                        }
                        Pair(name, parseVersion(version))
                    }
                    // FIXME: This looks gross
                    .forEach { entry ->
                        val customName: String? = EmojiPreference.getNameForCustom(context, entry.first)
                        if (customName != null) {
                            // Looks, like it is a custom pack
                            emojiPacks.add(EmojiPack.customEmoji(context, entry.first))
                        } else {
                            downloadedPacks[entry.first] = entry.second
                        }
                    }
            } else {
                Log.e("FilemojiCompat", "Emoji pack storage is not a directory!")
            }
        } else {
            emojiStorage.mkdir()
        }
    }

    private fun parseVersion(version: String): Version {
       return Version(version.split('.').stream()
            .mapToInt { subVersion: String -> subVersion.toIntOrNull() ?: 0}
           .toArray())
    }

    fun downloadedVersion(pack: String): Version? {
        return downloadedPacks[pack]
    }

    operator fun get(position: Int): EmojiPack {
        return emojiPacks[position]
    }

    operator fun get(packId: String): EmojiPack? {
        return this.emojiPacks.firstOrNull { pack -> pack.id == packId }
    }

    fun indexOf(pack: EmojiPack): Int {
        return this.emojiPacks.indexOf(pack)
    }

    fun addCustomPack(context: Context, hash: String): EmojiPack {
        val newEmojiPack = EmojiPack.customEmoji(context, hash)
        emojiPacks.add(emojiPacks.size - 1, newEmojiPack)
        return newEmojiPack
    }

    private fun customEmojiFile(context: Context): File? {
        val customFileName = EmojiPreference.getCustom(context)
        return if (customFileName != null) {
            File(emojiStorage, "$customFileName.ttf")
        } else {
            null
        }
    }

    companion object {
        @JvmStatic
        var defaultList: EmojiPackList? = null
            set(value) {
                if (field == null) {
                    field = value
                }
            }
    }
}