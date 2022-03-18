package de.c1710.filemojicompat_defaults

import android.content.Context
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version
import java.net.URI

class DefaultEmojiPackList {
    companion object {
        @JvmStatic
        fun get(context: Context): ArrayList<EmojiPack> {
            val blobmoji = DownloadableEmojiPack (
            "blobmoji",
            "Blobmoji",
                URI("https://github.com/C1710/blobmoji/raw/emoji14/fonts/BlobmojiCompat.ttf"),
                context.resources.getString(R.string.blobmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_blobmoji, context.theme),
                Version(intArrayOf(14, 0)),
                Uri.parse("https://github.com/C1710/blobmoji"),
                Uri.parse("https://raw.githubusercontent.com/C1710/blobmoji/emoji14/LICENSE"),
                context.resources.getString(R.string.blobmoji_description_long)
            )

            val noto = DownloadableEmojiPack (
                "noto",
                "Noto Emoji",
                URI("https://android.googlesource.com/platform/external/noto-fonts/+/refs/heads/master/emoji-compat/font/NotoColorEmojiCompat.ttf?format=TEXT"),
                context.resources.getString(R.string.noto_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_noto, context.theme),
                Version(intArrayOf(13, 1)),
                Uri.parse("https://github.com/googlefonts/noto-emoji"),
                Uri.parse("https://github.com/googlefonts/noto-emoji/blob/main/LICENSE"),
                context.resources.getString(R.string.noto_description_long)
            )

            val twemoji = DownloadableEmojiPack (
                "twemoji",
                "Twemoji",
                URI("https://github.com/C1710/twemoji/raw/master/fonts/TwemojiCompat.ttf"),
                context.resources.getString(R.string.twemoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_twemoji, context.theme),
                Version(intArrayOf(13, 1)),
                Uri.parse("https://twemoji.twitter.com/"),
                Uri.parse("https://raw.githubusercontent.com/twitter/twemoji/master/LICENSE-GRAPHICS"),
                context.resources.getString(R.string.twemoji_description_long)
            )

            val openmoji = DownloadableEmojiPack (
                "openmoji",
                "OpenMoji",
                URI("https://github.com/C1710/openmoji/raw/master/fonts/OpenMojiCompat.ttf"),
                context.resources.getString(R.string.openmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_openmoji, context.theme),
                Version(intArrayOf(13, 1)),
                Uri.parse("https://openmoji.org/"),
                Uri.parse("https://raw.githubusercontent.com/hfg-gmuend/openmoji/master/LICENSE.txt"),
                context.resources.getString(R.string.openmoji_description_long)
            )

            // Using alphabetical order here (although Blobmoji would probably stay on top anyway :P)
            return arrayListOf(blobmoji, noto, openmoji, twemoji)
        }
    }
}