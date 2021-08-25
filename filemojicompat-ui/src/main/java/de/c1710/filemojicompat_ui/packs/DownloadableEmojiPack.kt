package de.c1710.filemojicompat_ui.packs

import android.graphics.drawable.Drawable
import android.net.Uri
import de.c1710.filemojicompat_ui.helpers.Version
import de.c1710.filemojicompat_ui.structures.EmojiPack
import java.net.URL

class DownloadableEmojiPack(
    id: String,
    name: String,
    source: URL,
    description: String,
    icon: Drawable?,
    version: Version?,
    website: Uri? = null,
    license: Uri? = null,
    descriptionLong: String? = null
): EmojiPack(id, name, source, description, icon, version, website, license, descriptionLong) {
}