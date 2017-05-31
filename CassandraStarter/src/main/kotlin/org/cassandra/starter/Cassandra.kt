package org.cassandra.starter

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Start of stop handler for cassandra database service on any platform.
 *
 * @author Sachith Dickwella
 */
private class Cassandra {

    val logDirPath: String = "/var/log/cassandra"
    val logAccessLogFile: String = "/access.log"
    val logErrorLogFile: String = "/error.log"
    val pidFile: String = "/pid"

    fun start(command: String?) {
        val logDir: File = File(logDirPath)
        if (!logDir.exists()) logDir.mkdirs()

        val pBuilder: ProcessBuilder

        if (System.getProperty("os.name")
                .startsWith("windows", true)) {
            pBuilder = ProcessBuilder("cmd.exe", "/c", "cassandra.bat", "-p", pidFile)
        } else {
            pBuilder = ProcessBuilder(command, "-p", pidFile)
        }

        redirectOutput(pBuilder)
        pBuilder.start()
    }

    fun stop() {
        val pidFile: File = File(pidFile)
        var pid: String? = null
        BufferedReader(FileReader(pidFile)).use {
            pid = it.lines().findFirst().get()
        }

        if (pid != null) {
            val pBuilder: ProcessBuilder = ProcessBuilder("kill", "-9", pid)
            redirectOutput(pBuilder)

            pBuilder.start()
        }
    }

    fun restart() {
        // TODO code
    }

    private fun redirectOutput(pBuilder: ProcessBuilder): ProcessBuilder {
        return pBuilder.redirectOutput(File(logDirPath + logAccessLogFile))
                .redirectError(File(logDirPath + logErrorLogFile))
    }
}

private enum class Op {
    START, STOP, RESTART;

    override fun toString(): String {
        when (this) {
            START -> return "start"
            STOP -> return "stop"
            RESTART -> return "restart"
        }
    }
}

fun main(args: Array<String>) {
    val cas: Cassandra = Cassandra()
    if (args.isNotEmpty()) {
        val argc: Array<String> = Arrays.copyOf(args, 2)

        when (argc[0]) {
            Op.START.toString() -> cas.start(argc[1])
            Op.STOP.toString() -> cas.stop()
            else -> throw ExecutionException(IllegalArgumentException("Invalid operation provided"))
        }
    } else {
        throw IllegalArgumentException("Provide an operation to execute")
    }
}