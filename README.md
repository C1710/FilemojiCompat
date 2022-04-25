### DISCLAIMER
I am not affiliated with or supported by Google or any other people who develop the EmojiCompat library. I only made a more flexible implementation of one of their classes.
.

# FilemojiCompat
## What is this?
This is a library providing an easy solution to use [EmojiCompat](https://developer.android.com/guide/topics/ui/look-and-feel/emoji-compat) (or rather [`Emoji2`](https://developer.android.com/guide/topics/ui/look-and-feel/emoji2))
with different user-selectable emoji packs, including imported ones.

## How to integrate FilemojiCompat into your app
Integrating this library is done in a few steps.  
1. To use `Emoji2` without special widgets, you should use `androidx.appcompat` >= `1.4.0`. This way, all AppCompat components are already EmojiCompat-enabled.
2. You need to include a dependency for the library in your app's `build.gradle`. It is split into four parts - you only need one of these:
   | Package name                             | Description                                                                                                                                                                                                                                                                                                           |
   |------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
   | `de.c1710:filemojicompat:3.2.1`          | The main library part. It supports emoji fonts to be placed in your app's `data` directory. Works like FilemojiCompat 2                                                                                                                                                                                               |
   | `de.c1710:filemojicompat-ui:3.2.1`       | Includes an emoji picker and an emoji pack management. Can be integrated with `androidx.preferences`. Recommended                                                                                                                                                                                                     |
   | `de.c1710:filemojicompat-defaults:3.2.1` | A default list of four downloadable emoji packs: Blobmoji, Noto-Emoji, Openmoji and Twemoji. Recommended if the app has internet permissions                                                                                                                                                                          |
   | `de.c1710:filemojicompat-autoinit:3.2.1`       | An extension for `filemojicompat-defaults` which uses the `androidx.startup` library to directly initialize EmojiCompat with FilemojiCompat without having to modify your `Application` class, similar to how `Emoji2` would work. Recommended if you don't want to change the defaults or add any other emoji packs. |
3. If you don't want to use the emoji picker, you can continue with the [instructions for FilemojiCompat 2 below](#old-instructions-for-filemojicompat-2-maybe-not-up-to-date).  
   Otherwise, you need to initialize EmojiCompat/Emoji2/FilemojiCompat. If you use `filemojicompat-autoinit`, you can skip this step.  
   In your main `Application`''s `onCreate` function, the initialization can be done with one line (this example is for Kotlin, but it is the same for Java):
   ```kotlin
      override fun onCreate() {
         ...
         EmojiPackHelper.init(this, DefaultEmojiPackList.get(this))
      }
   ```
   - `DefaultEmojiPackList.get(this)` requires `filemojicompat-defaults`.
   - If you want to use other emoji packs or extend the list, you can create an `EmojiPackList` instance and add the packs to it (or append to `DefaultEmojiPackList.get(this)`).
   - If you want to disable/hide the file import feature, you can pass `allowPackImports = false`. For maximum flexibility for your users, it is recommended to leave it enabled.
   With this, your app should work, although it is not yet possible to change the emoji pack.
4. Now, the emoji pack picker has to be integrated. There are two ways here:
     - If you use the `androidx.preference` library, you can integrate it as a [preference](#integrating-the-emoji-picker-with-androidxpreferences)
     - If you don't use it or don't have any other preferences, you can also integrate it [manually](#integrating-the-emoji-picker-manually)

### Integrating the emoji picker with `androidx.preferences`
**Right now it is not possible to use it in XML.**  
Integrating the emoji picker with your preferences involves two steps.
1. You need to add the `EmojiPickerPreference` to your `PreferenceFragment` (in code).
   This is done in `onCreatePreferences()`. The example is based on https://developer.android.com/guide/topics/ui/settings#inflate_the_hierarchy:
   ```kotlin
   override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResources(R.xml.preferences, rootKey)
      // Add the emoji preference
      getPreferenceScreen().addPreference(EmojiPickerPreference.get(requireActivity(), null))
   }
   ```
2. In order to make the picker dialog show up, you need to override `onDisplayPreferenceDialog()`:
   ```kotlin
   @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (!EmojiPickerPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference);
        }
    }
   ```

### Integrating the emoji picker manually
The actual picker is a special `RecyclerView` adapter called `EmojiPackItemAdapter`.
It can therefore be easily integrated with any RecyclerView.
The easiest way to get an instance is `EmojiPackItemAdapter.get()`, which sets up everything to handle the emoji pack import.
However, it requires a Activity that supports the Android Activity Results API as a parameter, an `androidx` `ComponentActivity` fulfills these requirements.

## Old instructions for FilemojiCompat 2 (maybe not up to date)
> ## How do I get this library?
> That's relatively easy: Just add the following line to your module's > `build.gradle` inside `dependencies`:
> ```
> implementation 'de.c1710:filemojicompat:3.2.1'
> ```
> 
> ## Migration from FilemojiCompat 1.x
> This version has rather changes in its API.
> Instead of using the constructor (`new FileEmojiCompatConfig(...)`), > you'll need to use the `init`-function,
> which should provide the same parameters.
> 
> The `setReplaceAll(boolean, boolean)`-method is also not present anymore.
> It is replaced by `setReplaceAll(ReplaceStrategy)`.
> The following table translates the old parameters to the new one:
> | `replaceAll` | `replaceAllonFallback` | `strategy`                               |
> |--------------|------------------------|------------------------------------------|
> | `false`      | `false`                | `NEVER`                                  |
> | `true`       | `false`                | `NORMAL` (default)                       |
> | `true`       | `true`                 | `ALWAYS`                                 |
> | `false`      | `true`                 | _Removed as it does not make much sense_ |
> 
> If you want, you can now add the `patchImeSupport`-function to add support for keyboards to show all the new emojis.
> 
> ## How do I use it?
> 1. First of all, you will need to create a configuration through the `init`-function.
>    Examples:
>    ```java
>    // All examples assume that you have an Android Context provided
>    
>    // Creates a configuration that only provides the option to use a custom emoji font
>    FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context);
>    // Specifies that you want to use the file "Blobmoji.ttf" from the src/main/assets directory
>    FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, null, "Blobmoji.ttf");
>    // Loads the font from /storage/emulated/0/Android/data/[your.app.package]/files/Blobmoji.ttf (if provided; otherwise a default/fallback is used)
>    File fontFile = new File(context.getExternalFilesDir(null), "emoji/Blobmoji.ttf");
>    FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, fontFile);
>    ```
> 2. Then you can add some additional configuration, e.g. that you want to replace _all_ emojis, even 
>    if the fallback/default font is used:
>    ```java
>    // Just using the second example here. You can also call this function separately.
>    FileEmojiCompatConfig config = FileEmojiCompatConfig.init(context, null, "Blobmoji.ttf")
>                                     .setReplaceAll(ReplaceStrategy.ALWAYS);
>    ```
> 3. Once you have your configuration, you'll need to initialize `EmojiCompat` with it:
>    ```java
>    EmojiCompat.init(config);
>    ```
> 4. Wherever you want to use it, you'll need to either use the EmojiCompat widgets or add it manually,
>    as described [on the website](https://developer.android.com/guide/topics/ui/look-and-feel/emoji-compat#using-widgets-with-appcompat).
> 5. For some reason, it does not seem to automatically add the metadata required for keyboards to show
>    newer emojis. To do that you can simply call the following function with your text fields:
>    ```java
>    // Assuming that you have called the text input "editText"
>    
>    // When you still have the config
>    Utils.patchImeSupport(editText, config);
>    // When e.g. you have your replacement strategy set to ALWAYS and therefore can be sure that all 
>    // emojis are replaced or if you just don't care if something is a bit odd
>    // (I am not sure what exactly this does on the keyboard/IME side).
>    Utils.patchImeSupport(editText, true);
>    ```
> ### What happens if I don't provide the font file in `FileEmojiCompatConfig`?
> In this case, there won't be a visible difference to not using EmojiCompat.  
> 
> ### Older versions
> As JCenter/Bintray [will be shut down on 2021-05-01](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/), the library needed to switch to another service, which is in this case [MavenCentral](https://search.maven.org/artifact/de.c1710/filemojicompat) (and Github Packages, but due to an [issue](https://github.community/t/download-from-github-package-registry-without-authentication/14407/7), downloading through the maven repository is not possible without authentication).  
> 
> While the usage of MavenCentral should be very easy (you only might need to include it in your `repositories`-section), there's one disadvantage: Older versions of the library (that is  `1.0.17` or older) will not be available anymore, although I don't believe that these are needed