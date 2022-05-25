package com.anytypeio.anytype.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.Duration
import kotlin.test.assertEquals

class DeleteAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var helper: DateHelper

    lateinit var restoreAccount: RestoreAccount
    lateinit var logout: Logout

    lateinit var vm: DeletedAccountViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        restoreAccount = RestoreAccount(
            repo = repo
        )
        logout = Logout(
            repo = repo
        )
        vm = DeletedAccountViewModel(
            restoreAccount = restoreAccount,
            logout = logout,
            dateHelper = helper
        )
    }

    @Test
    fun `progress should be zero when view model is created`() {
        assertEquals(
            expected = 0f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,5 when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(15).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 0.5f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,6 when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(10).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 1 - 1 / 3f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,3 when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(20).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 1 - 2 / 3f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 1,0 when deadline equals to now`() {
        val nowInMillis = System.currentTimeMillis()
        vm.onStart(
            nowInMillis = nowInMillis,
            deadlineInMillis = nowInMillis
        )
        assertEquals(
            expected = 1f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0 when deadline is in 30days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(30).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 0f,
            actual = vm.progress.value
        )
    }
}