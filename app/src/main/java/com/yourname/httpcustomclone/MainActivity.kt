package com.yourname.httpcustomclone

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etHost = findViewById<EditText>(R.id.etHost)
        val etPort = findViewById<EditText>(R.id.etPort)
        val etPayload = findViewById<EditText>(R.id.etPayload)
        val etHeaders = findViewById<EditText>(R.id.etHeaders)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val tvLog = findViewById<TextView>(R.id.tvLog)

        btnStart.setOnClickListener {
            val host = etHost.text.toString()
            val port = etPort.text.toString()
            val payload = etPayload.text.toString()
            val headersRaw = etHeaders.text.toString()

            val headers = headersRaw.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }.toMap()

            val url = "https://$host:$port/"

            HttpClient.sendRequest(url, payload, headers) { result ->
                runOnUiThread { tvLog.text = result }
            }

            // Start VPN
            val intent = VpnService.prepare(this)
            if (intent != null) startActivityForResult(intent, 0)
            else startService(Intent(this, TunnelVpnService::class.java))
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, TunnelVpnService::class.java))
            runOnUiThread { tvLog.text = "Tunnel stopped" }
        }
    }
}
