package dashboard.btc

import model.TradeData
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import websocket.WebSocketReader

class BtcScreen(
    private val objectMapper: ObjectMapper,
    private val onTrade: ((TradeData) -> Unit)? = null
) {

    private lateinit var webSocketReader: WebSocketReader

    // =========================================================
    // COINLER
    // =========================================================

    private val symbols = listOf(
        "btcusdt",
        "ethusdt",
        "bnbusdt",
        "solusdt",
        "xrpusdt",
        "adausdt",
        "dogeusdt",
        "avaxusdt",
        "dotusdt",
        "linkusdt",
        "maticusdt",
        "ltcusdt",
        "trxusdt",
        "atomusdt",
        "nearusdt",
        "uniusdt",
        "aptusdt",
        "suiusdt",
        "filusdt",
        "etcusdt",
        "xlmusdt",
        "algousdt",
        "aaveusdt",
        "icpusdt",
        "arbusdt",
        "opusdt",
        "injusdt",
        "pepeusdt",
        "shibusdt",
        "wifusdt"
    )

    // =========================================================
    // SAYFA
    // =========================================================

    private var currentPage = 0

    private val coinsPerPage = 3

    private lateinit var pageLabel: Label
    private lateinit var coinGrid: GridPane
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    // =========================================================
    // STATUS
    // =========================================================

    private lateinit var wsStatusLabel: Label

    // =========================================================
    // COIN UI
    // =========================================================

    private val priceLabels =
        mutableMapOf<String, Label>()

    private val quantityLabels =
        mutableMapOf<String, Label>()

    private val tradeIdLabels =
        mutableMapOf<String, Label>()

    private val coinStatusLabels =
        mutableMapOf<String, Label>()

    // =========================================================
    // CREATE
    // =========================================================

    fun create(): VBox {

        val title =
            Label("BINANCE LIVE TRADE MONITOR")

        title.style = """
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        // =====================================================
        // STATUS
        // =====================================================

        wsStatusLabel =
            Label("● WS CONNECTING")

        wsStatusLabel.style = """
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-text-fill: orange;
        """.trimIndent()

        // =====================================================
        // SAYFA
        // =====================================================

        pageLabel =
            Label()

        pageLabel.style = """
            -fx-font-size: 17px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        previousButton =
            Button("← PREVIOUS")

        nextButton =
            Button("NEXT →")

        styleButton(previousButton)
        styleButton(nextButton)

        previousButton.setOnAction {
            previousPage()
        }

        nextButton.setOnAction {
            nextPage()
        }

        val navigation =
            javafx.scene.layout.HBox(
                15.0,
                previousButton,
                pageLabel,
                nextButton
            )

        navigation.alignment =
            Pos.CENTER

        // =====================================================
        // GRID
        // =====================================================

        coinGrid =
            GridPane()

        coinGrid.hgap = 15.0
        coinGrid.vgap = 15.0
        coinGrid.padding = Insets(15.0)
        coinGrid.alignment = Pos.CENTER

        updatePage()

        // =====================================================
        // ROOT
        // =====================================================

        val root =
            VBox(
                15.0,
                title,
                wsStatusLabel,
                navigation,
                coinGrid
            )

        root.alignment =
            Pos.CENTER

        root.padding =
            Insets(20.0)

        root.style = """
            -fx-background-color: #1e1e1e;
        """.trimIndent()

        return root
    }

    // =========================================================
    // WEBSOCKET
    // =========================================================

    fun startWebSocket() {

        webSocketReader =
            WebSocketReader(
                objectMapper
            ) { trade, _ ->

                Platform.runLater {

                    // Mevcut BTC ekranını güncelle
                    updateCoin(trade)

                    // Aynı canlı veriyi Portfolio'ya gönder
                    onTrade?.invoke(trade)
                }
            }

        try {

            webSocketReader.connect()

        } catch (e: Exception) {

            e.printStackTrace()

            Platform.runLater {

                wsStatusLabel.text =
                    "● WS ERROR"

                wsStatusLabel.style = """
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                    -fx-text-fill: red;
                """.trimIndent()
            }
        }
    }

    // =========================================================
    // UPDATE COIN
    // =========================================================

    private fun updateCoin(
        trade: TradeData
    ) {

        val symbol =
            trade.symbol
                ?.lowercase()
                ?: return

        if (!symbols.contains(symbol)) {
            return
        }

        val price =
            trade.price
                ?: return

        val quantity =
            trade.quantity
                ?: return

        priceLabels[symbol]
            ?.text =
            price.toPlainString()

        quantityLabels[symbol]
            ?.text =
            "Qty: ${quantity.toPlainString()}"

        tradeIdLabels[symbol]
            ?.text =
            "Trade: ${trade.tradeId}"

        coinStatusLabels[symbol]
            ?.text =
            "● LIVE"

        coinStatusLabels[symbol]
            ?.style = """
                -fx-font-size: 12px;
                -fx-text-fill: #00e676;
            """.trimIndent()

        wsStatusLabel.text =
            "● WS LIVE"

        wsStatusLabel.style = """
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-text-fill: #00e676;
        """.trimIndent()
    }

    // =========================================================
    // CARD
    // =========================================================

    private fun createCoinCard(
        symbol: String
    ): VBox {

        val name =
            Label(symbol.uppercase())

        name.style = """
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val price =
            Label("--")

        price.style = """
            -fx-font-size: 30px;
            -fx-font-weight: bold;
            -fx-text-fill: #00e676;
        """.trimIndent()

        val quantity =
            Label("Qty: --")

        quantity.style = """
            -fx-font-size: 14px;
            -fx-text-fill: #cccccc;
        """.trimIndent()

        val tradeId =
            Label("Trade: --")

        tradeId.style = """
            -fx-font-size: 13px;
            -fx-text-fill: #999999;
        """.trimIndent()

        val status =
            Label("● WAITING")

        status.style = """
            -fx-font-size: 12px;
            -fx-text-fill: orange;
        """.trimIndent()

        priceLabels[symbol] =
            price

        quantityLabels[symbol] =
            quantity

        tradeIdLabels[symbol] =
            tradeId

        coinStatusLabels[symbol] =
            status

        val card =
            VBox(
                8.0,
                name,
                price,
                quantity,
                tradeId,
                status
            )

        card.alignment =
            Pos.CENTER

        card.padding =
            Insets(20.0)

        card.prefWidth =
            330.0

        card.prefHeight =
            190.0

        card.style = """
            -fx-background-color: #252525;
            -fx-background-radius: 12px;
            -fx-border-color: #383838;
            -fx-border-radius: 12px;
            -fx-border-width: 1px;
        """.trimIndent()

        return card
    }

    // =========================================================
    // PAGE
    // =========================================================

    private fun updatePage() {

        coinGrid.children.clear()

        priceLabels.clear()
        quantityLabels.clear()
        tradeIdLabels.clear()
        coinStatusLabels.clear()

        val start =
            currentPage * coinsPerPage

        val end =
            minOf(
                start + coinsPerPage,
                symbols.size
            )

        val currentSymbols =
            symbols.subList(
                start,
                end
            )

        currentSymbols.forEachIndexed {
                index,
                symbol ->

            val card =
                createCoinCard(symbol)

            coinGrid.add(
                card,
                index,
                0
            )
        }

        val totalPages =
            (symbols.size + coinsPerPage - 1) /
                    coinsPerPage

        pageLabel.text =
            "PAGE ${currentPage + 1} / $totalPages"

        previousButton.isDisable =
            currentPage == 0

        nextButton.isDisable =
            currentPage >= totalPages - 1
    }

    // =========================================================
    // NEXT
    // =========================================================

    private fun nextPage() {

        val totalPages =
            (symbols.size + coinsPerPage - 1) /
                    coinsPerPage

        if (currentPage < totalPages - 1) {

            currentPage++

            updatePage()
        }
    }

    // =========================================================
    // PREVIOUS
    // =========================================================

    private fun previousPage() {

        if (currentPage > 0) {

            currentPage--

            updatePage()
        }
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private fun styleButton(
        button: Button
    ) {

        button.style = """
            -fx-background-color: #333333;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8px 16px;
        """.trimIndent()
    }

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {

        try {

            webSocketReader.close()

        } catch (_: Exception) {
        }
    }
}