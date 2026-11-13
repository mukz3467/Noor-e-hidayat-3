package com.example.data.repository

import com.example.data.model.AppLanguage

/**
 * Rights-gated catalog for the requested Urdu tafseer editions.
 *
 * No entry is exposed as selectable until both text redistribution rights and the
 * ayah-indexed corpus have been independently verified. Do not treat a software
 * repository license as permission for the books/texts stored in that repository.
 */
object VerifiedTafseerCatalog {
    enum class VerificationStatus {
        UNVERIFIED,
        AUTHORIZED_AND_COVERAGE_VERIFIED
    }

    data class Source(
        val id: String,
        val scholarName: String,
        val bookTitle: String,
        val originalLanguage: AppLanguage,
        val priority: Int,
        val sourceUrl: String?,
        val licenseEvidenceUrl: String?,
        val licenseNote: String,
        val status: VerificationStatus = VerificationStatus.UNVERIFIED,
        val verifiedAyahRecords: Int? = null,
        val missingAyahRecords: Int? = null,
        val offlineAvailable: Boolean = false
    )

    val requestedSources: List<Source> = listOf(
        Source(
            id = "ibn_kathir_urdu",
            scholarName = "حافظ ابن کثیر",
            bookTitle = "تفسیر القرآن العظیم (اردو)",
            originalLanguage = AppLanguage.URDU,
            priority = 1,
            sourceUrl = "https://github.com/spa5k/tafsir_api/tree/main/tafsir/tafseer-ibn-e-kaseer-urdu",
            licenseEvidenceUrl = "https://github.com/spa5k/tafsir_api/blob/main/LICENSE",
            licenseNote = "UNVERIFIED: upstream MIT license applies to software; text-specific redistribution rights have not been established."
        ),
        Source(
            id = "tafheem_ul_quran",
            scholarName = "مولانا ابوالاعلیٰ مودودی",
            bookTitle = "تفہیم القرآن",
            originalLanguage = AppLanguage.URDU,
            priority = 2,
            sourceUrl = null,
            licenseEvidenceUrl = null,
            licenseNote = "UNVERIFIED: authorized dataset or written redistribution permission required."
        ),
        Source(
            id = "maariful_quran",
            scholarName = "مفتی محمد شفیع",
            bookTitle = "معارف القرآن",
            originalLanguage = AppLanguage.URDU,
            priority = 3,
            sourceUrl = null,
            licenseEvidenceUrl = null,
            licenseNote = "UNVERIFIED: authorized dataset or written redistribution permission required."
        ),
        Source(
            id = "tafseer_usmani",
            scholarName = "مولانا شبیر احمد عثمانی",
            bookTitle = "تفسیر عثمانی",
            originalLanguage = AppLanguage.URDU,
            priority = 4,
            sourceUrl = null,
            licenseEvidenceUrl = null,
            licenseNote = "UNVERIFIED: authorized dataset or written redistribution permission required."
        ),
        Source(
            id = "ahsan_ul_bayan",
            scholarName = "حافظ صلاح الدین یوسف",
            bookTitle = "تفسیر احسن البیان",
            originalLanguage = AppLanguage.URDU,
            priority = 5,
            sourceUrl = null,
            licenseEvidenceUrl = null,
            licenseNote = "UNVERIFIED: authorized dataset or written redistribution permission required."
        )
    )

    /** Only explicitly rights-cleared, coverage-verified entries may be offered in the UI. */
    fun availableSources(): List<Source> = requestedSources.filter {
        it.status == VerificationStatus.AUTHORIZED_AND_COVERAGE_VERIFIED &&
            it.verifiedAyahRecords != null &&
            it.missingAyahRecords != null
    }

    fun source(sourceId: String): Source? = requestedSources.firstOrNull { it.id == sourceId }

    fun availabilityMessage(language: AppLanguage): String = when (language) {
        AppLanguage.URDU -> "اس تفسیر کا مستند اور قانونی طور پر مجاز متن فی الحال تصدیق شدہ نہیں؛ اس لیے متن دستیاب نہیں۔"
        AppLanguage.ARABIC -> "النص الموثق والمصرح بإعادة نشره لهذا التفسير غير متاح حالياً."
        AppLanguage.HINDI -> "इस तफ़सीर का प्रमाणित और पुनर्वितरण-अधिकृत पाठ अभी सत्यापित नहीं है।"
        AppLanguage.ENGLISH -> "Verified Tafseer is not currently available for this ayah. Source text reuse rights are not verified."
    }
}
