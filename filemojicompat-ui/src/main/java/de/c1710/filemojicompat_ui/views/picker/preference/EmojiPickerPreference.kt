package de.c1710.filemojicompat_ui.views.picker.preference

import android.content.Context
import android.util.AttributeSet
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.LifecycleOwner
import androidx.preference.DialogPreference
import androidx.preference.Preference.SummaryProvider
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EMOJI_PREFERENCE
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackImporter

/**
 * A preference dialog that can be easily integrated with the [androidx.preference] library.
 * @param activity In order to correctly handle emoji pack imports, it needs a surrounding Activity.
 *                 @see [androidx.preference.PreferenceFragmentCompat.requireActivity]
 */
class EmojiPickerPreference<A>(activity: A, attributeSet: AttributeSet? = null): DialogPreference(activity, attributeSet)
        where A : Context, A : ActivityResultRegistryOwner, A : LifecycleOwner {
    val importer = EmojiPackImporter(
        activity.activityResultRegistry,
        EmojiPackList.defaultList!!,
        activity as Context
    )

    init {
        activity.lifecycle.addObserver(importer)

        setDefaultValue(EmojiPreference.getDefault(context))
        summaryProvider = SummaryProvider<EmojiPickerPreference<A>> {
            EmojiPackList.defaultList!![EmojiPreference.getSelected(context)]?.name ?: ""
        }
        key = EMOJI_PREFERENCE

        dialogLayoutResource = R.layout.emoji_picker
    }
}

