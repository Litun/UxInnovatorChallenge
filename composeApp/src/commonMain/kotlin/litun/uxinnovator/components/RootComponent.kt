package litun.uxinnovator.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import litun.uxinnovator.domain.repository.UserRepository

class RootComponent(
    componentContext: ComponentContext,
    private val repository: UserRepository,
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
                is Config.UserFeed -> Child.UserFeed(UserFeedComponent(ctx, repository))
            }
        },
    )
}
