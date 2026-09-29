package rsbox.core.mechanics.npc

import kotlin.test.*

class NpcSpawnTest {
    @Test
    fun `bundled survey loads and includes Lumbridge`() {
        val spawns = loadNpcSpawns()
        assertEquals(800, spawns.size)
        assertTrue(spawns.any { it.x in 3200..3240 && it.y in 3200..3240 && it.level == 0 })
        assertTrue(spawns.all { it.radius in 0..5 })
    }

    @Test
    fun `vertical routes produce a wander radius and an observed spawn tile`() {
        val points = listOf(SpawnPoint(3200, 3200, 0), SpawnPoint(3200, 3204, 0))
        val spawn = SpawnRecord(1, 1, -1, points).toSpawn()
        assertEquals(4, spawn.radius)
        assertTrue(points.any { it.x == spawn.x && it.y == spawn.y })
        assertEquals(0, spawn.direction)
    }

    @Test
    fun `stationary spawn preserves its orientation`() {
        val spawn = SpawnRecord(1, 1, 1536, listOf(SpawnPoint(3200, 3200, 1))).toSpawn()
        assertEquals(0, spawn.radius)
        assertEquals(1, spawn.level)
        assertEquals(1536, spawn.direction)
    }

    @Test
    fun `empty survey is rejected`() {
        assertFailsWith<IllegalArgumentException> { SpawnRecord(1, 1, 0, emptyList()).toSpawn() }
    }
}
