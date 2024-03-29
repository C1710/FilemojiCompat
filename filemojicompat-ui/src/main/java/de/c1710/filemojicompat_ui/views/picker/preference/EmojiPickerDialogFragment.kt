package de.c1710.filemojicompat_ui.views.picker.preference

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.DelayedEmojiPreference
import de.c1710.filemojicompat_ui.helpers.EMOJI_PREFERENCE
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackImporter
import de.c1710.filemojicompat_ui.views.picker.EmojiPackItemAdapter

// Based on https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec

open class EmojiPickerDialogFragment private constructor (
    private val importer: EmojiPackImporter,
    private val callChangeListener: (String) -> Boolean = { _ -> true }
) : PreferenceDialogFragmentCompat() {

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val picker: RecyclerView? = view.findViewById(R.id.emoji_picker) as RecyclerView?
        picker?.adapter = EmojiPackItemAdapter(
            EmojiPackList.defaultList,
            importer,
            callChangeListener,
            DelayedEmojiPreference
        )
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (preference is EmojiPickerPreference) {
            if (positiveResult) {
                DelayedEmojiPreference.commitSelection(requireContext())
                (preference as EmojiPickerPreference).refresh()
            } else {
                DelayedEmojiPreference.dismissSelection(requireContext())
            }
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(importer: EmojiPackImporter, key: String = EMOJI_PREFERENCE,
                        callChangeListener: (String) -> Boolean = { _ -> true }): EmojiPickerDialogFragment {
            // From EditTextPreferenceDialogFragmentCompat
            val fragment = EmojiPickerDialogFragment(importer, callChangeListener)
            val bundle = Bundle(2)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}