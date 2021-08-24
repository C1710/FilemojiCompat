package de.c1710.filemojicompat_ui.views.picker

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.CustomEmojiCallback
import de.c1710.filemojicompat_ui.helpers.CustomEmojiHandler
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.structures.CUSTOM_PACK
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import java.io.File
import java.io.IOException

class EmojiPackItemAdapter(
    private val dataSet: EmojiPackList,
    private val customEmojiHandler: CustomEmojiHandler
): RecyclerView.Adapter<EmojiPackViewHolder>() {
    val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EmojiPackViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.emoji_pack_item, viewGroup, false)

        return EmojiPackViewHolder(view)
    }


    override fun onBindViewHolder(holder: EmojiPackViewHolder, position: Int) {
        val item = dataSet[position]

        holder.icon.setImageDrawable(item.icon)
        holder.name.text = item.name
        holder.description.text = item.description
        val isSelected = item.id == EmojiPreference.getSelected(holder.item.context.applicationContext)
        holder.selection.isChecked = isSelected
        if (isSelected) {
            EmojiPackViewHolder.selectedItem = holder.selection
        }

        if (item.isDownloaded(dataSet) && item.isCurrentVersion(dataSet)) {
            setDownloaded(holder, item)
        } else {
            if (item.getDownloadStatus() != null) {
                setDownloading(holder, item)
            } else {
                setDownloadable(holder, item)
            }
        }
    }

    override fun onViewRecycled(holder: EmojiPackViewHolder) {
        unbindDownload(holder)
        if (EmojiPackViewHolder.selectedItem == holder.selection) {
            EmojiPackViewHolder.selectedItem = null
        }
        super.onViewRecycled(holder)
    }

    private fun setDownloaded(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.selection.visibility = View.VISIBLE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.GONE

        holder.item.setOnClickListener {
            select(holder, item)
        }
    }

    private fun setDownloading(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.VISIBLE
        holder.cancel.visibility = View.VISIBLE
        holder.download.visibility = View.GONE

        // We are now interested in the progress
        bindToDownload(holder, item)

        holder.item.setOnClickListener {
            item.cancelDownload()
            setDownloadable(holder, item)
        }
    }

    private fun bindToDownload(holder: EmojiPackViewHolder, item: EmojiPack) {
        // First update the progress bar
        holder.progress.progress = (((item.getDownloadStatus()?.getProgress()) ?: 0.0) * holder.progress.max.toDouble()).toInt()

        val callback = object: EmojiPackDownloader.DownloadCallback {
            override fun onProgress(bytesRead: Long, contentLength: Long) {
                val maxProgress = holder.progress.max
                mainHandler.post {
                    holder.progress.progress = (bytesRead * maxProgress / contentLength).toInt()
                }
            }

            override fun onFailure(e: IOException) {
                Log.e("FilemojiCompat", "Download of Emoji Pack failed", e)
                unbindDownload(holder)
            }

            override fun onDone() {
                mainHandler.post {
                    setDownloaded(holder, item)
                }
                unbindDownload(holder)
            }
        }

        holder.downloadCallback = callback
        item.getDownloadStatus()?.addCallback(callback)
        holder.downloadBoundTo = item.getDownloadStatus()
    }

    private fun unbindDownload(holder: EmojiPackViewHolder) {
        if (holder.downloadCallback != null && holder.downloadBoundTo != null) {
            holder.downloadBoundTo!!.removeCallback(holder.downloadCallback!!)
        }
        holder.downloadCallback = null
        holder.downloadBoundTo = null
    }

    private fun setDownloadable(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.VISIBLE
        holder.download.setImageDrawable(ResourcesCompat.getDrawable(
            holder.download.context.resources,
            if (!item.isDownloaded(dataSet)) {
                R.drawable.ic_download
            } else {
                R.drawable.ic_update
            },
            holder.download.context.theme
        ))

        holder.item.setOnClickListener {
            item.download(dataSet)
            setDownloading(holder, item)
        }
    }

    private fun select(holder: EmojiPackViewHolder, item: EmojiPack) {
        if (item.id == CUSTOM_PACK) {
            // Open the file picker, etc.
            customEmojiHandler.pickCustomEmoji (
                object: CustomEmojiCallback {
                    override fun onLoaded(customEmoji: String) {
                        mainHandler.post {
                            EmojiPackViewHolder.selectedItem?.isChecked = false
                            EmojiPackViewHolder.selectedItem = holder.selection
                            holder.selection.isChecked = true
                            item.select(holder.selection.context.applicationContext)
                            EmojiPreference.setCustom(holder.item.context, customEmoji)
                        }
                    }
                }
            )
            // We'll only switch if the selection was successful!
        } else {
            EmojiPackViewHolder.selectedItem?.isChecked = false
            EmojiPackViewHolder.selectedItem = holder.selection
            holder.selection.isChecked = true
            item.select(holder.selection.context.applicationContext)
        }

        // Show a dialog to restart the app to apply the changes
    }


    override fun getItemCount(): Int = dataSet.size
}