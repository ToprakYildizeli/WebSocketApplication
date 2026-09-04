package dashboard.orders

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback

class OrdersScreen {

    // =========================================================
    // TABLE
    // =========================================================

    private lateinit var table: TableView<Order>

    // =========================================================
    // CREATE
    // =========================================================

    fun create(): BorderPane {

        val root =
            BorderPane()

        root.padding =
            Insets(25.0)

        root.style = """
            -fx-background-color: #1e1e1e;
        """.trimIndent()

        // =====================================================
        // TITLE
        // =====================================================

        val title =
            Label("ORDERS")

        title.style = """
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        val subtitle =
            Label("Demo trading activity")

        subtitle.style = """
            -fx-font-size: 13px;
            -fx-text-fill: #888888;
        """.trimIndent()

        // =====================================================
        // SUMMARY
        // =====================================================

        val openOrders =
            createSummary(
                "OPEN ORDERS",
                "3",
                "#ffb300"
            )

        val filledOrders =
            createSummary(
                "FILLED",
                "12",
                "#00e676"
            )

        val totalOrders =
            createSummary(
                "TOTAL",
                "18",
                "#ffffff"
            )

        val summary =
            HBox(
                15.0,
                openOrders,
                filledOrders,
                totalOrders
            )

        // =====================================================
        // TABLE
        // =====================================================
        table =
            TableView()

        table.columnResizePolicy =
            TableView.CONSTRAINED_RESIZE_POLICY

        createColumns()

        table.items =
            FXCollections.observableArrayList(
                mockOrders()
            )
        // =====================================================
        // CONTENT
        // =====================================================

        val content =
            VBox(
                20.0,
                VBox(
                    5.0,
                    title,
                    subtitle
                ),
                summary,
                table
            )

        VBox.setVgrow(
            table,
            javafx.scene.layout.Priority.ALWAYS
        )

        root.center =
            content

        return root
    }

    // =========================================================
    // SUMMARY CARD
    // =========================================================

    private fun createSummary(
        title: String,
        value: String,
        color: String
    ): VBox {

        val titleLabel =
            Label(title)

        titleLabel.style = """
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #777777;
        """.trimIndent()

        val valueLabel =
            Label(value)

        valueLabel.style = """
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: $color;
        """.trimIndent()

        val card =
            VBox(
                5.0,
                titleLabel,
                valueLabel
            )

        card.padding =
            Insets(18.0)

        card.prefWidth =
            180.0

        card.style = """
            -fx-background-color: #252525;
            -fx-background-radius: 12px;
            -fx-border-color: #383838;
            -fx-border-radius: 12px;
        """.trimIndent()

        return card
    }

    // =========================================================
    // COLUMNS
    // =========================================================

    private fun createColumns() {

        val symbol =
            TableColumn<Order, String>(
                "SYMBOL"
            )

        symbol.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.symbol
            )
        }

        val side =
            TableColumn<Order, String>(
                "SIDE"
            )

        side.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.side
            )
        }

        side.cellFactory =
            Callback {

                object :
                    TableCell<Order, String>() {

                    override fun updateItem(
                        item: String?,
                        empty: Boolean
                    ) {

                        super.updateItem(
                            item,
                            empty
                        )

                        if (empty || item == null) {

                            text = null

                            style = ""

                        } else {

                            text = item

                            style =
                                if (item == "BUY") {

                                    "-fx-text-fill: #00e676; -fx-font-weight: bold;"

                                } else {

                                    "-fx-text-fill: #ff5252; -fx-font-weight: bold;"
                                }
                        }
                    }
                }
            }

        val type =
            TableColumn<Order, String>(
                "TYPE"
            )

        type.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.type
            )
        }

        val price =
            TableColumn<Order, String>(
                "PRICE"
            )

        price.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.price
            )
        }

        val quantity =
            TableColumn<Order, String>(
                "QUANTITY"
            )

        quantity.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.quantity
            )
        }

        val status =
            TableColumn<Order, String>(
                "STATUS"
            )

        status.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.status
            )
        }

        status.cellFactory =
            Callback {

                object :
                    TableCell<Order, String>() {

                    override fun updateItem(
                        item: String?,
                        empty: Boolean
                    ) {

                        super.updateItem(
                            item,
                            empty
                        )

                        if (empty || item == null) {

                            text = null

                        } else {

                            text = item

                            style =
                                when (item) {

                                    "FILLED" ->
                                        "-fx-text-fill: #00e676; -fx-font-weight: bold;"

                                    "OPEN" ->
                                        "-fx-text-fill: #ffb300; -fx-font-weight: bold;"

                                    "CANCELLED" ->
                                        "-fx-text-fill: #777777; -fx-font-weight: bold;"

                                    else ->
                                        "-fx-text-fill: white;"
                                }
                        }
                    }
                }
            }

        val time =
            TableColumn<Order, String>(
                "TIME"
            )

        time.setCellValueFactory {
            javafx.beans.property.SimpleStringProperty(
                it.value.time
            )
        }

        table.columns.addAll(
            symbol,
            side,
            type,
            price,
            quantity,
            status,
            time
        )
    }

    // =========================================================
    // MOCK ORDERS
    // =========================================================

    private fun mockOrders(): List<Order> {

        return listOf(

            Order(
                "BTCUSDT",
                "BUY",
                "LIMIT",
                "$106,500",
                "0.0500",
                "FILLED",
                "12:32:41"
            ),

            Order(
                "ETHUSDT",
                "SELL",
                "LIMIT",
                "$4,300",
                "0.5000",
                "FILLED",
                "12:35:12"
            ),

            Order(
                "BTCUSDT",
                "BUY",
                "LIMIT",
                "$107,200",
                "0.0200",
                "OPEN",
                "12:40:05"
            ),

            Order(
                "SOLUSDT",
                "SELL",
                "MARKET",
                "$188.20",
                "5.0000",
                "FILLED",
                "12:42:17"
            ),

            Order(
                "BNBUSDT",
                "BUY",
                "LIMIT",
                "$680.50",
                "1.5000",
                "OPEN",
                "12:44:33"
            ),

            Order(
                "ETHUSDT",
                "BUY",
                "LIMIT",
                "$4,050",
                "0.7500",
                "CANCELLED",
                "12:51:02"
            )
        )
    }

    // =========================================================
    // ORDER MODEL
    // =========================================================

    data class Order(
        val symbol: String,
        val side: String,
        val type: String,
        val price: String,
        val quantity: String,
        val status: String,
        val time: String
    )

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {
    }
}