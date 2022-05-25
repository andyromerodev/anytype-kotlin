package com.anytypeio.anytype.presentation.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class SplashViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Mock
    lateinit var launchAccount: LaunchAccount

    @Mock
    lateinit var launchWallet: LaunchWallet

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var auth: AuthRepository

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var userSettingsRepo: UserSettingsRepository

    private lateinit var storeObjectTypes: StoreObjectTypes
    private lateinit var getLastOpenedObject: GetLastOpenedObject

    @Mock
    private lateinit var setDefaultEditorType: SetDefaultEditorType

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var appActionManager: AppActionManager

    @Mock
    private lateinit var getDefaultEditorType: GetDefaultEditorType

    lateinit var vm: SplashViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        storeObjectTypes = StoreObjectTypes(
            repo = repo,
            objectTypesProvider = objectTypesProvider
        )
        getLastOpenedObject = GetLastOpenedObject(
            authRepo = auth,
            blockRepo = repo
        )
    }

    private fun initViewModel() {
        vm = SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics,
            storeObjectTypes = storeObjectTypes,
            getLastOpenedObject = getLastOpenedObject,
            setDefaultEditorType = setDefaultEditorType,
            getDefaultEditorType = getDefaultEditorType,
            createPage = createPage,
            appActionManager = appActionManager
        )
    }

    @Test
    fun `should not execute use case when view model is created`() {

        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(null)

        initViewModel()

        runBlocking {
            verify(checkAuthorizationStatus, times(0)).invoke(any(), any(), any())
            verify(checkAuthorizationStatus, times(0)).invoke(any())
            verifyNoMoreInteractions(checkAuthorizationStatus)
        }
    }

    @Test
    fun `should invoke checkAuthorizationStatus when getDefaultPageType on error `() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn Either.Left(Exception("error"))
        }

        initViewModel()

        runBlocking {
            verify(getDefaultEditorType, times(1)).invoke(any())
            verify(checkAuthorizationStatus, times(1)).invoke(any())
        }
    }

    @Test
    fun `should invoke checkAuthorizationStatus when getDefaultPageType is object type `() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectType.PAGE_URL)

        initViewModel()

        runBlocking {
            verify(getDefaultEditorType, times(1)).invoke(any())
            verify(checkAuthorizationStatus, times(1)).invoke(any())
        }
    }

    @Test
    fun `should not invoke checkAuthorizationStatus when getDefaultPageType is null`() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = null)

        runBlocking {
            verify(checkAuthorizationStatus, times(0)).invoke(any())
        }
    }

    @Test
    fun `should start launching wallet if user is authorized`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectType.PAGE_URL)

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
        }
    }

    @Test
    fun `should start launching account if wallet is launched`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectType.PAGE_URL)

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
            verify(launchAccount, times(1)).invoke(any())
        }
    }

    //Todo can't mock Amplitude
//    @Test
//    fun `should emit appropriate navigation command if account is launched`() {
//
//        val status = AuthStatus.AUTHORIZED
//
//        val response = Either.Right(status)
//
//
//        //Mockito.`when`(amplitude.setUserId("accountId", true)).
//        stubCheckAuthStatus(response)
//        stubLaunchAccount()
//        stubLaunchWallet()
//
//        vm.onResume()
//
//        verify(amplitude, times(1)).setUserId("accountId", true)
//
//        vm.navigation.test().assertValue { value ->
//            value.peekContent() == AppNavigation.Command.StartDesktopFromSplash
//        }
//    }

//    @Test
//    fun `should emit appropriate navigation command if user is unauthorized`() {
//
//        val status = AuthStatus.UNAUTHORIZED
//
//        val response = Either.Right(status)
//
//        stubCheckAuthStatus(response)
//        stubGetLastOpenedObject()
//
//        vm.onResume()
//
//        vm.navigation.test().assertValue { value ->
//            value.peekContent() == AppNavigation.Command.OpenStartLoginScreen
//        }
//    }

//    @Test
//    fun `should retry launching wallet after failed launch and emit error`() {
//
//        // SETUP
//
//        val status = AuthStatus.AUTHORIZED
//
//        val response = Either.Right(status)
//
//        val exception = Exception(MockDataFactory.randomString())
//
//        stubCheckAuthStatus(response)
//
//        stubLaunchWallet(response = Either.Left(exception))
//
//        // TESTING
//
//        val state = vm.state.test()
//
//        state.assertNoValue()
//
//        vm.onResume()
//
//        state.assertValue { value -> value is ViewState.Error }
//
//        runBlocking {
//            verify(launchWallet, times(2)).invoke(any())
//        }
//    }

    private fun stubCheckAuthStatus(response: Either.Right<AuthStatus>) {
        checkAuthorizationStatus.stub {
            onBlocking { invoke(eq(Unit)) } doReturn response
        }
    }

    private fun stubLaunchWallet(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        launchWallet.stub {
            onBlocking { invoke(any()) } doReturn response
        }
    }

    private fun stubLaunchAccount() {
        launchAccount.stub {
            onBlocking { invoke(any()) } doReturn Either.Right("accountId")
        }
    }

    private fun stubGetLastOpenedObject() {
        auth.stub {
            onBlocking {
                getLastOpenedObjectId()
            } doReturn null
        }
    }

    private fun stubGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn Either.Right(GetDefaultEditorType.Response(type, name))
        }
    }
}