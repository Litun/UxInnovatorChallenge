package litun.uxinnovator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import litun.uxinnovator.ui.feed.UserFeedScreen
import litun.uxinnovator.ui.root.RootComponent
import litun.uxinnovator.ui.theme.AppTheme

@Composable
fun App(root: RootComponent) {
    var darkTheme by remember { mutableStateOf(true) }
    AppTheme(darkTheme = darkTheme) {
        Children(stack = root.stack) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.UserFeed -> UserFeedScreen(
                    component = instance.component,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }
}
