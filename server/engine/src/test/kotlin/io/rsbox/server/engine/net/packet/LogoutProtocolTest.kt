package io.rsbox.server.engine.net.packet

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.embedded.EmbeddedChannel
import io.rsbox.server.engine.net.Session
import io.rsbox.server.engine.net.game.GameProtocol
import io.rsbox.server.engine.net.packet.client.IfButton
import io.rsbox.server.engine.net.packet.client.IfButton1
import io.rsbox.server.engine.net.packet.server.Logout
import io.rsbox.server.util.buffer.toJagBuf
import io.rsbox.server.util.security.IsaacRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LogoutProtocolTest {
    @Test
    fun `revision 217 button payloads preserve component and slot`() {
        val channel = EmbeddedChannel(ChannelInboundHandlerAdapter())
        try {
            val session = Session(channel.pipeline().firstContext())
            val payload = Unpooled.buffer().writeInt((182 shl 16) or 12)
                .writeShort(65535).writeShort(65535)
            try {
                val button = IfButton1.decode(session, payload.toJagBuf())
                assertEquals(182, button.component.interfaceId)
                assertEquals(12, button.component.child)
                assertEquals(65535, button.slot)
                assertEquals(65535, button.item)
                assertFalse(payload.isReadable)
                payload.readerIndex(0)
                assertEquals(button.component, IfButton.decode(session, payload.toJagBuf()).component)
            } finally {
                payload.release()
            }
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    @Test
    fun `registered logout response encodes opcode 3 with no payload`() {
        GamePackets.load()
        assertFalse(GamePackets.clientPackets.isUnknown(34))
        assertFalse(GamePackets.clientPackets.isUnknown(50))
        val channel = EmbeddedChannel(ChannelInboundHandlerAdapter())
        val encoded = Unpooled.buffer()
        try {
            val session = Session(channel.pipeline().firstContext())
            val seed = intArrayOf(1, 2, 3, 4)
            session.encoderIsaac.init(seed)
            val expectedCipher = IsaacRandom().apply { init(seed) }
            GameProtocol(session).encode(Logout(), encoded)
            assertEquals(1, encoded.readableBytes())
            assertEquals((3 + expectedCipher.nextInt()) and 255, encoded.readUnsignedByte().toInt())
        } finally {
            encoded.release()
            channel.finishAndReleaseAll()
        }
    }
}
