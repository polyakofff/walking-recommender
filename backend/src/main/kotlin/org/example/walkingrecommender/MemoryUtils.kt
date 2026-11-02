package org.example.walkingrecommender

object MemoryUtils {

    fun heapUsedMb(): Long {
        val runtime = Runtime.getRuntime()

        return (runtime.totalMemory() - runtime.freeMemory()) /
                1024 / 1024
    }

    fun heapMaxMb(): Long {
        return Runtime.getRuntime().maxMemory() /
                1024 / 1024
    }
}