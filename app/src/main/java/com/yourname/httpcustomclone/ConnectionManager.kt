package com.yourname.httpcustomclone

import net.schmizz.sshj.connection.channel.direct.TCPIPForwardChannel
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class ConnectionManager(private val vpnOutput: FileOutputStream, private val sshTunnel: SshTunnel) {
    private val channels = ConcurrentHashMap<String, TCPIPForwardChannel>()

    fun routePacket(packet: IpPacket) {
        val key = "${packet.destIp}:${packet.destPort}"

        val channel = channels.getOrPut(key) {
            sshTunnel.openDirectChannel(packet.destIp, packet.destPort) ?: return
        }

        try {
            channel.outputStream.write(packet.payload)
            channel.outputStream.flush()
        } catch (e: Exception) {
            channels.remove(key)
            e.printStackTrace()
        }
    }

    fun closeAll() {
        channels.values.forEach { it.close() }
        channels.clear()
    }
}
