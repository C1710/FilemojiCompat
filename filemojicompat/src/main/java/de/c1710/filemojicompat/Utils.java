package de.c1710.filemojicompat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.emoji.text.EmojiCompat;

public class Utils {
    /**
     * For some reason, EmojiCompat does not seem to automatically add IME support to text inputs.
     * This function does this in a rather easy way.
     * @param text The {@link TextView} (typically an {@link androidx.emoji.widget.EmojiEditText}) to patch
     * @param config The configuration to use (will be used to retrieve whether all emojis are replaced)
     */
    public static void patchImeSupport(TextView text, FileEmojiCompatConfig config) {
        Bundle extras = text.getInputExtras(true);
        // The metaversion is just set to something that is large enough that it won't matter
        extras.putInt(EmojiCompat.EDITOR_INFO_METAVERSION_KEY, 0xc0ffee);
        extras.putBoolean(EmojiCompat.EDITOR_INFO_REPLACE_ALL_KEY, config.isReplaceAll());
    }


    /**
     * For some reason, EmojiCompat does not seem to automatically add IME support to text inputs.
     * This function does this in a rather easy way.
     * @param text The {@link TextView} (typically an {@link androidx.emoji.widget.EmojiEditText}) to patch
     * @param replaceAll Whether all emojis are replaced or not (cf. {@link FileEmojiCompatConfig#setReplaceAll(ReplaceStrategy)})
     */
    public static void patchImeSupport(TextView text, boolean replaceAll) {
        Bundle extras = text.getInputExtras(true);
        // The metaversion is just set to something that is large enough that it won't matter
        extras.putInt(EmojiCompat.EDITOR_INFO_METAVERSION_KEY, 0xc0ffee);
        extras.putBoolean(EmojiCompat.EDITOR_INFO_REPLACE_ALL_KEY, replaceAll);
    }
}
