package de.c1710.filemojicompat

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.emoji2.text.EmojiCompat.MetadataRepoLoader
import androidx.emoji2.text.EmojiCompat.MetadataRepoLoaderCallback
import androidx.emoji2.text.MetadataRepo
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class FileMetadataRepoLoader(
    private val context: Context,
    fontFile: File?,
    fallbackFontName: String?,
    fallbackEnabled: MutableBoolean?,
    dontWarnOnEmptyFileName: Boolean
) : MetadataRepoLoader {
    private val fontFile: File?
    private val fallbackFontName: String?
    private val fallbackEnabled: MutableBoolean?
    private val dontWarnOnEmptyFileName: Boolean

    /**
     * This loader can load emoji metadata from a file. It will do so on a new Thread
     * @param context The current Context (used to access the Asset Manager)
     * @param fontFile The file from where to load the main font (will proceed to fallback if null)
     * @param fallbackFontName The name of the fallback font (defaults to [.DEFAULT_FALLBACK])
     * @param fallbackEnabled A reference to a boolean that will be set to true if the fallback font is enabled
     */
    init {
        this.fontFile = fontFile
            ?: File(
                context.getExternalFilesDir(null),
                DEFAULT_FILE
            )
        this.fallbackFontName = fallbackFontName
        this.fallbackEnabled = fallbackEnabled
        this.dontWarnOnEmptyFileName = dontWarnOnEmptyFileName
    }

    override fun load(loaderCallback: MetadataRepoLoaderCallback) {
        loadSync(loaderCallback)
    }

    private fun loadAsync(loaderCallback: MetadataRepoLoaderCallback) {
        Thread { loadSync(loaderCallback) }.start()
    }

    fun loadSync(loaderCallback: MetadataRepoLoaderCallback) {
        if (fontFile != null && fontFile.exists() && fontFile.canRead()) {
            // The file seems to be okay. We can load the file
            val font = Typeface.createFromFile(fontFile)
            try {
                val fontStream = FileInputStream(fontFile)
                val repo = MetadataRepo.create(font, fontStream)
                fontStream.close()
                loaderCallback.onLoaded(repo)
            } catch (e: IOException) {
                Log.e("FilemojiCompat", "Could not load font file", e)
                loadFallback(loaderCallback)
            }
        } else {
            // Something is wrong with the file, load the fallback instead
            val notFound = FileNotFoundException(
                if (fontFile != null) fontFile.path else "null"
            )
            // Just don't warn on an empty file if it shouldn't
            if (fontFile == null || fontFile.toString() != "" || !dontWarnOnEmptyFileName) {
                Log.w("FilemojiCompat", "Could not load font file", notFound)
            }
            loadFallback(loaderCallback)
        }
    }

    private fun loadFallback(loaderCallback: MetadataRepoLoaderCallback) {
        fallbackEnabled!!.set(true)
        Log.i("FilemojiCompat", "Using the fallback font")
        val assetManager = context.assets
        try {
            val fallbackFontName = fallbackFontName ?: DEFAULT_FALLBACK
            val repo = MetadataRepo.create(assetManager, fallbackFontName)
            loaderCallback.onLoaded(repo)
        } catch (e: IOException) {
            Log.e("FilemojiCompat", "Could not load the fallback font", e)
            loaderCallback.onFailed(e)
        }
    }

    companion object {
        const val DEFAULT_FALLBACK = "NoEmojiCompat.ttf"
        const val DEFAULT_FILE = "EmojiCompat.ttf"
    }
}