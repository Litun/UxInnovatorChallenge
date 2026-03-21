package litun.uxinnovator

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import litun.uxinnovator.components.RootComponent
import litun.uxinnovator.di.initKoin
import litun.uxinnovator.domain.repository.UserRepository
import org.koin.core.KoinApplication
import platform.UIKit.UIViewController

private var koinApp: KoinApplication? = null

fun MainViewController(goRestToken: String): UIViewController {
    if (koinApp == null) {
        koinApp = initKoin(AppConfig(goRestToken))
    }
    val lifecycle = LifecycleRegistry()
    val root = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        repository = koinApp!!.koin.get<UserRepository>(),
    )
    lifecycle.resume()
    return ComposeUIViewController { App(root) }
}
