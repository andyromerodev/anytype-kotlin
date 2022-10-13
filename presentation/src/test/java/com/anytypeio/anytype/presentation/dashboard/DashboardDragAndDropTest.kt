package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockFactory.link
import com.anytypeio.anytype.presentation.mapper.toDashboardViews
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class DashboardDragAndDropTest : DashboardTestSetup() {

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `block dragging events do not alter overall state`() {

        // SETUP

        val profile = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Smart(SmartBlockType.HOME),
            fields = Block.Fields.empty()
        )

        val pages = listOf(
            createBlockLink(),
            createBlockLink()
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = pages.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        val events = flow {
            delay(delayInMillis)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard) + profile + pages,
                        type = SmartBlockType.HOME
                    )
                )
            )
        }

        stubGetConfig(
            Either.Right(config)
        )

        stubObserveEvents(
            params = InterceptEvents.Params(context = config.home),
            flow = events
        )

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = emptyList()
            )
        )

        // TESTING

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val blocks = listOf(profile) + pages

        val views = blocks.toDashboardViews(builder = builder)

        val expected = HomeDashboardStateMachine.State(
            isLoading = false,
            isInitialzed = true,
            blocks = views,
            childrenIdsList = dashboard.children,
            error = null
        )

        val from = 0
        val to = 1

        vm.state.test().assertValue(expected)

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        verifyNoInteractions(move)
        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start dispatching drag-and-drop actions when the dragged item is dropped`() {

        val pages = listOf(
            createBlockLink(),
            createBlockLink()
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = pages.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val blocks = listOf(dashboard) + pages

        val views = blocks.toDashboardViews(builder = builder)

        val from = 0
        val to = 1

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = pages.last().id,
                    blockIds = listOf(pages.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the last block before the first block`() {

        val links = listOf(
            createBlockLink(),
            createBlockLink(),
            createBlockLink()
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = links.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val blocks = listOf(dashboard) + links

        val views = blocks.toDashboardViews(builder = builder)

        val from = 2
        val to = 0

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links.first().id,
                    blockIds = listOf(links.last().id),
                    position = Position.TOP
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the first block after the second block`() {

        val links = listOf(
            createBlockLink(),
            createBlockLink(),
            createBlockLink()
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = links.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val blocks = listOf(dashboard) + links

        val views = blocks.toDashboardViews(builder = builder)

        val from = 0
        val to = 1

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links[1].id,
                    blockIds = listOf(links.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the first block after the third block`() {

        val links = listOf(
            createBlockLink(),
            createBlockLink(),
            createBlockLink()
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = links.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val blocks = listOf(dashboard) + links

        val views = blocks.toDashboardViews(builder = builder)

        val from = 0
        val to = 2

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links.last().id,
                    blockIds = listOf(links.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    fun createBlockLink(): Block =
        link(
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            content = StubLinkContent(
                type = Block.Content.Link.Type.PAGE,
            )
        )
}