package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import de.c1710.filemojicompat_ui.structures.SYSTEM_DEFAULT
import java.lang.ClassCastException

const val SHARED_PREFERENCES = "de.c1710.filemojicompat"
const val EMOJI_PREFERENCE = "de.c1710.filemojicompat.EMOJI_PREFERENCE"
const val CUSTOM_EMOJI = "de.c1710.filemojicompat.CUSTOM_EMOJI"

object EmojiPreference {

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
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        val prefs = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
        with (prefs.edit()) {
            putString(EMOJI_PREFERENCE, value)
            apply()
        }
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
        val sharedPreferenceName = context.packageName + "-" + SHARED_PREFERENCES

        val prefs = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
        with (prefs.edit()) {
            putString(CUSTOM_EMOJI, value)
            apply()
        }
    }
}