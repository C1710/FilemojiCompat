package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version

// TODO: This might pose a security issue due to missing certificate/signature checks...
/**
 * An emoji pack that is based on the [FontRequestEmojiCompatConfig].
 * It can either be created manually with a [FontRequest], with a [ProviderInfo] or automatically retrieved through [collectFontProviders].
 * @param request The font request to use as a source
 * @param id A unique name for the pack. It will be used internally at different places
 *           (e.g. when setting the preference or determining the file name). It should never change
 *           or get translated, etc.
 * @param name The user-facing name of the pack. May be translated or changed
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
class FontRequestEmojiPack(
    private var request: FontRequest,
    id: String,
    name: String,
    description: String,
    private val icon: Drawable?,
    version: Version? = null,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String
) : EmojiPack(
    id,
    name,
    description,
    version,
    website,
    license,
    descriptionLong
) {

    /**
     * Note: This might not work due to missing signatures/certificates
     */
    constructor(
        provider: ProviderInfo,
        packageManager: PackageManager,
        id: String = provider.packageName.replace('.', '_').replace('-', '_') + "_" + provider.name,
        name: String = provider.name,
        description: String,
        icon: Drawable? = provider.loadIcon(packageManager),
        version: Version? = null,
        website: Uri? = null,
        license: Uri? = null,
        descriptionLong: String
    ): this(
        FontRequest(provider.authority, provider.packageName, DEFAULT_EMOJI_QUERY, ArrayList()),
        id, name, description, icon, version, website, license, descriptionLong
    )

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        // TODO: Signatures?!
        return FontRequestEmojiCompatConfig(context, request).setReplaceAll(true)
    }

    override fun getIcon(context: Context): Drawable? = icon
}

// Copied from the private fields in DefaultEmojiCompatConfigFactory
private const val INTENT_LOAD_EMOJI_FONT = "androidx.content.action.LOAD_EMOJI_FONT"
private const val DEFAULT_EMOJI_QUERY = "emojicompat-emoji-font"

// FIXME: This probably won't work
/**
 * Collects all possible font providers to create [FontRequestEmojiPack]s.
 * However, right now there may be issues with signatures/certificates.
 */
private fun collectFontProviders(context: Context): List<FontRequestEmojiPack> {
    val packageManager = context.packageManager
    val intent = Intent(INTENT_LOAD_EMOJI_FONT)

    // Follows DefaultEmojiCompatConfigHelper_API19#queryIntentContentProviders
    val providers = packageManager
        .queryIntentContentProviders(intent, 0)
        .map { it.providerInfo }
        // We do not want to see system installed providers here, because that's just most likely
        // the normal, system default one
        // TODO: The only issue is - I don't know another provider!
        .filter { it.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM }

    return providers
        .map {
            FontRequestEmojiPack(
                it,
                packageManager,
                descriptionLong = TODO(),
                description = TODO()
            )
        }
}