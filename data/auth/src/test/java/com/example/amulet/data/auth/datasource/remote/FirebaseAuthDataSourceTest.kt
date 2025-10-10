package com.example.amulet.data.auth.datasource.remote

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.UserId
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.After

class FirebaseAuthDataSourceTest {

    @MockK(relaxed = true)
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var dataSource: FirebaseAuthDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(GoogleAuthProvider::class)
        dataSource = FirebaseAuthDataSource(firebaseAuth)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `signUp returns uid on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "new-user-123"
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
        }
        val email = "new@example.com"
        val password = "secret"

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns Tasks.forResult(authResult)

        val result = dataSource.signUp(UserCredentials(email = email, password = password))

        assertEquals(UserId("new-user-123"), result.component1())
        assertNull(result.component2())
    }

    @Test
    fun `signUp maps failures to app error`() = runTest {
        val email = "new@example.com"
        val password = "secret"
        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns Tasks.forException(FirebaseAuthInvalidCredentialsException("auth", "invalid"))

        val result = dataSource.signUp(UserCredentials(email = email, password = password))

        assertNull(result.component1())
        assertEquals(AppError.Unauthorized, result.component2())
    }

    @Test
    fun `signIn returns uid on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "user-123"
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
        }
        val email = "test@example.com"
        val password = "secret"

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns Tasks.forResult(authResult)

        val result = dataSource.signIn(UserCredentials(email = email, password = password))

        assertEquals(UserId("user-123"), result.component1())
        assertNull(result.component2())
    }

    @Test
    fun `signIn maps invalid credentials to unauthorized`() = runTest {
        val email = "test@example.com"
        val password = "wrong"
        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns Tasks.forException(FirebaseAuthInvalidCredentialsException("auth", "invalid"))

        val result = dataSource.signIn(UserCredentials(email = email, password = password))

        assertNull(result.component1())
        assertEquals(AppError.Unauthorized, result.component2())
    }

    @Test
    fun `signIn maps network exception to network error`() = runTest {
        val email = "test@example.com"
        val password = "secret"
        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns Tasks.forException(FirebaseNetworkException("offline"))

        val result = dataSource.signIn(UserCredentials(email = email, password = password))

        assertNull(result.component1())
        assertEquals(AppError.Network, result.component2())
    }

    @Test
    fun `signInWithGoogle returns uid on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "google-user-123"
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
        }
        val credential = mockk<AuthCredential>()
        every { GoogleAuthProvider.getCredential("sample-token", null) } returns credential
        every { firebaseAuth.signInWithCredential(credential) } returns Tasks.forResult(authResult)

        val result = dataSource.signInWithGoogle("sample-token")

        assertEquals(UserId("google-user-123"), result.component1())
        assertNull(result.component2())
    }

    @Test
    fun `signInWithGoogle maps failures to app error`() = runTest {
        val credential = mockk<AuthCredential>()
        every { GoogleAuthProvider.getCredential("sample-token", null) } returns credential
        every { firebaseAuth.signInWithCredential(credential) } returns Tasks.forException(FirebaseNetworkException("offline"))

        val result = dataSource.signInWithGoogle("sample-token")

        assertNull(result.component1())
        assertEquals(AppError.Network, result.component2())
    }

    @Test
    fun `signOut clears session successfully`() = runTest {
        every { firebaseAuth.signOut() } just runs

        val result = dataSource.signOut()

        assertNotNull(result.component1())
        assertNull(result.component2())
    }
}
