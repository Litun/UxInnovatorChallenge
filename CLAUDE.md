# UxInnovator — Claude Code Rules

## Project
Kotlin Multiplatform app (Android + iOS) using Compose Multiplatform. Implements a User Management System against the GoRest Public API.

## Architecture
- **Primary pattern**: Decompose (https://github.com/arkivanov/Decompose) for navigation and component lifecycle
- **Reference app**: Confetti (https://github.com/joreilly/Confetti) — follow its patterns for KMP structure, DI, and component organization. If you have a local checkout, add its path to `CLAUDE.local.md` (gitignored) so Claude can browse it directly.
- **Layers**: `domain` (models, interfaces, use cases) → `data` (API, DB implementations) → `ui` (Components, screens)
- All business logic in `commonMain`; platform code only via `expect/actual` or injected interfaces

## Decompose Patterns
- Every screen/feature is a **Component** — a plain Kotlin class implementing `ComponentContext by componentContext`
- **State**: Use `Value<T>` / `MutableValue<T>` (NOT ViewModel or StateFlow — those leak Android APIs into commonMain)
- **Navigation**: `StackNavigation` + `childStack` for screen navigation; `SlotNavigation` + `childSlot` for modals/dialogs/bottom sheets
- **Routing configs**: Sealed class with `@Serializable` subclasses per screen
- **Root component**: `RootComponent` owns top-level `childStack`, delegates to child components
- **Back handling**: Use Decompose's built-in `BackHandler` — do not use Android's BackHandler
- **Retained state**: Use `instanceKeeper.getOrCreate { }` for objects that survive config changes (replaces ViewModel)

### Component Structure Pattern
```kotlin
class UserListComponent(
    componentContext: ComponentContext,
    private val getUsersUseCase: GetUsersUseCase,
    private val onUserSelected: (User) -> Unit,
) : ComponentContext by componentContext {

    private val _state = MutableValue(UserListState())
    val state: Value<UserListState> = _state

    // business logic here
}
```

### Navigation Pattern
```kotlin
private val navigation = StackNavigation<Config>()
val stack: Value<ChildStack<*, Child>> = childStack(
    source = navigation,
    serializer = Config.serializer(),
    initialConfiguration = Config.UserList,
    handleBackButton = true,
    childFactory = ::createChild,
)

fun onUserSelected(id: Long) = navigation.push(Config.UserDetail(id))
fun onBack() = navigation.pop()
```

## KMP Rules
- **Never** put Android SDK (android.*, androidx.*) or UIKit imports in `commonMain`
- Use `expect/actual` for platform utilities (dispatchers, date formatting, DB driver)
- Source sets: `commonMain`, `androidMain`, `iosMain` — keep them minimal
- Follow Confetti's module organization: shared `commonMain` with thin platform entry points

## Naming Conventions
- Components: `UserListComponent`, `AddUserComponent`, `RootComponent`
- Repositories: `UserRepository` (interface in domain), `UserRepositoryImpl` (in data)
- Use Cases: `GetUsersUseCase`, `DeleteUserUseCase`, `AddUserUseCase`
- State/Config: `UserListState`, `RootConfig`, `UserListConfig`

## Dependencies
- **Always** declare versions in `gradle/libs.versions.toml` — never inline in build files
- **Decompose**: `com.arkivanov.decompose:decompose` + `com.arkivanov.decompose:extensions-compose`
- **Ktor**: networking (KMP-compatible)
- **SQLDelight** or **Room KMP**: local caching
- **Koin**: DI (use `koin-core` in commonMain + `koin-android` in androidMain)
- Use KMP-compatible versions of all libraries

## Dependency Docs
Fetch these when working with the relevant library:

| Library | Docs |
|---------|------|
| Koin | https://insert-koin.io/llms.txt |
| Ktor | https://raw.githubusercontent.com/ktorio/ktor/main/README.md |
| SQLDelight | https://raw.githubusercontent.com/sqldelight/sqldelight/master/README.md |
| Decompose | https://raw.githubusercontent.com/arkivanov/Decompose/master/README.md |

## Testing
- Unit tests for all Components and UseCases in `commonTest`
- Components are plain classes — test them without instrumentation
- Test file naming: `*Test.kt` alongside the feature

## Code Quality
- Run `./gradlew :composeApp:compileKotlinAndroid` to verify Android compilation
- Run `./gradlew :composeApp:allTests` to run all shared tests
- Use `simplify` skill after completing each layer

## GoRest API
- Base URL: `https://gorest.co.in/public/v2`
- Requires Bearer token in `Authorization` header for write operations (POST, DELETE)
- Key endpoints: `GET /users` (paginated), `POST /users`, `DELETE /users/{id}`
- Fetch from the **last page** of users for the feed
