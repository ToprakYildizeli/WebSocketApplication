package dashboard

import model.TradeData

// =============================================================
// TRADE BUS
// =============================================================
//
// Ekranlar arası canlı veri aktarımı.
//
// Önceden BTC ekranı, Portfolio ekranını doğrudan besliyordu.
// Bu iki ekranı birbirine bağlıyor ve ayrı ayrı derlenmelerini
// engelliyordu. Artık BTC veriyi buraya yayınlar, ilgilenen
// ekranlar buradan dinler; iki taraf da yalnızca ortak koddaki
// bu nesneyi tanır.
//
// =============================================================

object TradeBus {

    private val listeners =
        mutableListOf<(TradeData) -> Unit>()

    @Synchronized
    fun subscribe(
        listener: (TradeData) -> Unit
    ) {
        listeners.add(listener)
    }

    @Synchronized
    fun publish(
        trade: TradeData
    ) {
        listeners.forEach {
            it(trade)
        }
    }

    @Synchronized
    fun clear() {
        listeners.clear()
    }
}
