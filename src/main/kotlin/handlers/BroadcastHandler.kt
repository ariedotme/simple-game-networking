package me.ariedotme.handlers

import io.ktor.websocket.*
import me.ariedotme.Player
import me.ariedotme.World
import mmorpg.PacketOuterClass.Packet
import mmorpg.PacketOuterClass.PacketType
import mmorpg.PacketOuterClass.PlayerMove
import java.util.Base64


class BroadcastHandler(private val world: World) {

    suspend fun broadcastPlayerMove(player: Player, nearbyPlayers: List<Player>) {
        val packet = Packet.newBuilder()
            .setType(PacketType.PLAYER_MOVE)
            .setData(
                PlayerMove.newBuilder()
                    .setPlayerId(player.id)
                    .setX(player.x)
                    .setY(player.y)
                    .setZ(player.z)
                    .build()
                    .toByteString()
            )
            .build()

        val encodedPacket = Base64.getEncoder().encode(packet.toByteArray());

        nearbyPlayers.forEach { nearbyPlayer ->
            try {
                nearbyPlayer.session.send(Frame.Text(true, encodedPacket))
            } catch (e: Exception) {
                println("Failed to send move packet to player ${nearbyPlayer.id}: ${e.message}")
            }
        }
    }
}