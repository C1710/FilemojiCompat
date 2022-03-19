package de.c1710.filemojicompat_ui.views.picker.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.helpers.EMOJI_PREFERENCE
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.helpers.EmojiPreference
import de.c1710.filemojicompat_ui.pack_helpers.EmojiPackImporter

// Based on https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec

/**
 * A preference dialog that can be easily integrated with the [androidx.preference] library.
 * @param activity In order to correctly handle emoji pack imports, it needs a surrounding Activity.
 *                 @see [androidx.preference.PreferenceFragmentCompat.requireActivity]
 */
open class EmojiPickerPreference(
    val importer: EmojiPackImporter,
    context: Context,
    attributeSet: AttributeSet? = null): DialogPreference(context, attributeSet) {

    init {
        setDefaultValue(EmojiPreference.getDefault(context))
        summaryProvider = SummaryProvider<EmojiPickerPreference> {
            EmojiPackList.defaultList!![EmojiPreference.getSelected(context)]?.name ?: ""
        }
        key = EMOJI_PREFERENCE
        title = context.resources.getString(R.string.emoji_style)
        icon = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_custom_emojis,
            context.theme)

        dialogLayoutResource = R.layout.emoji_picker_dialog
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index) ?: EmojiPreference.getDefault(context)
    }

    fun refresh() {
        notifyChanged()
    }

    companion object {
        @JvmStatic
        fun <A> get(activity: A, attributeSet: AttributeSet? = null): EmojiPickerPreference
                where A : Context, A : ActivityResultRegistryOwner, A : LifecycleOwner {
            val importer = EmojiPackImporter(
                activity.activityResultRegistry,
                EmojiPackList.defaultList!!,
                activity as Context
            )
            activity.lifecycle.addObserver(importer)

            return EmojiPickerPreference(importer, activity, attributeSet)
        }

        /**
         * A helper method to easily handle emoji preferences.
         * It can be integrated into the PreferenceFragmentCompat.onDisplayPreferenceDialog method.
         * {@code
         *      override fun onDisplayPreferenceDialog(preference: Preference) {
         *          if (!EmojiPickerPreference.onDisplayPreferenceDialog(this, preference)) {
         *              super.onDisplayPreferenceDialog(preference)
         *          }
         *      }
         * }
         *
         * @param fragment The calling fragment which will host the dialog
         * @param preference The preference for which this method has been called.
         *                   May or may not be and EmojiPickerPreference.
         *
         * @return Whether the preference has been handled or not
         */
        @JvmStatic
        fun onDisplayPreferenceDialog(fragment: PreferenceFragmentCompat, preference: Preference?): Boolean {
            return if (preference is EmojiPickerPreference) {
                val callChangeListener = { value: String -> preference.callChangeListener(value) }
                val dialog = EmojiPickerDialogFragment.newInstance(
                    preference.importer,
                    callChangeListener = callChangeListener)
                dialog.show(fragment.parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
                true
            } else {
                false
            }
        }
    }
}

