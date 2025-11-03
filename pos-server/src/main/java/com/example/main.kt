package com.example

import com.example.network.TcpServer
import com.example.util.Config

fun main() {
    val config = Config.load()
    val server = TcpServer(config.port)
    server.start()
}