package dashboard

import dashboard.btc.BtcScreen
import dashboard.portfolio.PortfolioScreen


import com.fasterxml.jackson.databind.ObjectMapper
import customer.DashboardScreen
import dashboard.orders.OrdersScreen
import dashboard.performance.PerformanceScreen
import javafx.scene.control.Tab
import javafx.scene.control.TabPane

class DashboardController {

    private val objectMapper =
        ObjectMapper()

    private var btcScreen:
            BtcScreen? = null

    private var performanceScreen:
            PerformanceScreen? = null

    private var portfolioScreen:
            PortfolioScreen? = null

    private var ordersScreen:
            OrdersScreen? = null

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
        // PORTFOLIO
        // =====================================================

        if (DashboardScreen.PORTFOLIO in screens) {

            println(
                "Portfolio screen starting..."
            )

            portfolioScreen =
                PortfolioScreen()

            val tab =
                Tab(
                    "PORTFOLIO"
                )

            tab.isClosable =
                false

            tab.content =
                portfolioScreen!!.create()

            tabPane.tabs.add(
                tab
            )
        }

        // =====================================================
        // ORDERS
        // =====================================================

        if (DashboardScreen.ORDERS in screens) {

            println(
                "Orders screen starting..."
            )

            ordersScreen =
                OrdersScreen()

            val tab =
                Tab(
                    "ORDERS"
                )

            tab.isClosable =
                false

            tab.content =
                ordersScreen!!.create()

            tabPane.tabs.add(
                tab
            )
        }

        // =====================================================
        // PERFORMANCE
        // =====================================================

        if (DashboardScreen.PERFORMANCE in screens) {

            println(
                "Performance screen starting..."
            )

            performanceScreen =
                PerformanceScreen()

            val tab =
                Tab(
                    "PERFORMANCE"
                )

            tab.isClosable =
                false

            tab.content =
                performanceScreen!!.create()

            tabPane.tabs.add(
                tab
            )

            performanceScreen!!
                .startMonitor()
        }

        // =====================================================
        // BTC
        // =====================================================

        if (DashboardScreen.BTC in screens) {

            println(
                "BTC screen starting..."
            )

            btcScreen =
                BtcScreen(
                    objectMapper
                ) { trade ->

                    // =========================================
                    // PORTFOLIO LIVE DATA
                    // =========================================

                    portfolioScreen?.updateTrade(
                        trade
                    )
                }

            val tab =
                Tab(
                    "BTC"
                )

            tab.isClosable =
                false

            tab.content =
                btcScreen!!.create()

            tabPane.tabs.add(
                tab
            )

            // =================================================
            // WEBSOCKET
            // =================================================

            btcScreen!!
                .startWebSocket()
        }

        // =====================================================
        // NONE
        // =====================================================

        if (screens.isEmpty()) {

            println(
                "No screens enabled."
            )
        }
    }

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {

        println(
            "DashboardController stopping..."
        )

        // =====================================================
        // BTC
        // =====================================================

        try {

            btcScreen?.stop()

        } catch (_: Exception) {
        }

        // =====================================================
        // PERFORMANCE
        // =====================================================

        try {

            performanceScreen?.stop()

        } catch (_: Exception) {
        }

        // =====================================================
        // PORTFOLIO
        // =====================================================

        try {

            portfolioScreen?.stop()

        } catch (_: Exception) {
        }

        // =====================================================
        // ORDERS
        // =====================================================

        try {

            ordersScreen?.stop()

        } catch (_: Exception) {
        }
    }
}