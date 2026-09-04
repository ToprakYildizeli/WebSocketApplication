package dashboard

import customer.CustomerFeatures
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.koin.java.KoinJavaComponent.getKoin

class Dashboard : Application() {

    private val controller =
        DashboardController()

    private val features =
        getKoin().get<CustomerFeatures>()

    override fun start(stage: Stage) {

        val tabPane =
            TabPane()

        tabPane.tabClosingPolicy =
            TabPane.TabClosingPolicy.UNAVAILABLE

        // =====================================================
        // CUSTOMER FEATURES
        // =====================================================

        val screens =
            features.dashboardScreens

        println(
            "Enabled screens: $screens"
        )

        // =====================================================
        // DASHBOARD
        // =====================================================

        controller.start(
            tabPane,
            screens
        )

        // =====================================================
        // ROOT
        // =====================================================

        val root =
            BorderPane()

        root.center =
            tabPane

        root.padding =
            Insets(10.0)

        root.style = """
            -fx-background-color: #1e1e1e;
        """.trimIndent()

        // =====================================================
        // SCENE
        // =====================================================

        val scene =
            Scene(
                root,
                1200.0,
                800.0
            )

        scene.stylesheets.add(
            javaClass
                .getResource("/dashboard.css")
                ?.toExternalForm()
                ?: throw IllegalStateException(
                    "dashboard.css not found"
                )
        )

        // =====================================================
        // STAGE
        // =====================================================

        stage.title =
            "Market Application"

        stage.scene =
            scene

        stage.show()
    }

    // =========================================================
    // STOP
    // =========================================================

    override fun stop() {

        println(
            "Dashboard stopping..."
        )

        controller.stop()
    }
}
