package su.plo.voice.flashback.network

import com.google.common.io.ByteStreams
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket
import su.plo.voice.proto.packets.tcp.clientbound.LanguagePacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class VoiceSetupPacketCodec : StreamCodec<RegistryFriendlyByteBuf, VoiceSetupPacket> {
    override fun decode(buf: RegistryFriendlyByteBuf): VoiceSetupPacket {
        val publicKey = ByteArray(buf.readInt())
        buf.readBytes(publicKey)
        val privateKey = ByteArray(buf.readInt())
        buf.readBytes(privateKey)

        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKeySpec = X509EncodedKeySpec(publicKey)
        val privateKeySpec = PKCS8EncodedKeySpec(privateKey)

        val connection = buf.readPacket(ConnectionPacket())
        val config = buf.readPacket(ConfigPacket())
        val playerList = buf.readPacket(PlayerListPacket())
        val language = buf.readPacket(LanguagePacket())

        return VoiceSetupPacket(
            KeyPair(
                keyFactory.generatePublic(publicKeySpec),
                keyFactory.generatePrivate(privateKeySpec),
            ),
            connection,
            config,
            playerList,
            language,
        )
    }

    override fun encode(
        buf: RegistryFriendlyByteBuf,
        voiceSetupPacket: VoiceSetupPacket,
    ) {
        val keyPair = voiceSetupPacket.keyPair

        val publicKey = keyPair.public.encoded
        val privateKey = keyPair.private.encoded

        buf.writeInt(publicKey.size)
        buf.writeBytes(publicKey)

        buf.writeInt(privateKey.size)
        buf.writeBytes(privateKey)

        buf.writePacket(voiceSetupPacket.connection)
        buf.writePacket(voiceSetupPacket.config)
        buf.writePacket(voiceSetupPacket.playerList)
        buf.writePacket(voiceSetupPacket.language)
    }

    private fun <T : Packet<*>> FriendlyByteBuf.readPacket(packet: T): T {
        val packetBytes = ByteArray(readInt())
        readBytes(packetBytes)

        packet.read(ByteStreams.newDataInput(packetBytes))

        return packet
    }

    private fun FriendlyByteBuf.writePacket(packet: Packet<*>) {
        val packetBytes =
            ByteStreams
                .newDataOutput()
                .also { packet.write(it) }
                .toByteArray()

        writeInt(packetBytes.size)
        writeBytes(packetBytes)
    }
}
