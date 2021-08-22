package de.c1710.filemojicompat;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.MetadataRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileMetadataRepoLoader implements EmojiCompat.MetadataRepoLoader {
    public static final String DEFAULT_FALLBACK = "NoEmojiCompat.ttf";
    public static final String DEFAULT_FILE = "EmojiCompat.ttf";

    private final Context context;
    private final File fontFile;
    private final String fallbackFontName;
    private final MutableBoolean fallbackEnabled;


    /**
     * This loader can load emoji metadata from a file. It will do so on a new Thread
     * @param context The current Context (used to access the Asset Manager)
     * @param fontFile The file from where to load the main font (will proceed to fallback if null)
     * @param fallbackFontName The name of the fallback font (defaults to {@link #DEFAULT_FALLBACK})
     * @param fallbackEnabled A reference to a boolean that will be set to true if the fallback font is enabled
     */
    public FileMetadataRepoLoader(
            @NonNull Context context,
            @Nullable File fontFile,
            @Nullable String fallbackFontName,
            @Nullable MutableBoolean fallbackEnabled
            ) {
        this.context = context;
        this.fontFile = fontFile != null ? fontFile : new File(context.getExternalFilesDir(null), DEFAULT_FILE);
        this.fallbackFontName = fallbackFontName;
        this.fallbackEnabled = fallbackEnabled;
    }

    @Override
    public void load(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
        this.loadSync(loaderCallback);
    }

    private void loadAsync(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
        new Thread(() -> this.loadSync(loaderCallback)).start();
    }

    public void loadSync(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
        if (fontFile != null && fontFile.exists() && fontFile.canRead()) {
            // The file seems to be okay. We can load the file
            Typeface font = Typeface.createFromFile(this.fontFile);
            try {
                FileInputStream fontStream = new FileInputStream(fontFile);
                MetadataRepo repo = MetadataRepo.create(font, fontStream);
                fontStream.close();
                loaderCallback.onLoaded(repo);
            } catch (IOException e) {
                Log.e("FilemojiCompat", "Could not load font file", e);
                loadFallback(loaderCallback);
            }
        } else {
            // Something is wrong with the file, load the fallback instead
            FileNotFoundException notFound = new FileNotFoundException(
                    fontFile != null ? fontFile.getPath() : "null"
            );
            Log.w("FilemojiCompat", "Could not load font file", notFound);
            loadFallback(loaderCallback);
        }
    }

    private void loadFallback(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
        this.fallbackEnabled.set(true);
        Log.i("FilemojiCompat", "Using the fallback font");

        final AssetManager assetManager = this.context.getAssets();
        try {
            String fallbackFontName = this.fallbackFontName != null ? this.fallbackFontName : DEFAULT_FALLBACK;
            final MetadataRepo repo = MetadataRepo.create(assetManager, fallbackFontName);
            loaderCallback.onLoaded(repo);
        } catch (IOException e) {
            Log.e("FilemojiCompat", "Could not load the fallback font", e);
            loaderCallback.onFailed(e);
        }
    }
}
