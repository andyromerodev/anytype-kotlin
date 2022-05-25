package com.anytypeio.anytype.presentation.editor.picker

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.editor.picker.DocumentIconActionMenuViewModel.Contract.*
import com.anytypeio.anytype.presentation.editor.picker.DocumentIconActionMenuViewModel.ViewState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Deprecated("To be deleted")
class DocumentIconActionMenuViewModel(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val setImageIcon: SetDocumentImageIcon,
    private val dispatcher: Dispatcher<Payload>,
    private val details: DetailModificationManager
) : ViewStateViewModel<ViewState>(), StateReducer<State, Event> {

    private val events = ConflatedBroadcastChannel<Event>()
    private val actions = Channel<Action>()
    private val flow: Flow<State> = events.asFlow().scan(State.init(), function)

    override val function: suspend (State, Event) -> State
        get() = { state, event -> reduce(state, event) }

    init {
        flow
            .map { state ->
                when {
                    state.error != null -> ViewState.Error(state.error)
                    state.isCompleted -> ViewState.Exit
                    state.isUploading -> ViewState.Uploading
                    else -> ViewState.Idle
                }
            }
            .onEach { stateData.postValue(it) }
            .launchIn(viewModelScope)

        actions
            .consumeAsFlow()
            .onEach { action ->
                when (action) {
                    is Action.SetEmojiIcon -> setEmojiIcon(
                        params = SetDocumentEmojiIcon.Params(
                            target = action.target,
                            emoji = action.unicode,
                            context = action.context
                        )
                    ).proceed(
                        success = {
                            dispatcher.send(it)
                            details.setEmojiIcon(target = action.target, unicode = action.unicode)
                            events.send(Event.OnCompleted)
                        },
                        failure = { events.send(Event.Failure(it)) }
                    )
                    is Action.ClearEmoji -> setEmojiIcon(
                        params = SetDocumentEmojiIcon.Params(
                            target = action.target,
                            emoji = "",
                            context = action.context
                        )
                    ).proceed(
                        success = { payload ->
                            dispatcher.send(payload)
                            details.removeIcon(action.target)
                            events.send(Event.OnCompleted)
                        },
                        failure = { events.send(Event.Failure(it)) }
                    )
                    is Action.SetImageIcon -> setImageIcon(
                        SetDocumentImageIcon.Params(
                            context = action.context,
                            path = action.path
                        )
                    ).proceed(
                        failure = { events.send(Event.Failure(it)) },
                        success = { (payload, hash) ->
                            dispatcher.send(payload)
                            details.setImageIcon(target = action.context, hash = hash)
                            events.send(Event.OnCompleted)
                        }
                    )
                    is Action.PickRandomEmoji -> {
                        val random = Emoji.DATA.random().random()
                        events.send(
                            Event.OnRandomEmojiSelected(
                                target = action.target,
                                context = action.context,
                                unicode = random
                            )
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: Event) {
        viewModelScope.launch { events.send(event) }
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Uploading : ViewState()
        object Exit : ViewState()
        object Idle : ViewState()
        data class Error(val message: String) : ViewState()
    }

    sealed class Contract {

        sealed class Action {

            class PickRandomEmoji(
                val context: String,
                val target: String
            ) : Action()

            class ClearEmoji(
                val target: String,
                val context: String
            ) : Action()

            class SetEmojiIcon(
                val unicode: String,
                val target: String,
                val context: String
            ) : Action()

            class SetImageIcon(
                val context: String,
                val path: String
            ) : Action()
        }

        data class State(
            val isLoading: Boolean,
            val isUploading: Boolean = false,
            val isCompleted: Boolean = false,
            val error: String? = null
        ) : Contract() {
            companion object {
                fun init() = State(isLoading = false)
            }
        }

        sealed class Event : Contract() {

            class OnImagePickedFromGallery(
                val context: String,
                val path: String
            ) : Event()

            class OnSetRandomEmojiClicked(
                val target: String,
                val context: String
            ) : Event()

            class OnRandomEmojiSelected(
                val unicode: String,
                val context: String,
                val target: String
            ) : Event()

            class OnRemoveEmojiSelected(
                val context: String,
                val target: String
            ) : Event()

            object OnCompleted : Event()

            class Failure(val error: Throwable) : Event()
        }
    }

    override suspend fun reduce(state: State, event: Event): State {
        return when (event) {
            is Event.OnRandomEmojiSelected -> state.copy(
                isLoading = true
            ).also {
                actions.send(
                    Action.SetEmojiIcon(
                        target = event.target,
                        context = event.context,
                        unicode = event.unicode
                    )
                )
            }
            is Event.OnSetRandomEmojiClicked -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Action.PickRandomEmoji(
                            target = event.target,
                            context = event.context
                        )
                    )
                }
            }
            is Event.OnRemoveEmojiSelected -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Action.ClearEmoji(
                            context = event.context,
                            target = event.target
                        )
                    )
                }
            }
            is Event.OnImagePickedFromGallery -> {
                state.copy(
                    isUploading = true,
                    isLoading = false,
                ).also {
                    actions.send(
                        Action.SetImageIcon(
                            context = event.context,
                            path = event.path
                        )
                    )
                }
            }
            is Event.OnCompleted -> state.copy(
                isLoading = false,
                isCompleted = true,
                error = null
            )
            is Event.Failure -> state.copy(
                isLoading = false,
                isCompleted = false,
                error = event.error.toString()
            )
        }
    }
}