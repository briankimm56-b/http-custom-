package com.yourname.httpcustomclone

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor
import kotlin.concurrent.thread

class TunnelVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var sshTunnel = SshTunnel()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val host = intent?.getStringExtra("host") ?: return START_NOT_STICKY
        val port = intent.getIntExtra("port", 22)
        val user = intent.getStringExtra("user") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""

        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .addRoute("0.0.0.0", 0)
            .setSession("HttpCustomClone")

        vpnInterface = builder.establish()

        thread {
            if (sshTunnel.connect(host, port, user, pass)) {
                // Here you would read from vpnInterface.fileDescriptor and write to SSH channel
                // For MVP we just keep connection alive
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sshTunnel.disconnect()
        vpnInterface?.close()
        super.onDestroy()
    }
}
