package model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class TradeData {

    @JsonProperty("e")
    var eventType: String? = null

    @JsonProperty("E")
    var eventTime: Long = 0

    @JsonProperty("s")
    var symbol: String? = null

    @JsonProperty("t")
    var tradeId: Long = 0

    @JsonProperty("p")
    var price: BigDecimal? = null

    @JsonProperty("q")
    var quantity: BigDecimal? = null

    @JsonProperty("T")
    var tradeTime: Long = 0

    @JsonProperty("m")
    var buyerIsMarketMaker: Boolean = false
}