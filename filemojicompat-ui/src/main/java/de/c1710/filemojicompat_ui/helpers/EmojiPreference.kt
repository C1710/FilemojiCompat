package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.c1710.filemojicompat_ui.interfaces.EmojiPreferenceInterface
import de.c1710.filemojicompat_ui.packs.SYSTEM_DEFAULT

const val SHARED_PREFERENCES = "de.c1710.filemojicompat"
const val EMOJI_PREFERENCE = "de.c1710.filemojicompat.EMOJI_PREFERENCE"
const val DEFAULT_PREFERENCE = "de.c1710.filemojicompat.DEFAULT_EMOJI_PACK"

/**
 * An easy-to-use helper for dealing with storing and retrieving emoji pack preferences.
 * @see EmojiPreferenceInterface
 */
object EmojiPreference: EmojiPreferenceInterface {
    private var sharedPreferenceName: String? = null

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context
            .getSharedPreferences(getOrSetSharedPreferenceName(context), Context.MODE_PRIVATE)
    }

    private fun getOrSetSharedPreferenceName(context: Context): String {
        if (sharedPreferenceName == null) {
            sharedPreferenceName = "%s_%s".format(context.packageName, SHARED_PREFERENCES)
        }

        return sharedPreferenceName!!
    }

    /**
     * Returns the id/hash of the currently selected emoji pack
     */
    override fun getSelected(context: Context): String {
        return try {
            getSharedPreferences(context)
                .getString(EMOJI_PREFERENCE, getDefault(context)) ?: getDefault(context)
        } catch (e: ClassCastException) {
            Log.e("FilemojiCompat", "Emoji preference is not a String; using default", e)
            getDefault(context)
        }
    }

    /**
     * Stores the id/hash of the currently/newly selected emoji pack
     */
    override fun setSelected(context: Context, value: String) {
        Log.d("FilemojiCompat", "Switching selected emoji pack to: %s".format(value))
        val prefs = getSharedPreferences(context)

        with(prefs.edit()) {
            putString(EMOJI_PREFERENCE, value)
            apply()
        }

        EmojiPackHelper.reset(context)
    }

    /**
     * Returns the name of the default pack for this application.
     * Note: Usually, this is [SYSTEM_DEFAULT], but it may be overridden, if the application e.g.
     * comes with an Asset-based default pack.
     */
    override fun getDefault(context: Context): String {
        return try {
            getSharedPreferences(context)
                .getString(DEFAULT_PREFERENCE, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT
        } catch (e: java.lang.ClassCastException) {
            Log.e(
                "FilemojiCompat",
                "Default Emoji preference is not a String; using sytem default",
                e
            )
            SYSTEM_DEFAULT
        }
    }

    /**
     * Sets the id/hash of the default emoji pack for this application.
     * Note: You should <i>never</i> use a [de.c1710.filemojicompat_ui.packs.FileBasedEmojiPack] here, because it may be deleted by
     * the user. If you want to update their choice, use [setSelected] instead.
     * Usually you would use a [de.c1710.filemojicompat_ui.packs.AssetEmojiPack] here.
     */
    override fun setDefault(context: Context, value: String) {
        val prefs = getSharedPreferences(context)

        with(prefs.edit()) {
            putString(DEFAULT_PREFERENCE, value)
            apply()
        }
    }


    /**
     * The preference name/key for the custom names preference
     */
    private var customNamesPreferenceName: String? = null

    private fun getCustomNamesPreferences(context: Context): SharedPreferences {
        if (customNamesPreferenceName == null) {
            customNamesPreferenceName =
                getOrSetSharedPreferenceName(context) + "_CustomNames"
        }

        return context
            .getSharedPreferences(customNamesPreferenceName!!, Context.MODE_PRIVATE)
    }

    /**
     * Returns the name assigned for a certain custom/imported emoji pack.
     * Note: You should only use this for display, internally, you should use the [de.c1710.filemojicompat_ui.structures.EmojiPack.id]/hash of the pack.
     * @param hash The file hash of the pack
     * @return A user-assigned name of the pack (or null if it doesn't exist)
     */
    override fun getNameForCustom(context: Context, hash: String): String? {
        return getCustomNamesPreferences(context)
            .getString(hash, null)
    }

    /**
     * Sets the user-visible name for a custom emoji pack.
     */
    override fun setNameForCustom(context: Context, name: String, hash: String) {
        val prefs = getCustomNamesPreferences(context)

        with(prefs.edit()) {
            putString(hash, name)
            apply()
        }
    }
}