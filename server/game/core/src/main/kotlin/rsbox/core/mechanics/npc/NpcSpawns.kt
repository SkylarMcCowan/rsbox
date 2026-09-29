package rsbox.core.mechanics.npc

import io.rsbox.server.common.inject
import io.rsbox.server.engine.api.ext.on_init
import io.rsbox.server.engine.model.Direction
import io.rsbox.server.engine.model.World
import io.rsbox.server.engine.model.coord.Tile
import io.rsbox.server.engine.model.entity.Npc
import org.tinylog.kotlin.Logger
import kotlin.math.min
import kotlin.random.Random

class NpcSpawns : io.rsbox.server.script.GameScript() {
    private val world: World by inject()

    init {
        on_init {
            val spawns = loadNpcSpawns()
            var loaded = 0
            for (spawn in spawns) {
                if (world.cache.configArchive.npcs[spawn.id] == null) {
                    Logger.warn("Skipping unknown NPC ${spawn.id} at ${spawn.x},${spawn.y},${spawn.level}.")
                    continue
                }
                val npc = Npc(spawn.id, Tile(spawn.x, spawn.y, spawn.level))
                npc.wanderRadius = spawn.radius
                npc.direction = Direction.fromOrientation(spawn.direction)
                world.addNpc(npc)
                npc.wander(npc.wanderRadius)
                loaded++
            }
            Logger.info("Spawned $loaded NPCs from ${spawns.size} spawn records.")
        }
    }
}


fun Npc.wander(radius: Int) {
    if(radius == 0) return
    val wanderRadius = min(radius, 5)
    task {
        while(true) {
            if(Random.nextInt(1, 8) != 7) {
                wait(ticks = 1)
                continue
            } else {
                val dest = spawnTile.translate(x = Random.nextInt(-wanderRadius, wanderRadius + 1), y = Random.nextInt(-wanderRadius, wanderRadius + 1))
                walkTo(dest)
                wait(ticks = 1)
            }
        }
    }
}