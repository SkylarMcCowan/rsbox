package io.rsbox.server.engine.net.packet.server

import io.rsbox.server.engine.model.gameplay.ItemStack
import io.rsbox.server.engine.net.Session
import io.rsbox.server.engine.net.game.*
import io.rsbox.server.util.buffer.*

@ServerPacket(opcode = 74, type = PacketType.VARIABLE_SHORT)
data class IfSetText(val component: Int, val text: String) : Packet {
    companion object : Codec<IfSetText> {
        override fun encode(session: Session, packet: IfSetText, out: JagByteBuf) {
            out.writeInt(packet.component); out.writeString(packet.text)
        }
    }
}

@ServerPacket(opcode = 50, type = PacketType.FIXED)
data class IfCloseSub(val component: Int) : Packet {
    companion object : Codec<IfCloseSub> {
        override fun encode(session: Session, packet: IfCloseSub, out: JagByteBuf) { out.writeInt(packet.component) }
    }
}

@ServerPacket(opcode = 55, type = PacketType.VARIABLE_SHORT)
data class UpdateInventory(val items: List<ItemStack?>) : Packet {
    companion object : Codec<UpdateInventory> {
        override fun encode(session: Session, packet: UpdateInventory, out: JagByteBuf) {
            out.writeInt(-1); out.writeShort(93); out.writeShort(packet.items.size)
            for (item in packet.items) {
                val amount = item?.amount ?: 0
                out.writeByte(minOf(255, amount))
                if (amount >= 255) out.writeInt(amount, endian = MIDDLE)
                out.writeShort((item?.id ?: -1) + 1)
            }
        }
    }
}

@ServerPacket(opcode = 28, type = PacketType.FIXED)
data class UpdateSkill(val skill: Int, val level: Int, val xp: Int) : Packet {
    companion object : Codec<UpdateSkill> {
        override fun encode(session: Session, packet: UpdateSkill, out: JagByteBuf) {
            out.writeByte(packet.skill); out.writeByte(packet.level, transform = SUB); out.writeInt(packet.xp, endian = MIDDLE)
        }
    }
}
