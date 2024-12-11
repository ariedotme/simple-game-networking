package me.ariedotme

import io.ktor.server.websocket.*

data class Player(
    val id: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    val session: DefaultWebSocketServerSession
)

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

    fun updatePlayerPosition(id: String, x: Float, y: Float, z: Float) {
        players[id]?.let {
            it.x = x
            it.y = y
            it.z = z
            println("Player $id moved to ($x, $y, $z)")
        }
    }

    fun getPlayers(): List<Player> = players.values.toList()
}