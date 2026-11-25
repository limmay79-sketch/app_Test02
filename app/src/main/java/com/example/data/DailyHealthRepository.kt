package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dailyHealthDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "daily_health_prefs")

interface DailyHealthRepository {
  fun observeRecord(todayKeyProvider: () -> String): Flow<DailyHealthRecord>

  suspend fun resetIfNewDay(todayKey: String)

  suspend fun toggleMedication(slot: MedicationSlot, todayKey: String)

  suspend fun addWaterCup(todayKey: String)

  suspend fun removeWaterCup(todayKey: String)
}

class DataStoreDailyHealthRepository(
  private val dataStore: DataStore<Preferences>,
) : DailyHealthRepository {

  constructor(context: Context) : this(context.applicationContext.dailyHealthDataStore)

  override fun observeRecord(todayKeyProvider: () -> String): Flow<DailyHealthRecord> {
    return dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        val todayKey = todayKeyProvider()
        val savedDate = prefs[KEY_RECORD_DATE]
        if (savedDate == todayKey) {
          DailyHealthRecord(
            dateKey = todayKey,
            morningTaken = prefs[KEY_MORNING_TAKEN] ?: false,
            lunchTaken = prefs[KEY_LUNCH_TAKEN] ?: false,
            dinnerTaken = prefs[KEY_DINNER_TAKEN] ?: false,
            waterCups = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0),
          )
        } else {
          DailyHealthRecord(dateKey = todayKey)
        }
      }
  }

  override suspend fun resetIfNewDay(todayKey: String) {
    dataStore.edit { prefs -> ensureTodayDate(prefs, todayKey) }
  }

  override suspend fun toggleMedication(slot: MedicationSlot, todayKey: String) {
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, todayKey)
      val key =
        when (slot) {
          MedicationSlot.MORNING -> KEY_MORNING_TAKEN
          MedicationSlot.LUNCH -> KEY_LUNCH_TAKEN
          MedicationSlot.DINNER -> KEY_DINNER_TAKEN
        }
      val current = prefs[key] ?: false
      prefs[key] = !current
    }
  }

  override suspend fun addWaterCup(todayKey: String) {
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, todayKey)
      val current = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0)
      prefs[KEY_WATER_CUPS] = current + 1
    }
  }

  override suspend fun removeWaterCup(todayKey: String) {
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, todayKey)
      val current = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0)
      prefs[KEY_WATER_CUPS] = (current - 1).coerceAtLeast(0)
    }
  }

  private fun ensureTodayDate(prefs: MutablePreferences, todayKey: String) {
    if (prefs[KEY_RECORD_DATE] != todayKey) {
      prefs[KEY_RECORD_DATE] = todayKey
      prefs[KEY_MORNING_TAKEN] = false
      prefs[KEY_LUNCH_TAKEN] = false
      prefs[KEY_DINNER_TAKEN] = false
      prefs[KEY_WATER_CUPS] = 0
    }
  }

  companion object {
    private val KEY_RECORD_DATE = stringPreferencesKey("record_date")
    private val KEY_MORNING_TAKEN = booleanPreferencesKey("morning_taken")
    private val KEY_LUNCH_TAKEN = booleanPreferencesKey("lunch_taken")
    private val KEY_DINNER_TAKEN = booleanPreferencesKey("dinner_taken")
    private val KEY_WATER_CUPS = intPreferencesKey("water_cups")

    fun currentDateKey(date: Date = Date()): String =
      SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    fun formatKoreanDisplayDate(date: Date = Date()): String =
      SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREA).format(date)

    fun millisUntilNextMidnight(nowMillis: Long = System.currentTimeMillis()): Long {
      val nextMidnight =
        Calendar.getInstance().apply {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
      return (nextMidnight.timeInMillis - nowMillis + 150L).coerceAtLeast(500L)
    }
  }
}
