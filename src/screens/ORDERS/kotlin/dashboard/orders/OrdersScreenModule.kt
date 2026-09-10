package dashboard.orders

import customer.DashboardScreen
import dashboard.ScreenModule
import javafx.scene.Node

class OrdersScreenModule : ScreenModule {

    override val screen =
        DashboardScreen.ORDERS

    private val ordersScreen =
        OrdersScreen()

    override fun create(): Node =
        ordersScreen.create()

    override fun stop() {
        ordersScreen.stop()
    }
}
