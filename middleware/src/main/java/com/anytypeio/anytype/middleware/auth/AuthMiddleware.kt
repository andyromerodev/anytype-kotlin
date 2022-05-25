package com.anytypeio.anytype.middleware.auth

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.FeaturesConfigEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.data.auth.repo.AuthRemote
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.converters.toAccountEntity
import com.anytypeio.anytype.middleware.interactor.Middleware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthMiddleware(
    private val middleware: Middleware,
    private val events: EventProxy
) : AuthRemote {

    override suspend fun startAccount(
        id: String,
        path: String
    ): Triple<AccountEntity, FeaturesConfigEntity, AccountStatus> {
        val response = middleware.selectAccount(id, path)
        val account = AccountEntity(
            id = response.id,
            name = response.name,
            color = response.avatar?.color
        )
        val featuresConfig = FeaturesConfigEntity(
            enableDataView = response.enableDataView,
            enableDebug = response.enableDebug,
            enableChannelSwitch = response.enableChannelSwitch,
            enableSpaces = response.enableSpaces
        )
        return Triple(account, featuresConfig, response.accountStatus ?: AccountStatus.Unknown)
    }

    override suspend fun createAccount(
        name: String,
        avatarPath: String?,
        invitationCode: String
    ) = withContext(Dispatchers.IO) {
        middleware.createAccount(name, avatarPath, invitationCode).let { response ->
            AccountEntity(
                id = response.id,
                name = response.name,
                color = response.avatar?.color
            )
        }
    }

    override suspend fun deleteAccount(): AccountStatus = middleware.deleteAccount()
    override suspend fun restoreAccount(): AccountStatus = middleware.restoreAccount()

    override suspend fun recoverAccount() = withContext(Dispatchers.IO) {
        middleware.recoverAccount()
    }

    override fun observeAccounts() = events
        .flow()
        .filter { event ->
            event.messages.any { message ->
                message.accountShow != null
            }
        }
        .map { event ->
            event.messages.filter { message ->
                message.accountShow != null
            }
        }
        .flatMapConcat { messages -> messages.asFlow() }
        .map {
            val event = it.accountShow
            checkNotNull(event)
            event.toAccountEntity()
        }

    override suspend fun createWallet(
        path: String
    ) = WalletEntity(mnemonic = middleware.createWallet(path).mnemonic)

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        middleware.recoverWallet(path, mnemonic)
    }

    override suspend fun convertWallet(entropy: String): String = middleware.convertWallet(entropy)

    override suspend fun logout(clearLocalRepositoryData: Boolean) {
        middleware.logout(clearLocalRepositoryData)
    }

    override suspend fun getVersion(): String {
        return middleware.getMiddlewareVersion().version
    }
}