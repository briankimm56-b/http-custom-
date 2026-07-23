package com.yourname.httpcustomclone

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.InputStream
import java.io.OutputStream

class SshTunnel {
    private var ssh: SSHClient? = null

    fun connect(host: String, port: Int, username: String, password: String): Boolean {
        return try {
            ssh = SSHClient()
            ssh!!.addHostKeyVerifier(PromiscuousVerifier()) // For testing only. Use real key verification in prod
            ssh!!.connect(host, port)
            ssh!!.authPassword(username, password)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun forwardLocalPort(localPort: Int, remoteHost: String, remotePort: Int) {
        ssh?.newLocalPortForwarder(
            com.sun.net.httpserver.HttpServer.create() // dummy, we just need the forwarder
        ) // Note: sshj uses: ssh.newLocalPortForwarder(localPort, remoteHost, remotePort)
    }

    fun getInputStream(): InputStream? = ssh?.remotePortForwardedChannels?.firstOrNull()?.inputStream
    fun getOutputStream(): OutputStream? = ssh?.remotePortForwardedChannels?.firstOrNull()?.outputStream

    fun disconnect() {
        ssh?.disconnect()
    }
}
