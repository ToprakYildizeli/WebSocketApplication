package dashboard

import customer.DashboardScreen
import javafx.scene.control.Tab
import javafx.scene.control.TabPane

class DashboardController {

    // =========================================================
    // TAB SIRASI
    // =========================================================
    //
    // Ekran sınıflarına referans yok, yalnızca enum değerleri.
    //
    // =========================================================

    private val displayOrder =
        listOf(
            DashboardScreen.PORTFOLIO,
            DashboardScreen.ORDERS,
            DashboardScreen.PERFORMANCE,
            DashboardScreen.BTC
        )

    private val activeModules =
        mutableListOf<ScreenModule>()

    // =========================================================
    // START
    // =========================================================

    fun start(
        tabPane: TabPane,
        screens: Set<DashboardScreen>
    ) {

        println(
            "DashboardController starting..."
        )

        println(
            "Enabled screens: $screens"
        )

        // =====================================================
        // NONE
        // =====================================================

        if (screens.isEmpty()) {

            println(
                "No screens enabled."
            )

            return
        }

        // =====================================================
        // CREATE
        // =====================================================

        displayOrder
            .filter { it in screens }
            .forEach { screen ->

                println(
                    "$screen screen starting..."
                )

                val module =
                    ScreenRegistry.load(screen)

                activeModules.add(module)

                val tab =
                    Tab(screen.name)

                tab.isClosable =
                    false

                tab.content =
                    module.create()

                tabPane.tabs.add(tab)
            }

        // =====================================================
        // START
        // =====================================================
        //
        // Tüm ekranlar kurulduktan sonra başlatılır; böylece
        // canlı veri akmaya başladığında dinleyiciler
        // (TradeBus.subscribe) çoktan kayıtlıdır.
        //
        // =====================================================

        activeModules.forEach { module ->
            module.start()
        }
    }

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {

        println(
            "DashboardController stopping..."
        )

        activeModules.forEach { module ->

            try {

                module.stop()

            } catch (_: Exception) {
            }
        }

        activeModules.clear()

        TradeBus.clear()
    }
}
