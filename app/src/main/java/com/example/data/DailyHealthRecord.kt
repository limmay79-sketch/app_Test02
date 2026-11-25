package com.example.data

enum class MedicationSlot {
  MORNING,
  LUNCH,
  DINNER,
}

data class DailyHealthRecord(
  val dateKey: String,
  val morningTaken: Boolean = false,
  val lunchTaken: Boolean = false,
  val dinnerTaken: Boolean = false,
  val waterCups: Int = 0,
) {
  val allMedicationsTaken: Boolean
    get() = morningTaken && lunchTaken && dinnerTaken

  val takenCount: Int
    get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

  fun isTaken(slot: MedicationSlot): Boolean =
    when (slot) {
      MedicationSlot.MORNING -> morningTaken
      MedicationSlot.LUNCH -> lunchTaken
      MedicationSlot.DINNER -> dinnerTaken
    }
}
