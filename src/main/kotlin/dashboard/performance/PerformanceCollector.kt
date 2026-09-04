package dashboard.performance

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

data class PerformanceSnapshot(
    val processCpu: Double,
    val systemCpu: Double,

    val usedMemoryMb: Long,
    val allocatedMemoryMb: Long,
    val maxMemoryMb: Long,

    val heapUsedMb: Long,
    val heapMaxMb: Long,

    val activeThreads: Int,
    val peakThreads: Int,
    val daemonThreads: Int,

    val gcCount: Long,
    val gcTimeMs: Long,

    val uptimeSeconds: Long,
    val pid: Long
)

class PerformanceCollector {

    // =========================================================
    // OS
    // =========================================================

    private val osBean =
        ManagementFactory.getOperatingSystemMXBean()
                as OperatingSystemMXBean

    // =========================================================
    // RUNTIME
    // =========================================================

    private val runtime =
        Runtime.getRuntime()

    private val runtimeBean =
        ManagementFactory.getRuntimeMXBean()

    // =========================================================
    // MEMORY
    // =========================================================

    private val memoryBean =
        ManagementFactory.getMemoryMXBean()

    // =========================================================
    // THREADS
    // =========================================================

    private val threadBean: ThreadMXBean =
        ManagementFactory.getThreadMXBean()

    // =========================================================
    // GC
    // =========================================================

    private val gcBeans =
        ManagementFactory.getGarbageCollectorMXBeans()

    // =========================================================
    // COLLECT
    // =========================================================

    fun collect(): PerformanceSnapshot {

        // -----------------------------------------------------
        // CPU
        // -----------------------------------------------------

        val processCpu =
            readCpu(
                osBean.processCpuLoad
            )

        val systemCpu =
            readCpu(
                osBean.cpuLoad
            )

        // -----------------------------------------------------
        // JVM MEMORY
        // -----------------------------------------------------

        val usedMemory =
            runtime.totalMemory() -
                    runtime.freeMemory()

        val allocatedMemory =
            runtime.totalMemory()

        val maxMemory =
            runtime.maxMemory()

        // -----------------------------------------------------
        // HEAP
        // -----------------------------------------------------

        val heap =
            memoryBean.heapMemoryUsage

        // -----------------------------------------------------
        // GC
        // -----------------------------------------------------

        val gcCount =
            gcBeans.sumOf {

                if (it.collectionCount >= 0) {
                    it.collectionCount
                } else {
                    0L
                }
            }

        val gcTime =
            gcBeans.sumOf {

                if (it.collectionTime >= 0) {
                    it.collectionTime
                } else {
                    0L
                }
            }

        // -----------------------------------------------------
        // PID
        // -----------------------------------------------------

        val pid =
            try {

                ProcessHandle
                    .current()
                    .pid()

            } catch (e: Exception) {

                -1L
            }

        // -----------------------------------------------------
        // SNAPSHOT
        // -----------------------------------------------------

        return PerformanceSnapshot(

            processCpu =
                processCpu,

            systemCpu =
                systemCpu,

            usedMemoryMb =
                toMb(usedMemory),

            allocatedMemoryMb =
                toMb(allocatedMemory),

            maxMemoryMb =
                toMb(maxMemory),

            heapUsedMb =
                toMb(heap.used),

            heapMaxMb =
                toMb(heap.max),

            activeThreads =
                threadBean.threadCount,

            peakThreads =
                threadBean.peakThreadCount,

            daemonThreads =
                threadBean.daemonThreadCount,

            gcCount =
                gcCount,

            gcTimeMs =
                gcTime,

            uptimeSeconds =
                runtimeBean.uptime / 1000,

            pid =
                pid
        )
    }

    // =========================================================
    // CPU
    // =========================================================

    private fun readCpu(
        value: Double
    ): Double {

        if (value.isNaN()) {
            return 0.0
        }

        if (value.isInfinite()) {
            return 0.0
        }

        if (value < 0.0) {
            return 0.0
        }

        return value * 100.0
    }

    // =========================================================
    // BYTES -> MB
    // =========================================================

    private fun toMb(
        bytes: Long
    ): Long {

        if (bytes <= 0) {
            return 0
        }

        return bytes / 1024 / 1024
    }
}