package com.example.vpnil

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Key
import com.wireguard.config.KeyPair
import com.wireguard.config.Peer
import java.net.InetAddress

class VpnILService : Service() {

    private var backend: GoBackend? = null
    private val tunnel = VpnILTunnel()

    companion object {
        const val ACTION_CONNECT = "com.example.vpnil.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpnil.DISCONNECT"
        const val EXTRA_SERVER_ID = "EXTRA_SERVER_ID"

        @Volatile
        var isRunning = false
        @Volatile
        var activeServerLabel: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        backend = GoBackend(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                disconnectTunnel()
                return START_NOT_STICKY
            }
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                val server = ServerRepository.servers.find { it.id == serverId }
                    ?: ServerRepository.servers.first()
                connectTunnel(server)
                return START_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun connectTunnel(server: VpnServer) {
        Thread {
            try {
                val wgBackend = backend ?: GoBackend(this).also { backend = it }
                val config = buildWireGuardConfig(server)
                val state = wgBackend.setState(tunnel, Tunnel.State.UP, config)
                isRunning = state == Tunnel.State.UP
                activeServerLabel = if (isRunning) "${server.country} (${server.code})" else null
            } catch (_: Exception) {
                isRunning = false
                activeServerLabel = null
            }
        }.start()
    }

    private fun disconnectTunnel() {
        Thread {
            try {
                backend?.setState(tunnel, Tunnel.State.DOWN, null)
            } catch (_: Exception) {
            } finally {
                isRunning = false
                activeServerLabel = null
                stopSelf()
            }
        }.start()
    }

    private fun buildWireGuardConfig(server: VpnServer): Config {
        val keyPair = KeyPair(Key.fromBase64(server.clientPrivateKey))
        val interfaceBuilder = Interface.Builder()
            .parsePrivateKey(keyPair.privateKey.toBase64())
            .addAddress(InetNetwork.parse(server.clientAddress))
            .addDnsServer(InetAddress.getByName(server.dns))

        val peerBuilder = Peer.Builder()
            .parsePublicKey(server.serverPublicKey)
            .setEndpoint(InetEndpoint.parse(server.endpoint))

        server.allowedIps.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            peerBuilder.addAllowedIp(InetNetwork.parse(it))
        }

        return Config.Builder()
            .setInterface(interfaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        disconnectTunnel()
        super.onDestroy()
    }

    private class VpnILTunnel : Tunnel {
        override fun getName(): String = "VpnILTunnel"
        override fun onStateChange(newState: Tunnel.State) {
            isRunning = newState == Tunnel.State.UP
        }
    }
}
