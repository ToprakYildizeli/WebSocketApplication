package dashboard.performance

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class PerformanceScreen {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private companion object {

        const val HISTORY_SIZE = 60
        const val UPDATE_INTERVAL_SECONDS = 1L
    }

    // =========================================================
    // UI
    // =========================================================

    private lateinit var cpuLabel: Label
    private lateinit var systemCpuLabel: Label

    private lateinit var ramLabel: Label
    private lateinit var heapLabel: Label

    private lateinit var threadLabel: Label
    private lateinit var gcLabel: Label

    private lateinit var uptimeLabel: Label
    private lateinit var pidLabel: Label

    private lateinit var cpuChart: LineChart<Number, Number>
    private lateinit var ramChart: LineChart<Number, Number>

    private lateinit var cpuSeries: XYChart.Series<Number, Number>
    private lateinit var ramSeries: XYChart.Series<Number, Number>

    // =========================================================
    // COLLECTOR
    // =========================================================

    private val collector =
        PerformanceCollector()

    // =========================================================
    // SCHEDULER
    // =========================================================

    private val scheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor()

    // =========================================================
    // CREATE
    // =========================================================

    fun create(): VBox {

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        val title =
            Label("PERFORMANCE MONITOR")

        title.style = """
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()

        // -----------------------------------------------------
        // LABELS
        // -----------------------------------------------------

        cpuLabel =
            createLabel("PROCESS CPU: --")

        systemCpuLabel =
            createLabel("SYSTEM CPU: --")

        ramLabel =
            createLabel("RAM: --")

        heapLabel =
            createLabel("HEAP: --")

        threadLabel =
            createLabel("THREADS: --")

        gcLabel =
            createLabel("GC: --")

        uptimeLabel =
            createLabel("UPTIME: --")

        pidLabel =
            createLabel("PID: --")

        // -----------------------------------------------------
        // SERIES
        // -----------------------------------------------------

        cpuSeries =
            XYChart.Series()

        cpuSeries.name =
            "CPU"

        ramSeries =
            XYChart.Series()

        ramSeries.name =
            "RAM"

        // -----------------------------------------------------
        // CHARTS
        // -----------------------------------------------------

        cpuChart =
            createChart("CPU")

        ramChart =
            createChart("RAM")

        cpuChart.data.add(
            cpuSeries
        )

        ramChart.data.add(
            ramSeries
        )

        // -----------------------------------------------------
        // CHART BOX
        // -----------------------------------------------------

        val chartBox =
            HBox(
                15.0,
                cpuChart,
                ramChart
            )

        chartBox.alignment =
            Pos.CENTER

        HBox.setHgrow(
            cpuChart,
            Priority.ALWAYS
        )

        HBox.setHgrow(
            ramChart,
            Priority.ALWAYS
        )

        // -----------------------------------------------------
        // INFO GRID
        // -----------------------------------------------------

        val infoBox =
            GridPane()

        infoBox.hgap =
            35.0

        infoBox.vgap =
            10.0

        infoBox.alignment =
            Pos.CENTER

        // ROW 1

        infoBox.add(
            cpuLabel,
            0,
            0
        )

        infoBox.add(
            systemCpuLabel,
            1,
            0
        )

        infoBox.add(
            ramLabel,
            2,
            0
        )

        infoBox.add(
            heapLabel,
            3,
            0
        )

        // ROW 2

        infoBox.add(
            threadLabel,
            0,
            1
        )

        infoBox.add(
            gcLabel,
            1,
            1
        )

        infoBox.add(
            uptimeLabel,
            2,
            1
        )

        infoBox.add(
            pidLabel,
            3,
            1
        )

        // -----------------------------------------------------
        // MAIN BOX
        // -----------------------------------------------------

        val box =
            VBox(
                15.0,
                title,
                chartBox,
                infoBox
            )

        box.alignment =
            Pos.TOP_CENTER

        box.padding =
            Insets(20.0)

        box.style = """
            -fx-background-color: #34566e;
        """.trimIndent()

        return box
    }

    // =========================================================
    // CHART
    // =========================================================

    private fun createChart(
        title: String
    ): LineChart<Number, Number> {

        val xAxis =
            NumberAxis(
                0.0,
                HISTORY_SIZE.toDouble(),
                10.0
            )

        val yAxis =
            NumberAxis(
                0.0,
                10.0,
                2.0
            )

        xAxis.label =
            "SECONDS"

        yAxis.label =
            "%"

        xAxis.setForceZeroInRange(
            true
        )

        yAxis.setForceZeroInRange(
            true
        )

        val chart =
            LineChart<Number, Number>(
                xAxis,
                yAxis
            )

        chart.title =
            title

        chart.setAnimated(
            false
        )

        chart.setCreateSymbols(
            false
        )

        chart.setLegendVisible(
            false
        )

        chart.prefHeight =
            160.0

        chart.minHeight =
            160.0

        chart.maxHeight =
            160.0

        chart.prefWidth =
            400.0

        return chart
    }

    // =========================================================
    // START MONITOR
    // =========================================================

    fun startMonitor() {

        println("Performance monitor started.")

        scheduler.scheduleAtFixedRate({

            println(">>> PERFORMANCE TICK <<<")

            try {

                val snapshot =
                    collector.collect()

                println(
                    "PERFORMANCE -> " +
                            "CPU=${snapshot.processCpu} " +
                            "SYSTEM=${snapshot.systemCpu} " +
                            "RAM=${snapshot.usedMemoryMb}/${snapshot.maxMemoryMb} " +
                            "THREADS=${snapshot.activeThreads} " +
                            "UPTIME=${snapshot.uptimeSeconds}"
                )

                Platform.runLater {

                    println(">>> UPDATING PERFORMANCE UI <<<")

                    updateUi(snapshot)
                }

            } catch (e: Throwable) {

                println("!!! PERFORMANCE ERROR !!!")
                e.printStackTrace()
            }

        }, 0, 1, TimeUnit.SECONDS)
    }

    // =========================================================
    // UPDATE UI
    // =========================================================

    private fun updateUi(
        snapshot: PerformanceSnapshot
    ) {

        // -----------------------------------------------------
        // CPU
        // -----------------------------------------------------

        cpuLabel.text =
            "PROCESS CPU: %.1f%%"
                .format(
                    snapshot.processCpu
                )

        systemCpuLabel.text =
            "SYSTEM CPU: %.1f%%"
                .format(
                    snapshot.systemCpu
                )

        // -----------------------------------------------------
        // RAM
        // -----------------------------------------------------

        val ramPercent =
            if (
                snapshot.maxMemoryMb > 0
            ) {

                snapshot.usedMemoryMb
                    .toDouble() /
                        snapshot.maxMemoryMb
                            .toDouble() *
                        100.0

            } else {

                0.0
            }

        ramLabel.text =
            "RAM: %d / %d MB (%.1f%%)"
                .format(
                    snapshot.usedMemoryMb,
                    snapshot.maxMemoryMb,
                    ramPercent
                )

        // -----------------------------------------------------
        // HEAP
        // -----------------------------------------------------

        heapLabel.text =
            "HEAP: %d / %d MB"
                .format(
                    snapshot.heapUsedMb,
                    snapshot.heapMaxMb
                )

        // -----------------------------------------------------
        // THREADS
        // -----------------------------------------------------

        threadLabel.text =
            "THREADS: %d | PEAK: %d | DAEMON: %d"
                .format(
                    snapshot.activeThreads,
                    snapshot.peakThreads,
                    snapshot.daemonThreads
                )

        // -----------------------------------------------------
        // GC
        // -----------------------------------------------------

        gcLabel.text =
            "GC: %d | %d ms"
                .format(
                    snapshot.gcCount,
                    snapshot.gcTimeMs
                )

        // -----------------------------------------------------
        // UPTIME
        // -----------------------------------------------------

        uptimeLabel.text =
            "UPTIME: ${
                formatUptime(
                    snapshot.uptimeSeconds
                )
            }"

        // -----------------------------------------------------
        // PID
        // -----------------------------------------------------

        pidLabel.text =
            "PID: ${snapshot.pid}"

        // -----------------------------------------------------
        // CHARTS
        // -----------------------------------------------------

        addPoint(
            cpuSeries,
            snapshot.processCpu
        )

        addPoint(
            ramSeries,
            ramPercent
        )

        // -----------------------------------------------------
        // COLORS
        // -----------------------------------------------------

        updateCpuColor(
            snapshot.processCpu
        )

        updateRamColor(
            ramPercent
        )
    }

    // =========================================================
    // ADD CHART POINT
    // =========================================================

    private fun addPoint(
        series: XYChart.Series<Number, Number>,
        value: Double
    ) {

        series.data.add(
            XYChart.Data(
                series.data.size,
                value
            )
        )

        if (
            series.data.size >
            HISTORY_SIZE
        ) {

            series.data.removeAt(0)
        }

        series.data.forEachIndexed {
                index,
                data ->

            data.xValue =
                index
        }
    }

    // =========================================================
    // CPU COLOR
    // =========================================================

    private fun updateCpuColor(
        cpu: Double
    ) {

        val color =
            when {

                cpu >= 90.0 ->
                    "#ff5252"

                cpu >= 70.0 ->
                    "#ffca28"

                else ->
                    "#69f0ae"
            }

        cpuLabel.style = """
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()
    }

    // =========================================================
    // RAM COLOR
    // =========================================================

    private fun updateRamColor(
        ram: Double
    ) {

        val color =
            when {

                ram >= 90.0 ->
                    "#ff5252"

                ram >= 70.0 ->
                    "#ffca28"

                else ->
                    "#69f0ae"
            }

        ramLabel.style = """
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """.trimIndent()
    }

    // =========================================================
    // UPTIME
    // =========================================================

    private fun formatUptime(
        seconds: Long
    ): String {

        val hours =
            seconds / 3600

        val minutes =
            (seconds % 3600) / 60

        val secs =
            seconds % 60

        return "%02d:%02d:%02d".format(
            hours,
            minutes,
            secs
        )
    }

    // =========================================================
    // LABEL
    // =========================================================

    private fun createLabel(
        text: String
    ): Label {

        return Label(text).apply {

            style = """
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #cccccc;
            """.trimIndent()
        }
    }

    // =========================================================
    // STOP
    // =========================================================

    fun stop() {

        if (
            !scheduler.isShutdown
        ) {

            scheduler.shutdownNow()

            println(
                "Performance monitor stopped."
            )
        }
    }
}