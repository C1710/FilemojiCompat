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
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.*
import de.c1710.filemojicompat_ui.packs.CustomEmojiPack
import de.c1710.filemojicompat_ui.packs.DeletableEmojiPack
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.packs.FilePickerDummyEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import java.io.File
import java.io.IOException

// From SnackbarManager#LONG_DURATION_MS
const val SNACKBAR_DURATION_LONG: Long = 2750

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

        // These are some basic things that don't change on state transitions
        holder.icon.setImageDrawable(
            item.getIcon(holder.icon.context) ?:
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

        // Now, add a listener for state changes
        registerPackListener(holder, item)

        setState(holder, item)
    }

    /**
     * There are quite a few states:
     * - Available: Pack can be chosen, etc.
     * - Downloadable: Pack can be downloaded
     * - Downloading: Pack is currently being downloaded (Shows a different icon if it's just not the current version)
     * - FilePicker: Entry the file picker entry
     * - Deleting: Entry is waiting for deletion (on the UI it says that it _is_ deleted; this is a lie though)
     * - Deleted: Will directly transition to Downloadable for downloadable packs and to removal for
     *            custom packs
     */
    private fun setState(holder: EmojiPackViewHolder, item: EmojiPack) {
        when {
            item is DeletableEmojiPack && item.isGettingDeleted() -> setDeleting(holder, item)
            item is DownloadableEmojiPack -> {
                when {
                    item.isDownloaded(dataSet) && item.isCurrentVersion(dataSet) -> setAvailable(holder, item)
                    item.isDownloading() -> setDownloading(holder, item)
                    else -> setDownloadable(holder, item)
                }
            }
            item is FilePickerDummyEmojiPack -> setFilePicker(holder)
            else -> setAvailable(holder, item)
        }
    }

    private fun registerPackListener(holder: EmojiPackViewHolder, pack: EmojiPack) {

        val listener = object: EmojiPackListener {
            override fun onSelected(context: Context, pack: EmojiPack) {
                holder.selection.isChecked = true
            }

            override fun onUnSelected(context: Context, pack: EmojiPack) {
                holder.selection.isChecked = false
            }
        }

        holder.packListener = listener
        pack.addListener(listener)

        if (pack is DeletableEmojiPack) {
            val deletionListener = object: EmojiPackDeletionListener {
                override fun onDeleted(context: Context, pack: DeletableEmojiPack, oldIndex: Int) {
                    setDeleted(holder, pack, oldIndex)
                }

                override fun onDeletionScheduled(
                    context: Context,
                    pack: DeletableEmojiPack,
                    timeToDeletion: Long
                ) {

                    // Adjust the UI
                    setDeleting(holder, pack)

                    // Show snackbar
                    Snackbar.make(
                        holder.itemView,
                        // Show the name of the emoji font as well
                        holder.itemView.context.getString(R.string.pack_deleted, pack.name),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.undo) { pack.cancelDeletion(holder.item.context) }
                        .show()
                }

                override fun onDeleteCancelled(context: Context, pack: DeletableEmojiPack) {
                    setAvailable(holder, pack)
                }
            }

            holder.packDeletionListener = deletionListener
            pack.addDeletionListener(deletionListener)
        }
    }

    private fun transitionCollapsedAndExpanded(
        holder: EmojiPackViewHolder,
        expand: Boolean
    ) {
        // TODO: Animate
        holder.description.visibility = visible(!expand)
        holder.expandedItem.visibility = visible(expand)
    }

    private fun bindExpandedItem(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.expandedItem.visibility = View.GONE

        holder.descriptionLong.text = item.descriptionLong ?: item.description

        holder.version.visibility = visible(item.version != null && !(item.version?.isZero() ?: true))
        holder.version.text = holder.version.context.getString(R.string.version).format(
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

    override fun onViewRecycled(holder: EmojiPackViewHolder) {
        unbindDownload(holder)
        holder.packListener?.let { holder.pack?.removeListener(it) }
        holder.packListener = null
        holder.packDeletionListener?.let {
            if (holder.pack is DeletableEmojiPack) {
                (holder.pack as DeletableEmojiPack).removeDeletionListener(it)
            } else {
                Log.wtf("FilemojiCompat", "ViewHolder had deletion listener with non-deletable Emoji Pack")
            }
        }
        holder.packDeletionListener = null
        super.onViewRecycled(holder)
    }

    // STATE TRANSITIONS

    private fun setAvailable(holder: EmojiPackViewHolder, item: EmojiPack) {
        holder.itemView.visibility = View.VISIBLE
        holder.item.visibility = View.VISIBLE
        holder.selection.visibility = View.VISIBLE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.GONE
        holder.importFile.visibility = View.GONE
        holder.delete.visibility = visible(item is DeletableEmojiPack)
        holder.description.visibility = visible(!holder.descriptionLong.isVisible)

        holder.selection.isEnabled = true
        holder.selection.isChecked = item == EmojiPack.selectedPack

        holder.selection.setOnClickListener {
            // Well, it selects itself even with this custom onClickListener, so let's undo that
            holder.selection.isChecked = false
            item.select(holder.itemView.context)
        }

        holder.delete.setOnClickListener {
            if (item is DeletableEmojiPack) {
                item.scheduleDeletion(
                    holder.itemView.context,
                    SNACKBAR_DURATION_LONG,
                    mainHandler,
                    dataSet
                )
            }
        }
    }

    private fun setDeleting(holder: EmojiPackViewHolder, item: DeletableEmojiPack) {
        // FIXME: Looks weird for custom emoji pack
        holder.itemView.visibility = visible(item !is CustomEmojiPack)
        holder.selection.isEnabled = false
        holder.delete.visibility = View.GONE
    }

    private fun setDeleted(holder: EmojiPackViewHolder, item: DeletableEmojiPack, index: Int) {
        when (item) {
            is CustomEmojiPack -> {
                remove(item, index)
            }
            is DownloadableEmojiPack -> {
                setDownloadable(holder, item)
            }
            else -> {
                Log.wtf(
                    "FilemojiCompat",
                    "Deleted pack is neither Custom, nor Downloadable and therefore should not be deletable: %s (Type: %s)"
                        .format(
                            item.id,
                            item::class.qualifiedName
                        )
                )
            }
        }
    }

    private fun setFilePicker(holder: EmojiPackViewHolder) {
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.GONE
        holder.cancel.visibility = View.GONE
        holder.download.visibility = View.GONE
        holder.importFile.visibility = View.VISIBLE
        holder.delete.visibility = View.GONE

        holder.importFile.setOnClickListener {
            pickCustomEmoji(holder)
        }
    }

    private fun setDownloading(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        holder.item.visibility = View.VISIBLE
        holder.selection.visibility = View.GONE
        holder.progress.visibility = View.VISIBLE
        holder.cancel.visibility = View.VISIBLE
        holder.download.visibility = View.GONE
        holder.description.visibility = View.GONE
        holder.importFile.visibility = View.GONE

        // We are now interested in the progress
        bindToDownload(holder, item)

        holder.cancel.setOnClickListener {
            item.cancelDownload()
            setDownloadable(holder, item)
        }
    }

    private fun setDownloadable(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        holder.item.visibility = View.VISIBLE
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

    private fun remove(item: EmojiPack, index: Int) {
        if (index != -1) {
            dataSet.removePack(item)
            notifyItemRemoved(index)
        } else {
            Log.w("FilemojiCompat", "remove: Emoji pack %s was not in the list".format(item.id))
        }
    }


    private fun bindToDownload(holder: EmojiPackViewHolder, item: DownloadableEmojiPack) {
        // First update the progress bar
        holder.progress.progress = displayedProgress (
            item.getDownloadStatus()?.bytesRead ?: 0,
            item.getDownloadStatus()?.size ?: 0,
            holder.progress.max
        )

        val callback = object: EmojiPackDownloader.DownloadListener {
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

        holder.downloadListener = callback
        item.getDownloadStatus()?.addListener(callback)
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
        if (holder.downloadListener != null && holder.downloadBoundTo != null) {
            holder.downloadBoundTo!!.removeListener(holder.downloadListener!!)
        }
        holder.downloadListener = null
        holder.downloadBoundTo = null
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
                    // The new pack is now at the second to last position
                    notifyItemInserted(dataSet.size - 2)
                    newEmojiPack.select(context)
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

private fun visible(isVisible: Boolean): Int {
    return if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}