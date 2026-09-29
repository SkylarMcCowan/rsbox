package rsbox.core.mechanics.npc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.max

data class NpcSpawn(val id: Int, val x: Int, val y: Int, val level: Int, val radius: Int, val direction: Int)

@Serializable
internal data class SpawnPoint(val x: Int, val y: Int, val plane: Int)

@Serializable
internal data class SpawnRecord(val npc: Int, val index: Int, val orientation: Int, val points: List<SpawnPoint>) {
    fun toSpawn(): NpcSpawn {
        require(points.isNotEmpty()) { "NPC $npc (survey index $index) has no spawn points." }
        require(points.all { it.plane in 0..3 && it.x in 0..16383 && it.y in 0..16383 }) {
            "NPC $npc (survey index $index) has invalid coordinates."
        }
        val plane = points.minOf { it.plane }
        val samePlane = points.filter { it.plane == plane }
        val minX = samePlane.minOf { it.x }
        val maxX = samePlane.maxOf { it.x }
        val minY = samePlane.minOf { it.y }
        val maxY = samePlane.maxOf { it.y }
        val centerX = minX + (maxX - minX) / 2
        val centerY = minY + (maxY - minY) / 2
        // Choose an observed tile rather than spawning inside an obstacle at the midpoint.
        val point = samePlane.minBy { max(abs(it.x - centerX), abs(it.y - centerY)) }
        val radius = max(maxX - minX, maxY - minY).coerceAtMost(5)
        return NpcSpawn(npc, point.x, point.y, plane, radius, if (orientation == -1) 0 else orientation)
    }
}

fun loadNpcSpawns(): List<NpcSpawn> {
    val text = SpawnRecord::class.java.getResourceAsStream("/data/npc_spawns.json")
        ?.bufferedReader()?.use { it.readText() }
        ?: error("Missing NPC spawn resource: /data/npc_spawns.json")
    return Json.decodeFromString<List<SpawnRecord>>(text).map { it.toSpawn() }
}
