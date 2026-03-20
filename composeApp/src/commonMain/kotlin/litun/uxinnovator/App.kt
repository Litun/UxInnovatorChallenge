package litun.uxinnovator

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import litun.uxinnovator.ui.feed.UserFeedScreen
import litun.uxinnovator.ui.root.RootComponent
import litun.uxinnovator.ui.theme.AppTheme

@Composable
fun App(root: RootComponent) {
    AppTheme {
        Children(stack = root.stack) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.UserFeed -> UserFeedScreen(instance.component)
            }
        }
    }
}
