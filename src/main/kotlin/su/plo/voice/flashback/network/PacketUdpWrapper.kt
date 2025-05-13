package su.plo.voice.flashback.network

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import su.plo.voice.flashback.FlashbackVoiceAddon
import su.plo.voice.proto.packets.udp.PacketUdp

data class PacketUdpWrapper(
    val packetUdp: PacketUdp,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = type

    companion object {
        val type = CustomPacketPayload.Type<PacketUdpWrapper>(FlashbackVoiceAddon.modResourceLocation("packet_udp"))
        val streamCodec = PacketUdpCodec()
    }
}
