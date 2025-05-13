package su.plo.voice.flashback.playback

import com.moulberry.flashback.Flashback
import com.moulberry.flashback.playback.ReplayPlayer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import su.plo.slib.mod.channel.ByteArrayPayload
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.voice.flashback.FlashbackVoiceAddon
import su.plo.voice.flashback.event.FlashbackEvents
import su.plo.voice.flashback.network.VoiceSetupPacket
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.server.ModVoiceServer
import java.security.KeyPair

class VoiceSetupListener(
    private val addon: FlashbackVoiceAddon,
) : ClientPlayNetworking.PlayPayloadHandler<VoiceSetupPacket> {
    init {
        FlashbackEvents.EXPORT_END.register {
            addon.voiceClient.sourceManager.clear()
            addon.voiceClient.deviceManager.outputDevice
                .ifPresent { it.reload() }
        }
    }

    // todo: add public API reset method in ClientAudioSource
//    private val sourceResetMethod by lazy {
//        BaseClientAudioSource::class.java
//            .getDeclaredMethod(
//                "resetAsync",
//                AudioSourceResetEvent.Cause::class.java,
//            ).also { it.isAccessible = true }
//    }

    override fun receive(
        voiceSetupPacket: VoiceSetupPacket,
        context: ClientPlayNetworking.Context,
    ) {
        val stateInitialized = addon.voiceClient.serverInfo.isPresent
        if (stateInitialized) {
            // client is already connected and state is already set,
            // and we don't have to do it again;
            // but we need to reset all existing sources
            addon.voiceClient.sourceManager.sources
                .forEach { source ->
                    source.closeAsync()
                }
            return
        }

        val replayServer = Flashback.getReplayServer() ?: return
        currentKeyPair = voiceSetupPacket.keyPair

        for (replayViewer in replayServer.replayViewers) {
            replayViewer.sendPlasmoVoicePacket(voiceSetupPacket.connection)
            replayViewer.sendPlasmoVoicePacket(voiceSetupPacket.config)
            replayViewer.sendPlasmoVoicePacket(voiceSetupPacket.playerList)
            replayViewer.sendPlasmoVoicePacket(voiceSetupPacket.language)
        }
    }

    private fun ReplayPlayer.sendPlasmoVoicePacket(packet: Packet<*>) {
        val modChannelManager = ModChannelManager.Companion
        val codec = modChannelManager.getOrRegisterCodec(ModVoiceServer.CHANNEL)

        val encoded = PacketTcpCodec.encode(packet)

        ServerPlayNetworking.send(
            this,
            ByteArrayPayload(codec.type, encoded),
        )
    }

    companion object {
        var currentKeyPair: KeyPair? = null
    }
}
