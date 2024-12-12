package me.ariedotme

import io.ktor.server.websocket.*

data class Player(
    val id: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    val session: DefaultWebSocketServerSession
) {
    fun isWithinRange(other: Player, range: Float): Boolean {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz <= range * range
    }
}

class World {
    private val players = mutableMapOf<String, Player>()

    fun addPlayer(id: String, session: DefaultWebSocketServerSession): Player {
        val player = Player(id, session = session)
        players[id] = player
        println("Player added: $id")
        return player
    }

    fun removePlayer(id: String) {
        players.remove(id)
        println("Player removed: $id")
    }

    fun updatePlayerPosition(id: String, x: Float, y: Float, z: Float): List<Player> {
        val player = players[id] ?: return emptyList()
        player.x = x
        player.y = y
        player.z = z
        println("Player $id moved to ($x, $y, $z)")

        return players.values.filter { it.id != id && player.isWithinRange(it, 10f) }
    }

    fun getPlayers(): List<Player> = players.values.toList()
}