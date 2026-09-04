package customer

import java.nio.file.Files
import java.nio.file.Path

class CustomerGenerator(
    private val customersDirectory: Path
) {

    fun generate(
        customerName: String,
        dashboardScreens: Set<DashboardScreen>
    ) {
        val customerId =
            customerName.toCustomerId()

        validateCustomerName(customerName)
        validateFeatures(dashboardScreens)

        val customerDirectory =
            customersDirectory.resolve(customerId)

        if (Files.exists(customerDirectory)) {
            error("Customer already exists: $customerName")
        }

        val kotlinDirectory =
            customerDirectory.resolve("kotlin")

        Files.createDirectories(kotlinDirectory)

        try {

            generateConfig(
                customerId = customerId,
                displayName = customerName,
                kotlinDirectory = kotlinDirectory
            )

            generateFeatures(
                customerId = customerId,
                kotlinDirectory = kotlinDirectory,
                dashboardScreens = dashboardScreens
            )

        } catch (exception: Exception) {

            customerDirectory
                .toFile()
                .deleteRecursively()

            throw exception
        }
    }

    private fun validateFeatures(
        selectedScreens: Set<DashboardScreen>
    ) {

        selectedScreens.forEach { screen ->

            val rule =
                FeatureRules.rules[screen]
                    ?: return@forEach

            val missingRequirements =
                rule.requires.filterNot {
                    it in selectedScreens
                }

            if (missingRequirements.isNotEmpty()) {

                val requiredNames =
                    missingRequirements.joinToString(", ") {
                        it.name
                    }

                error(
                    "${screen.name} requires: $requiredNames"
                )
            }

            val excludedScreens =
                rule.excludes.filter {
                    it in selectedScreens
                }

            if (excludedScreens.isNotEmpty()) {

                val excludedNames =
                    excludedScreens.joinToString(", ") {
                        it.name
                    }

                error(
                    "${screen.name} cannot be used together with: $excludedNames"
                )
            }
        }
    }

    private fun generateConfig(
        customerId: String,
        displayName: String,
        kotlinDirectory: Path
    ) {

        val className =
            customerId.toCustomerClassName()

        val content = """
            package $customerId

            import customer.CustomerConfig

            class ${className}Config : CustomerConfig {
                override val name = "$displayName"
                override val logoPath = "/$customerId/logo.png"
            }
        """.trimIndent()

        Files.writeString(
            kotlinDirectory.resolve(
                "${className}Config.kt"
            ),
            content
        )
    }

    private fun generateFeatures(
        customerId: String,
        kotlinDirectory: Path,
        dashboardScreens: Set<DashboardScreen>
    ) {

        val className =
            customerId.toCustomerClassName()

        val screens =
            dashboardScreens.joinToString(",\n") {
                "            DashboardScreen.${it.name}"
            }

        val content = """
            package $customerId

            import customer.CustomerFeatures
            import customer.DashboardScreen

            class ${className}Features : CustomerFeatures {

                override val dashboardScreens =
                    setOf(
            $screens
                    )
            }
        """.trimIndent()

        Files.writeString(
            kotlinDirectory.resolve(
                "${className}Features.kt"
            ),
            content
        )
    }

    private fun validateCustomerName(
        customerName: String
    ) {

        require(customerName.isNotBlank()) {
            "Customer name cannot be empty."
        }

        require(
            customerName.matches(
                Regex("[A-Za-z][A-Za-z0-9]*")
            )
        ) {
            "Customer name must start with a letter and contain only letters and numbers."
        }
    }

    private fun String.toCustomerId(): String =
        "customer" +
                replaceFirstChar {
                    it.uppercase()
                }

    private fun String.toCustomerClassName(): String =
        replaceFirstChar {
            it.uppercase()
        }
}
