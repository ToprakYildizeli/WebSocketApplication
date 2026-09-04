package customerD

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerDFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC
        )
}