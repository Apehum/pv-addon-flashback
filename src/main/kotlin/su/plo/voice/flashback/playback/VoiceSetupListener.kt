package su.plo.voice.flashback.playback

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import su.plo.voice.flashback.FlashbackVoiceAddon
import su.plo.voice.flashback.event.FlashbackEvents
import su.plo.voice.flashback.network.VoiceSetupPacket
import kotlin.jvm.optionals.getOrNull

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

    override fun receive(
        voiceSetupPacket: VoiceSetupPacket,
        context: ClientPlayNetworking.Context,
    ) {
        val stateInitialized = addon.voiceClient.serverInfo.isPresent
        if (stateInitialized) {
            // client is already connected and state is already set,
            // and we don't have to do it again;
            // but we need to reset all existing sources
            // todo: add public API reset function in ClientAudioSource
            addon.voiceClient.sourceManager.sources
                .forEach { source -> source.source.stop() }
            return
        }

        val serverConnection = addon.voiceClient.serverConnection.getOrNull() ?: return
        serverConnection.keyPair = voiceSetupPacket.keyPair

        serverConnection.handle(voiceSetupPacket.connection)
        serverConnection.handle(voiceSetupPacket.config)
        serverConnection.handle(voiceSetupPacket.playerList)
        serverConnection.handle(voiceSetupPacket.language)
    }
}
