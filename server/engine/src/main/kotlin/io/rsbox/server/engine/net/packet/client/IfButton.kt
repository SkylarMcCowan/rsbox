package io.rsbox.server.engine.net.packet.client

import io.rsbox.server.engine.api.interf
import io.rsbox.server.engine.api.logout_tab
import io.rsbox.server.engine.model.ui.Component
import io.rsbox.server.engine.net.Session
import io.rsbox.server.engine.net.game.*
import io.rsbox.server.engine.net.packet.server.Logout
import io.rsbox.server.util.buffer.JagByteBuf

// Revision 217's legacy interface button packet (ClientPacket.field3080).
@ClientPacket(opcode = 34, type = PacketType.FIXED, length = 4)
data class IfButton(val component: Component) : Packet {
    override fun handle(session: Session) {
        handleLogoutButton(session, component)
    }

    companion object : Codec<IfButton> {
        override fun decode(session: Session, buf: JagByteBuf) = IfButton(Component(buf.readInt()))
    }
}

// Revision 217's first interface option (ClientPacket.field3095).
@ClientPacket(opcode = 50, type = PacketType.FIXED, length = 8)
data class IfButton1(val component: Component, val slot: Int, val item: Int) : Packet {
    override fun handle(session: Session) {
        handleLogoutButton(session, component)
    }

    companion object : Codec<IfButton1> {
        override fun decode(session: Session, buf: JagByteBuf) =
            IfButton1(Component(buf.readInt()), buf.readUnsignedShort(), buf.readUnsignedShort())
    }
}

private fun handleLogoutButton(session: Session, component: Component) {
    if (component != interf.logout_tab.child(12)) return
    if (interf.logout_tab !in session.player.ui.overlays.values) return
    // Closing after the logout packet flushes lets the client return to its login screen.
    // Session.onDisconnect performs the existing player save and world cleanup.
    session.writeAndClose(Logout())
}
