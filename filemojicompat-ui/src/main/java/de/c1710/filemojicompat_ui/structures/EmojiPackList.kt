package de.c1710.filemojicompat_ui.structures

import android.content.Context
import android.util.Log
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.packs.CustomEmojiPack
import de.c1710.filemojicompat_ui.packs.FilePickerDummyEmojiPack
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class EmojiPackList(
    context: Context,
    storageDirectory: String = "emoji",
    private var emojiPacks: ArrayList<EmojiPack>
) {
    val size: Int
        get() { return emojiPacks.size }
    val emojiStorage: File = File(context.getExternalFilesDir(null), storageDirectory)

    // Only store the IDs of the downloaded packs
    var downloadedPacks: HashMap<String, Version> = HashMap()

    init {
        emojiPacks.add(0, SystemDefaultEmojiPack.setAndGetSystemDefaultPack(context))
        loadStoredPacks(context)
        emojiPacks.add(FilePickerDummyEmojiPack.setAndGetFilePickerPack(context))
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
                            emojiPacks.add(CustomEmojiPack(context, entry.first))
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
        val newEmojiPack = CustomEmojiPack(context, hash)
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