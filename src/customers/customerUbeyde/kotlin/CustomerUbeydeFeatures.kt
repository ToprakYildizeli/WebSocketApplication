package customerUbeyde

import customer.CustomerFeatures
import customer.DashboardScreen

class CustomerUbeydeFeatures : CustomerFeatures {

    override val dashboardScreens =
        setOf(
            DashboardScreen.BTC,
DashboardScreen.PORTFOLIO,
DashboardScreen.ORDERS
        )
}