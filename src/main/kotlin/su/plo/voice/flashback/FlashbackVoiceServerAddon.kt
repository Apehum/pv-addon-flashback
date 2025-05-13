package su.plo.voice.flashback

import com.moulberry.flashback.Flashback
import net.fabricmc.api.ModInitializer
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent
import su.plo.voice.api.server.event.connection.TcpPacketSendEvent

@Addon(
    id = "pv-addon-flashback",
    version = "1.0.0", // todo: build const
    authors = ["Apehum"],
)
class FlashbackVoiceServerAddon : ModInitializer {
    override fun onInitialize() {
        PlasmoVoiceServer.getAddonsLoader().load(this)
    }

    // disable server-side completely in replays
    @EventSubscribe
    fun onPacketReceived(event: TcpPacketReceivedEvent) {
        if (!Flashback.isInReplay()) return
        event.isCancelled = true
    }

    @EventSubscribe
    fun onPacketSend(event: TcpPacketSendEvent) {
        if (!Flashback.isInReplay()) return
        event.isCancelled = true
    }
}
