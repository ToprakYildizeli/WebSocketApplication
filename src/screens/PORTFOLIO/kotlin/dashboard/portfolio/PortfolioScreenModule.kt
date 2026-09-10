package dashboard.portfolio

import customer.DashboardScreen
import dashboard.ScreenModule
import dashboard.TradeBus
import javafx.scene.Node

class PortfolioScreenModule : ScreenModule {

    override val screen =
        DashboardScreen.PORTFOLIO

    private val portfolioScreen =
        PortfolioScreen()

    override fun create(): Node {

        val content =
            portfolioScreen.create()

        // Canlı veriyi BTC ekranından değil,
        // ortak veri yolundan alıyoruz.
        TradeBus.subscribe { trade ->
            portfolioScreen.updateTrade(trade)
        }

        return content
    }

    override fun stop() {
        portfolioScreen.stop()
    }
}
