package customerRtg

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerRtgFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC
        )
}