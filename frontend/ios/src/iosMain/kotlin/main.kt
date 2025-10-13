import androidx.compose.ui.window.ComposeUIViewController
import com.timothymarias.cookingapp.shared.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }