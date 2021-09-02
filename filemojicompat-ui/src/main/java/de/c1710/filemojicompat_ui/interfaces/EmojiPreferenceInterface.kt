package de.c1710.filemojicompat_ui.interfaces

import android.content.Context

interface EmojiPreferenceInterface {
    fun getSelected(context: Context): String
    fun setSelected(context: Context, value: String)

    fun getDefault(context: Context): String
    fun setDefault(context: Context, value: String)

    fun getNameForCustom(context: Context, hash: String): String?
    fun setNameForCustom(context: Context, name: String, hash: String)
}