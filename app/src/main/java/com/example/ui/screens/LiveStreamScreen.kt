package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.localization.AppLocalization
import com.example.ui.components.InAppYouTubePlayer
import com.example.ui.components.NoorTopBar

data class StreamServer(val id: String, val name: String)

@Composable
fun LiveStreamScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedChannel by remember { mutableStateOf(0) } // 0 = Makkah, 1 = Madinah
    var serverIndex by remember { mutableIntStateOf(0) }
    var reloadKey by remember { mutableIntStateOf(0) }

    // Public live broadcast video IDs from the Saudi Quran TV and Sunnah TV channels. Stream availability can change.
    val makkahServers = listOf(
        StreamServer("eC4LfEVxvKg", "Saudi Quran TV (Official Makkah broadcast)"),
        StreamServer("vdvjUpl03q0", "Alternative Makkah stream"),
        StreamServer("Qvcimc6QQY8", "Alternative Makkah stream 2")
    )

    val madinahServers = listOf(
        StreamServer("Rs7St51oDDc", "Saudi Sunnah TV (Official Madinah broadcast)"),
        StreamServer("9A1S0xAPVIs", "Alternative Madinah stream"),
        StreamServer("BtMUUgApnPs", "Alternative Madinah stream 2")
    )

    val currentServers = if (selectedChannel == 0) makkahServers else madinahServers
    val safeServerIndex = serverIndex.coerceIn(0, currentServers.size - 1)
    val activeServer = currentServers[safeServerIndex]

    val channelTitle = if (selectedChannel == 0) {
        AppLocalization.get("live_makkah_title", language)
    } else {
        AppLocalization.get("live_madinah_title", language)
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("live_stream", language),
                language = language,
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = { reloadKey++ },
                        modifier = Modifier.testTag("reload_stream_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Stream")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Channel Switcher Tabs
            TabRow(
                selectedTabIndex = selectedChannel,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedChannel == 0,
                    onClick = {
                        selectedChannel = 0
                        serverIndex = 0
                    },
                    text = {
                        Text(
                            text = "Makkah (Haram)",
                            fontWeight = if (selectedChannel == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_live_makkah")
                )
                Tab(
                    selected = selectedChannel == 1,
                    onClick = {
                        selectedChannel = 1
                        serverIndex = 0
                    },
                    text = {
                        Text(
                            text = "Madinah (Nabawi)",
                            fontWeight = if (selectedChannel == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_live_madinah")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated In-App Video Player (Never leaves the app)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("embedded_live_player")
            ) {
                key(activeServer.id, reloadKey) {
                    InAppYouTubePlayer(
                        videoId = activeServer.id,
                        modifier = Modifier.fillMaxSize(),
                        autoplay = true,
                        onFallbackRequested = {
                            serverIndex = (serverIndex + 1) % currentServers.size
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stream Server Switcher Bar
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Broadcast Stream",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeServer.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = {
                                serverIndex = (serverIndex + 1) % currentServers.size
                                reloadKey++
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("switch_stream_server_btn")
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Switch Server", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Server selection chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentServers.forEachIndexed { idx, srv ->
                            val isSel = idx == safeServerIndex
                            Surface(
                                onClick = {
                                    serverIndex = idx
                                    reloadKey++
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Server ${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Status & Information Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = channelTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // Live Status Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                    )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "FEED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppLocalization.get("official_source", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "Saudi Broadcasting Channels",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Embedded public broadcasts from Saudi Quran TV (Makkah) and Saudi Sunnah TV (Madinah). Streams may occasionally be unavailable or restarted by the broadcaster.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
