package io.rsbox.server.engine.net.packet.server

import io.rsbox.server.engine.net.Session
import io.rsbox.server.engine.net.game.*
import io.rsbox.server.util.buffer.JagByteBuf

@ServerPacket(opcode = 3, type = PacketType.FIXED)
class Logout : Packet {
    companion object : Codec<Logout> {
        override fun encode(session: Session, packet: Logout, out: JagByteBuf) = Unit
    }
}
