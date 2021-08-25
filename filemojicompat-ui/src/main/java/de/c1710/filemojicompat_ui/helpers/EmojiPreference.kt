package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.c1710.filemojicompat_ui.packs.SYSTEM_DEFAULT

const val SHARED_PREFERENCES = "de.c1710.filemojicompat"
const val EMOJI_PREFERENCE = "de.c1710.filemojicompat.EMOJI_PREFERENCE"
const val CUSTOM_EMOJI = "de.c1710.filemojicompat.CUSTOM_EMOJI"

object EmojiPreference {
    var initialSelection: String? = null
    private var sharedPreferenceName: String? = null

    fun getSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferenceName == null) {
            sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES
        }

        return context
            .getSharedPreferences(sharedPreferenceName!!, Context.MODE_PRIVATE)
    }

    fun getSelected(context: Context): String {
        return try {
            getSharedPreferences(context)
                .getString(EMOJI_PREFERENCE, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT
        } catch (e: ClassCastException) {
            Log.e("FilemojiCompat", "Emoji preference is not a String; using sytem default", e)
            SYSTEM_DEFAULT
        }
    }


    fun setSelected(context: Context, value: String) {
        Log.d("FilemojiCompat", "Switching selected emoji pack to: %s".format(value))
        val prefs = getSharedPreferences(context)

        // First, store the original setting to later determine whether it has been changed
        setInitial(context)

        with (prefs.edit()) {
            putString(EMOJI_PREFERENCE, value)
            apply()
        }

        EmojiPackHelper.reset(context)
    }

    fun getCustom(context: Context): String? {
        return try {
            getSharedPreferences(context)
                .getString(CUSTOM_EMOJI, null)
        } catch (e: ClassCastException) {
            Log.e("FilemojiCompat", "Custom Emoji preference is not a String", e)
            null
        }
    }


    private var customNamesPreferenceName: String? = null

    fun getCustomNamesPreferences(context: Context): SharedPreferences {
        if (customNamesPreferenceName == null) {
            customNamesPreferenceName = context.packageName + "-" + SHARED_PREFERENCES + "-CustomNames"
        }

        return context
            .getSharedPreferences(customNamesPreferenceName!!, Context.MODE_PRIVATE)
    }

    fun getNameForCustom(context: Context, hash: String): String? {
        return getCustomNamesPreferences(context)
            .getString(hash, null)
    }

    fun setNameForCustom(context: Context, name: String, hash: String) {
        val prefs = getCustomNamesPreferences(context)

        with(prefs.edit()) {
            putString(hash, name)
            apply()
        }
    }

    private fun setInitial(context: Context) {
        if(initialSelection == null ) {
            initialSelection = getSelected(context)
        }
    }

    fun hasEmojiPackChanged(context: Context): Boolean {
        return initialSelection != null && getSelected(context) != initialSelection
    }
}