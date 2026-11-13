package com.example

import com.example.data.model.AppLanguage
import com.example.data.repository.VerifiedTafseerCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifiedTafseerCatalogTest {
    @Test
    fun requestedScholarsAreInPriorityOrderAndNotFalselyMarkedAvailable() {
        val sources = VerifiedTafseerCatalog.requestedSources
        assertEquals(5, sources.size)
        assertEquals(listOf(1, 2, 3, 4, 5), sources.map { it.priority })
        assertEquals(
            listOf("ibn_kathir_urdu", "tafheem_ul_quran", "maariful_quran", "tafseer_usmani", "ahsan_ul_bayan"),
            sources.map { it.id }
        )
        assertTrue(sources.all { it.verifiedAyahRecords == null && it.missingAyahRecords == null })
        assertTrue(sources.all { it.status == VerifiedTafseerCatalog.VerificationStatus.UNVERIFIED })
        assertTrue(sources.none { it.offlineAvailable })
        assertTrue(VerifiedTafseerCatalog.availableSources().isEmpty())
    }

    @Test
    fun unknownSourceIsNotResolvedToAnotherScholarsText() {
        assertNull(VerifiedTafseerCatalog.source("not-a-real-scholar"))
    }

    @Test
    fun availabilityMessageIsLocalizedForAllSupportedLanguages() {
        listOf(
            AppLanguage.URDU,
            AppLanguage.ENGLISH,
            AppLanguage.HINDI,
            AppLanguage.ARABIC
        ).forEach { language ->
            assertFalse(VerifiedTafseerCatalog.availabilityMessage(language).isBlank())
        }
        assertTrue(VerifiedTafseerCatalog.availabilityMessage(AppLanguage.ENGLISH)
            .contains("not currently available"))
    }
}
