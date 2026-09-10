package dashboard.btc

import com.fasterxml.jackson.databind.ObjectMapper
import customer.DashboardScreen
import dashboard.ScreenModule
import javafx.scene.Node
import org.koin.java.KoinJavaComponent.getKoin

class BtcScreenModule : ScreenModule {

    override val screen =
        DashboardScreen.BTC

    private val btcScreen =
        BtcScreen(
            getKoin().get<ObjectMapper>()
        )

    override fun create(): Node =
        btcScreen.create()

    override fun start() {
        btcScreen.startWebSocket()
    }

    override fun stop() {
        btcScreen.stop()
    }
}
