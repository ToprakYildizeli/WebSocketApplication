package customerC

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerCFeatures : CustomerFeatures {

    override val dashboardScreens = setOf(
        DashboardScreen.BTC
    )

}