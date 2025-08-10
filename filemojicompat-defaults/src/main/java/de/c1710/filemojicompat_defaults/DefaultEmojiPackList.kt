package de.c1710.filemojicompat_defaults

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.versions.Version
import androidx.core.net.toUri

class DefaultEmojiPackList {
    companion object {
        // TODO: Make all icons have the same size
        @JvmStatic
        fun get(context: Context): ArrayList<EmojiPack> {
            val blobmoji = DownloadableEmojiPack (
            "blobmoji",
            "Blobmoji",
                "https://github.com/C1710/blobmoji/raw/main/fonts/BlobmojiCompat.ttf".toUri(),
                context.resources.getString(R.string.blobmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_blobmoji, null),
                Version(intArrayOf(15, 0)),
                "https://github.com/C1710/blobmoji".toUri(),
                "https://raw.githubusercontent.com/C1710/blobmoji/emoji14/LICENSE".toUri(),
                context.resources.getString(R.string.blobmoji_description_long),
                tintableIcon = false
            )

            val fluent = DownloadableEmojiPack (
                "fluent",
                context.resources.getString(R.string.fluent_name),
                "https://github.com/C1710/fluentui-emoji/raw/main/fonts/FluentEmojiCompat.ttf".toUri(),
                context.resources.getString(R.string.fluent_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_fluent, null),
                Version(intArrayOf(14, 0)),
                "https://github.com/microsoft/fluentui-emoji".toUri(),
                "https://github.com/microsoft/fluentui-emoji/blob/main/LICENSE".toUri(),
                context.resources.getString(R.string.fluent_description_long),
                tintableIcon = false
            )

            val noto = DownloadableEmojiPack (
                "noto",
                "Noto Emoji",
                "https://github.com/googlefonts/noto-emoji/raw/refs/heads/main/fonts/Noto-COLRv1-emojicompat.ttf".toUri(),
                context.resources.getString(R.string.noto_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_noto, null),
                Version(intArrayOf(16, 0)),
                "https://github.com/googlefonts/noto-emoji".toUri(),
                "https://github.com/googlefonts/noto-emoji/blob/main/LICENSE".toUri(),
                context.resources.getString(R.string.noto_description_long),
                tintableIcon = false
            )

            val twemoji = DownloadableEmojiPack (
                "twemoji",
                "Twemoji",
                "https://github.com/C1710/twemoji/raw/master/fonts/TwemojiCompat.ttf".toUri(),
                context.resources.getString(R.string.twemoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_twemoji, null),
                Version(intArrayOf(14, 0)),
                "https://twemoji.twitter.com/".toUri(),
                "https://raw.githubusercontent.com/twitter/twemoji/master/LICENSE-GRAPHICS".toUri(),
                context.resources.getString(R.string.twemoji_description_long),
                tintableIcon = false
            )

            val openmoji = DownloadableEmojiPack (
                "openmoji",
                "OpenMoji",
                "https://github.com/C1710/openmoji/raw/master/fonts/OpenMojiCompat.ttf".toUri(),
                context.resources.getString(R.string.openmoji_description),
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_openmoji, null),
                Version(intArrayOf(14, 0, 2)),
                "https://openmoji.org/".toUri(),
                "https://raw.githubusercontent.com/hfg-gmuend/openmoji/master/LICENSE.txt".toUri(),
                context.resources.getString(R.string.openmoji_description_long),
                tintableIcon = false
            )

            // Using alphabetical order here
            return arrayListOf(blobmoji, noto, openmoji, twemoji, fluent)
        }
    }
}