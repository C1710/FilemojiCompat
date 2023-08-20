package de.c1710.filemojicompat_testapp

import android.util.Log
import android.view.View
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiCompat.EMOJI_SUPPORTED
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import de.c1710.filemojicompat_ui.helpers.EmojiPackList
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.views.picker.EmojiPackViewHolder
import de.c1710.filemojicompat_ui.views.picker.HolderPackInfo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

val EMOJIS_TO_TEST = mapOf(
    "\uD83D\uDE03" to (1 to 0), // grinning face - Emoji 1.0
    "\uD83E\uDD70" to (11 to 0), // smiling face with hearts - Emoji 11.0
    "\uD83E\uDD78" to (13 to 0), // disguised face - Emoji 13.0
    "\uD83D\uDE35\u200D\uD83D\uDCAB" to (13 to 1), // face with spiral eyes - Emoji 13.1
    "\uD83E\uDEE0" to (14 to 0), // melting face - Emoji 14.0
    "\uD83E\uDEE8" to (15 to 0), // shaking face - Emoji 15.0
    "\uD83D\uDC26\u200D\uD83D\uDD25" to (15 to 1) // phoenix - Emoji 15.1
)

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestDownloadSetEmojiPack {
    @JvmField
    @Rule
    val activityScenarioRule = ActivityScenarioRule(PreferenceActivity::class.java)

    @Test
    fun selectImportNotoColorEmoji() {
        selectImportEmojiPack("noto", 14 to 0)
    }

    private fun selectImportEmojiPack(@Suppress("SameParameterValue") id: String, targetVersion: Pair<Int, Int>) {
        onView(withId(R.id.emoji_preference))
            .perform(RecyclerViewActions.actionOnHolderItem(emojiPackIdMatcher(id),
                clickChildViewWithId(de.c1710.filemojicompat_ui.R.id.emoji_pack_download)))
        val pack = EmojiPackList.defaultList[id]

        if (pack is DownloadableEmojiPack) {
            onIdle {
                pack.getDownloadStatus()!!.addListener(object : EmojiPackDownloadListener {
                    override fun onProgress(bytesRead: Long, contentLength: Long) {
                        Log.d("selectImportEmojiPack", "downloading: %s".format(bytesRead.toFloat() / contentLength.toFloat()))
                    }

                    override fun onFailure(e: IOException?) {
                        // Fail
                        throw e ?: RuntimeException("Download failed")
                    }

                    override fun onCancelled() {
                        // Fail
                        throw RuntimeException("Download cancelled")
                    }

                    override fun onDone() {
                        Log.d("selectImportEmojiPack", "downloaded")
                        onIdle {
                            testAllEmojis(targetVersion)
                        }
                    }

                })
            }
        } else {
            testAllEmojis(targetVersion)
        }
    }

    private fun testAllEmojis(targetVersion: Pair<Int, Int>) {
        for ((emoji, version) in EMOJIS_TO_TEST) {
            if (version.first < targetVersion.first
                || (version.first == targetVersion.first && version.second <= targetVersion.second))
                Log.w("testAllEmojis", "Testing %s".format(emoji))
                assertEquals(EMOJI_SUPPORTED, EmojiCompat.get().getEmojiMatch(emoji, 4269))
        }
    }

    @OptIn(HolderPackInfo::class)
    private fun emojiPackIdMatcher(@Suppress("SameParameterValue") id: String): Matcher<EmojiPackViewHolder> {
        return object: TypeSafeMatcher<EmojiPackViewHolder>() {
            override fun describeTo(description: Description?) {
                description?.appendText("item with emoji pack \"%s\"".format(id))
            }

            override fun matchesSafely(item: EmojiPackViewHolder): Boolean {
                val actualId = item.getPackId()
                return if (actualId != null) {
                    actualId == id
                } else {
                    false
                }
            }
        }
    }

    companion object {
        // https://stackoverflow.com/a/30338665
        fun clickChildViewWithId(id: Int): ViewAction {
            return object: ViewAction {
                override fun getDescription(): String {
                    return "click on sub-view"
                }

                override fun getConstraints(): Matcher<View>? {
                    // We don't need any special Views to click them
                    return null
                }

                override fun perform(uiController: UiController?, view: View?) {
                    val child = view?.findViewById<View>(id)
                    child?.performClick()
                }
            }
        }
    }
}