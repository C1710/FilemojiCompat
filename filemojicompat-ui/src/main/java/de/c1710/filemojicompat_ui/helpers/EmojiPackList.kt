package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import de.c1710.filemojicompat_ui.packs.CustomEmojiPack
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.packs.FilePickerDummyEmojiPack
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version
import java.io.File

class EmojiPackList(
    context: Context,
    storageDirectory: String = "emoji",
    private var emojiPacks: ArrayList<EmojiPack>
): Iterable<EmojiPack> {
    internal val size: Int
        get() {
            return emojiPacks.size
        }

    internal val emojiStorage: File = File(context.getExternalFilesDir(null), storageDirectory)

    init {
        emojiPacks.add(0, SystemDefaultEmojiPack.getSystemDefaultPack(context))
        loadStoredPacks(context)
        // TODO: First evaluate, whether this is a security-problem/possible with signatures...
        // emojiPacks.addAll(collectFontProviders(context))
        emojiPacks.add(FilePickerDummyEmojiPack.setAndGetFilePickerPack(context))

        EmojiPack.selectedPack = getSelectedPack(context)
    }

    private fun loadStoredPacks(context: Context) {
        if (emojiStorage.exists()) {
            if (emojiStorage.isDirectory) {
                // This cannot be null as we have already checked that we have a directory
                emojiStorage.listFiles()!!.asSequence()
                    .filter { file: File -> file.extension == "ttf" }
                    .map { file: File -> file.nameWithoutExtension }
                    // Format: name-ver.sion.co.de
                    .map { file: String -> file.split('-', ignoreCase = true, limit = 2) }
                    .map { nameVersion: List<String> ->
                        val name = nameVersion[0]
                        val version = nameVersion.getOrNull(1)
                        Pair(name, Version.fromStringOrNull(version))
                    } // We now have a Pair with the name and the version (or null if no version is given)
                    .forEach { entry ->
                        // We distinguish here between custom and downloadable packs
                        // Unfortunately, we don't store type information here.
                        // But we store the names of custom packs, so if there is one, we can assume
                        // that it is a custom pack
                        val customName: String? =
                            EmojiPreference.getNameForCustom(context, entry.first)
                        if (customName != null) {
                            // Looks, like it is a custom pack
                            emojiPacks.add(CustomEmojiPack(context, entry.first))
                        } else {
                            // Okay, it's a downloaded pack. We can now add the actually downloaded
                            // version
                            val existingEntry = get(entry.first)
                            if(existingEntry != null) {
                                // it is a downloadable pack
                                if (existingEntry is DownloadableEmojiPack) {
                                    Log.d("FilemojiCompat", "Updating downloaded version for %s: %s".format(existingEntry, entry.second))
                                    existingEntry.downloadedVersion = entry.second
                                } else {
                                    Log.w(
                                        "FilemojiCompat", "loadStoredPacks: stored pack %s is " +
                                                "neither a custom pack (at least without a name), nor a Downloadable pack, but %s"
                                                    .format(entry.first, existingEntry::class)
                                    )
                                }
                            } else {
                                Log.w("FilemojiCompat", "loadStoredPacks: Unknown pack: %s".format(entry.first))
                            }
                        }
                    }
            } else {
                Log.e("FilemojiCompat", "Emoji pack storage is not a directory!")
            }
        } else {
            emojiStorage.mkdir()
            Log.i("FilemojiCompat", "loadStoredPacks: Emoji storage does not exist; creating it")
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

    internal operator fun get(position: Int): EmojiPack {
        return emojiPacks[position]
    }

    operator fun get(packId: String): EmojiPack? {
        return this.emojiPacks.firstOrNull { pack -> pack.id == packId }
    }

    operator fun contains(pack: EmojiPack): Boolean {
        return pack in emojiPacks
    }

    fun indexOf(pack: EmojiPack): Int {
        return this.emojiPacks.indexOf(pack)
    }

    fun packIds(): List<String> {
        return emojiPacks
            .map { pack -> pack.id }
    }

    /**
     * Creates a Custom/imported emoji pack based on the given hash, adds it to the list and returns
     * it.
     * @param hash The hash for the file containing the pack
     * @return The pack that has been created and added
     */
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

    override fun iterator(): Iterator<EmojiPack> {
        return emojiPacks.iterator()
    }

    companion object {
        /**
         * The default/"Singleton" emoji pack list. Can only be set once
         */
        @JvmStatic
        var defaultList: EmojiPackList? = null
            set(value) {
                if (field == null) {
                    field = value
                }
            }
    }
}