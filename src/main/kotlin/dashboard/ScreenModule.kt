package dashboard

import customer.DashboardScreen
import javafx.scene.Node

// =============================================================
// SCREEN MODULE
// =============================================================
//
// Bir dashboard ekranının ortak koda verdiği söz.
//
// Ortak kod (DashboardController) hiçbir ekran sınıfını
// doğrudan tanımaz, yalnızca bu arayüzü bilir. Böylece
// seçilmeyen ekranların kodu derlemeye hiç girmeden
// uygulama derlenebilir.
//
// =============================================================

interface ScreenModule {

    val screen: DashboardScreen

    fun create(): Node

    fun start() {}

    fun stop() {}
}
