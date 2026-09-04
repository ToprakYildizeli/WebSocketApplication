package customer

data class FeatureRule(
    val requires: Set<DashboardScreen> = emptySet(),
    val excludes: Set<DashboardScreen> = emptySet()
)

object FeatureRules {

    val rules =
        mapOf(


            DashboardScreen.BTC to FeatureRule(),

            DashboardScreen.PERFORMANCE to FeatureRule(
                requires = setOf(
                    DashboardScreen.BTC
                )
            ),

            DashboardScreen.PORTFOLIO to FeatureRule(
                requires = setOf(
                    DashboardScreen.BTC,
                    DashboardScreen.ORDERS
                ),
                excludes = setOf(
                    DashboardScreen.PERFORMANCE
                )
            ),

            DashboardScreen.ORDERS to FeatureRule(
                requires = setOf(
                    DashboardScreen.BTC,
                    DashboardScreen.PORTFOLIO
                ),
                excludes = setOf(
                    DashboardScreen.PERFORMANCE
                )
            )
        )
}