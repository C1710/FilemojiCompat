package de.c1710.filemojicompat;
/*
 * Adapted from https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 *     Copyright (C) 2017 The Android Open Source Project
 * Modifications Copyright (C) 2018 Constantin A.
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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.MetadataRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * A simple implementation of EmojiCompat.Config using typeface files.
 * Based on:
 * https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 * Changes are marked with comments. Formatting and other simple changes are not always marked.
 */
public class FileEmojiCompatConfig extends EmojiCompat.Config {
    // The class name is obviously changed from the original file
    private final static String TAG = "FileEmojiCompatConfig";
    /**
     * The default name of the fallback font
     */
    private static final String FONT_FALLBACK = "NoEmojiCompat.ttf";
    /**
     * This boolean indicates whether the fallback solution is used.
     */
    private boolean fallback;
    /**
     * Indicates whether all emojis should be replaced when the fallback font is used.
     */
    private boolean replaceAllOnFallback = false;

    private MutableBoolean fallbackEnabled;

    private final Context context;
    private final File fontFile;
    private final String fallbackFontName;

    /**
     * Create a new configuration for this EmojiCompat based on a file
     *
     * @param context      Context instance
     * @param fontFile     The file containing the EmojiCompat font
     * @param fallbackFontName The asset path of the fallback font
     */
    private FileEmojiCompatConfig(@NonNull Context context,
                                 // NEW
                                 @Nullable File fontFile,
                                 @Nullable String fallbackFontName,
                                 @Nullable MutableBoolean fallbackEnabled) {
        super(new FileMetadataRepoLoader(context, fontFile, fallbackFontName, fallbackEnabled));
        this.context = context;
        this.fontFile = fontFile;
        this.fallbackFontName = fallbackFontName;
        this.fallbackEnabled = fallbackEnabled;
    }

    /**
     * Create a new configuration for this EmojiCompat based on a file
     *
     * @param context      Context instance
     * @param fontFile     The file containing the EmojiCompat font
     * @param fallbackFontName The asset path of the fallback font
     */
    public static FileEmojiCompatConfig init(
            @NonNull Context context,
            @Nullable File fontFile,
            @Nullable String fallbackFontName
        ) {
        MutableBoolean fallbackEnabled = new MutableBoolean(false);
        FileEmojiCompatConfig config = new FileEmojiCompatConfig(
                context,
                fontFile,
                fallbackFontName,
                fallbackEnabled
        );
        return config;
    }

    public static FileEmojiCompatConfig init(
            @NonNull Context context,
            @Nullable File fontFile
    ) {
        return init(context, fontFile, null);
    }

    public static FileEmojiCompatConfig init(
            @NonNull Context context,
            @Nullable String fontFile,
            @Nullable String fallbackFontName
    ) {
        return init(context, new File(fontFile != null ? fontFile : ""), fallbackFontName);
    }

    public static FileEmojiCompatConfig init(
            @NonNull Context context,
            @Nullable String fontFile
    ) {
        return init(context, fontFile, null);
    }

    /**
     * Creates a new FileEmojiCompatConfig based on an asset.
     * <p>
     * The default location for a substituting font is
     * {@code /sdcard/Android/data/your.apps.package/files/EmojiCompat.ttf}.
     *
     * @param context   The app's context is needed for several tasks
     * @param assetPath The path inside the {@code assets} folder for the default font file
     * @return A FileEmojiCompatConfig which will use the given font by default
     */
    public static FileEmojiCompatConfig createFromAsset(@NonNull Context context,
                                                        @Nullable String assetPath) {
        if (assetPath != null) {
            FileEmojiCompatConfig config = init(context,
                    (File) null,
                    assetPath);
            config.replaceAllOnFallback = true;
            return config;
        } else {
            return createFromAsset(context);
        }
    }

    /**
     * Creates a new FileEmojiCompatConfig based on an asset.
     * <p>
     * The default location for a substituting font is
     * {@code /sdcard/Android/data/your.apps.package/files/EmojiCompat.ttf}.
     * <p>
     * The default name for the Assets font is {@code NoEmojiCompat.ttf}.
     * If you wish to use a different name for this font, please use
     * {@link #createFromAsset(Context, String)}.
     *
     * @param context The app's context is needed for several tasks
     * @return A FileEmojiCompatConfig which will use the given font by default
     */
    public static FileEmojiCompatConfig createFromAsset(@NonNull Context context) {
        return createFromAsset(context, FONT_FALLBACK);
    }


    @Override
    public FileEmojiCompatConfig setReplaceAll(boolean replaceAll) {
        return setReplaceAll(replaceAll, this.replaceAllOnFallback);
    }

    /**
     * Replace all emojis
     *
     * @param replaceAll           Whether all emojis should be replaced
     * @param replaceAllOnFallback true if this is supposed to be the case even when using the fallback font.
     *                             Useful if the NoEmojiCompat.ttf is overridden by a "real" EmojiCompat font.
     * @return This EmojiCompat.Config
     */
    public FileEmojiCompatConfig setReplaceAll(boolean replaceAll, boolean replaceAllOnFallback) {
        this.replaceAllOnFallback = replaceAllOnFallback;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (fallbackEnabled == null) {
                // Looks like we don't know whether the fallback is enabled.
                // Therefore, we'll need to try loading again...
                EmojiCompat.MetadataRepoLoaderCallback dummyCallback = new EmojiCompat.MetadataRepoLoaderCallback() {
                    @Override
                    public void onLoaded(@NonNull MetadataRepo metadataRepo) {}

                    @Override
                    public void onFailed(@Nullable Throwable throwable) {}
                };

                this.fallbackEnabled = new MutableBoolean(false);

                // Now, load it again
                FileMetadataRepoLoader loader = new FileMetadataRepoLoader(
                        this.context,
                        this.fontFile,
                        this.fallbackFontName,
                        this.fallbackEnabled
                );

                loader.loadSync(dummyCallback);
                // Now, fallbackEnabled is set.
            }

            if (!fallbackEnabled.get() || replaceAllOnFallback) {
                super.setReplaceAll(replaceAll);
            } else {
                super.setReplaceAll(false);
                if (replaceAll) {
                    // If replaceAll would have been set to false anyway, there's no need for apologizing.
                    Log.w(TAG, "setReplaceAll: Cannot replace all emojis. Fallback font is active");
                }
            }
        }
        return this;
    }

}
