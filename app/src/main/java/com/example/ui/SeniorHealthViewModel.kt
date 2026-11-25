package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRecord
import com.example.data.DailyHealthRepository
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.MedicationSlot
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface SeniorHealthUiState {
  data object Loading : SeniorHealthUiState

  data class Ready(
    val displayDate: String,
    val record: DailyHealthRecord,
    val dailyWaterGoal: Int = DEFAULT_WATER_GOAL,
  ) : SeniorHealthUiState

  companion object {
    const val DEFAULT_WATER_GOAL = 8
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SeniorHealthViewModel(
  private val repository: DailyHealthRepository,
  private val dateKeyProvider: () -> String = { DataStoreDailyHealthRepository.currentDateKey() },
  private val displayDateProvider: () -> String = {
    DataStoreDailyHealthRepository.formatKoreanDisplayDate()
  },
  private val millisUntilMidnightProvider: () -> Long = {
    DataStoreDailyHealthRepository.millisUntilNextMidnight()
  },
) : ViewModel() {

  private val manualRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  private val activeDateKeyFlow: Flow<String> =
    merge(
        flow {
          while (currentCoroutineContext().isActive) {
            emit(dateKeyProvider())
            val waitMillis = millisUntilMidnightProvider().coerceAtLeast(500L)
            delay(waitMillis)
          }
        },
        manualRefreshTrigger.map { dateKeyProvider() },
      )
      .distinctUntilChanged()

  val uiState: StateFlow<SeniorHealthUiState> =
    activeDateKeyFlow
      .onEach { dateKey -> repository.resetIfNewDay(dateKey) }
      .flatMapLatest { dateKey ->
        repository.observeRecord { dateKey }.map { record ->
          SeniorHealthUiState.Ready(
            displayDate = displayDateProvider(),
            record = record,
          )
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeniorHealthUiState.Loading,
      )

  fun refreshDateIfNeeded() {
    manualRefreshTrigger.tryEmit(Unit)
  }

  fun toggleMedication(slot: MedicationSlot) {
    val todayKey = dateKeyProvider()
    manualRefreshTrigger.tryEmit(Unit)
    viewModelScope.launch { repository.toggleMedication(slot, todayKey) }
  }

  fun addWaterCup() {
    val todayKey = dateKeyProvider()
    manualRefreshTrigger.tryEmit(Unit)
    viewModelScope.launch { repository.addWaterCup(todayKey) }
  }

  fun removeWaterCup() {
    val todayKey = dateKeyProvider()
    manualRefreshTrigger.tryEmit(Unit)
    viewModelScope.launch { repository.removeWaterCup(todayKey) }
  }

  class Factory(
    private val repository: DailyHealthRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(SeniorHealthViewModel::class.java)) {
        return SeniorHealthViewModel(repository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }

  companion object {
    fun formatDateFor(date: Date): String =
      DataStoreDailyHealthRepository.formatKoreanDisplayDate(date)
  }
}
