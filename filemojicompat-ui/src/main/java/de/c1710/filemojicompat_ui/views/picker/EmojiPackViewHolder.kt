package de.c1710.filemojicompat_ui.views.picker

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.structures.DownloadStatus

class EmojiPackViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val icon: ImageView = view.findViewById(R.id.emoji_pack_icon)
    val name: TextView = view.findViewById(R.id.emoji_pack_name)
    val description: TextView = view.findViewById(R.id.emoji_pack_description)
    val download: ImageView = view.findViewById(R.id.emoji_pack_download)
    val selection: RadioButton = view.findViewById(R.id.emoji_pack_selction)
    val cancel: ImageView = view.findViewById(R.id.emoji_pack_cancel)
    val progress: ProgressBar = view.findViewById(R.id.emoji_pack_progress)
    val item: ConstraintLayout = view.findViewById(R.id.emoji_pack_item)

    var downloadCallback: EmojiPackDownloader.DownloadCallback? = null
    var downloadBoundTo: DownloadStatus? = null

    companion object {
        // I promise to always delete that reference again!
        @SuppressLint("StaticFieldLeak")
        var selectedItem: RadioButton? = null
    }
}