package su.plo.voice.flashback.network

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import su.plo.voice.flashback.FlashbackVoiceAddon
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket
import su.plo.voice.proto.packets.tcp.clientbound.LanguagePacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket
import java.security.KeyPair

class VoiceSetupPacket(
    val keyPair: KeyPair,
    val connection: ConnectionPacket,
    val config: ConfigPacket,
    val playerList: PlayerListPacket,
    val language: LanguagePacket,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = type

    companion object {
        val type = CustomPacketPayload.Type<VoiceSetupPacket>(FlashbackVoiceAddon.modResourceLocation("setup"))
        val streamCodec = VoiceSetupPacketCodec()
    }
}
