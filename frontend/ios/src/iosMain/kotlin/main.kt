import androidx.compose.ui.window.ComposeUIViewController
import com.timothymarias.cookingapp.shared.App
import com.timothymarias.cookingapp.shared.ServiceLocator
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    // Initialize shared SQLDelight database and repositories for iOS
    ServiceLocator.init()
    App()
}