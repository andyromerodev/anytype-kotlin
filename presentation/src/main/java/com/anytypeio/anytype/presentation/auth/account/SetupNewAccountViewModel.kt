package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class SetupNewAccountViewState{
    object Loading: SetupNewAccountViewState()
    object Success: SetupNewAccountViewState()
    data class Error(val message: String) : SetupNewAccountViewState()
    data class InvalidCodeError(val message: String) : SetupNewAccountViewState()
    data class ErrorNetwork(val msg: String) : SetupNewAccountViewState()
}

class SetupNewAccountViewModel(
    private val session: Session,
    private val createAccount: CreateAccount,
    private val analytics: Analytics,
    private val storeObjectTypes: StoreObjectTypes
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    private val _state = MutableLiveData<SetupNewAccountViewState>()
    val state: LiveData<SetupNewAccountViewState>
        get() = _state

    init {
        _state.postValue(SetupNewAccountViewState.Loading)
        proceedWithCreatingAccount()
    }

    fun onRetryClicked() {
        _state.postValue(SetupNewAccountViewState.Loading)
        proceedWithCreatingAccount()
    }

    private fun proceedWithCreatingAccount() {
        Timber.d("Starting setting up new account")
        val startTime = System.currentTimeMillis()
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = session.name ?: throw IllegalStateException("Name not set"),
                avatarPath = session.avatarPath,
                invitationCode = session.invitationCode
            )
        ) { result ->
            result.either(
                fnL = { error ->
                    when(error) {
                        CreateAccountException.BadInviteCode -> {
                            _state.postValue(SetupNewAccountViewState.InvalidCodeError("Invalid invitation code!"))
                            viewModelScope.launch {
                                delay(300)
                                navigation.postValue(EventWrapper(AppNavigation.Command.ExitToInvitationCodeScreen))
                            }
                        }
                        CreateAccountException.NetworkError -> {
                            _state.postValue(
                                SetupNewAccountViewState.ErrorNetwork(
                                    "Failed to create your account due to a network error: ${error.message}"
                                )
                            )
                        }
                        CreateAccountException.OfflineDevice -> {
                            _state.postValue(
                                SetupNewAccountViewState.ErrorNetwork(
                                    "Your device seems to be offline. Please, check your connection and try again."
                                )
                            )
                        }
                        else -> {
                            _state.postValue(
                                SetupNewAccountViewState.Error(
                                    "Error while creating an account: ${error.message ?:"Unknown error"}"
                                )
                            )
                        }
                    }
                    Timber.e(error, "Error while creating account")
                },
                fnR = { account ->
                    updateUserProps(account.id)
                    sendAuthEvent(startTime)
                    _state.postValue(SetupNewAccountViewState.Success)
                    proceedWithUpdatingObjectTypesStore()
                }
            )
        }
    }

    private fun proceedWithUpdatingObjectTypesStore() {
        viewModelScope.launch {
            storeObjectTypes.invoke(Unit).process(
                failure = {
                    Timber.e(it, "Error while store account object types")
                    navigateToDashboard()
                },
                success = {
                    navigateToDashboard()
                }
            )
        }
    }

    private fun navigateToDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromSignUp))
    }

    private fun updateUserProps(id: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(id)
        )
    }

    private fun sendAuthEvent(start: Long) {
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = start,
            middleTime = System.currentTimeMillis(),
            eventName = EventsDictionary.createAccount
        )
    }
}