package customerB

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerBFeatures : CustomerFeatures {

    override val dashboardScreens = setOf(
        DashboardScreen.BTC,
        DashboardScreen.PERFORMANCE
    )

}