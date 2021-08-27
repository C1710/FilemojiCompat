package de.c1710.filemojicompat_ui.packs

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.graphics.drawable.Drawable
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.structures.EmojiPack

// TODO: This might pose a security issue due to missing certificate/signature checks...
class FontRequestEmojiPack(
    val provider: ProviderInfo,
    packageManager: PackageManager,
): EmojiPack(
    provider.packageName.replace('.', '_').replace('-', '_') + "_" + provider.name,
    provider.name,
    "TODO",
    null,
    null,
    null,
    "TODO looooooong"
) {
    val icon = provider.loadIcon(packageManager)

    override fun load(context: Context, list: EmojiPackList): EmojiCompat.Config {
        // TODO: Signatures?!
        val fontRequest = FontRequest(provider.authority, provider.packageName, DEFAULT_EMOJI_QUERY, ArrayList())
        return FontRequestEmojiCompatConfig(context, fontRequest)
    }

    override fun isCurrentVersion(list: EmojiPackList): Boolean = true
    override fun getIcon(context: Context): Drawable? = icon
}

// TODO: Implement

// Copied from the private fields in DefaultEmojiCompatConfigFactory
private const val INTENT_LOAD_EMOJI_FONT = "androidx.content.action.LOAD_EMOJI_FONT"
private const val DEFAULT_EMOJI_QUERY = "emojicompat-emoji-font"

fun collectFontProviders(context: Context): List<FontRequestEmojiPack> {
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

    val packs = providers
        .map { FontRequestEmojiPack(
            it,
            packageManager
        ) }

    return packs
}