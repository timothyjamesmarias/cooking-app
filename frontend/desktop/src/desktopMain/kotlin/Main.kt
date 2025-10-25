import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.timothymarias.cookingapp.shared.App
import com.timothymarias.cookingapp.shared.ServiceLocator

fun main() = application {
    // Initialize shared SQLDelight database and repositories for Desktop
    ServiceLocator.init()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cooking App"
    ) {
        App()
    }
}