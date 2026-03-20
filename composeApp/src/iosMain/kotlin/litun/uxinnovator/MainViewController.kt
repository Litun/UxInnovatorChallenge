package litun.uxinnovator

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import litun.uxinnovator.di.initKoin
import litun.uxinnovator.ui.root.RootComponent
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
        getUsersUseCase = koinApp!!.koin.get(),
    )
    lifecycle.resume()
    return ComposeUIViewController { App(root) }
}
