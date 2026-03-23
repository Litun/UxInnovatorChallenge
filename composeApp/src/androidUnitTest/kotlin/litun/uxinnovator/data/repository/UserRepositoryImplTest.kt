package litun.uxinnovator.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import litun.uxinnovator.data.api.UserApiService
import litun.uxinnovator.data.db.AppDatabase
import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.data.model.UserDto
import litun.uxinnovator.data.model.UsersPage
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun makeDto(id: Long, name: String, email: String, gender: Gender, status: UserStatus) =
    UserDto(id = id, name = name, email = email, gender = gender, status = status)

private fun makePage(vararg dtos: UserDto, totalPages: Int = 1) =
    UsersPage(
        users = dtos.toList(),
        currentPage = 1,
        totalPages = totalPages,
        totalCount = dtos.size
    )

class UserRepositoryImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: AppDatabase
    private lateinit var fakeApi: FakeUserApiService
    private lateinit var repo: UserRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        database = AppDatabase(driver)
        fakeApi = FakeUserApiService()
        repo = UserRepositoryImpl(fakeApi, database)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun refreshUsers_singlePage_upsertsAllUsers() = runTest {
        val dtos = listOf(
            makeDto(1, "Alice", "alice@example.com", Gender.FEMALE, UserStatus.ACTIVE),
            makeDto(2, "Bob", "bob@example.com", Gender.MALE, UserStatus.INACTIVE),
        )
        fakeApi.pages[1] = makePage(*dtos.toTypedArray())

        repo.refreshUsers()

        val users = repo.observeUsers().first()
        assertEquals(2, users.size)
        assertTrue(users.any { it.id == 1L && it.name == "Alice" })
        assertTrue(users.any { it.id == 2L && it.name == "Bob" })
    }

    @Test
    fun refreshUsers_multiPage_fetchesOnlyLastPage() = runTest {
        val firstPageDtos =
            listOf(makeDto(1, "Old", "old@example.com", Gender.MALE, UserStatus.INACTIVE))
        val lastPageDtos =
            listOf(makeDto(2, "New", "new@example.com", Gender.FEMALE, UserStatus.ACTIVE))
        fakeApi.pages[1] = makePage(*firstPageDtos.toTypedArray(), totalPages = 2)
        fakeApi.pages[2] = makePage(*lastPageDtos.toTypedArray(), totalPages = 2)

        repo.refreshUsers()

        val users = repo.observeUsers().first()
        assertEquals(1, users.size)
        assertEquals(2L, users.first().id)
    }

    @Test
    fun createUser_persistsAndReturnsDomainObject() = runTest {
        val dto = makeDto(42, "Carol", "carol@example.com", Gender.FEMALE, UserStatus.ACTIVE)
        fakeApi.createResult = dto

        val user = repo.createUser("Carol", "carol@example.com", Gender.FEMALE, UserStatus.ACTIVE)

        assertEquals(User(42, "Carol", "carol@example.com", Gender.FEMALE, UserStatus.ACTIVE), user)
        assertEquals(1, repo.observeUsers().first().size)
        assertEquals(42L, repo.observeUsers().first().first().id)
    }

    @Test
    fun deleteUser_removesFromDatabase() = runTest {
        database.userQueries.upsert(10, "Eve", "eve@example.com", "female", "active")

        repo.deleteUser(10)

        assertTrue(repo.observeUsers().first().isEmpty())
    }

    @Test
    fun observeUsers_mapsGenderAndStatusStringsFromDb() = runTest {
        database.userQueries.upsert(77, "Frank", "frank@example.com", "male", "inactive")

        val user = repo.observeUsers().first().single()

        assertEquals(Gender.MALE, user.gender)
        assertEquals(UserStatus.INACTIVE, user.status)
    }
}

private class FakeUserApiService : UserApiService {
    val pages = mutableMapOf<Int, UsersPage>()
    var createResult: UserDto? = null

    override suspend fun getUsers(page: Int, perPage: Int): UsersPage =
        pages[page] ?: makePage()

    override suspend fun createUser(request: CreateUserRequest): UserDto =
        createResult ?: error("createResult not set")

    override suspend fun deleteUser(id: Long) = Unit
}
