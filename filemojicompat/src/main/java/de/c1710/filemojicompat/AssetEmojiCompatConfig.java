package de.c1710.filemojicompat;
/*
 * Original file (https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java):
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.MetadataRepo;

/**
 * A simple implementation of EmojiCompat.Config using typeface assets.
 * Based on:
 * https://android.googlesource.com/platform/frameworks/support/+/master/emoji/bundled/src/main/java/android/support/text/emoji/bundled/BundledEmojiCompatConfig.java
 * Changes are marked with comments. Formatting and other simple changes are not always marked.
 *
 * @deprecated Please use {@link FileEmojiCompatConfig#createFromAsset(Context, String)} instead
 * for greater flexibility.
 */
@Deprecated
public class AssetEmojiCompatConfig extends EmojiCompat.Config {
    // The class name is obviously changed from the original file

    /**
     * Create a new configuration for this EmojiCompat
     *
     * @param assetName The file name/path of the requested font
     * @param context   Context instance
     */
    public AssetEmojiCompatConfig(@NonNull Context context,
                                  // NEW
                                  @NonNull String assetName) {
        // This one is oviously new
        super(new FileMetadataRepoLoader(context, null, assetName, null));
    }
}
