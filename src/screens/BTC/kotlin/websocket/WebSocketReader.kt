package websocket

import com.fasterxml.jackson.databind.ObjectMapper
import model.TradeData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.Properties
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WebSocketReader(
    private val objectMapper: ObjectMapper,
    private val onTrade: (TradeData, String) -> Unit
) {

    private var webSocket: WebSocket? = null

    // =========================================================
    // CONFIG
    // =========================================================

    private val config = Properties()

    init {

        val inputStream =
            WebSocketReader::class.java
                .getResourceAsStream("/config.properties")
                ?: throw RuntimeException(
                    "config.properties bulunamadı!"
                )

        config.load(inputStream)
        inputStream.close()
    }

    // =========================================================
    // SYMBOLS
    // =========================================================

    private val symbols: List<String> =
        config
            .getProperty("binance.symbols")
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    // =========================================================
    // SSL
    // =========================================================

    private fun createUnsafeSslContext(): SSLContext {

        val trustAll =
            object : X509TrustManager {

                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers():
                        Array<X509Certificate> {

                    return emptyArray()
                }
            }

        return SSLContext
            .getInstance("TLS")
            .apply {

                init(
                    null,
                    arrayOf<TrustManager>(trustAll),
                    SecureRandom()
                )
            }
    }

    // =========================================================
    // CONNECT
    // =========================================================

    fun connect(symbol: String = "btcusdt") {

        /*
         * Şimdilik symbol parametresini kullanmıyoruz.
         *
         * Config dosyasındaki bütün coinler
         * aynı WebSocket bağlantısından dinleniyor.
         */

        val streams =
            symbols.joinToString("/") {
                "${it}@trade"
            }

        val baseUrl =
            config.getProperty(
                "binance.websocket.url"
            )

        val url =
            "$baseUrl?streams=$streams"

        val timeout =
            config
                .getProperty(
                    "binance.connection.timeout"
                )
                .toLong()

        val userAgent =
            config.getProperty(
                "binance.user-agent"
            )

        val origin =
            config.getProperty(
                "binance.origin"
            )

        // =====================================================
        // LOG
        // =====================================================

        println()
        println("========================================")
        println("BINANCE WEBSOCKET")
        println("========================================")

        println(
            "Streams: ${symbols.size}"
        )

        println(
            "Timeout: ${timeout}s"
        )

        println(
            "URL:"
        )

        println(url)

        println(
            "========================================"
        )

        // =====================================================
        // SSL
        // =====================================================

        val sslContext =
            createUnsafeSslContext()

        // =====================================================
        // HTTP CLIENT
        // =====================================================

        val client =
            HttpClient
                .newBuilder()
                .sslContext(sslContext)
                .connectTimeout(
                    Duration.ofSeconds(timeout)
                )
                .build()

        // =====================================================
        // WEBSOCKET LISTENER
        // =====================================================

        val listener =
            object : WebSocket.Listener {

                private val buffer =
                    StringBuilder()

                // =================================================
                // OPEN
                // =================================================

                override fun onOpen(
                    webSocket: WebSocket
                ) {

                    println()
                    println("========================================")
                    println("WEBSOCKET CONNECTED")
                    println("========================================")

                    println(
                        "Listening to ${symbols.size} coins"
                    )

                    println(
                        "========================================"
                    )

                    this@WebSocketReader.webSocket =
                        webSocket

                    webSocket.request(1)
                }

                // =================================================
                // TEXT
                // =================================================

                override fun onText(
                    webSocket: WebSocket,
                    data: CharSequence,
                    last: Boolean
                ): CompletionStage<*> {

                    buffer.append(data)

                    if (last) {

                        val json =
                            buffer.toString()

                        buffer.setLength(0)

                        try {

                            // =====================================
                            // JSON ROOT
                            // =====================================

                            val root =
                                objectMapper.readTree(
                                    json
                                )

                            // =====================================
                            // DATA
                            // =====================================

                            val dataNode =
                                root.get("data")

                            if (
                                dataNode == null ||
                                dataNode.isNull
                            ) {

                                webSocket.request(1)

                                return CompletableFuture
                                    .completedFuture<Void>(null)
                            }

                            // =====================================
                            // TRADE
                            // =====================================

                            val trade =
                                objectMapper.treeToValue(
                                    dataNode,
                                    TradeData::class.java
                                )

                            // =====================================
                            // DASHBOARD
                            // =====================================

                            onTrade(
                                trade,
                                json
                            )

                        } catch (e: Exception) {

                            println()
                            println("========================================")
                            println("JSON ERROR")
                            println("========================================")

                            println(
                                e.message
                            )

                            println()
                            println("RAW:")

                            println(json)

                            println(
                                "========================================"
                            )
                        }
                    }

                    // Bir sonraki mesajı iste
                    webSocket.request(1)

                    return CompletableFuture
                        .completedFuture<Void>(null)
                }

                // =================================================
                // ERROR
                // =================================================

                override fun onError(
                    webSocket: WebSocket,
                    error: Throwable
                ) {

                    println()
                    println("========================================")
                    println("WEBSOCKET ERROR")
                    println("========================================")

                    error.printStackTrace()

                    println(
                        "========================================"
                    )
                }

                // =================================================
                // CLOSE
                // =================================================

                override fun onClose(
                    webSocket: WebSocket,
                    statusCode: Int,
                    reason: String
                ): CompletionStage<*> {

                    println()
                    println("========================================")
                    println("WEBSOCKET CLOSED")
                    println("========================================")

                    println(
                        "Code: $statusCode"
                    )

                    println(
                        "Reason: $reason"
                    )

                    println(
                        "========================================"
                    )

                    this@WebSocketReader.webSocket =
                        null

                    return CompletableFuture
                        .completedFuture<Void>(null)
                }
            }

        // =========================================================
        // CONNECT
        // =========================================================

        client
            .newWebSocketBuilder()

            .connectTimeout(
                Duration.ofSeconds(timeout)
            )

            .header(
                "User-Agent",
                userAgent
            )

            .header(
                "Origin",
                origin
            )

            .buildAsync(
                URI.create(url),
                listener
            )

            .whenComplete {
                    socket,
                    error ->

                if (error != null) {

                    println()
                    println(
                        "WebSocket connection failed:"
                    )

                    error.printStackTrace()

                } else {

                    println()
                    println(
                        "WebSocket connection established."
                    )
                }
            }
    }

    // =========================================================
    // CLOSE
    // =========================================================

    fun close() {

        try {

            webSocket
                ?.sendClose(
                    WebSocket.NORMAL_CLOSURE,
                    "Application closing"
                )
                ?.join()

        } catch (e: Exception) {

            println(
                "Close error: ${e.message}"
            )
        }

        webSocket = null
    }
}
