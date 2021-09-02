package de.c1710.filemojicompat_ui.views.picker

import android.content.Context
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EMOJI_PREFERENCE
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference

class EmojiPickerPreference<A>(private val activity: A): Preference(activity)
        where A : Context, A : ActivityResultRegistryOwner, A : LifecycleOwner {

    init {
        setDefaultValue(EmojiPreference.getDefault(context))
        summaryProvider = SummaryProvider<EmojiPickerPreference<A>> {
            EmojiPackList.defaultList!![EmojiPreference.getSelected(context)]?.name ?: ""
        }
        key = EMOJI_PREFERENCE
    }

    override fun onClick() {
        super.onClick()

        val layout = LayoutInflater.from(context).inflate(R.layout.emoji_picker, null)

        val recyclerView: RecyclerView = layout.findViewById(R.id.emoji_picker)

        recyclerView.adapter = EmojiPackItemAdapter.get(activity, this::callChangeListener)

        AlertDialog.Builder(context)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }
}