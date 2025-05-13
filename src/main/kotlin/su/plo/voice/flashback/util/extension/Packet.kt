package su.plo.voice.flashback.util.extension

import su.plo.slib.mod.channel.ByteArrayPayload
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.server.ModVoiceServer

fun Packet<*>.encodeToByteArrayPayload(): ByteArrayPayload {
    val modChannelManager = ModChannelManager.Companion
    val codec = modChannelManager.getOrRegisterCodec(ModVoiceServer.CHANNEL)

    val encoded = PacketTcpCodec.encode(this)

    return ByteArrayPayload(codec.type, encoded)
}
