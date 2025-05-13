package su.plo.voice.flashback.network

import com.google.common.io.ByteStreams
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import su.plo.voice.proto.packets.PacketHandler
import su.plo.voice.proto.packets.udp.PacketUdpCodec

class PacketUdpCodec : StreamCodec<RegistryFriendlyByteBuf, PacketUdpWrapper> {
    override fun decode(buf: RegistryFriendlyByteBuf): PacketUdpWrapper {
        val length = buf.readableBytes()

        val data = ByteArray(length)
        buf.readBytes(data)

        val udpPacket =
            PacketUdpCodec
                .decode(ByteStreams.newDataInput(data))
                .orElseThrow { IllegalArgumentException("Failed to decode UDP packet") }

        return PacketUdpWrapper(udpPacket)
    }

    override fun encode(
        buf: RegistryFriendlyByteBuf,
        udpPacketWrapper: PacketUdpWrapper,
    ) {
        val udpPacket = udpPacketWrapper.packetUdp
        val encoded = PacketUdpCodec.encode(udpPacket.getPacket<PacketHandler>(), udpPacket.secret)
        buf.writeBytes(encoded)
    }
}
