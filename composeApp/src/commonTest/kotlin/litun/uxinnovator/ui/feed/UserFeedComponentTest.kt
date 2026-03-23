package litun.uxinnovator.ui.feed

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import litun.uxinnovator.components.UserFeedComponent
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.domain.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val sampleUsers = listOf(
    User(
        id = 1L,
        name = "Alice",
        email = "alice@example.com",
        gender = Gender.FEMALE,
        status = UserStatus.ACTIVE
    ),
    User(
        id = 2L,
        name = "Bob",
        email = "bob@example.com",
        gender = Gender.MALE,
        status = UserStatus.ACTIVE
    ),
)

private class FakeUserRepository(
    private val result: Result<List<User>>,
) : UserRepository {
    private val usersFlow = MutableStateFlow<List<User>>(emptyList())

    override fun observeUsers(): Flow<List<User>> = usersFlow

    override suspend fun refreshUsers() {
        usersFlow.value = result.getOrThrow()
    }

    override suspend fun createUser(
        name: String,
        email: String,
        gender: Gender,
        status: UserStatus
    ): User = error("not used")

    override suspend fun deleteUser(id: Long) = error("not used")
}

private fun makeContext(): DefaultComponentContext {
    val lifecycle = LifecycleRegistry()
    lifecycle.resume()
    return DefaultComponentContext(lifecycle = lifecycle)
}

class UserFeedComponentTest {

    @Test
    fun loadingStateSetOnInit() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val component = UserFeedComponent(
            makeContext(),
            FakeUserRepository(Result.success(sampleUsers)),
            dispatcher
        )

        assertTrue(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertTrue(component.state.value.users.isEmpty())
    }

    @Test
    fun successStateAfterLoad() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val component = UserFeedComponent(
            makeContext(),
            FakeUserRepository(Result.success(sampleUsers)),
            dispatcher
        )

        testScheduler.advanceUntilIdle()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(sampleUsers, component.state.value.users)
    }

    @Test
    fun errorStateOnFailure() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val component = UserFeedComponent(
            makeContext(),
            FakeUserRepository(Result.failure(RuntimeException("Network error"))),
            dispatcher
        )

        testScheduler.advanceUntilIdle()

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
        assertEquals("Network error", component.state.value.error)
        assertTrue(component.state.value.users.isEmpty())
    }

    @Test
    fun refreshClearsErrorAndReloads() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var callCount = 0
        val usersFlow = MutableStateFlow<List<User>>(emptyList())
        val repo = object : UserRepository {
            override fun observeUsers(): Flow<List<User>> = usersFlow

            override suspend fun refreshUsers() {
                callCount++
                if (callCount == 1) throw RuntimeException("First failure")
                usersFlow.value = sampleUsers
            }

            override suspend fun createUser(
                name: String,
                email: String,
                gender: Gender,
                status: UserStatus
            ): User = error("not used")

            override suspend fun deleteUser(id: Long) = error("not used")
        }
        val component = UserFeedComponent(makeContext(), repo, dispatcher)

        testScheduler.advanceUntilIdle()
        assertNotNull(component.state.value.error)

        component.refresh()
        testScheduler.advanceUntilIdle()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(sampleUsers, component.state.value.users)
    }
}
