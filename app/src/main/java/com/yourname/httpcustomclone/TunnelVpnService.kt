package com.yourname.httpcustomclone

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class TunnelVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var sshTunnel = SshTunnel()
    private val executor = Executors.newSingleThreadExecutor()
    private var running = false
    private lateinit var connectionManager: ConnectionManager

    companion object {
        const val CHANNEL_ID = "VpnServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Connecting..."))

        val host = intent?.getStringExtra("host") ?: return START_NOT_STICKY
        val port = intent.getIntExtra("port", 22)
        val user = intent.getStringExtra("user") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""

        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)
            .setSession("HttpCustomClone")
            .setMtu(1500)

        vpnInterface = builder.establish()
        running = true

        Thread {
            if (!sshTunnel.connect(host, port, user, pass)) {
                updateNotification("SSH Failed")
                stopSelf()
                return@Thread
            }

            updateNotification("Tunnel Active")
            val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
            connectionManager = ConnectionManager(vpnOutput, sshTunnel)

            val buffer = ByteBuffer.allocate(32767)
            while (running) {
                val length = vpnInput.read(buffer.array())
                if (length > 0) {
                    buffer.limit(length)
                    val packet = IpPacket.parse(buffer)
                    if (packet != null && (packet.protocol == 6 || packet.protocol == 17)) { // TCP or UDP
                        connectionManager.routePacket(packet)
                    }
                    buffer.clear()
                }
            }
        }.start()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, TunnelVpnService::class.java).apply { action = "STOP" }
        val pendingStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HTTP Custom Clone")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .addAction(android.R.drawable.ic_delete, "Stop", pendingStop)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }

    override fun onDestroy() {
        running = false
        executor.shutdownNow()
        sshTunnel.disconnect()
        connectionManager.closeAll()
        vpnInterface?.close()
        stopForeground(true)
        super.onDestroy()
    }
}
