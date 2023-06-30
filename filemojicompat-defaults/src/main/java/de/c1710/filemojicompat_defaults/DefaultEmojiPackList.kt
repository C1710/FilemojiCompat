package de.c1710.filemojicompat_defaults

import android.content.Context
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version

class DefaultEmojiPackList {
    companion object {
        // TODO: Make all icons have the same size
        @JvmStatic
        fun get(context: Context): ArrayList<EmojiPack> {
            val blobmoji = DownloadableEmojiPack (
            "blobmoji",
            "Blobmoji",
                Uri.parse("https://github.com/C1710/blobmoji/raw/main/fonts/BlobmojiCompat.ttf"),
                context.resources.getString(R.string.blobmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_blobmoji, null),
                Version(intArrayOf(15, 0)),
                Uri.parse("https://github.com/C1710/blobmoji"),
                Uri.parse("https://raw.githubusercontent.com/C1710/blobmoji/emoji14/LICENSE"),
                context.resources.getString(R.string.blobmoji_description_long),
                tintableIcon = false
            )

            val fluent = DownloadableEmojiPack (
                "fluent",
                context.resources.getString(R.string.fluent_broken_name),
                Uri.parse("https://github.com/C1710/fluentui-emoji/raw/main/fonts/FluentEmojiCompat.ttf"),
                context.resources.getString(R.string.fluent_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_fluent, null),
                Version(intArrayOf(14, 0)),
                Uri.parse("https://github.com/microsoft/fluentui-emoji"),
                Uri.parse("https://github.com/microsoft/fluentui-emoji/blob/main/LICENSE"),
                context.resources.getString(R.string.fluent_description_long),
                tintableIcon = false
            )

            val noto = DownloadableEmojiPack (
                "noto",
                "Noto Emoji",
                Uri.parse("https://github.com/C1710/noto-fonts/raw/master/emoji-compat/font/NotoColorEmojiCompat.ttf"),
                context.resources.getString(R.string.noto_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_noto, null),
                Version(intArrayOf(15, 0)),
                Uri.parse("https://github.com/googlefonts/noto-emoji"),
                Uri.parse("https://github.com/googlefonts/noto-emoji/blob/main/LICENSE"),
                context.resources.getString(R.string.noto_description_long),
                tintableIcon = false
            )

            val twemoji = DownloadableEmojiPack (
                "twemoji",
                "Twemoji",
                Uri.parse("https://github.com/C1710/twemoji/raw/master/fonts/TwemojiCompat.ttf"),
                context.resources.getString(R.string.twemoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_twemoji, null),
                Version(intArrayOf(14, 0)),
                Uri.parse("https://twemoji.twitter.com/"),
                Uri.parse("https://raw.githubusercontent.com/twitter/twemoji/master/LICENSE-GRAPHICS"),
                context.resources.getString(R.string.twemoji_description_long),
                tintableIcon = false
            )

            val openmoji = DownloadableEmojiPack (
                "openmoji",
                "OpenMoji",
                Uri.parse("https://github.com/C1710/openmoji/raw/master/fonts/OpenMojiCompat.ttf"),
                context.resources.getString(R.string.openmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_openmoji, null),
                Version(intArrayOf(14, 0, 2)),
                Uri.parse("https://openmoji.org/"),
                Uri.parse("https://raw.githubusercontent.com/hfg-gmuend/openmoji/master/LICENSE.txt"),
                context.resources.getString(R.string.openmoji_description_long),
                tintableIcon = false
            )

            // Using alphabetical order here
            return arrayListOf(blobmoji, noto, openmoji, twemoji, fluent)
        }
    }
}