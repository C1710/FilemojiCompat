package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.emoji2.text.EmojiCompat
import de.c1710.filemojicompat.NoEmojiCompatConfig
import de.c1710.filemojicompat_ui.R
import de.c1710.filemojicompat_ui.packs.SystemDefaultEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPack
import java.io.IOException

object EmojiPackHelper {
    /**
     * Initializes the helper. The [EmojiPackList.defaultList] will be created including the
     * packs given as a parameter (plus system default and any imported packs).
     * If the list is empty and the [EmojiPackList.defaultList] already exists, it will not be overridden.
     *
     * One of these [init]-functions should be called to set up [EmojiCompat] with this pack management.
     *
     * @param context Can be the Application Context, etc.
     * @param emojiPacks A list of the emoji packs you want to add hardcoded.
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Context, emojiPacks: ArrayList<EmojiPack> = ArrayList()) {
        if (EmojiPackList.defaultList == null || emojiPacks.isNotEmpty()) {
            EmojiPackList.defaultList = EmojiPackList(context, emojiPacks = emojiPacks)
            Log.w("FilemojiCompat", "init: No Emoji Pack list created. Using empty one")
        }

        val config: EmojiCompat.Config = getCurrentConfig(context)

        try {
            EmojiCompat.init(config)
        } catch (e: IOException) {
            Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_SHORT).show()
            Log.e("FilemojiCompat", "init: Could not load emoji pack %s".format(EmojiPreference.getSelected(context)), e)

            EmojiCompat.init(NoEmojiCompatConfig(context))
        }
    }

    /**
     * Reloads the currently set emoji configuration.
     * Note: This assumes that [EmojiPackList.defaultList] has already been created!
     * @param context Can be the Application Context, etc.
     */
    fun reset(context: Context) {
        val config = getCurrentConfig(context)

        EmojiCompat.reset(config)
    }

    private fun getCurrentConfig(context: Context): EmojiCompat.Config {
        val list = EmojiPackList.defaultList!!

        val emojiPack = list[EmojiPreference.getSelected(context)]
        val selectedPack = if (emojiPack != null) {
            emojiPack
        } else {
            Log.e(
                "FilemojiCompat", "generateCurrentConfig: selected emoji pack %s not in list"
                    .format(EmojiPreference.getSelected(context))
            )
            Toast.makeText(context, R.string.loading_failed, Toast.LENGTH_SHORT).show()
            SystemDefaultEmojiPack.getSystemDefaultPack(context)
        }

        return selectedPack.load(context, list)
    }
}