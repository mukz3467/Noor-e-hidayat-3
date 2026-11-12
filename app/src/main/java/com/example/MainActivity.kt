package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.AppTheme
import com.example.data.repository.QuranAudioManager
import com.example.data.repository.QuranData
import com.example.data.repository.QuranOfflineSyncManager
import com.example.data.repository.QuranTafseerRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.AppDirectionProvider
import com.example.ui.screens.*
import com.example.ui.theme.NooreHidayatTheme

sealed class Screen {
    object Home : Screen()
    object QuranList : Screen()
    data class QuranReader(val surahNumber: Int) : Screen()
    object LiveStream : Screen()
    data class Lectures(val initialQuery: String? = null) : Screen()
    object Learning : Screen()
    object MoreHub : Screen()
    object Hadith : Screen()
    object PrayerTimes : Screen()
    object Qibla : Screen()
    object Zakat : Screen()
    object Duas : Screen()
    object Tasbeeh : Screen()
    object Calendar : Screen()
    object Podcasts : Screen()
    object Downloads : Screen()
    object Bookmarks : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepo = UserPreferencesRepository(applicationContext)
        QuranData.init(applicationContext)
        QuranAudioManager.init(applicationContext)
        QuranOfflineSyncManager.startAutoSync(applicationContext)
        QuranTafseerRepository.init(applicationContext)
        enableEdgeToEdge()

        setContent {
            val language by preferencesRepo.language.collectAsStateWithLifecycle()
            val theme by preferencesRepo.theme.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            // Custom navigation backstack handling
            if (currentScreen !is Screen.Home) {
                BackHandler {
                    currentScreen = when (currentScreen) {
                        is Screen.QuranReader -> Screen.QuranList
                        else -> Screen.Home
                    }
                }
            }

            NooreHidayatTheme(selectedTheme = theme) {
                AppDirectionProvider(language = language) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    language = language,
                                    onNavigateToQuran = { currentScreen = Screen.QuranList },
                                    onNavigateToLive = { currentScreen = Screen.LiveStream },
                                    onNavigateToLectures = { query -> currentScreen = Screen.Lectures(query) },
                                    onNavigateToLearning = { currentScreen = Screen.Learning },
                                    onNavigateToMoreHub = { currentScreen = Screen.MoreHub },
                                    onNavigateToPrayerTimes = { currentScreen = Screen.PrayerTimes },
                                    onNavigateToSettings = { currentScreen = Screen.Settings },
                                    onLanguageChanged = { newLang -> preferencesRepo.setLanguage(newLang) }
                                )
                            }
                            is Screen.QuranList -> {
                                QuranListScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home },
                                    onSelectSurah = { surahNumber ->
                                        currentScreen = Screen.QuranReader(surahNumber)
                                    }
                                )
                            }
                            is Screen.QuranReader -> {
                                QuranReaderScreen(
                                    surahNumber = screen.surahNumber,
                                    language = language,
                                    onBack = { currentScreen = Screen.QuranList },
                                    onNavigateToSurah = { newSurah ->
                                        currentScreen = Screen.QuranReader(newSurah)
                                    }
                                )
                            }
                            is Screen.LiveStream -> {
                                LiveStreamScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Lectures -> {
                                LecturesScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home },
                                    initialQuery = screen.initialQuery
                                )
                            }
                            is Screen.Learning -> {
                                LearningScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.MoreHub -> {
                                MoreHubScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home },
                                    onNavigate = { destinationId ->
                                        currentScreen = when (destinationId) {
                                            "hadith" -> Screen.Hadith
                                            "tafseer" -> Screen.QuranList
                                            "duas" -> Screen.Duas
                                            "tasbeeh" -> Screen.Tasbeeh
                                            "prayer_times" -> Screen.PrayerTimes
                                            "azan" -> Screen.PrayerTimes
                                            "qibla" -> Screen.Qibla
                                            "zakat" -> Screen.Zakat
                                            "calendar" -> Screen.Calendar
                                            "podcasts" -> Screen.Podcasts
                                            "bookmarks" -> Screen.Bookmarks
                                            "downloads" -> Screen.Downloads
                                            "settings" -> Screen.Settings
                                            else -> Screen.Home
                                        }
                                    }
                                )
                            }
                            is Screen.Hadith -> {
                                HadithScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.PrayerTimes -> {
                                PrayerTimesScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Qibla -> {
                                QiblaScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Zakat -> {
                                ZakatScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Duas -> {
                                DuasScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Tasbeeh -> {
                                TasbeehScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Calendar -> {
                                HijriCalendarScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Podcasts -> {
                                PodcastsScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Downloads -> {
                                DownloadsScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Bookmarks -> {
                                BookmarksScreen(
                                    language = language,
                                    onBack = { currentScreen = Screen.Home },
                                    onNavigateToSurah = { surahNumber ->
                                        currentScreen = Screen.QuranReader(surahNumber)
                                    }
                                )
                            }
                            is Screen.Settings -> {
                                SettingsScreen(
                                    currentLanguage = language,
                                    currentTheme = theme,
                                    onLanguageSelected = { newLang ->
                                        preferencesRepo.setLanguage(newLang)
                                    },
                                    onThemeSelected = { newTheme ->
                                        preferencesRepo.setTheme(newTheme)
                                    },
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
