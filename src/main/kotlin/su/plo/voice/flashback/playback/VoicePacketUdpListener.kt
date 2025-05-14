package su.plo.voice.flashback.playback

import com.google.common.collect.Maps
import com.moulberry.flashback.Flashback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import su.plo.voice.flashback.FlashbackVoiceAddon
import su.plo.voice.flashback.invoke
import su.plo.voice.flashback.isCameraRemotePlayer
import su.plo.voice.flashback.network.PacketUdpWrapper
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import kotlin.jvm.optionals.getOrNull

class VoicePacketUdpListener(
    private val addon: FlashbackVoiceAddon,
) : ClientPlayNetworking.PlayPayloadHandler<PacketUdpWrapper> {
    private val voiceClient by lazy { addon.voiceClient }

    private val selfAudioHandler = SelfAudioHandler()

    override fun receive(
        packetUdpWrapper: PacketUdpWrapper,
        context: ClientPlayNetworking.Context,
    ) {
        val replayServer = Flashback.getReplayServer() ?: return
        if (replayServer.isProcessingSnapshot) return

        val shouldSend =
            if (Flashback.isExporting()) {
                FlashbackVoiceAddon.config.exportVoiceChat() &&
                    Flashback.EXPORT_JOB.settings.recordAudio() &&
                    Flashback.EXPORT_JOB.currentTickDouble > 0.0
            } else {
                !replayServer.fastForwarding && !replayServer.replayPaused
            }
        if (!shouldSend) return

        val stateInitialized = voiceClient.serverInfo.isPresent
        if (!stateInitialized) return

        val packetUdp = packetUdpWrapper.packetUdp

        when (val packet = packetUdp.packetUntyped) {
            is SourceAudioPacket -> handleSourceAudio(packet)
            is PlayerAudioPacket -> selfAudioHandler.handleSelfAudio(packet)
            is SelfAudioInfoPacket -> selfAudioHandler.handleSelfAudioInfo(packet)
        }
    }

    private fun handleSourceAudio(packet: SourceAudioPacket) {
        val source =
            voiceClient.sourceManager
                .getSourceById(packet.sourceId)
                ?.getOrNull()
                ?: return

        if (source.sourceInfo.state != packet.sourceState) return
        source.process(packet)
    }

    private inner class SelfAudioHandler {
        private val maxPackets = 50

        private val packetIndex: MutableMap<Long, Int> = Maps.newHashMap()
        private val packets: MutableList<PlayerAudioPacket> = ArrayList()

        private var currentPacketIndex = 0

        fun handleSelfAudio(packet: PlayerAudioPacket) {
            val currentPacket = if (packets.size > currentPacketIndex) packets[currentPacketIndex] else null
            if (currentPacket != null) {
                packetIndex.remove(currentPacket.sequenceNumber)
            }

            if (packets.size > currentPacketIndex) {
                packets[currentPacketIndex] = packet
            } else {
                packets.add(packet)
            }
            packetIndex[packet.sequenceNumber] = currentPacketIndex

            currentPacketIndex = (currentPacketIndex + 1) % maxPackets
        }

        fun handleSelfAudioInfo(packet: SelfAudioInfoPacket) {
            val shouldPlay =
                voiceClient.sourceManager
                    .getSelfSourceInfo(packet.sourceId)
                    .map { selfSourceInfo ->
                        if (!isCameraRemotePlayer() && selfSourceInfo.selfSourceInfo.sourceInfo is DirectSourceInfo) return@map false
                        if (voiceClient.sourceManager.getSourceById(packet.sourceId, false).isPresent) return@map true

                        voiceClient.sourceManager.createOrUpdateSource(selfSourceInfo.selfSourceInfo.sourceInfo)
                        true
                    }.orElse(true)

            val source =
                voiceClient.sourceManager
                    .getSourceById(packet.sourceId, false)
                    .getOrNull() ?: return

            if (source.isActivated() && !shouldPlay) {
                source.closeAsync()
            } else if (!shouldPlay) {
                return
            }

            val packetData =
                packet.data
                    ?.getOrNull()
                    ?: run {
                        val index = packetIndex.getOrDefault(packet.sequenceNumber, -1)
                        if (index == -1) return
                        packets[index].data
                    }

            source.process(
                SourceAudioPacket(
                    packet.sequenceNumber,
                    source.sourceInfo.state,
                    packetData,
                    packet.sourceId,
                    packet.distance,
                ),
            )
        }
    }
}
