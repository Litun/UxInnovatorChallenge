package litun.uxinnovator.data.api

import kotlinx.coroutines.test.runTest
import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.GoRestException
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.getTestEnv
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests against the real GoRest API.
 * Requires network access. The HttpClient engine is auto-discovered at runtime
 * (OkHttp on Android JVM, Darwin on iOS).
 *
 * Token is read from local.properties (GO_REST_TOKEN_FOR_INTEGRATION_TESTS)
 * injected as a JVM system property by Gradle, or from the CI environment variable.
 */
class GoRestApiServiceIntegrationTest {

    private val token = requireNotNull(getTestEnv("GO_REST_TOKEN_FOR_INTEGRATION_TESTS")) {
        "Set GO_REST_TOKEN_FOR_INTEGRATION_TESTS in local.properties or as an environment variable"
    }
    private val service = GoRestApiService(token = token)

    @AfterTest
    fun tearDown() = service.close()

    // --- GET /users ---

    @Test
    fun getUsers_page1_returnsPaginatedResult() = runTest {
        val page = service.getUsers(page = 1)

        assertTrue(page.users.isNotEmpty(), "Page 1 should have users")
        assertEquals(1, page.currentPage)
        assertTrue(page.totalPages > 1, "Should have multiple pages")
        assertTrue(page.totalCount > 0, "Total count should be > 0")
    }

    @Test
    fun getUsers_page1_userHasExpectedFields() = runTest {
        val page = service.getUsers(page = 1)
        val user = page.users.first()

        assertTrue(user.id > 0)
        assertTrue(user.name.isNotEmpty())
        assertTrue(user.email.isNotEmpty())
        assertNotNull(user.gender)
        assertNotNull(user.status)
    }

    @Test
    fun getUsers_lastPage_returnsUsersWithinLimit() = runTest {
        val firstPage = service.getUsers(page = 1)
        val lastPage = service.getUsers(page = firstPage.totalPages)

        assertTrue(lastPage.users.isNotEmpty(), "Last page must have users")
        assertTrue(lastPage.users.size <= 10, "Should not exceed per_page limit of 10")
    }

    // --- POST /users ---

    @Test
    fun createUser_validRequest_returns201WithUser() = runTest {
        val email = uniqueEmail()
        val request = CreateUserRequest(
            name = "Integration Test User",
            email = email,
            gender = Gender.MALE,
            status = UserStatus.ACTIVE,
        )

        val created = service.createUser(request)
        try {
            assertTrue(created.id > 0, "Created user should have a server-assigned id")
            assertEquals("Integration Test User", created.name)
            assertEquals(email, created.email)
            assertEquals(Gender.MALE, created.gender)
            assertEquals(UserStatus.ACTIVE, created.status)
        } finally {
            service.deleteUser(created.id)
        }
    }

    @Test
    fun createUser_duplicateEmail_throwsValidationError() = runTest {
        val request = CreateUserRequest(
            name = "Duplicate Email User",
            email = uniqueEmail(),
            gender = Gender.FEMALE,
            status = UserStatus.ACTIVE,
        )
        val created = service.createUser(request)

        try {
            val ex = assertFailsWith<GoRestException.ValidationError> {
                service.createUser(request) // same email again → 422
            }
            assertTrue(ex.errors.any { it.field == "email" }, "Error should reference email field")
        } finally {
            service.deleteUser(created.id)
        }
    }

    @Test
    fun createUser_invalidToken_throwsAuthError() = runTest {
        val badService = GoRestApiService(token = "invalid_token_xyz")
        try {
            assertFailsWith<GoRestException.AuthError> {
                badService.createUser(CreateUserRequest("Test", uniqueEmail(), Gender.MALE, UserStatus.ACTIVE))
            }
        } finally {
            badService.close()
        }
    }

    // --- DELETE /users/{id} ---

    @Test
    fun deleteUser_existingUser_succeedsWithNoContent() = runTest {
        val created = service.createUser(
            CreateUserRequest("Delete Me", uniqueEmail(), Gender.FEMALE, UserStatus.ACTIVE)
        )
        try {
            service.deleteUser(created.id)
        } catch (e: Exception) {
            // If delete itself fails, attempt cleanup and rethrow
            runCatching { service.deleteUser(created.id) }
            throw e
        }
    }

    @Test
    fun deleteUser_alreadyDeleted_throwsNotFound() = runTest {
        val created = service.createUser(
            CreateUserRequest("Delete Twice", uniqueEmail(), Gender.MALE, UserStatus.INACTIVE)
        )
        service.deleteUser(created.id)

        val ex = assertFailsWith<GoRestException.NotFound> {
            service.deleteUser(created.id) // second delete → 404
        }
        assertNotNull(ex.message)
    }

    @Test
    fun deleteUser_nonExistentId_throwsNotFound() = runTest {
        assertFailsWith<GoRestException.NotFound> {
            service.deleteUser(id = 999_999_999L)
        }
    }

    private fun uniqueEmail(): String {
        val suffix = Random.nextLong(100_000_000L, 999_999_999L)
        return "ux_integration_$suffix@test.example"
    }
}
