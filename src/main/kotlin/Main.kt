package me.ariedotme

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.Identity.encode
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import me.ariedotme.handlers.BroadcastHandler
import me.ariedotme.handlers.PacketHandler
import mmorpg.PacketOuterClass
import mmorpg.PacketOuterClass.HandshakeServer
import mmorpg.PacketOuterClass.Packet
import java.util.Base64

val world = World()
val broadcastHandler = BroadcastHandler(world)
val packetHandler = PacketHandler(world, broadcastHandler)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSockets()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureSockets() {
    install(WebSockets)
}

fun Application.configureRouting() {
    routing {
        webSocket("/ws") {
            val playerId = generatePlayerId()
            val player = world.addPlayer(playerId, this)
            val handshakePacket = Packet.newBuilder().setType(PacketOuterClass.PacketType.HANDSHAKE_SERVER).setData(
                HandshakeServer.newBuilder().setPlayerId(playerId).build().toByteString()
            ).build()
            val encodedHandshakePacket = Base64.getEncoder().encode(handshakePacket.toByteArray());

            player.session.send(Frame.Text(true, encodedHandshakePacket))

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Binary) {
                        val packet = Packet.parseFrom(frame.data)
                        packetHandler.handlePacket(packet, playerId)
                    }
                }
            } finally {
                world.removePlayer(playerId)
            }
        }
    }
}

fun generatePlayerId(): String = "Player-${System.currentTimeMillis()}"