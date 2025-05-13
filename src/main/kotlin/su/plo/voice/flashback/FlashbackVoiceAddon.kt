package su.plo.voice.flashback

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.addon.injectPlasmoVoice
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.flashback.network.PacketUdpWrapper
import su.plo.voice.flashback.network.VoiceSetupPacket
import su.plo.voice.flashback.playback.VoicePacketUdpListener
import su.plo.voice.flashback.playback.VoiceReplayEvents
import su.plo.voice.flashback.playback.VoiceSetupListener

@Addon(
    id = "pv-addon-flashback",
    scope = AddonLoaderScope.CLIENT,
    version = "1.0.0", // todo: build const
    authors = ["Apehum"],
)
class FlashbackVoiceAddon :
    AddonInitializer,
    ClientModInitializer {
    val voiceClient by injectPlasmoVoice<PlasmoVoiceClient>()

    override fun onInitializeClient() {
        instance = this

        PlasmoVoiceClient.getAddonsLoader().load(this)

        PayloadTypeRegistry.playS2C().register(VoiceSetupPacket.type, VoiceSetupPacket.streamCodec)
        PayloadTypeRegistry.playS2C().register(PacketUdpWrapper.type, PacketUdpWrapper.streamCodec)

        ClientPlayNetworking.registerGlobalReceiver(VoiceSetupPacket.type, VoiceSetupListener(this))
        ClientPlayNetworking.registerGlobalReceiver(PacketUdpWrapper.type, VoicePacketUdpListener(this))
    }

    override fun onAddonInitialize() {
        val recorder = VoicePacketRecorder(voiceClient)
        voiceClient.eventBus.register(this, recorder)

        val replayEvents = VoiceReplayEvents(this, voiceClient)
        voiceClient.eventBus.register(this, replayEvents)
    }

    companion object {
        @JvmField
        val LOGGER = LoggerFactory.getLogger(FlashbackVoiceAddon::class.java)

        @JvmStatic
        lateinit var instance: FlashbackVoiceAddon
            private set

        val modId = "pvaddonflashback"

        fun modResourceLocation(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(modId, path)
    }
}
