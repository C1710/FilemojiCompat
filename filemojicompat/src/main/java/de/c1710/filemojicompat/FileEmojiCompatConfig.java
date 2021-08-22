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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.MetadataRepo;

import java.io.File;

/**
 * A simple implementation of EmojiCompat.Config using typeface files.
 * Based on:
 * https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 * Changes are marked with comments. Formatting and other simple changes are not always marked.
 */
public class FileEmojiCompatConfig extends EmojiCompat.Config {
    /**
     * The default name of the fallback font
     */
    private static final String FONT_FALLBACK = "NoEmojiCompat.ttf";

    private ReplaceStrategy replacementStrategy = ReplaceStrategy.NORMAL;

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
    public static FileEmojiCompatConfig init (
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
        // We need to adjust the replacement status
        ((EmojiCompat.Config) config).setReplaceAll(!fallbackEnabled.get());
        return config;
    }

    public static FileEmojiCompatConfig init (
            @NonNull Context context,
            @Nullable File fontFile
    ) {
        return init(context, fontFile, null);
    }

    public static FileEmojiCompatConfig init (
            @NonNull Context context,
            @Nullable String fontFile,
            @Nullable String fallbackFontName
    ) {
        return init(context, new File(fontFile != null ? fontFile : ""), fallbackFontName);
    }

    public static FileEmojiCompatConfig init (
            @NonNull Context context,
            @Nullable String fontFile
    ) {
        return init(context, fontFile, null);
    }

    public static FileEmojiCompatConfig init (@NonNull Context context) {
        return init(context, (File) null, null);
    }

    /**
     * Creates a new FileEmojiCompatConfig based on an asset. Will set the replacement strategy
     * to treat the "normal" and the asset version equally.
     * <p>
     * The default location for a substituting font is
     * {@code /sdcard/Android/data/your.apps.package/files/EmojiCompat.ttf}.
     *
     * @param context   The app's context is needed for several tasks
     * @param assetPath The path inside the {@code assets} folder for the default font file
     * @return A FileEmojiCompatConfig which will use the given font by default
     */
    public static FileEmojiCompatConfig createFromAsset (@NonNull Context context,
                                                         @Nullable String assetPath) {
        if (assetPath != null) {
            FileEmojiCompatConfig config = init(context,
                    (File) null,
                    assetPath);
            config.setReplaceAll(config.replacementStrategy == ReplaceStrategy.NEVER ? ReplaceStrategy.NEVER : ReplaceStrategy.ALWAYS);
            return config;
        } else {
            return createFromAsset(context);
        }
    }

    /**
     * Creates a new FileEmojiCompatConfig based on an asset. Will set the replacement strategy
     * to treat the "normal" and the asset version equally.
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

    /**
     * Sets the strategy to use when it comes to replacing all or only unsupported emojis.
     * @param strategy The strategy to use.
     * @return This
     */
    public FileEmojiCompatConfig setReplaceAll(ReplaceStrategy strategy) {
        this.replacementStrategy = strategy;

        switch (strategy) {
            case NEVER:
                super.setReplaceAll(false);
                break;
            case NORMAL:
                super.setReplaceAll(!this.getFallbackEnabled());
                break;
            case ALWAYS:
                super.setReplaceAll(true);
                break;
        }
        // Else: Ignore
        return this;
    }

    /**
     * Specifies whether all emojis should be replaced even for the fallback/default emoji font.
     * @param replaceAll True if the provided emoji font should also replace all emojis
     *                   (useful e.g., if you already provide a custom emoji font)
     * @return This
     */
    @Override
    public FileEmojiCompatConfig setReplaceAll(boolean replaceAll) {
        return setReplaceAll(replaceAll ? ReplaceStrategy.ALWAYS : ReplaceStrategy.NORMAL);
    }


    private boolean getFallbackEnabled() {
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

        return fallbackEnabled.get();
    }

    /**
     * @return Whether all emojis <i>are</i> replaced. Note: This does <i>not</i> return the
     * strategy used, but how the actual state is.
     */
    public boolean isReplaceAll() {
        return (this.replacementStrategy == ReplaceStrategy.ALWAYS
            || this.replacementStrategy == ReplaceStrategy.NORMAL && !getFallbackEnabled()
        );
    }
}
