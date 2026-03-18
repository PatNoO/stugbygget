package com.example.stugbygget.feature.auth.ui

import com.example.stugbygget.domain.model.AppUser
import com.example.stugbygget.domain.repository.AuthRepository
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.SignInWithEmailPasswordUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Auth state ────────────────────────────────────────────────────────────

    @Test
    fun `authenticated user observed sets currentUser in state`() = runTest {
        val user = AppUser(uid = "u1", displayName = "Eva", email = "eva@test.com")
        val vm = buildViewModel(currentUser = user)

        assertEquals(user, vm.uiState.value.currentUser)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `no authenticated user sets currentUser to null`() = runTest {
        val vm = buildViewModel(currentUser = null)

        assertNull(vm.uiState.value.currentUser)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `auth observe error sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnObserve = true)

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `blank email sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onEmailPasswordSignIn("", "secret")

        assertEquals("Email and password are required.", vm.uiState.value.errorMessage)
    }

    @Test
    fun `blank password sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onEmailPasswordSignIn("eva@test.com", "")

        assertEquals("Email and password are required.", vm.uiState.value.errorMessage)
    }

    @Test
    fun `email without at-sign sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onEmailPasswordSignIn("notanemail", "secret")

        assertEquals("Enter a valid email address.", vm.uiState.value.errorMessage)
    }

    // ── Sign-in ───────────────────────────────────────────────────────────────

    @Test
    fun `valid credentials calls sign-in and clears errorMessage`() = runTest {
        val repo = FakeAuthRepository()
        val vm = buildViewModel(repo = repo)

        vm.onEmailPasswordSignIn("eva@test.com", "secret")

        assertTrue(repo.signInCalled)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `sign-in failure sets errorMessage and clears isLoading`() = runTest {
        val vm = buildViewModel(throwOnSignIn = true)

        vm.onEmailPasswordSignIn("eva@test.com", "wrong")

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `sign-in trims whitespace from email`() = runTest {
        val repo = FakeAuthRepository()
        val vm = buildViewModel(repo = repo)

        vm.onEmailPasswordSignIn("  eva@test.com  ", "secret")

        assertEquals("eva@test.com", repo.lastSignInEmail)
    }

    // ── Draft fields ──────────────────────────────────────────────────────────

    @Test
    fun `onEmailChanged updates email and clears errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onEmailPasswordSignIn("", "") // trigger error

        vm.onEmailChanged("eva@test.com")

        assertEquals("eva@test.com", vm.uiState.value.email)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `onPasswordChanged updates password and clears errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onEmailPasswordSignIn("", "") // trigger error

        vm.onPasswordChanged("newpassword")

        assertEquals("newpassword", vm.uiState.value.password)
        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Sign-out ──────────────────────────────────────────────────────────────

    @Test
    fun `signOut calls repository`() = runTest {
        val repo = FakeAuthRepository()
        val vm = buildViewModel(repo = repo)

        vm.signOut()

        assertTrue(repo.signOutCalled)
    }

    @Test
    fun `signOut failure sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnSignOut = true)

        vm.signOut()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        currentUser: AppUser? = null,
        throwOnObserve: Boolean = false,
        throwOnSignIn: Boolean = false,
        throwOnSignOut: Boolean = false,
        repo: FakeAuthRepository = FakeAuthRepository(
            currentUser = currentUser,
            throwOnObserve = throwOnObserve,
            throwOnSignIn = throwOnSignIn,
            throwOnSignOut = throwOnSignOut
        )
    ): AuthViewModel = AuthViewModel(
        observeAuthUserUseCase = ObserveAuthUserUseCase(repo),
        signInWithEmailPasswordUseCase = SignInWithEmailPasswordUseCase(repo),
        signOutUseCase = SignOutUseCase(repo)
    )

    private class FakeAuthRepository(
        private val currentUser: AppUser? = null,
        private val throwOnObserve: Boolean = false,
        private val throwOnSignIn: Boolean = false,
        private val throwOnSignOut: Boolean = false
    ) : AuthRepository {
        var signInCalled = false
        var signOutCalled = false
        var lastSignInEmail: String? = null

        override fun observeCurrentUser(): Flow<AppUser?> {
            if (throwOnObserve) throw RuntimeException("Auth stream failed")
            return flowOf(currentUser)
        }

        override suspend fun signInWithEmailPassword(email: String, password: String) {
            if (throwOnSignIn) throw RuntimeException("Wrong credentials")
            signInCalled = true
            lastSignInEmail = email
        }

        override suspend fun signOut() {
            if (throwOnSignOut) throw RuntimeException("Sign-out failed")
            signOutCalled = true
        }
    }
}
