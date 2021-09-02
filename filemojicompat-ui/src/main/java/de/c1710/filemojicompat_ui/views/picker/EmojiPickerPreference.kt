package de.c1710.filemojicompat_ui.views.picker

import android.content.Context
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.LifecycleOwner
import androidx.preference.DialogPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EMOJI_PREFERENCE
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference

/**
 * A preference dialog that can be easily integrated with the [androidx.preference] library.
 * @param activity In order to correctly handle emoji pack imports, it needs a surrounding Activity.
 *                 @see [androidx.preference.PreferenceFragmentCompat.requireActivity]
 */
class EmojiPickerPreference<A>(private val activity: A): DialogPreference(activity)
        where A : Context, A : ActivityResultRegistryOwner, A : LifecycleOwner {

    init {
        setDefaultValue(EmojiPreference.getDefault(context))
        summaryProvider = SummaryProvider<EmojiPickerPreference<A>> {
            EmojiPackList.defaultList!![EmojiPreference.getSelected(context)]?.name ?: ""
        }
        key = EMOJI_PREFERENCE

        dialogLayoutResource = R.layout.emoji_picker
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        val picker: RecyclerView? = holder?.findViewById(R.id.emoji_picker) as RecyclerView?
        picker?.adapter = EmojiPackItemAdapter.get(activity, this::callChangeListener)
    }
}