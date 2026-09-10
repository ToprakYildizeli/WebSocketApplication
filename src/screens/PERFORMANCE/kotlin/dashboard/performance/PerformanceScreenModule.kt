package dashboard.performance

import customer.DashboardScreen
import dashboard.ScreenModule
import javafx.scene.Node

class PerformanceScreenModule : ScreenModule {

    override val screen =
        DashboardScreen.PERFORMANCE

    private val performanceScreen =
        PerformanceScreen()

    override fun create(): Node =
        performanceScreen.create()

    override fun start() {
        performanceScreen.startMonitor()
    }

    override fun stop() {
        performanceScreen.stop()
    }
}
