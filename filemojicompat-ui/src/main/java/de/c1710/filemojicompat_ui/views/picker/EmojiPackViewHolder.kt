package de.c1710.filemojicompat_ui.views.picker

import android.annotation.SuppressLint
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.structures.DownloadStatus

class EmojiPackViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val item: ConstraintLayout = view.findViewById(R.id.emoji_pack_item)
    val icon: ImageView = view.findViewById(R.id.emoji_pack_icon)
    val name: TextView = view.findViewById(R.id.emoji_pack_name)
    val description: TextView = view.findViewById(R.id.emoji_pack_description)
    val download: ImageView = view.findViewById(R.id.emoji_pack_download)
    val selection: RadioButton = view.findViewById(R.id.emoji_pack_selction)
    val cancel: ImageView = view.findViewById(R.id.emoji_pack_cancel)
    val progress: ProgressBar = view.findViewById(R.id.emoji_pack_progress)
    val importFile: ImageView = view.findViewById(R.id.emoji_pack_import)

    val expandedItem: ConstraintLayout = view.findViewById(R.id.emoji_pack_expanded_item)
    val descriptionLong: TextView = view.findViewById(R.id.emoji_pack_description_long)
    val version: TextView = view.findViewById(R.id.emoji_pack_version)
    val website: Button = view.findViewById(R.id.emoji_pack_website)
    val license: Button = view.findViewById(R.id.emoji_pack_license)
    val delete: Button = view.findViewById(R.id.emoji_pack_delete)


    var downloadCallback: EmojiPackDownloader.DownloadCallback? = null
    var downloadBoundTo: DownloadStatus? = null

    companion object {
        // I promise to always delete that reference again!
        @SuppressLint("StaticFieldLeak")
        var selectedItem: RadioButton? = null
    }
}