package com.tgm.tgmc.core.data.local

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages direct device-to-device offline connection using TCP Sockets.
 * This can be used when devices are connected to the same Wi-Fi network
 * or when one device creates a Local Hotspot and the other connects to it.
 */
@Singleton
class OfflineConnectionManager @Inject constructor() {

    private val port = 8080
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null

    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    /**
     * Call this on the CHILD device to start listening for offline connections.
     */
    suspend fun startServer() = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(port)
            Log.i("OfflineConnection", "Server started on port $port, waiting for connection...")
            clientSocket = serverSocket?.accept()
            Log.i("OfflineConnection", "Client connected from ${clientSocket?.inetAddress?.hostAddress}")
            
            setupStreams(clientSocket!!)
            onConnectionStateChanged?.invoke(true)
            
            listenForMessages()
        } catch (e: Exception) {
            Log.e("OfflineConnection", "Server error: ${e.message}")
            stopConnection()
        }
    }

    /**
     * Call this on the PARENT device to connect to the Child's IP address.
     * @param ipAddress The IP address of the Child device (e.g. 192.168.43.1 if Child is Hotspot)
     */
    suspend fun connectToDevice(ipAddress: String) = withContext(Dispatchers.IO) {
        try {
            Log.i("OfflineConnection", "Connecting to $ipAddress:$port...")
            clientSocket = Socket()
            clientSocket?.connect(InetSocketAddress(ipAddress, port), 5000)
            
            setupStreams(clientSocket!!)
            onConnectionStateChanged?.invoke(true)
            
            listenForMessages()
        } catch (e: Exception) {
            Log.e("OfflineConnection", "Client error: ${e.message}")
            stopConnection()
        }
    }

    private fun setupStreams(socket: Socket) {
        writer = PrintWriter(socket.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    private suspend fun listenForMessages() = withContext(Dispatchers.IO) {
        try {
            while (true) {
                val message = reader?.readLine()
                if (message != null) {
                    onMessageReceived?.invoke(message)
                } else {
                    break // Connection closed
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineConnection", "Message listening error: ${e.message}")
        } finally {
            stopConnection()
        }
    }

    /**
     * Send a JSON string payload over the direct connection.
     */
    fun sendMessage(jsonMessage: String) {
        Thread {
            try {
                writer?.println(jsonMessage)
            } catch (e: Exception) {
                Log.e("OfflineConnection", "Send error: ${e.message}")
            }
        }.start()
    }

    fun stopConnection() {
        try {
            reader?.close()
            writer?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("OfflineConnection", "Stop error: ${e.message}")
        } finally {
            reader = null
            writer = null
            clientSocket = null
            serverSocket = null
            onConnectionStateChanged?.invoke(false)
        }
    }
}
