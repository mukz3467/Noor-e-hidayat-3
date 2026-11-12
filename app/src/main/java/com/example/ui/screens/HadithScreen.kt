package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Hadith
import com.example.data.repository.HadithData
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar

@Composable
fun HadithScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCollection by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredHadiths = remember(selectedCollection, searchQuery) {
        HadithData.hadiths.filter { hadith ->
            val matchCol = selectedCollection == "All" || hadith.collection == selectedCollection
            val matchQuery = searchQuery.isBlank() ||
                    hadith.textEnglish.contains(searchQuery, ignoreCase = true) ||
                    hadith.textUrdu.contains(searchQuery) ||
                    hadith.textArabic.contains(searchQuery) ||
                    hadith.bookName.contains(searchQuery, ignoreCase = true) ||
                    hadith.hadithNumber.toString().contains(searchQuery)
            matchCol && matchQuery
        }
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("hadith", language),
                language = language,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Hadith text, narrator or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("hadith_search_input")
            )

            // Collection Switcher Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                FilterChip(
                    selected = selectedCollection == "All",
                    onClick = { selectedCollection = "All" },
                    label = { Text("All Collections") }
                )
                FilterChip(
                    selected = selectedCollection == "Sahih al-Bukhari",
                    onClick = { selectedCollection = "Sahih al-Bukhari" },
                    label = { Text(AppLocalization.get("bukhari", language)) }
                )
                FilterChip(
                    selected = selectedCollection == "Sahih Muslim",
                    onClick = { selectedCollection = "Sahih Muslim" },
                    label = { Text(AppLocalization.get("muslim", language)) }
                )
            }

            // Hadith List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredHadiths, key = { it.id }) { hadith ->
                    HadithCard(hadith = hadith, language = language)
                }
            }
        }
    }
}

@Composable
fun HadithCard(hadith: Hadith, language: AppLanguage) {
    val context = LocalContext.current
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_card_${hadith.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Collection, Book & Hadith Number
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${hadith.collection} #${hadith.hadithNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = hadith.bookName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = hadith.grade,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Arabic Hadith Text
            Text(
                text = hadith.textArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Translation based on selected language
            val trans = when (language) {
                AppLanguage.URDU -> hadith.textUrdu
                AppLanguage.HINDI -> hadith.textHindi
                else -> hadith.textEnglish
            }
            Text(
                text = trans,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Narrator: ${hadith.narrator}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Attribution: ${hadith.sourceAttribution}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Action toolbar: Bookmark, Copy, Share
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        isBookmarked = !isBookmarked
                        Toast.makeText(context, if (isBookmarked) "Saved to bookmarks" else "Bookmark removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hadith", "${hadith.textArabic}\n\n$trans\n(${hadith.collection} #${hadith.hadithNumber})")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }

                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${hadith.textArabic}\n\n$trans\n\n(${hadith.collection} #${hadith.hadithNumber} - Noor-e-Hidayat)")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Hadith"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}
