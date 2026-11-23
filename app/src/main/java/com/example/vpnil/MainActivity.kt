package com.example.vpnil

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepForestBg = Color(0xFF071710)
private val DarkEmeraldSurface = Color(0xFF0E291D)
private val CardBorderGreen = Color(0xFF1B4332)
private val EmeraldAccent = Color(0xFF10B981)
private val MutedSageText = Color(0xFF8FB8A6)
private val OffWhiteText = Color(0xFFE8F5EE)

class MainActivity : ComponentActivity() {

    private var isConnected by mutableStateOf(VpnILService.isRunning)
    private var selectedServer by mutableStateOf(ServerRepository.servers.first())
    private var statusText by mutableStateOf(
        if (VpnILService.isRunning) {
            "Terhubung ke ${VpnILService.activeServerLabel ?: selectedServer.country}"
        } else {
            "Siap Dihubungkan"
        }
    )

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startTunnelService(selectedServer)
        } else {
            isConnected = false
            statusText = "Izin VPN Ditolak"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VpnILTheme {
                VpnILAllCountryScreen(
                    isConnected = isConnected,
                    statusText = statusText,
                    servers = ServerRepository.servers,
                    selectedServer = selectedServer,
                    onSelectServer = { server ->
                        if (!isConnected) {
                            selectedServer = server
                        }
                    },
                    onToggleVpn = {
                        if (isConnected) {
                            stopTunnelService()
                        } else {
                            requestVpnPermissionAndConnect()
                        }
                    }
                )
            }
        }
    }

    private fun requestVpnPermissionAndConnect() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startTunnelService(selectedServer)
        }
    }

    private fun startTunnelService(server: VpnServer) {
        val intent = Intent(this, VpnILService::class.java).apply {
            action = VpnILService.ACTION_CONNECT
            putExtra(VpnILService.EXTRA_SERVER_ID, server.id)
        }
        startService(intent)
        isConnected = true
        statusText = "Terhubung ke ${server.country} (${server.code})"
    }

    private fun stopTunnelService() {
        val intent = Intent(this, VpnILService::class.java).apply {
            action = VpnILService.ACTION_DISCONNECT
        }
        startService(intent)
        isConnected = false
        statusText = "Siap Dihubungkan"
    }
}

@Composable
fun VpnILTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = EmeraldAccent,
        background = DeepForestBg,
        surface = DarkEmeraldSurface,
        onPrimary = DeepForestBg,
        onBackground = OffWhiteText,
        onSurface = OffWhiteText
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun VpnILAllCountryScreen(
    isConnected: Boolean,
    statusText: String,
    servers: List<VpnServer>,
    selectedServer: VpnServer,
    onSelectServer: (VpnServer) -> Unit,
    onToggleVpn: () -> Unit
) {
    val ringColor by animateColorAsState(
        targetValue = if (isConnected) EmeraldAccent else CardBorderGreen,
        animationSpec = tween(350),
        label = "ring"
    )
    val buttonBg by animateColorAsState(
        targetValue = if (isConnected) EmeraldAccent else DarkEmeraldSurface,
        animationSpec = tween(350),
        label = "buttonBg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isConnected) DeepForestBg else OffWhiteText,
        animationSpec = tween(350),
        label = "iconTint"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A2218), DeepForestBg)))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header dengan Logo SVG Perisai
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkEmeraldSurface)
                        .border(1.dp, CardBorderGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_vpn),
                        contentDescription = "Logo VpnIL",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "VpnIL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhiteText
                    )
                    Text(
                        text = "Global Multi-Region Tunnel",
                        fontSize = 12.sp,
                        color = MutedSageText
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (isConnected) EmeraldAccent.copy(alpha = 0.16f) else DarkEmeraldSurface,
                border = BorderStroke(1.dp, if (isConnected) EmeraldAccent else CardBorderGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) EmeraldAccent else MutedSageText)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) selectedServer.code else "SIAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConnected) EmeraldAccent else MutedSageText
                    )
                }
            }
        }

        // Tombol Daya Tengah dengan Ikon SVG Power
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .clip(CircleShape)
                    .background(ringColor.copy(alpha = 0.14f))
                    .border(2.dp, ringColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .border(2.dp, ringColor, CircleShape)
                        .clickable { onToggleVpn() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power),
                            contentDescription = "Tombol Koneksi VPN",
                            tint = iconTint,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isConnected) "PUTUSKAN" else "HUBUNGKAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconTint,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OffWhiteText
            )
            Text(
                text = "Endpoint: ${selectedServer.endpoint}",
                fontSize = 12.sp,
                color = MutedSageText
            )
        }

        // Daftar Pilihan Negara dengan Ikon SVG Globe
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkEmeraldSurface,
            border = BorderStroke(1.dp, CardBorderGreen)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_globe),
                        contentDescription = "Daftar Negara",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih Lokasi Negara",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OffWhiteText
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(servers, key = { it.id }) { server ->
                        val active = server.id == selectedServer.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isConnected) { onSelectServer(server) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) EmeraldAccent.copy(alpha = 0.16f) else DeepForestBg,
                            border = BorderStroke(
                                1.dp,
                                if (active) EmeraldAccent else CardBorderGreen
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkEmeraldSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = server.code,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) EmeraldAccent else OffWhiteText
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = server.country,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OffWhiteText
                                        )
                                        Text(
                                            text = server.city,
                                            fontSize = 11.sp,
                                            color = MutedSageText
                                        )
                                    }
                                }

                                Text(
                                    text = if (active) "TERPILIH" else "PILIH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) EmeraldAccent else MutedSageText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
