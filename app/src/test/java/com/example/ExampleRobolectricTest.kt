package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.DailyHealthRecord
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.MedicationSlot
import com.example.ui.SeniorHealthScreen
import com.example.ui.SeniorHealthUiState
import com.example.ui.SeniorHealthViewModel
import com.example.ui.theme.SeniorHealthTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("약·물 체크", appName)
  }

  @Test
  fun `datastore repository persists today record and resets on new date`() = runTest {
    val dataStoreFile = File(tempFolder.root, "senior_health_test.preferences_pb")
    val dataStore = PreferenceDataStoreFactory.create(produceFile = { dataStoreFile })
    val repository = DataStoreDailyHealthRepository(dataStore)

    val today = "2026-11-17"
    repository.toggleMedication(MedicationSlot.MORNING, today)
    repository.addWaterCup(today)
    repository.addWaterCup(today)

    val savedToday = repository.observeRecord { today }.first()
    assertTrue(savedToday.morningTaken)
    assertFalse(savedToday.lunchTaken)
    assertEquals(2, savedToday.waterCups)

    val tomorrow = "2026-11-18"
    repository.resetIfNewDay(tomorrow)
    val nextDayRecord = repository.observeRecord { tomorrow }.first()
    assertFalse(nextDayRecord.morningTaken)
    assertEquals(0, nextDayRecord.waterCups)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `viewmodel automatically resets medication and water checks when midnight passes`() =
    runTest {
      val testDispatcher = StandardTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      try {
        val dataStoreFile = File(tempFolder.root, "midnight_reset_test.preferences_pb")
        val dataStore =
          PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { dataStoreFile },
          )
        val repository = DataStoreDailyHealthRepository(dataStore)

        var currentKey = "2026-11-17"
        var currentDisplay = "2026년 11월 17일 화요일"
        val viewModel =
          SeniorHealthViewModel(
            repository = repository,
            dateKeyProvider = { currentKey },
            displayDateProvider = { currentDisplay },
            millisUntilMidnightProvider = { 5_000L },
          )

        val collectorJob = launch { viewModel.uiState.collect {} }
        viewModel.uiState.filterIsInstance<SeniorHealthUiState.Ready>().first()

        viewModel.toggleMedication(MedicationSlot.MORNING)
        viewModel.toggleMedication(MedicationSlot.DINNER)
        viewModel.addWaterCup()
        viewModel.addWaterCup()
        viewModel.addWaterCup()

        val beforeMidnight =
          viewModel.uiState
            .filterIsInstance<SeniorHealthUiState.Ready>()
            .first {
              it.record.morningTaken && it.record.dinnerTaken && it.record.waterCups == 3
            }
        assertTrue(beforeMidnight.record.morningTaken)
        assertTrue(beforeMidnight.record.dinnerTaken)
        assertEquals(3, beforeMidnight.record.waterCups)

        // Simulate clock crossing midnight while the app remains open
        currentKey = "2026-11-18"
        currentDisplay = "2026년 11월 18일 수요일"
        advanceTimeBy(5_001L)
        runCurrent()

        val afterMidnight =
          viewModel.uiState
            .filterIsInstance<SeniorHealthUiState.Ready>()
            .first { it.record.dateKey == "2026-11-18" }
        assertEquals("2026년 11월 18일 수요일", afterMidnight.displayDate)
        assertFalse(afterMidnight.record.morningTaken)
        assertFalse(afterMidnight.record.lunchTaken)
        assertFalse(afterMidnight.record.dinnerTaken)
        assertEquals(0, afterMidnight.record.waterCups)

        collectorJob.cancel()
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `tapping medication and water buttons updates screen state`() {
    var record by mutableStateOf(DailyHealthRecord(dateKey = "2026-11-17"))

    composeTestRule.setContent {
      SeniorHealthTheme {
        SeniorHealthScreen(
          uiState =
            SeniorHealthUiState.Ready(
              displayDate = "2026년 11월 17일 화요일",
              record = record,
            ),
          onToggleMedication = { slot ->
            record =
              when (slot) {
                MedicationSlot.MORNING -> record.copy(morningTaken = !record.morningTaken)
                MedicationSlot.LUNCH -> record.copy(lunchTaken = !record.lunchTaken)
                MedicationSlot.DINNER -> record.copy(dinnerTaken = !record.dinnerTaken)
              }
          },
          onAddWaterCup = { record = record.copy(waterCups = record.waterCups + 1) },
          onRemoveWaterCup = {
            record = record.copy(waterCups = (record.waterCups - 1).coerceAtLeast(0))
          },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_date_text").assertTextContains("2026년 11월 17일 화요일")

    composeTestRule.onNodeWithTag("med_button_morning").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("복용 완료").assertIsDisplayed()
    composeTestRule.onNodeWithTag("all_meds_done_banner").assertDoesNotExist()

    composeTestRule.onNodeWithTag("med_button_lunch").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("all_meds_done_banner").assertDoesNotExist()

    composeTestRule.onNodeWithTag("med_button_dinner").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("all_meds_done_banner").performScrollTo()
    composeTestRule.onNodeWithTag("all_meds_done_text").assertTextContains("오늘도 건강 챙기기 성공!")
    composeTestRule.onNodeWithText("오늘도 건강 챙기기 성공!").assertIsDisplayed()

    composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("water_count_text").assertTextContains("1잔")

    composeTestRule.onNodeWithTag("water_add_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("water_count_text").assertTextContains("2잔")
  }
}
