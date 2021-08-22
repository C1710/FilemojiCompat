### DISCLAIMER
I am not affiliated with or supported by Google or any other people who orignally developed the EmojiCompat library. I only made a more flexible implementation of one of their classes.

### Important note on older versions
As JCenter/Bintray [will be shut down on 2021-05-01](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/), the library needed to switch to another service, which is in this case [MavenCentral](https://search.maven.org/artifact/de.c1710/filemojicompat) (and Github Packages, but due to an [issue](https://github.community/t/download-from-github-package-registry-without-authentication/14407/7), downloading through the maven repository is not possible without authentication).  

While the usage of MavenCentral should be very easy (you only might need to include it in your `repositories`-section), there's one disadvantage: Older versions of the library (that is  `1.0.17` or older) will not be available anymore, although I don't believe that these are needed.

# FileMojiCompat
## What is this?
This is a library providing an easy solution to use [EmojiCompat](https://developer.android.com/guide/topics/ui/look-and-feel/emoji-compat) 
with fonts that are either stored in the `assets`-folder with another name than `NotoColorEmojiCompat.ttf` or you can use
EmojiCompat fonts which are stored anywhere on the device's local storage.
## How do I get this library?
That's relatively easy: Just add the following line to your module's `build.gradle` inside `dependencies`:
```
implementation 'de.c1710:filemojicompat:2.0.0'
```

## Migration from FilemojiCompat 1.x
This version has rather changes in its API.
Instead of using the constructor (`new FileEmojiCompatConfig(...)`), you'll need to use the `init`-function,
which should provide the same parameters.

The `setReplaceAll(boolean, boolean)`-method is also not present anymore.
It is replaced by `setReplaceAll(ReplaceStrategy)`.
The following table translates the old parameters to the new one:
| `replaceAll` | `replaceAllonFallback` | `strategy`                               |
|--------------|------------------------|------------------------------------------|
| `false`      | `false`                | `NEVER`                                  |
| `true`       | `false`                | `NORMAL` (default)                       |
| `true`       | `true`                 | `ALWAYS`                                 |
| `false`      | `true`                 | _Removed as it does not make much sense_ |

If you want, you can now add the `patchImeSupport`-function to add support for keyboards to show all
the new emojis.

## How do I use it?
1. First of all, you will need to create a configuration through the `init`-function.
   Examples:
   ```java
   // All examples assume that you have an Android Context provided
   
   // Creates a configuration that only provides the option to use a custom emoji font
   FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context);
   // Specifies that you want to use the file "Blobmoji.ttf" from the src/main/assets directory
   FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, null, "Blobmoji.ttf");
   // Loads the font from /storage/emulated/0/Android/data/[your.app.package]/files/Blobmoji.ttf (if provided; otherwise a default/fallback is used)
   File fontFile = new File(context.getExternalFilesDir(null), "emoji/Blobmoji.ttf");
   FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, fontFile);
   ```
2. Then you can add some additional configuration, e.g. that you want to replace _all_ emojis, even 
   if the fallback/default font is used:
   ```java
   // Just using the second example here. You can also call this function separately.
   FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, null, "Blobmoji.ttf")
                                    .setReplaceAll(ReplaceStrategy.ALWAYS);
   ```
3. Once you have your configuration, you'll need to initialize `EmojiCompat` with it:
   ```java
   EmojiCompat.init(config);
   ```
4. Wherever you want to use it, you'll need to either use the EmojiCompat widgets or add it manually,
   as described [on the website](https://developer.android.com/guide/topics/ui/look-and-feel/emoji-compat#using-widgets-with-appcompat).
5. For some reason, it does not seem to automatically add the metadata required for keyboards to show
   newer emojis. To do that you can simply call the following function with your text fields:
   ```java
   // Assuming that you have called the text input "editText"
   
   // When you still have the config
   Utils.patchImeSupport(editText, config);
   // When e.g. you have your replacement strategy set to ALWAYS and therefore can be sure that all 
   // emojis are replaced or if you just don't care if something is a bit odd
   // (I am not sure what exactly this does on the keyboard/IME side).
   Utils.patchImeSupport(editText, true);
   ```
### What happens if I don't provide the font file in `FileEmojiCompatConfig`?
In this case, there won't be a visible difference to not using EmojiCompat.  