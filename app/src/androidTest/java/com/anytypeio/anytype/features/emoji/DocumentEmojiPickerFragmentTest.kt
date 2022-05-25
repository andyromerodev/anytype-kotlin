package com.anytypeio.anytype.features.emoji

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.RemoveDocumentIcon
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.model.EmojiModel
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils.withRecyclerView
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class DocumentEmojiPickerFragmentTest {

    @Mock
    lateinit var detailModificationManager: DetailModificationManager

    @Mock
    lateinit var suggester: EmojiSuggester

    @Mock
    lateinit var provider: EmojiProvider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var setEmojiIcon: SetDocumentEmojiIcon
    private lateinit var setImageIcon: SetDocumentImageIcon
    private lateinit var removeDocumentIcon: RemoveDocumentIcon

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        setEmojiIcon = SetDocumentEmojiIcon(repo = repo)
        setImageIcon = SetDocumentImageIcon(repo = repo)
        removeDocumentIcon = RemoveDocumentIcon(repo = repo)
        TestDocumentEmojiPickerFragment.testViewModelFactory =
            ObjectIconPickerViewModelFactory(
                emojiProvider = provider,
                emojiSuggester = suggester,
                setEmojiIcon = setEmojiIcon,
                setImageIcon = setImageIcon,
                removeDocumentIcon = removeDocumentIcon,
                dispatcher = Dispatcher.Default(),
                analytics = analytics
            )
    }

    @Test
    fun shouldHaveNothingVisible() {

        // SETUP

        provider.stub {
            on { emojis } doReturn emptyArray()
        }

        launchFragment(bundleOf())

        // TESTING

        onView(withId(R.id.clearSearchText)).apply {
            check(matches(not(isDisplayed())))
        }

        onView(withId(R.id.progressBar)).apply {
            check(matches(not(isDisplayed())))
        }

        onView(withId(R.id.pickerRecycler)).apply {
            check(matches(isDisplayed()))
            check(matches(hasChildCount(0)))
        }
    }

    @Test
    fun shouldHaveHeaderAndSixVisibleEmojis() {

        // SETUP

        val emojis = arrayOf(
            arrayOf(
                "😀",
                "😃",
                "😄",
                "😁",
                "😆",
                "😅"
            )
        )

        provider.stub {
            on { provider.emojis } doReturn emojis
        }

        launchFragment(bundleOf())

        // TESTING

        onView(withId(R.id.clearSearchText)).apply {
            check(matches(not(isDisplayed())))
        }

        onView(withId(R.id.progressBar)).apply {
            check(matches(not(isDisplayed())))
        }

        onView(withId(R.id.pickerRecycler)).apply {
            check(matches(isDisplayed()))
            check(matches(hasChildCount(emojis.first().size.inc())))
        }

        onView(withRecyclerView(R.id.pickerRecycler).atPositionOnView(0, R.id.category)).apply {
            check(matches(withText(R.string.category_smileys_and_people)))
        }
    }

    @Test
    fun shouldFindOnlyOneDogEmoji() {

        // SETUP

        val query = "dog"

        val dog = "🐶"

        val emojis = arrayOf(
            Emoji.DATA[0].take(6).toTypedArray(),
            Emoji.DATA[1].take(6).toTypedArray(),
            Emoji.DATA[2].take(6).toTypedArray(),
            Emoji.DATA[3].take(6).toTypedArray(),
            Emoji.DATA[4].take(6).toTypedArray(),
            Emoji.DATA[5].take(6).toTypedArray(),
            Emoji.DATA[6].take(6).toTypedArray(),
            Emoji.DATA[7].take(6).toTypedArray()
        )

        provider.stub {
            on { provider.emojis } doReturn emojis
        }

        suggester.stub {
            onBlocking { search(query) } doReturn listOf(
                EmojiModel(
                    category = MockDataFactory.randomString(),
                    name = MockDataFactory.randomString(),
                    emoji = dog
                )
            )
        }

        launchFragment(bundleOf())

        // TESTING

        onView(withId(R.id.filterInputField)).apply {
            perform(click())
            perform(typeText(query))
        }

        // Verifying results

        Thread.sleep(500)

        onView(withId(R.id.pickerRecycler)).apply {
            check(matches(hasChildCount(1)))
        }

        verifyBlocking(suggester, times(1)) { search(any()) }
    }

    @Test
    fun shouldNotSearchIfTextIsCleared() {

        // SETUP

        val query = "dog"

        val dog = "🐶"

        val emojis = arrayOf(
            Emoji.DATA[0].take(6).toTypedArray(),
            Emoji.DATA[1].take(6).toTypedArray(),
            Emoji.DATA[2].take(6).toTypedArray(),
            Emoji.DATA[3].take(6).toTypedArray(),
            Emoji.DATA[4].take(6).toTypedArray(),
            Emoji.DATA[5].take(6).toTypedArray(),
            Emoji.DATA[6].take(6).toTypedArray(),
            Emoji.DATA[7].take(6).toTypedArray()
        )

        provider.stub {
            on { provider.emojis } doReturn emojis
        }

        suggester.stub {
            onBlocking { search(query) } doReturn listOf(
                EmojiModel(
                    category = MockDataFactory.randomString(),
                    name = MockDataFactory.randomString(),
                    emoji = dog
                )
            )
        }

        val scenario = launchFragment(bundleOf())

        // TESTING

        onView(withId(R.id.filterInputField)).apply {
            perform(click())
            perform(typeText(query))
        }

        Thread.sleep(500)

        onView(withId(R.id.pickerRecycler)).apply {
            check(matches(hasChildCount(1)))
        }

        // Clearing text after first query

        onView(withId(R.id.clearSearchText)).apply {
            perform(click())
            perform(closeSoftKeyboard())
        }

        // Verifying results

        Thread.sleep(500)

        scenario.onFragment { fragment ->
            val count = fragment.binding.pickerRecycler.adapter?.itemCount
            assertEquals(
                expected = (6 * 8) + 8,
                actual = count
            )
        }

        verifyBlocking(suggester, times(1)) { search(any()) }
    }

    @Test
    fun shouldNotSearchIfTextIsDeletedByBackspace() {

        // SETUP

        val query = "dog"

        val dog = "🐶"

        val emojis = arrayOf(
            Emoji.DATA[0].take(6).toTypedArray(),
            Emoji.DATA[1].take(6).toTypedArray(),
            Emoji.DATA[2].take(6).toTypedArray(),
            Emoji.DATA[3].take(6).toTypedArray(),
            Emoji.DATA[4].take(6).toTypedArray(),
            Emoji.DATA[5].take(6).toTypedArray(),
            Emoji.DATA[6].take(6).toTypedArray(),
            Emoji.DATA[7].take(6).toTypedArray()
        )

        provider.stub {
            on { provider.emojis } doReturn emojis
        }

        suggester.stub {
            onBlocking { search(query) } doReturn listOf(
                EmojiModel(
                    category = MockDataFactory.randomString(),
                    name = MockDataFactory.randomString(),
                    emoji = dog
                )
            )
        }

        val scenario = launchFragment(bundleOf())

        // TESTING

        onView(withId(R.id.filterInputField)).apply {
            perform(click())
            perform(typeText(query))
        }

        Thread.sleep(500)

        onView(withId(R.id.pickerRecycler)).apply {
            check(matches(hasChildCount(1)))
        }

        // Removing text after first query

        onView(withId(R.id.filterInputField)).apply {
            repeat(query.length) { perform(pressKey(KeyEvent.KEYCODE_DEL)) }
            perform(closeSoftKeyboard())
        }

        // Verifying results

        Thread.sleep(500)

        scenario.onFragment { fragment ->
            val count = fragment.binding.pickerRecycler.adapter?.itemCount
            assertEquals(
                expected = (6 * 8) + 8,
                actual = count
            )
        }

        verifyBlocking(suggester, times(1)) { search(query) }
        verifyBlocking(suggester, times(1)) { search(any()) }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestDocumentEmojiPickerFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}