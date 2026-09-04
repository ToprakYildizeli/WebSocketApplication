package dashboard.portfolio

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.TradeData
import java.math.BigDecimal
import java.math.RoundingMode

class PortfolioScreen {

    // =========================================================
    // MOCK PORTFOLIO
    // =========================================================
    //
    // Gerçek Binance account değildir.
    // Demo amaçlı sanal holdings.
    //
    // BTC:
    // 1 BTC
    // Alış fiyatı = 75,000 USDT
    //
    // ETH:
    // 2 ETH
    // Alış fiyatı = 3,500 USDT
    //
    // SOL:
    // 10 SOL
    // Alış fiyatı = 150 USDT
    //
    // USDT:
    // 2,000 USDT
    //
    // =========================================================

    private val holdings =
        listOf(

            Holding(
                symbol = "BTC",
                pair = "BTCUSDT",
                quantity = BigDecimal("1.0"),
                averageBuyPrice = BigDecimal("75000")
            ),

            Holding(
                symbol = "ETH",
                pair = "ETHUSDT",
                quantity = BigDecimal("2.0"),
                averageBuyPrice = BigDecimal("3500")
            ),

            Holding(
                symbol = "SOL",
                pair = "SOLUSDT",
                quantity = BigDecimal("10.0"),
                averageBuyPrice = BigDecimal("150")
            ),

            Holding(
                symbol = "USDT",
                pair = "USDT",
                quantity = BigDecimal("2000"),
                averageBuyPrice = BigDecimal("1")
            )
        )

    // =========================================================
    // CURRENT PRICES
    // =========================================================

    private val currentPrices =
        mutableMapOf<String, BigDecimal>()

    // =========================================================
    // UI
    // =========================================================

    private lateinit var totalBalanceLabel: Label
    private lateinit var totalProfitLabel: Label
    private lateinit var liveStatusLabel: Label

    private lateinit var assetGrid: GridPane

    // =========================================================
    // CREATE
    // =========================================================

    fun create(): BorderPane {

        initializeDemoPrices()

        val root =
            BorderPane()

        root.padding =
            Insets(25.0)

        root.style = """
            -fx-background-color: #1e1e1e;
        """.trimIndent()

        // =====================================================
        // HEADER
        // =====================================================

        val title =
            Label("PORTFOLIO")

        title.style = """
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val subtitle =
            Label("Demo Trading Account")

        subtitle.style = """
            -fx-font-size: 13px;
            -fx-text-fill: #888888;
        """.trimIndent()

        liveStatusLabel =
            Label("● MARKET DATA WAITING")

        liveStatusLabel.style = """
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: orange;
        """.trimIndent()

        val header =
            VBox(
                5.0,
                title,
                subtitle,
                liveStatusLabel
            )

        // =====================================================
        // BALANCE CARD
        // =====================================================

        val balanceTitle =
            Label("TOTAL BALANCE")

        balanceTitle.style = """
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #777777;
        """.trimIndent()

        totalBalanceLabel =
            Label("$0.00")

        totalBalanceLabel.style = """
            -fx-font-size: 36px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val profitTitle =
            Label("TOTAL P&L")

        profitTitle.style = """
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #777777;
        """.trimIndent()

        totalProfitLabel =
            Label("$0.00")

        totalProfitLabel.style = """
            -fx-font-size: 17px;
            -fx-font-weight: bold;
            -fx-text-fill: #00e676;
        """.trimIndent()

        val balanceContent =
            VBox(
                7.0,
                balanceTitle,
                totalBalanceLabel,
                profitTitle,
                totalProfitLabel
            )

        balanceContent.padding =
            Insets(22.0)

        balanceContent.prefWidth =
            500.0

        balanceContent.style = """
            -fx-background-color: #252525;
            -fx-background-radius: 14px;
            -fx-border-color: #383838;
            -fx-border-radius: 14px;
            -fx-border-width: 1px;
        """.trimIndent()

        // =====================================================
        // ASSET TITLE
        // =====================================================

        val assetsTitle =
            Label("ASSETS")

        assetsTitle.style = """
            -fx-font-size: 17px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        // =====================================================
        // TABLE
        // =====================================================

        assetGrid =
            GridPane()

        assetGrid.hgap = 20.0
        assetGrid.vgap = 12.0

        assetGrid.prefWidth = 900.0

        createTableHeader()

        holdings.forEachIndexed {
                index,
                holding ->

            createAssetRow(
                holding,
                index + 1
            )
        }

        // =====================================================
        // CONTENT
        // =====================================================

        val content =
            VBox(
                20.0,
                header,
                balanceContent,
                assetsTitle,
                assetGrid
            )

        content.padding =
            Insets(0.0)

        root.center =
            content

        updatePortfolio()

        return root
    }

    // =========================================================
    // INITIAL DEMO PRICES
    // =========================================================

    private fun initializeDemoPrices() {

        currentPrices["BTCUSDT"] =
            BigDecimal("80000")

        currentPrices["ETHUSDT"] =
            BigDecimal("4000")

        currentPrices["SOLUSDT"] =
            BigDecimal("180")

        currentPrices["USDT"] =
            BigDecimal.ONE
    }

    // =========================================================
    // TABLE HEADER
    // =========================================================

    private fun createTableHeader() {

        addHeader(
            "ASSET",
            0
        )

        addHeader(
            "QUANTITY",
            1
        )

        addHeader(
            "AVG. BUY",
            2
        )

        addHeader(
            "CURRENT PRICE",
            3
        )

        addHeader(
            "VALUE",
            4
        )

        addHeader(
            "P&L",
            5
        )

        addHeader(
            "ALLOCATION",
            6
        )
    }

    private fun addHeader(
        text: String,
        column: Int
    ) {

        val label =
            Label(text)

        label.style = """
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #777777;
        """.trimIndent()

        assetGrid.add(
            label,
            column,
            0
        )
    }

    // =========================================================
    // ASSET ROW
    // =========================================================

    private fun createAssetRow(
        holding: Holding,
        row: Int
    ) {

        val asset =
            Label(holding.symbol)

        asset.style = """
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val quantity =
            Label(
                holding.quantity.formatQuantity()
            )

        quantity.style = """
            -fx-font-size: 14px;
            -fx-text-fill: #cccccc;
        """.trimIndent()

        val averageBuy =
            Label(
                "$${holding.averageBuyPrice.formatMoney()}"
            )

        averageBuy.style = """
            -fx-font-size: 14px;
            -fx-text-fill: #aaaaaa;
        """.trimIndent()

        val currentPrice =
            Label("--")

        currentPrice.style = """
            -fx-font-size: 14px;
            -fx-text-fill: #cccccc;
        """.trimIndent()

        val value =
            Label("--")

        value.style = """
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val profit =
            Label("--")

        profit.style = """
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-text-fill: #00e676;
        """.trimIndent()

        val allocation =
            Label("--")

        allocation.style = """
            -fx-font-size: 14px;
            -fx-text-fill: #00e676;
        """.trimIndent()

        holding.currentPriceLabel =
            currentPrice

        holding.valueLabel =
            value

        holding.profitLabel =
            profit

        holding.allocationLabel =
            allocation

        assetGrid.add(
            asset,
            0,
            row
        )

        assetGrid.add(
            quantity,
            1,
            row
        )

        assetGrid.add(
            averageBuy,
            2,
            row
        )

        assetGrid.add(
            currentPrice,
            3,
            row
        )

        assetGrid.add(
            value,
            4,
            row
        )

        assetGrid.add(
            profit,
            5,
            row
        )

        assetGrid.add(
            allocation,
            6,
            row
        )
    }

    // =========================================================
    // TRADE UPDATE
    // =========================================================
    //
    // BtcScreen'den gelen TradeData buraya gelir.
    //
    // Örnek:
    //
    // BTCUSDT = 82,500
    //
    // 1 BTC × 82,500
    // = 82,500 USDT
    //
    // =========================================================

    fun updateTrade(
        trade: TradeData
    ) {

        val symbol =
            trade.symbol
                ?.uppercase()
                ?: return

        val price =
            trade.price
                ?: return

        updatePrice(
            symbol,
            price
        )
    }

    // =========================================================
    // PRICE UPDATE
    // =========================================================

    fun updatePrice(
        symbol: String,
        price: BigDecimal
    ) {

        val normalizedSymbol =
            symbol.uppercase()

        if (
            holdings.none {
                it.pair == normalizedSymbol
            }
        ) {
            return
        }

        currentPrices[
            normalizedSymbol
        ] = price

        updatePortfolio()

        liveStatusLabel.text =
            "● MARKET DATA LIVE"

        liveStatusLabel.style = """
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #00e676;
        """.trimIndent()
    }

    // =========================================================
    // UPDATE PORTFOLIO
    // =========================================================

    private fun updatePortfolio() {

        var totalBalance =
            BigDecimal.ZERO

        var totalCost =
            BigDecimal.ZERO

        val values =
            mutableMapOf<
                    Holding,
                    BigDecimal
                    >()

        // -----------------------------------------------------
        // CALCULATE
        // -----------------------------------------------------

        for (holding in holdings) {

            val currentPrice =
                currentPrices[
                    holding.pair
                ]
                    ?: holding.averageBuyPrice

            val currentValue =
                holding.quantity
                    .multiply(currentPrice)

            val cost =
                holding.quantity
                    .multiply(
                        holding.averageBuyPrice
                    )

            values[holding] =
                currentValue

            totalBalance =
                totalBalance.add(
                    currentValue
                )

            totalCost =
                totalCost.add(
                    cost
                )
        }

        val totalProfit =
            totalBalance.subtract(
                totalCost
            )

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        totalBalanceLabel.text =
            "$${totalBalance.formatMoney()}"

        val profitPercentage =
            if (totalCost > BigDecimal.ZERO) {

                totalProfit
                    .divide(
                        totalCost,
                        6,
                        RoundingMode.HALF_UP
                    )
                    .multiply(
                        BigDecimal("100")
                    )

            } else {

                BigDecimal.ZERO
            }

        val profitSign =
            if (
                totalProfit >=
                BigDecimal.ZERO
            ) {
                "+"
            } else {
                ""
            }

        totalProfitLabel.text =
            "$profitSign$${totalProfit.formatMoney()} " +
                    "($profitSign${profitPercentage.formatMoney()}%)"

        totalProfitLabel.style =
            if (
                totalProfit >=
                BigDecimal.ZERO
            ) {

                """
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-text-fill: #00e676;
                """.trimIndent()

            } else {

                """
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-text-fill: #ff5252;
                """.trimIndent()
            }

        // -----------------------------------------------------
        // ROWS
        // -----------------------------------------------------

        for (holding in holdings) {

            val currentPrice =
                currentPrices[
                    holding.pair
                ]
                    ?: holding.averageBuyPrice

            val currentValue =
                values[holding]
                    ?: BigDecimal.ZERO

            val cost =
                holding.quantity
                    .multiply(
                        holding.averageBuyPrice
                    )

            val profit =
                currentValue.subtract(
                    cost
                )

            val allocation =
                if (
                    totalBalance >
                    BigDecimal.ZERO
                ) {

                    currentValue
                        .divide(
                            totalBalance,
                            6,
                            RoundingMode.HALF_UP
                        )
                        .multiply(
                            BigDecimal("100")
                        )

                } else {

                    BigDecimal.ZERO
                }

            holding.currentPriceLabel?.text =
                "$${currentPrice.formatMoney()}"

            holding.valueLabel?.text =
                "$${currentValue.formatMoney()}"

            val profitSign =
                if (
                    profit >=
                    BigDecimal.ZERO
                ) {
                    "+"
                } else {
                    ""
                }

            holding.profitLabel?.text =
                "$profitSign$${profit.formatMoney()}"

            holding.profitLabel?.style =
                if (
                    profit >=
                    BigDecimal.ZERO
                ) {

                    """
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #00e676;
                    """.trimIndent()

                } else {

                    """
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #ff5252;
                    """.trimIndent()
                }

            holding.allocationLabel?.text =
                "${allocation.formatMoney()}%"
        }
    }

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {
        // Şimdilik kapatılacak resource yok.
    }

    // =========================================================
    // HOLDING
    // =========================================================

    private class Holding(
        val symbol: String,
        val pair: String,
        val quantity: BigDecimal,
        val averageBuyPrice: BigDecimal
    ) {

        var currentPriceLabel: Label? = null

        var valueLabel: Label? = null

        var profitLabel: Label? = null

        var allocationLabel: Label? = null
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private fun BigDecimal.formatMoney(): String {

        return String.format(
            "%,.2f",
            this.toDouble()
        )
    }

    private fun BigDecimal.formatQuantity(): String {

        return String.format(
            "%,.6f",
            this.toDouble()
        )
    }
}