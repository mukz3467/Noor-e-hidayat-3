package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar

data class MoreHubItem(
    val id: String,
    val titleKey: String,
    val icon: ImageVector,
    val category: String,
    val testTag: String
)

@Composable
fun MoreHubScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MoreHubItem("hadith", "hadith", Icons.Default.CollectionsBookmark, "Authentic Collections", "hub_item_hadith"),
        MoreHubItem("tafseer", "tafseer", Icons.Default.LibraryBooks, "Quran Exegesis", "hub_item_tafseer"),
        MoreHubItem("duas", "duas", Icons.Default.VolunteerActivism, "Supplications", "hub_item_duas"),
        MoreHubItem("tasbeeh", "tasbeeh", Icons.Default.Fingerprint, "Daily Dhikr", "hub_item_tasbeeh"),
        MoreHubItem("prayer_times", "prayer_times", Icons.Default.AccessTime, "Daily Schedule", "hub_item_prayer"),
        MoreHubItem("azan", "azan_notifications", Icons.Default.NotificationsActive, "Alerts & Audio", "hub_item_azan"),
        MoreHubItem("qibla", "qibla", Icons.Default.Explore, "Compass Bearing", "hub_item_qibla"),
        MoreHubItem("zakat", "zakat", Icons.Default.Calculate, "Wealth & Nisab", "hub_item_zakat"),
        MoreHubItem("calendar", "calendar", Icons.Default.CalendarMonth, "Hijri & Events", "hub_item_calendar"),
        MoreHubItem("podcasts", "podcasts", Icons.Default.Podcasts, "Scholarly Audio", "hub_item_podcasts"),
        MoreHubItem("bookmarks", "bookmarks", Icons.Default.Bookmark, "Saved Verses & Hadith", "hub_item_bookmarks"),
        MoreHubItem("downloads", "downloads", Icons.Default.DownloadForOffline, "Offline Storage", "hub_item_downloads")
    )

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("more_options", language),
                language = language,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(items) { item ->
                Card(
                    onClick = { onNavigate(item.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .testTag(item.testTag)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = AppLocalization.get(item.titleKey, language),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = AppLocalization.get(item.titleKey, language),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
