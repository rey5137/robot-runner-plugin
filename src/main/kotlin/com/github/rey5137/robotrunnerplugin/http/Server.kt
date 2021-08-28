package com.github.rey5137.robotrunnerplugin.http

import com.github.rey5137.robotrunnerplugin.http.Protocol.readRequest
import com.github.rey5137.robotrunnerplugin.http.Protocol.writeBadResponse
import com.github.rey5137.robotrunnerplugin.http.Protocol.writeGoodResponse
import org.json.JSONObject
import java.io.IOException
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.Map.Entry

/**
 * @author Gong Zhang
 */
class Server(private val port: Int, private val delegate: IDelegate) {
    private var executor: ExecutorService? = null
    private var serverSocket: ServerSocket? = null

    @get:Synchronized
    var isStarted = false
        private set
    var logger: PrintStream? = System.err

    @Synchronized
    fun start() {
        check(!isStarted) { "server already started" }
        val serverSocket: ServerSocket
        serverSocket = try {
            ServerSocket(port)
        } catch (e: IOException) {
            throw ServerException(e)
        }
        this.serverSocket = serverSocket
        val executor = Executors.newCachedThreadPool()
        this.executor = executor
        executor.execute {
            while (true) {
                try {
                    val socket = serverSocket.accept()
                    val conn = Connection(socket, delegate)
                    synchronized(this@Server) {
                        if (!isStarted) {
                            return@execute   // finish listener
                        }
                        executor.execute(conn)
                    }
                } catch (ignored: IOException) {
                    return@execute   // finish listener
                }
            }
        }
        isStarted = true
    }

    @Synchronized
    fun stop() {
        check(isStarted) { "server does not started" }
        executor!!.shutdown()
        executor = null
        try {
            serverSocket!!.close()
        } catch (ignored: IOException) {
        }
        serverSocket = null
        isStarted = false
    }

    internal inner class Connection(private val socket: Socket, private val delegate: IDelegate) : Runnable {
        override fun run() {
            try {
                socket.getOutputStream().use { os ->
                    socket.getInputStream().use { `is` ->
                        val req: Entry<String, JSONObject> = readRequest(`is`)
                        val method: String = req.key
                        val payload: JSONObject = req.value
                        var result: Any? = null
                        var exception: Exception? = null
                        try {
                            result = delegate.handleRequest(method, payload)
                        } catch (ex: Exception) {
                            exception = ex
                        }
                        if (exception != null) {
                            writeBadResponse(os, exception.message)
                        } else {
                            writeGoodResponse(os, result)
                        }
                    }
                }
            } catch (ex: Exception) {
                if (logger != null) {
                    ex.printStackTrace(logger)
                }
            }
        }
    }

}