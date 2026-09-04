package customerA

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerAFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC,
            DashboardScreen.PORTFOLIO,
            DashboardScreen.ORDERS
        )
}
