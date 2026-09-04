package customerG

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerGFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC,
DashboardScreen.PERFORMANCE
        )
}