package customer

import java.util.Properties

class ScreenCatalog(
    private val resourcePath: String = "/screens.properties"
) {

    fun available(): List<DashboardScreen> {

        val properties =
            Properties()

        val inputStream =
            ScreenCatalog::class.java
                .getResourceAsStream(resourcePath)
                ?: throw RuntimeException(
                    "$resourcePath bulunamadı!"
                )

        properties.load(inputStream)
        inputStream.close()

        return properties
            .getProperty("dashboard.screens")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { DashboardScreen.valueOf(it) }
    }
}
