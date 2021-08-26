package de.c1710.filemojicompat_ui.views.picker

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.CustomEmojiCallback
import de.c1710.filemojicompat_ui.helpers.CustomEmojiHandler
import de.c1710.filemojicompat_ui.helpers.EmojiPackDownloader
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.packs.FilePickerDummyEmojiPack
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

        holder.icon.setImageDrawable(
            item.icon ?:
            ResourcesCompat.getDrawable(
                holder.icon.context.resources,
                R.drawable.ic_custom_emojis,
                holder.icon.context.theme
            )
        )
        holder.name.text = item.name
        holder.description.text = item.description

        holder.item.setOnClickListener {
            transitionCollapsedAndExpanded(holder, holder.expandedItem.visibility == View.GONE)
        }

        // Handle the expanded item
        bindExpandedItem(holder, item)

        val isSelected = item.id == EmojiPreference.getSelected(holder.item.context)
        holder.selection.isChecked = isSelected
        if (isSelected) {
            EmojiPackViewHolder.selectedItem = holder.selection
        }

        if (item is DownloadableEmojiPack) {
            if (item.isDownloading()) {
                setDownloading(holder, item)
            }
            if (item.isDownloaded(dataSet) && item.isCurrentVersion(dataSet)) {
                setAvailable(holder, item)
            } else {
                setDownloadable(holder, item)
            }
        } else if (item is FilePickerDummyEmojiPack) {
            setFilePicker(holder)
        } else {
            // As of now, there are no other special cases. We can mark the pack as somehow available
            setAvailable(holder, item)
        }
    }

    private fun transitionCollapsedAndExpanded(
        holder: EmojiPackViewHolder,
        expand: Boolean
    ) {
        holder.description.visibility = visible(!expand)
        holder.expandedItem.visibility = visible(expand)
    }

    private fun bindExpandedItem(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.expandedItem.visibility = View.GONE

        holder.descriptionLong.text = item.descriptionLong ?: item.description

        holder.version.visibility = visible(item.version != null && !(item.version?.isZero() ?: true))
        holder.version.text = "%s: %s".format(
            holder.version.context.getText(R.string.version),
            item.version?.version?.joinToString(".")
        )

        holder.website.visibility = visible(item.website != null)
        holder.website.setOnClickListener {
            if (item.website != null) {
                // https://stackoverflow.com/a/3004542/5070653
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = item.website
                holder.website.context.startActivity(intent)
            }
        }

        holder.license.visibility = visible(item.license != null)
        holder.license.setOnClickListener {
            if (item.license != null) {
                // https://stackoverflow.com/a/3004542/5070653
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = item.license
                holder.license.context.startActivity(intent)
            }
        }
    }

    private fun visible(isVisible: Boolean): Int {
        return if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onViewRecycled(holder: EmojiPackViewHolder) {
        unbindDownload(holder)
        if (EmojiPackViewHolder.selectedItem == holder.selection) {
            EmojiPackViewHolder.selectedItem = null
        }
        super.onViewRecycled(holder)
    }

    private fun setAvailable(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.selection.visibility = View.VISIBLE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.GONE
        holder.importFile.visibility = View.GONE
        holder.delete.visibility = visible(item.isDeletable())

        holder.selection.setOnClickListener {
            // Well, it selects itself even with this custom onClickListener, so let's undo that
            holder.selection.isChecked = false
            select(holder, item)
        }

        // TODO:
        // For deletion, we want multiple things:
        // 1. Clicking delete should only switch the UI until the Snackbar has expired
        //    - However, what if the user immediately clicks Download again for a DownloadableEmoji?
        // 2. Different behavior for different kinds:
        //    - Custom emoji packs need their complete entry removed, in the UI and later the emoji list
        //    - Downloadable emoji packs only need to be reset to a downloadable state again
        //    - Everything else is not deletable, but what about new types? Default behavior?
    }

    private fun setFilePicker(holder: EmojiPackViewHolder) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.GONE
        holder.importFile.visibility = View.VISIBLE

        holder.importFile.setOnClickListener {
            pickCustomEmoji(holder)
        }
    }

    private fun setDownloading(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.VISIBLE
        holder.cancel.visibility = View.VISIBLE
        holder.download.visibility = View.GONE
        holder.importFile.visibility = View.GONE

        // We are now interested in the progress
        bindToDownload(holder, item)

        holder.cancel.setOnClickListener {
            item.cancelDownload()
            setDownloadable(holder, item)
        }
    }

    private fun bindToDownload(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        // First update the progress bar
        holder.progress.progress = displayedProgress (
            item.getDownloadStatus()?.getBytesRead() ?: 0,
            item.getDownloadStatus()?.getSize() ?: 0,
            holder.progress.max
        )

        val callback = object: EmojiPackDownloader.DownloadCallback {
            override fun onProgress(bytesRead: Long, contentLength: Long) {
                val maxProgress = holder.progress.max
                mainHandler.post {
                    holder.progress.progress = displayedProgress(bytesRead, contentLength, maxProgress)
                }
            }

            override fun onFailure(e: IOException) {
                Log.e("FilemojiCompat", "Download of Emoji Pack failed", e)
                unbindDownload(holder)
            }

            override fun onDone() {
                mainHandler.post {
                    item.select(holder.item.context)
                    setAvailable(holder, item)
                }
                unbindDownload(holder)
            }
        }

        holder.downloadCallback = callback
        item.getDownloadStatus()?.addCallback(callback)
        holder.downloadBoundTo = item.getDownloadStatus()
    }

    private fun displayedProgress(bytesRead: Long, contentLength: Long, maxProgress: Int): Int {
        // Normal, linear progress: (bytesRead * maxProgress / contentLength).toInt()
        // According to https://chrisharrison.net/projects/progressbars/ProgBarHarrison.pdf
        // slightly accelerating progress bars are perceived as faster.
        // Therefore either Power or Fast Power is used. We use Power:
        var progressDouble: Double = ((bytesRead * maxProgress).toDouble() / contentLength)
        progressDouble = (progressDouble + (1 - progressDouble) * 0.03) *
                         (progressDouble + (1 - progressDouble) * 0.03)
        // TODO: Make this behavior optional?
        return progressDouble.toInt()
    }

    private fun unbindDownload(holder: EmojiPackViewHolder) {
        if (holder.downloadCallback != null && holder.downloadBoundTo != null) {
            holder.downloadBoundTo!!.removeCallback(holder.downloadCallback!!)
        }
        holder.downloadCallback = null
        holder.downloadBoundTo = null
    }

    private fun setDownloadable(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.VISIBLE
        holder.importFile.visibility = View.GONE
        holder.delete.visibility = View.GONE

        holder.download.setImageDrawable(ResourcesCompat.getDrawable(
            holder.download.context.resources,
            if (!item.isDownloaded(dataSet)) {
                R.drawable.ic_download
            } else {
                R.drawable.ic_update
            },
            holder.download.context.theme
        ))

        holder.download.setOnClickListener {
            item.download(dataSet)
            setDownloading(holder, item)
        }
    }

    private fun select(holder: EmojiPackViewHolder, item: EmojiPack) {
        EmojiPackViewHolder.selectedItem?.isChecked = false
        EmojiPackViewHolder.selectedItem = holder.selection
        holder.selection.isChecked = true
        item.select(holder.selection.context.applicationContext)
    }

    private fun pickCustomEmoji(holder: EmojiPackViewHolder) {
        // Open the file picker, etc.
        customEmojiHandler.pickCustomEmoji (
            object: CustomEmojiCallback {
                override fun onLoaded(customEmoji: String) {
                    mainHandler.post {
                        // Okay, we have a new emoji.
                        registerCustomEmoji(holder.item, customEmoji)
                    }
                }
            }
        )
    }

    private fun registerCustomEmoji(view: ViewGroup, hash: String) {
        val context = view.context
        val existingPack = dataSet[hash]
        if (existingPack == null) {
            val inflater = LayoutInflater.from(context)
            val dialogLayout = inflater.inflate(R.layout.emoji_pack_naming_dialog, null)
            val inputField: EditText = dialogLayout.findViewById(R.id.emoji_pack_naming)

            // First, we will need to determine a name
            val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.name_custom_emoji)
                .setCancelable(true)
                .setView(dialogLayout)
                .setPositiveButton(R.string.save) { _: DialogInterface, _: Int ->
                    // Store the name
                    val name = inputField.text.trim().toString()
                    EmojiPreference.setNameForCustom(context, name, hash)

                    // Add the new custom emoji pack add select it
                    val newEmojiPack = dataSet.addCustomPack(context, hash)
                    newEmojiPack.select(context)
                    // We will now deselect the current selected item.
                    // When the RecyclerView is notified, it will bind the appropriate ViewHolder
                    // Which will then start out with the Radio Button being checked.
                    EmojiPackViewHolder.selectedItem?.isChecked = false
                    EmojiPackViewHolder.selectedItem = null
                    // The new pack is now at the second to last position
                    notifyItemInserted(dataSet.size - 2)
                }
                .setOnCancelListener {
                    // If we don't want to save it, delete it...
                    val packFile = File(dataSet.emojiStorage, "$hash.ttf")
                    packFile.delete()
                }.create()

            inputField.addTextChangedListener(afterTextChanged = { s: Editable? ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrBlank()
            })

            dialog.show()

            // Initially, we should not be able to save as the name is empty
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !inputField.text.isNullOrBlank()
        } else {
            // Whoops, looks like the pack was already present :S
            // FIXME: How to find the correct ViewHolder to select the pack?!
            // existingPack.select(context)
            Toast.makeText(context, "Pack already imported!", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = dataSet.size

    companion object {
        @JvmStatic
        fun <A> get(activity: A) : EmojiPackItemAdapter
            where A: Context, A: ActivityResultRegistryOwner, A: LifecycleOwner
        {
            val customEmojiHandler = CustomEmojiHandler (
                    activity.activityResultRegistry,
                    EmojiPackList.defaultList!!,
                    activity
                )
            activity.lifecycle.addObserver(customEmojiHandler)
            return EmojiPackItemAdapter(
                EmojiPackList.defaultList!!,
                customEmojiHandler
            )
        }
    }
}