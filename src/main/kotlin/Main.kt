package me.ariedotme

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import mmorpg.PacketOuterClass.*
import java.util.*

val world = World()

val serverScope = CoroutineScope(Dispatchers.IO + Job())

fun startWorldBroadcasting() {
    serverScope.launch {
        while (true) {
            broadcastWorldState()
            delay(100)
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSockets()
        configureRouting()
        startWorldBroadcasting()
    }.start(wait = true)
}

fun Application.configureSockets() {
    install(WebSockets)
}

fun Application.configureRouting() {
    routing {
        webSocket("/ws") {
            val playerId = call.request.queryParameters["id"] ?: generatePlayerId()
            world.addPlayer(playerId, this)

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Binary) {
                        val packet = Packet.parseFrom(frame.data)
                        handlePacket(packet, playerId)
                    }
                }
            } finally {
                world.removePlayer(playerId)
            }
        }
    }
}

suspend fun handlePacket(packet: Packet, playerId: String) {
    when (packet.type) {
        PacketType.HANDSHAKE -> {
            val handshake = Handshake.parseFrom(packet.data)
            println("Handshake received from ${handshake.playerName} (version: ${handshake.version})")
        }
        PacketType.CHAT_MESSAGE -> {
            val chatMessage = ChatMessage.parseFrom(packet.data)
            println("Chat from ${chatMessage.senderId}: ${chatMessage.content}")
        }
        PacketType.PLAYER_MOVE -> {
            val move = PlayerMove.parseFrom(packet.data)
            world.updatePlayerPosition(playerId, move.x, move.y, move.z)

            broadcastWorldState()
        }
        else -> println("Unknown packet type")
    }
}


suspend fun broadcastWorldState() {
    val worldState = WorldState.newBuilder()
    world.getPlayers().forEach { player ->
        worldState.addPlayers(
            PlayerMove.newBuilder()
                .setPlayerId(player.id)
                .setX(player.x)
                .setY(player.y)
                .setZ(player.z)
                .build()
        )
    }

    val packet = Packet.newBuilder()
        .setType(PacketType.WORLD_STATE)
        .setData(worldState.build().toByteString())
        .build()

    val encodedData = Base64.getEncoder().encodeToString(packet.toByteArray())

    world.getPlayers().forEach { player ->
        try {
            player.session.send(Frame.Text(encodedData)) // Send as Base64-encoded text
            println("Sent Base64 packet to player: ${player.id}")
        } catch (e: Exception) {
            println("Failed to send packet to player ${player.id}: ${e.message}")
        }
    }
}

fun generatePlayerId(): String = "Player-${System.currentTimeMillis()}"