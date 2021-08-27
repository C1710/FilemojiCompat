package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import de.c1710.filemojicompat_ui.packs.CustomEmojiPack
import de.c1710.filemojicompat_ui.packs.FilePickerDummyEmojiPack
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.Version
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class EmojiPackList(
    context: Context,
    storageDirectory: String = "emoji",
    private var emojiPacks: ArrayList<EmojiPack>
) {
    internal val size: Int
        get() {
            return emojiPacks.size
        }

    internal val emojiStorage: File = File(context.getExternalFilesDir(null), storageDirectory)

    // Only store the IDs of the downloaded packs
    internal var downloadedVersions: HashMap<String, Version> = HashMap()

    init {
        emojiPacks.add(0, SystemDefaultEmojiPack.getSystemDefaultPack(context))
        loadStoredPacks(context)
        // TODO: First evaluate, whether this is not a security-problem...
        // emojiPacks.addAll(collectFontProviders(context))
        emojiPacks.add(FilePickerDummyEmojiPack.setAndGetFilePickerPack(context))

        EmojiPack.selectedPack = getSelectedPack(context)
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
                        Pair(name, Version.fromString(version))
                    } // Now the actual logic...
                    // FIXME: This looks gross
                    .forEach { entry ->
                        // We distinguish here between custom and downloadable packs
                        // This needs to be done as we don't have any information about the types of packs yet
                        val customName: String? =
                            EmojiPreference.getNameForCustom(context, entry.first)
                        if (customName != null) {
                            // Looks, like it is a custom pack
                            emojiPacks.add(CustomEmojiPack(context, entry.first))
                        } else {
                            downloadedVersions[entry.first] = entry.second
                        }
                    }
            } else {
                Log.e("FilemojiCompat", "Emoji pack storage is not a directory!")
            }
        } else {
            emojiStorage.mkdir()
        }
    }

    private fun getSelectedPack(context: Context): EmojiPack {
        val selection = EmojiPreference.getSelected(context)

        return emojiPacks.find {
            it.id == selection
        } ?: run {
            Log.w("FilemojiCompat", "Selected pack not found; using default")
            getDefaultPack(context)
        }
    }

    internal fun getDefaultPack(context: Context): EmojiPack {
        val default = EmojiPreference.getDefault(context)

        return emojiPacks.find {
            it.id == default
        } ?: run {
            Log.w("FilemojiCompat", "Default pack not found; using system default")
            SystemDefaultEmojiPack.getSystemDefaultPack(context)
        }
    }

    internal fun downloadedVersion(pack: String): Version? {
        return downloadedVersions[pack]
    }

    internal operator fun get(position: Int): EmojiPack {
        return emojiPacks[position]
    }

    internal operator fun get(packId: String): EmojiPack? {
        return this.emojiPacks.firstOrNull { pack -> pack.id == packId }
    }

    internal operator fun contains(pack: EmojiPack): Boolean {
        return pack in emojiPacks
    }

    internal fun indexOf(pack: EmojiPack): Int {
        return this.emojiPacks.indexOf(pack)
    }

    fun addCustomPack(context: Context, hash: String): EmojiPack {
        val newEmojiPack = CustomEmojiPack(context, hash)
        emojiPacks.add(emojiPacks.size - 1, newEmojiPack)
        return newEmojiPack
    }

    fun addPack(pack: EmojiPack, index: Int = size) {
        emojiPacks.add(index, pack)
    }

    fun removePack(pack: EmojiPack) {
        emojiPacks.remove(pack)
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