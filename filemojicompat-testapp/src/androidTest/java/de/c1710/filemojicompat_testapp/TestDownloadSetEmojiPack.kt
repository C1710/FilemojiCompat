package de.c1710.filemojicompat_testapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import de.c1710.filemojicompat_ui.views.picker.EmojiPackViewHolder
import de.c1710.filemojicompat_ui.views.picker.HolderPackInfo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestDownloadSetEmojiPack {
    @JvmField
    @Rule
    val activityScenarioRule = ActivityScenarioRule(PreferenceActivity::class.java)

    @Test
    fun selectImportNotoColorEmoji() {
        onView(ViewMatchers.withId(R.id.emoji_preference))
            .perform(RecyclerViewActions.scrollToHolder(emojiPackIdMatcher("noto")))
    }

    @OptIn(HolderPackInfo::class)
    private fun emojiPackIdMatcher(@Suppress("SameParameterValue") id: String): Matcher<EmojiPackViewHolder> {
        return object: TypeSafeMatcher<EmojiPackViewHolder>() {
            override fun describeTo(description: Description?) {
                description!!.appendText("item with emoji pack \"%s\"".format(id))
            }

            override fun matchesSafely(item: EmojiPackViewHolder?): Boolean {
                // matchesSafely guarantees that it's not null
                val actualId = item!!.getPackId()
                return if (actualId != null) {
                    actualId == id
                } else {
                    false
                }
            }

        }
    }
}