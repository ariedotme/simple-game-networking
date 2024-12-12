package me.ariedotme.handlers

import me.ariedotme.World
import mmorpg.PacketOuterClass.*

class PacketHandler(private val world: World, private val broadcastHandler: BroadcastHandler) {

    suspend fun handlePacket(packet: Packet, playerId: String) {
        when (packet.type) {
            PacketType.HANDSHAKE_CLIENT -> {
                val handshake = HandshakeClient.parseFrom(packet.data)
                println("Handshake received from ${handshake.playerName} (version: ${handshake.version})")
            }
            PacketType.PLAYER_MOVE -> {
                val move = PlayerMove.parseFrom(packet.data)
                val player = world.getPlayers().find { it.id == playerId } ?: return
                val nearbyPlayers = world.updatePlayerPosition(playerId, move.x, move.y, move.z)

                broadcastHandler.broadcastPlayerMove(player, nearbyPlayers)
            }
            else -> println("Unknown packet type")
        }
    }
}