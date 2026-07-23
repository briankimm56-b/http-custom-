package com.yourname.httpcustomclone

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import net.schmizz.sshj.connection.channel.direct.TCPIPForwardChannel
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors

class TunnelVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var sshTunnel = SshTunnel()
    private val executor = Executors.newFixedThreadPool(2)
    private var running = false

    companion object {
        const val CHANNEL_ID = "VpnServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Start Foreground with notification
        val notification = createNotification("Connecting...")
        startForeground(NOTIFICATION_ID, notification)

        val host = intent?.getStringExtra("host") ?: return START_NOT_STICKY
        val port = intent.getIntExtra("port", 22)
        val user = intent.getStringExtra("user") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""
        val targetHost = intent.getStringExtra("targetHost") ?: "google.com"
        val targetPort = intent.getIntExtra("targetPort", 80)

        // 2. Start VPN
        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .setSession("HttpCustomClone")
            .setMtu(1500)

        vpnInterface = builder.establish()
        running = true
        updateNotification("Connected to $host")

        Thread {
            if (!sshTunnel.connect(host, port, user, pass)) {
                updateNotification("SSH Connection Failed")
                stopSelf()
                return@Thread
            }

            val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)

            val channel: TCPIPForwardChannel = sshTunnel.openDirectChannel(targetHost, targetPort)
                ?: return@Thread

            val sshInput = channel.inputStream
            val sshOutput = channel.outputStream

            updateNotification("Tunnel Active: $targetHost:$targetPort")

            executor.execute { forward(vpnInput, sshOutput) }
            executor.execute { forward(sshInput, vpnOutput) }
        }.start()

        return START_STICKY
    }

    private fun forward(input: java.io.InputStream, output: java.io.OutputStream) {
        val buffer = ByteArray(32767)
        try {
            while (running) {
                val length = input.read(buffer)
                if (length > 0) {
                    output.write(buffer, 0, length)
                    output.flush()
                } else if (length < 0) break
            }
        } catch (e: Exception) { e.printStackTrace() }
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
        vpnInterface?.close()
        stopForeground(true)
        super.onDestroy()
    }
}
