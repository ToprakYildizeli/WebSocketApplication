import customerCreator.CustomerCreatorApp
import di.creatorModule
import javafx.application.Application
import org.koin.core.context.startKoin

fun main() {

    startKoin {
        modules(
            creatorModule()
        )
    }

    Application.launch(CustomerCreatorApp::class.java)
}