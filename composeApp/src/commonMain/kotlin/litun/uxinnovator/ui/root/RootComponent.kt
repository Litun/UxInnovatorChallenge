package litun.uxinnovator.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import litun.uxinnovator.domain.usecase.GetUsersUseCase
import litun.uxinnovator.ui.feed.UserFeedComponent

class RootComponent(
    componentContext: ComponentContext,
    private val getUsersUseCase: GetUsersUseCase,
) : ComponentContext by componentContext {

    @Serializable
    sealed class Config {
        @Serializable
        data object UserFeed : Config()
    }

    sealed class Child {
        class UserFeed(val component: UserFeedComponent) : Child()
    }

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.UserFeed,
        handleBackButton = true,
        childFactory = { config, ctx ->
            when (config) {
                is Config.UserFeed -> Child.UserFeed(UserFeedComponent(ctx, getUsersUseCase))
            }
        },
    )
}
