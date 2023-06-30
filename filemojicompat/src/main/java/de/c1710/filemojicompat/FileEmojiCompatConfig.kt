package de.c1710.filemojicompat

import android.content.Context
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiCompat.MetadataRepoLoaderCallback
import androidx.emoji2.text.MetadataRepo
import java.io.File

/*
 * Adapted from https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 *     Copyright (C) 2017 The Android Open Source Project
 * Modifications Copyright (C) 2018 Constantin A.
 * Note: The files has been converted to Kotlin, therefore the whole code got changed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * A simple implementation of EmojiCompat.Config using typeface files.
 * Based on:
 * https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 * Changes are marked with comments. Formatting and other simple changes are not always marked.
 */
class FileEmojiCompatConfig
/**
 * Create a new configuration for this EmojiCompat based on a file
 *
 * @param context      Context instance
 * @param fontFile     The file containing the EmojiCompat font
 * @param fallbackFontName The asset path of the fallback font
 */
private constructor (
    private val context: Context,  // NEW
    private val fontFile: File?,
    private val fallbackFontName: String?,
    var fallbackEnabled: MutableBoolean?,
    dontWarnOnEmptyFileName: Boolean
) : EmojiCompat.Config(
    FileMetadataRepoLoader(
        context,
        fontFile,
        fallbackFontName,
        fallbackEnabled,
        dontWarnOnEmptyFileName
    )
) {
    private var replacementStrategy = ReplaceStrategy.NORMAL

    /**
     * Sets the strategy to use when it comes to replacing all or only unsupported emojis.
     * @param strategy The strategy to use.
     * @return This
     */
    fun setReplaceAll(strategy: ReplaceStrategy): FileEmojiCompatConfig {
        replacementStrategy = strategy
        when (strategy) {
            ReplaceStrategy.NEVER -> super.setReplaceAll(false)
            ReplaceStrategy.NORMAL -> super.setReplaceAll(!getFallbackEnabled())
            ReplaceStrategy.ALWAYS -> super.setReplaceAll(true)
        }
        // Else: Ignore
        return this
    }

    /**
     * Specifies whether all emojis should be replaced even for the fallback/default emoji font.
     * @param replaceAll True if the provided emoji font should also replace all emojis
     * (useful e.g., if you already provide a custom emoji font)
     * @return This
     */
    override fun setReplaceAll(replaceAll: Boolean): FileEmojiCompatConfig {
        return setReplaceAll(if (replaceAll) ReplaceStrategy.ALWAYS else ReplaceStrategy.NORMAL)
    }

    private fun getFallbackEnabled(): Boolean {
        if (fallbackEnabled == null) {
            // Looks like we don't know whether the fallback is enabled.
            // Therefore, we'll need to try loading again...
            val dummyCallback: MetadataRepoLoaderCallback = object : MetadataRepoLoaderCallback() {
                override fun onLoaded(metadataRepo: MetadataRepo) {}
                override fun onFailed(throwable: Throwable?) {}
            }
            fallbackEnabled = MutableBoolean(false)

            // Now, load it again
            val loader = FileMetadataRepoLoader(
                context,
                fontFile,
                fallbackFontName,
                fallbackEnabled,
                false
            )
            loader.loadSync(dummyCallback)
            // Now, fallbackEnabled is set.
        }
        return fallbackEnabled!!.get()
    }

    companion object {
        /**
         * Create a new configuration for this EmojiCompat based on a file
         *
         * @param context      Context instance
         * @param fontFile     The file containing the EmojiCompat font
         * @param fallbackFontName The asset path of the fallback font
         * @param dontWarnOnEmptyFileName If set to true, don't log a warning (only info)
         * if the empty file was given as fontFile.
         * This is used in FilemojiCompat-UI when an asset is loaded
         */
        @JvmOverloads
        fun init(
            context: Context,
            fontFile: File? = null,
            fallbackFontName: String? = null,
            dontWarnOnEmptyFileName: Boolean = false
        ): FileEmojiCompatConfig {
            val fallbackEnabled = MutableBoolean(false)
            val config = FileEmojiCompatConfig(
                context,
                fontFile,
                fallbackFontName,
                fallbackEnabled,
                dontWarnOnEmptyFileName
            )
            // We need to adjust the replacement status
            (config as EmojiCompat.Config).setReplaceAll(!fallbackEnabled.get())
            return config
        }

        @JvmOverloads
        fun init(
            context: Context,
            fontFile: String?,
            fallbackFontName: String? = null
        ): FileEmojiCompatConfig {
            return init(context, File(fontFile ?: ""), fallbackFontName)
        }

    }
}