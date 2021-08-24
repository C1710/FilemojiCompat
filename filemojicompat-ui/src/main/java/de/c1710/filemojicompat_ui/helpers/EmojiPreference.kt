package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import de.c1710.filemojicompat_ui.structures.CUSTOM_PACK
import de.c1710.filemojicompat_ui.structures.SYSTEM_DEFAULT
import java.lang.ClassCastException

const val SHARED_PREFERENCES = "de.c1710.filemojicompat"
const val EMOJI_PREFERENCE = "de.c1710.filemojicompat.EMOJI_PREFERENCE"
const val CUSTOM_EMOJI = "de.c1710.filemojicompat.CUSTOM_EMOJI"

object EmojiPreference {
    var initialSelection: String? = null
    var initialCustom: String? = null

    fun getSelected(context: Context): String {
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        return try {
            context
                .getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
                .getString(EMOJI_PREFERENCE, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT
        } catch (e: ClassCastException) {
            Log.e("FilemojiCompat", "Emoji preference is not a String; using sytem default", e)
            SYSTEM_DEFAULT
        }
    }


    fun setSelected(context: Context, value: String) {
        Log.d("FilemojiCompat", "Switching selected emoji pack to: %s".format(value))
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        val prefs = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)

        // First, store the original setting to later determine whether it has been changed
        setInitial(context)

        with (prefs.edit()) {
            putString(EMOJI_PREFERENCE, value)
            apply()
        }

        EmojiPackHelper.reset(context)
    }

    fun getCustom(context: Context): String? {
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        return try {
            context
                .getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
                .getString(CUSTOM_EMOJI, null)
        } catch (e: ClassCastException) {
            Log.e("FilemojiCompat", "Custom Emoji preference is not a String", e)
            null
        }
    }

    fun setCustom(context: Context, value: String) {
        Log.d("FilemojiCompat", "Switching custom emoji pack to: %s".format(value))
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        val prefs = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)

        // First, store the original setting to later determine whether it has been changed
        setInitial(context)

        with (prefs.edit()) {
            putString(CUSTOM_EMOJI, value)
            apply()
        }

        EmojiPackHelper.reset(context)
    }

    private fun setInitial(context: Context) {
        if(initialSelection == null ) {
            initialSelection = getSelected(context)
        }
        if(initialCustom == null) {
            initialCustom = getCustom(context)
        }
    }

    private fun hasSelectionChanged(context: Context): Boolean {
        return initialSelection != null && getSelected(context) != initialSelection
    }

    private fun hasCustomChanged(context: Context): Boolean {
        return initialSelection != null && getCustom(context) != initialCustom
    }

    fun hasEmojiPackChanged(context: Context): Boolean {
        return hasSelectionChanged(context)
                || (getSelected(context) == CUSTOM_PACK && hasCustomChanged(context))
    }
}