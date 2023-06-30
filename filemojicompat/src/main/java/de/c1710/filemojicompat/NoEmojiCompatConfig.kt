package de.c1710.filemojicompat;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.MetadataRepo;

import java.io.IOException;

public class NoEmojiCompatConfig extends EmojiCompat.Config {
    public NoEmojiCompatConfig(Context context) {
        super(new DummyMetadataLoader(context));
        setReplaceAll(false);
    }

    private static class DummyMetadataLoader implements EmojiCompat.MetadataRepoLoader {
        private final Context context;

        DummyMetadataLoader(Context context) {
            this.context = context;
        }

        @Override
        public void load(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
            final AssetManager assetManager = this.context.getAssets();
            try {
                final MetadataRepo repo = MetadataRepo.create(assetManager, "NoEmojiCompat");
                loaderCallback.onLoaded(repo);
            } catch (IOException e) {
                Log.e("FilemojiCompat", "Could not load the fallback font", e);
                loaderCallback.onFailed(e);
            }
        }
    }
}
