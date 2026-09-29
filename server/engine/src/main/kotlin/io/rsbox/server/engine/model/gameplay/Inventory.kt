package io.rsbox.server.engine.model.gameplay

import kotlinx.serialization.Serializable

@Serializable
data class ItemStack(val id: Int, val amount: Int = 1)

data class ShopItem(val id: Int, val name: String, val price: Int, val stackable: Boolean = false, val healing: Int = 0)

object Goods {
    const val COINS = 995
    val items = listOf(
        ShopItem(315, "Shrimps", 5, healing = 3),
        ShopItem(333, "Trout", 15, healing = 7),
        ShopItem(379, "Lobster", 50, healing = 12),
        ShopItem(1351, "Bronze axe", 16),
        ShopItem(1265, "Bronze pickaxe", 20),
        ShopItem(590, "Tinderbox", 2),
        ShopItem(2347, "Hammer", 2),
        ShopItem(1931, "Pot", 1),
        ShopItem(1925, "Bucket", 2),
        ShopItem(561, "Nature rune", 180, true),
        ShopItem(556, "Air rune", 5, true),
        ShopItem(554, "Fire rune", 5, true)
    ).associateBy { it.id }
    fun stackable(id: Int) = id == COINS || items[id]?.stackable == true
}

class Inventory(saved: List<ItemStack?> = List(28) { null }) {
    private var slots = saved.toMutableList()
    init {
        require(slots.size == 28)
        require(slots.filterNotNull().all { it.id >= 0 && it.amount > 0 && (Goods.stackable(it.id) || it.amount == 1) })
    }
    fun snapshot(): List<ItemStack?> = slots.toList()
    operator fun get(slot: Int) = slots.getOrNull(slot)
    fun count(id: Int): Long = slots.filterNotNull().filter { it.id == id }.sumOf { it.amount.toLong() }

    // Both halves commit together, so insufficient space never consumes coins/items.
    fun exchange(removeId: Int, removeAmount: Int, addId: Int, addAmount: Int): Boolean {
        if (removeAmount < 0 || addAmount < 0 || count(removeId) < removeAmount) return false
        val next = Inventory(snapshot())
        if (!next.remove(removeId, removeAmount) || !next.add(addId, addAmount)) return false
        slots = next.slots
        return true
    }
    fun add(id: Int, amount: Int): Boolean {
        if (id < 0 || amount < 0) return false
        if (amount == 0) return true
        val next = slots.toMutableList()
        if (Goods.stackable(id)) {
            val existing = next.indexOfFirst { it?.id == id }
            val slot = if (existing >= 0) existing else next.indexOf(null)
            if (slot < 0) return false
            val total = (next[slot]?.amount ?: 0).toLong() + amount
            if (total > Int.MAX_VALUE) return false
            next[slot] = ItemStack(id, total.toInt())
        } else {
            if (next.count { it == null } < amount) return false
            repeat(amount) { next[next.indexOf(null)] = ItemStack(id) }
        }
        slots = next
        return true
    }
    fun remove(id: Int, amount: Int): Boolean {
        if (amount < 0 || count(id) < amount) return false
        var remaining = amount
        for (i in slots.indices) {
            val item = slots[i] ?: continue
            if (item.id != id) continue
            val removed = minOf(item.amount, remaining)
            slots[i] = if (removed == item.amount) null else item.copy(amount = item.amount - removed)
            remaining -= removed
            if (remaining == 0) break
        }
        return true
    }
    companion object {
        fun starter() = Inventory().apply { add(Goods.COINS, 1000); add(333, 5) }
    }
}

class ShopStock {
    private val stock = Goods.items.keys.associateWith { 100 }.toMutableMap()
    fun count(id: Int) = stock[id] ?: 0
    fun restock() { stock.replaceAll { _, value -> minOf(100, value + 1) } }
    fun buy(inventory: Inventory, id: Int, quantity: Int): Boolean {
        val item = Goods.items[id] ?: return false
        if (quantity !in 1..100 || count(id) < quantity) return false
        val price = item.price.toLong() * quantity
        if (price > Int.MAX_VALUE || !inventory.exchange(Goods.COINS, price.toInt(), id, quantity)) return false
        stock[id] = count(id) - quantity
        return true
    }
    fun sell(inventory: Inventory, id: Int, quantity: Int): Boolean {
        val item = Goods.items[id] ?: return false
        if (quantity !in 1..100 || count(id) + quantity > 1000) return false
        val price = maxOf(1, item.price / 2) * quantity
        if (!inventory.exchange(id, quantity, Goods.COINS, price)) return false
        stock[id] = count(id) + quantity
        return true
    }
}
