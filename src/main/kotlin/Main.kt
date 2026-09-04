import customer.CustomerFactory
import customer.CustomerFeatures
import customer.FeatureValidator
import dashboard.Dashboard
import di.appModule
import javafx.application.Application
import org.koin.core.context.startKoin

fun main() {

    val config =
        CustomerFactory.config()

    val features =
        CustomerFactory.features()

    println("Starting application for customer: ${config.name}")

    startKoin {
        modules(
            appModule(
                customerConfig = config,
                customerFeatures = features
            )
        )
    }

    val koin = org.koin.core.context.GlobalContext.get()

    val customerFeatures = koin.get<CustomerFeatures>()

    val featureValidator = koin.get<FeatureValidator>()

    featureValidator.validate(customerFeatures.dashboardScreens)

    Application.launch(Dashboard::class.java)
}