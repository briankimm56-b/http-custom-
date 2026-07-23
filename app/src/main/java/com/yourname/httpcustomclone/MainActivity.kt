package com.yourname.httpcustomclone

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class MainActivity : AppCompatActivity() {

    private var vpnServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etHost = findViewById<EditText>(R.id.etHost)
        val etPort = findViewById<EditText>(R.id.etPort)
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etPayload = findViewById<EditText>(R.id.etPayload)
        val etHeaders = findViewById<EditText>(R.id.etHeaders)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val tvLog = findViewById<TextView>(R.id.tvLog)

        btnStart.setOnClickListener {
            val host = etHost.text.toString()
            val port = etPort.text.toString()
            val user = etUser.text.toString()
            val pass = etPass.text.toString()
            val payload = etPayload.text.toString()
            val headersRaw = etHeaders.text.toString()

            val headers = headersRaw.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }.toMap()

            val url = "https://$host:${port.toIntOrNull() ?: 443}/"

            HttpClient.sendRequest(url, payload, headers) { result ->
                runOnUiThread { tvLog.text = result }
            }

            // Create VPN service intent with SSH tunnel configuration
            val intent = Intent(this, TunnelVpnService::class.java)
            intent.putExtra("host", host) // SSH server
            intent.putExtra("port", port.toIntOrNull() ?: 22)
            intent.putExtra("user", user)
            intent.putExtra("pass", pass)
            intent.putExtra("targetHost", "www.google.com") // Where traffic goes
            intent.putExtra("targetPort", 80)

            // Store intent for onActivityResult
            this.vpnServiceIntent = intent

            // Request VPN permission
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_PERMISSION_REQUEST_CODE)
            } else {
                startService(intent)
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, TunnelVpnService::class.java))
            runOnUiThread { tvLog.text = "Tunnel stopped" }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK && vpnServiceIntent != null) {
                startService(vpnServiceIntent)
                Toast.makeText(this, "VPN tunnel starting...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST_CODE = 100
    }
}
