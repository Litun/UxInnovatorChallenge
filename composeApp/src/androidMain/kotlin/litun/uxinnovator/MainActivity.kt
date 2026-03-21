package litun.uxinnovator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import litun.uxinnovator.components.RootComponent
import litun.uxinnovator.domain.repository.UserRepository
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val root = RootComponent(
            componentContext = defaultComponentContext(),
            repository = GlobalContext.get().get<UserRepository>(),
        )

        setContent {
            App(root)
        }
    }
}
