package customer

class FeatureValidator {

    fun validate(
        screens: Set<DashboardScreen>
    ) {


        if (DashboardScreen.BTC !in screens) {

            throw IllegalArgumentException(
                "BTC is always required"
            )
        }


        FeatureRules.rules.forEach {
                (screen, rule) ->

            if (screen !in screens) {
                return@forEach
            }

            rule.requires.forEach {
                    requiredScreen ->

                if (requiredScreen !in screens) {

                    throw IllegalArgumentException(
                        "$screen requires $requiredScreen"
                    )
                }
            }

            rule.excludes.forEach {
                    excludedScreen ->

                if (excludedScreen in screens) {

                    throw IllegalArgumentException(
                        "$screen excludes $excludedScreen"
                    )
                }
            }
        }
    }
}