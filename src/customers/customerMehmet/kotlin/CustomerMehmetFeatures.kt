package customerMehmet

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerMehmetFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC,
DashboardScreen.PORTFOLIO,
DashboardScreen.ORDERS
        )
}