package com.example

import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.repository.TasbeehRepository
import com.example.localization.AppLocalization
import org.junit.Assert.*
import org.junit.Test

class ExampleRobolectricTest {

  private class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = HashMap(map)
    override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = (map[key] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = EditorImpl()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    inner class EditorImpl : SharedPreferences.Editor {
      private val temp = mutableMapOf<String, Any?>()
      private var clearFlag = false

      override fun putString(key: String?, value: String?): SharedPreferences.Editor { temp[key ?: ""] = value; return this }
      override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { temp[key ?: ""] = values; return this }
      override fun putInt(key: String?, value: Int): SharedPreferences.Editor { temp[key ?: ""] = value; return this }
      override fun putLong(key: String?, value: Long): SharedPreferences.Editor { temp[key ?: ""] = value; return this }
      override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { temp[key ?: ""] = value; return this }
      override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { temp[key ?: ""] = value; return this }
      override fun remove(key: String?): SharedPreferences.Editor { temp[key ?: ""] = null; return this }
      override fun clear(): SharedPreferences.Editor { clearFlag = true; return this }
      override fun commit(): Boolean { apply(); return true }
      override fun apply() {
        if (clearFlag) {
          map.clear()
          clearFlag = false
        }
        temp.forEach { (k, v) ->
          if (v == null) map.remove(k) else map[k] = v
        }
        temp.clear()
      }
    }
  }

  @Test
  fun testAppLocalizationStrings() {
    val nameEn = AppLocalization.get("app_name", AppLanguage.ENGLISH)
    val nameUr = AppLocalization.get("app_name", AppLanguage.URDU)
    val nameAr = AppLocalization.get("app_name", AppLanguage.ARABIC)
    val nameHi = AppLocalization.get("app_name", AppLanguage.HINDI)

    assertEquals("Noor-e-Hidayat", nameEn)
    assertEquals("نورِ ہدایت", nameUr)
    assertEquals("نور الهداية", nameAr)
    assertEquals("नूर-ए-हिदायत", nameHi)
  }

  @Test
  fun testTasbeehLocalizationKeys() {
    val keys = listOf(
      "hub_item_tasbeeh",
      "tap_to_count",
      "target",
      "target_completed",
      "rounds",
      "undo",
      "reset",
      "sound",
      "vibration",
      "screen_awake",
      "history",
      "statistics",
      "today_total",
      "all_time_total",
      "custom_target",
      "add_custom_dhikr",
      "change_dhikr",
      "save_session"
    )

    val languages = listOf(
      AppLanguage.URDU,
      AppLanguage.ENGLISH,
      AppLanguage.ARABIC,
      AppLanguage.HINDI
    )

    languages.forEach { lang ->
      keys.forEach { key ->
        val text = AppLocalization.get(key, lang)
        assertTrue("Localization key $key should be present in ${lang.displayName}", text.isNotBlank())
      }
    }
  }

  @Test
  fun testTasbeehDhikrPresetsAndDuroodIbrahimIntegrity() {
    val presets = TasbeehRepository.PRESET_DHIKRS
    assertTrue("Should contain at least 7 verified presets", presets.size >= 7)

    // Verify Durood Ibrahim is present with complete verified Arabic
    val durood = presets.firstOrNull { it.id == "durood_ibrahim" }
    assertNotNull("Durood Ibrahim must be included", durood)
    assertTrue("Durood must contain Allahumma salli", durood!!.arabic.contains("اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ") || durood.arabic.contains("اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ"))
    assertTrue("Durood must contain kama sallayta", durood.arabic.contains("كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ") || durood.arabic.contains("كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ"))
    assertTrue("Durood must contain Allahumma barik", durood.arabic.contains("اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ") || durood.arabic.contains("اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ"))
    assertTrue("Durood must contain Hameedum Majeed", durood.arabic.contains("حَمِيدٌ مَجِيدٌ"))

    // Verify SubhanAllah, Alhamdulillah, Allahu Akbar, Astaghfirullah, La ilaha illallah
    val ids = presets.map { it.id }
    assertTrue(ids.contains("subhanallah"))
    assertTrue(ids.contains("alhamdulillah"))
    assertTrue(ids.contains("allahuakbar"))
    assertTrue(ids.contains("astaghfirullah"))
    assertTrue(ids.contains("lailahaillallah"))
  }

  @Test
  fun testTasbeehRepositoryCountingAndPersistence() {
    val fakePrefs = FakeSharedPreferences()
    val repo = TasbeehRepository(fakePrefs)

    // Reset initially
    repo.reset()
    repo.setTarget(33)
    assertEquals(0, repo.currentCount.value)
    assertEquals(33, repo.currentTarget.value)

    // Increment
    val reached1 = repo.increment()
    assertEquals(1, repo.currentCount.value)
    assertFalse(reached1)

    // Tap more
    repeat(4) { repo.increment() }
    assertEquals(5, repo.currentCount.value)

    // Undo
    repo.undo()
    assertEquals(4, repo.currentCount.value)

    // Changing target should NOT reset current count
    repo.setTarget(100)
    assertEquals(4, repo.currentCount.value)
    assertEquals(100, repo.currentTarget.value)

    // Reset sets count to 0
    repo.reset()
    assertEquals(0, repo.currentCount.value)
  }

  @Test
  fun testTasbeehTargetReachedNeverAutoResets() {
    val fakePrefs = FakeSharedPreferences()
    val repo = TasbeehRepository(fakePrefs)

    repo.reset()
    repo.setTarget(5)

    var reached = false
    repeat(5) {
      reached = repo.increment()
    }

    assertTrue("Target reached must trigger on 5th tap", reached)
    assertEquals("Counter must NEVER auto reset when target reached", 5, repo.currentCount.value)

    // User can continue counting past target
    repo.increment()
    assertEquals("Counter can continue counting beyond target", 6, repo.currentCount.value)
  }

  @Test
  fun testTasbeehHistoryAndStatistics() {
    val fakePrefs = FakeSharedPreferences()
    val repo = TasbeehRepository(fakePrefs)

    repo.clearAllHistory()
    assertEquals(0, repo.history.value.size)

    repo.reset()
    repeat(33) { repo.increment() }
    val savedItem = repo.saveHistorySession(isCompleted = true)
    assertEquals(1, repo.history.value.size)

    assertEquals(33, savedItem.count)
    assertEquals(33, savedItem.target)
    assertTrue(savedItem.isTargetCompleted)

    // Test delete single history record
    repo.deleteHistoryItem(savedItem.id)
    assertEquals(0, repo.history.value.size)
  }
}
