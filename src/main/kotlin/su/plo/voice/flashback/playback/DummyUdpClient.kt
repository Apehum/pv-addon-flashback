package su.plo.voice.flashback.playback

import org.apache.logging.log4j.LogManager
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.event.connection.ServerInfoInitializedEvent
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent
import su.plo.voice.api.client.socket.UdpClient
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.proto.packets.Packet
import java.net.InetSocketAddress
import java.util.Optional
import java.util.UUID

class DummyUdpClient(
    private val voiceClient: PlasmoVoiceClient,
    private val secret: UUID,
) : UdpClient {
    private val logger = LogManager.getLogger()
    private val address = InetSocketAddress("127.0.0.1", 1)

    private var connected = false

    override fun connect(
        ip: String,
        port: Int,
    ) {
    }

    override fun close(reason: UdpClientClosedEvent.Reason) {
    }

    override fun sendPacket(packet: Packet<*>?) {
    }

    override fun getSecret(): UUID = secret

    override fun getRemoteAddress(): Optional<InetSocketAddress> = Optional.of(address)

    override fun isClosed(): Boolean = false

    override fun isConnected(): Boolean = connected

    override fun isTimedOut(): Boolean = false

    @EventSubscribe
    fun onServerInfoUpdate(event: ServerInfoInitializedEvent) {
        if (connected) return

        logger.info("Connected to fake UDP client")
        this.connected = true

        voiceClient.eventBus.fire(UdpClientConnectedEvent(this))
    }
}
