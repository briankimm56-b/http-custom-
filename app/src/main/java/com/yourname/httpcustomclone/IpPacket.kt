package com.yourname.httpcustomclone

import java.nio.ByteBuffer

data class IpPacket(
    val version: Int,
    val protocol: Int,
    val sourceIp: String,
    val destIp: String,
    val sourcePort: Int,
    val destPort: Int,
    val payload: ByteArray,
    val buffer: ByteBuffer
) {
    companion object {
        fun parse(buffer: ByteBuffer): IpPacket? {
            buffer.rewind()
            val versionAndIhl = buffer.get().toInt()
            val version = versionAndIhl shr 4
            val ihl = (versionAndIhl and 0x0F) * 4

            buffer.position(9)
            val protocol = buffer.get().toInt() and 0xFF

            val srcIp = "${buffer.get() and 0xFF}.${buffer.get() and 0xFF}.${buffer.get() and 0xFF}.${buffer.get() and 0xFF}"
            val dstIp = "${buffer.get() and 0xFF}.${buffer.get() and 0xFF}.${buffer.get() and 0xFF}.${buffer.get() and 0xFF}"

            buffer.position(ihl)
            val srcPort = buffer.short.toInt() and 0xFFFF
            val dstPort = buffer.short.toInt() and 0xFFFF

            buffer.position(ihl)
            val payload = ByteArray(buffer.remaining())
            buffer.get(payload)

            buffer.rewind()
            return IpPacket(version, protocol, srcIp, dstIp, srcPort, dstPort, payload, buffer)
        }
    }
}
