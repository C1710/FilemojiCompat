package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.FileEmojiCompatConfig
import de.c1710.filemojicompat.FileMetadataRepoLoader
import de.c1710.filemojicompat.ReplaceStrategy
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version
import de.c1710.filemojicompat_ui.versions.VersionProvider
import java.io.File

/**
 * An emoji pack that is present as a ttf in the src/main/assets directory of the module.
 * The id will be "Asset-EmojiPack-pack.ttf" (with "pack.ttf" being the assetPath, with '/' replaced by '_')
 * For the parameters, cf. [EmojiPack]
 */
class AssetEmojiPack(
    private val assetPath: String = FileMetadataRepoLoader.DEFAULT_FALLBACK,
    name: String,
    description: String,
    private val icon: Drawable?,
    version: VersionProvider? = Version(IntArray(0)),
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
) : EmojiPack(
    "Asset-EmojiPack-%s".format(assetPath.replace('/', '_')),
    name, description, version, website, license, descriptionLong
) {
    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        // By using an empty file name, we force FileEmojiCompat to load the asset/fallback
        return FileEmojiCompatConfig.init(context, File(""), assetPath, true)
            .setReplaceAll(ReplaceStrategy.ALWAYS)
    }

    override fun getIcon(context: Context): Drawable? = this.icon
}