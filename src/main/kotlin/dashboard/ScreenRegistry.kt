package dashboard

import customer.DashboardScreen

// =============================================================
// SCREEN REGISTRY
// =============================================================
//
// Ekran modüllerini isimle yükler.
//
// CustomerFactory'nin müşteri sınıflarını yüklediği yöntemin
// aynısı: ortak kodda derleme zamanı referansı yoktur, sınıf
// yalnızca o ekran build'e dahil edildiyse bulunur.
//
// Beklenen isim:
//   dashboard.<paket>.<Prefix>ScreenModule
//
// Örnek:
//   BTC -> dashboard.btc.BtcScreenModule
//
// =============================================================

object ScreenRegistry {

    fun load(
        screen: DashboardScreen
    ): ScreenModule {

        val className =
            classNameOf(screen)

        try {

            val clazz =
                Class.forName(className)

            return clazz
                .getDeclaredConstructor()
                .newInstance() as ScreenModule

        } catch (exception: ClassNotFoundException) {

            throw IllegalStateException(
                "Screen $screen is enabled for this customer but was not " +
                        "compiled into this build ($className not found). " +
                        "Check the 'screens' entry in " +
                        "src/customers/<customer>/customer.properties",
                exception
            )
        }
    }

    private fun classNameOf(
        screen: DashboardScreen
    ): String {

        val packageName =
            screen.name.lowercase()

        val classPrefix =
            packageName.replaceFirstChar {
                it.uppercase()
            }

        return "dashboard.$packageName.${classPrefix}ScreenModule"
    }
}
