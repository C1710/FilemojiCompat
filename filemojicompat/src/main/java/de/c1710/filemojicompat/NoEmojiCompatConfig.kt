package de.c1710.filemojicompat

import android.content.Context
import android.util.Log
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiCompat.MetadataRepoLoader
import androidx.emoji2.text.EmojiCompat.MetadataRepoLoaderCallback
import androidx.emoji2.text.MetadataRepo
import java.io.IOException

class NoEmojiCompatConfig(context: Context) : EmojiCompat.Config(DummyMetadataLoader(context)) {
    init {
        setReplaceAll(false)
    }

    private class DummyMetadataLoader(private val context: Context) :
        MetadataRepoLoader {
        override fun load(loaderCallback: MetadataRepoLoaderCallback) {
            val assetManager = context.assets
            try {
                val repo = MetadataRepo.create(assetManager, "NoEmojiCompat")
                loaderCallback.onLoaded(repo)
            } catch (e: IOException) {
                Log.e("FilemojiCompat", "Could not load the fallback font", e)
                loaderCallback.onFailed(e)
            }
        }
    }
}