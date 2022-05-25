package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.exceptions.AccountIsDeletedException
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.presentation.auth.model.SelectAccountView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    val state by lazy { MutableLiveData<List<SelectAccountView>>() }
    val error by lazy { MutableLiveData<String>() }

    private val accountChannel = Channel<Account>()

    private val accounts = accountChannel
        .consumeAsFlow()
        .scan(emptyList<Account>()) { list, value -> list + value }
        .drop(1)

    init {
        startObservingAccounts()
        startLoadingAccount()
        startDispatchingAccumulatedAccounts()
    }

    private fun startDispatchingAccumulatedAccounts() {
        accounts
            .onEach { result ->
                state.postValue(
                    result.map { account ->
                        SelectAccountView.AccountView(
                            id = account.id,
                            name = account.name,
                            image = account.avatar
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e ->
                    if (e is AccountIsDeletedException) {
                        error.postValue("This account is deleted. Try using another account or create a new one.")
                    } else {
                        error.postValue("Error while account loading \n ${e.localizedMessage}")
                    }
                    Timber.e(e, "Error while account loading")
                    navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
                },
                fnR = {
                    Timber.d("Account loading successfully finished")
                }
            )
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts.build().take(1).collect { account ->
                onFirstAccountLoaded(account.id)
            }
        }
    }

    private fun onFirstAccountLoaded(id: String) {
        navigation.postValue(EventWrapper(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onProfileClicked(id: String) {
        navigation.postValue(EventWrapper(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onAddProfileClicked() {
        // TODO
    }

    override fun onCleared() {
        super.onCleared()
        accountChannel.close()
    }
}