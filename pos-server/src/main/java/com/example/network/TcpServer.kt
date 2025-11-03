package com.example.network

import com.example.di.ServiceFactory
import java.net.ServerSocket
import java.net.Socket

class TcpServer(private val port: Int) {

    private val serviceFactory = ServiceFactory

    fun start() {
        val serverSocket = ServerSocket(port)
        println("POS Acquirer Server started on port $port")

        while (true) {
            val client: Socket = serverSocket.accept()
            println("New connection from ${client.inetAddress.hostAddress}")

            Thread {
                val handler = ClientHandler(client, serviceFactory)
                handler.handle()
            }.start()
        }
    }
}
