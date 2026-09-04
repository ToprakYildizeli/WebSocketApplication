package customer

object CustomerFactory {

    private val customer =
        System.getProperty("customer")
            ?: error("Customer is not specified")

    private val customerSuffix =
        customer
            .removePrefix("customer")
            .replaceFirstChar { it.uppercase() }

    private val customerPackage =
        "customer$customerSuffix"

    private val customerClassPrefix =
        "Customer$customerSuffix"

    fun config(): CustomerConfig {

        val clazz = Class.forName(
            "$customerPackage.${customerClassPrefix}Config"
        )

        return clazz
            .getDeclaredConstructor()
            .newInstance() as CustomerConfig
    }

    fun features(): CustomerFeatures {

        val clazz = Class.forName(
            "$customerPackage.${customerClassPrefix}Features"
        )

        return clazz
            .getDeclaredConstructor()
            .newInstance() as CustomerFeatures
    }
}