package customerAli

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerAliFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC,
DashboardScreen.PORTFOLIO,
DashboardScreen.ORDERS
        )
}