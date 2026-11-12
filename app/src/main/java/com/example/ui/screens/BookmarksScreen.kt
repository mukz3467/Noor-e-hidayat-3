package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.BookmarkItem
import com.example.data.model.BookmarkType
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar

@Composable
fun BookmarksScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onNavigateToSurah: (Int) -> Unit
) {
    val context = LocalContext.current
    var bookmarks by remember {
        mutableStateOf(
            listOf(
                BookmarkItem("bm_1", BookmarkType.QURAN, "Surah Al-Fatihah, Ayah 1", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", surahNumber = 1, ayahNumber = 1),
                BookmarkItem("bm_2", BookmarkType.HADITH, "Sahih al-Bukhari, Hadith 1", "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", hadithId = "bukhari_1"),
                BookmarkItem("bm_3", BookmarkType.DUA, "Sayyid al-Istighfar", "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ")
            )
        )
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("bookmarks", language),
                language = language,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        if (bookmarks.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = "No saved bookmarks yet.\nTap the bookmark icon on any Ayah or Hadith to save it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(bookmarks, key = { it.id }) { bm ->
                    Card(
                        onClick = {
                            if (bm.surahNumber != null) {
                                onNavigateToSurah(bm.surahNumber)
                            } else {
                                Toast.makeText(context, "Opening ${bm.title}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bookmark_${bm.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bm.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = bm.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = {
                                    bookmarks = bookmarks.filter { it.id != bm.id }
                                    Toast.makeText(context, "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
