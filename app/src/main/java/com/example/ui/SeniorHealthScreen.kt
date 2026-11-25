package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyHealthRecord
import com.example.data.MedicationSlot
import com.example.ui.components.WaterCupIcon
import com.example.ui.theme.DinnerIndigoBg
import com.example.ui.theme.DinnerIndigoInk
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvoryCardBorder
import com.example.ui.theme.IvoryCardSurface
import com.example.ui.theme.IvoryElevatedBanner
import com.example.ui.theme.IvoryGlow
import com.example.ui.theme.LunchCoralBg
import com.example.ui.theme.LunchCoralInk
import com.example.ui.theme.MedDoneBannerBg
import com.example.ui.theme.MedDoneBannerText
import com.example.ui.theme.MedTakenBadgeBg
import com.example.ui.theme.MedTakenGreen
import com.example.ui.theme.MedTakenGreenBorder
import com.example.ui.theme.MorningAmberBg
import com.example.ui.theme.MorningAmberInk
import com.example.ui.theme.WarmSlateInk
import com.example.ui.theme.WaterBlueBorder
import com.example.ui.theme.WaterBlueDark
import com.example.ui.theme.WaterBluePrimary
import com.example.ui.theme.WaterBlueSurface

@Composable
fun SeniorHealthScreen(
  uiState: SeniorHealthUiState,
  onToggleMedication: (MedicationSlot) -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptics = LocalHapticFeedback.current

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = IvoryBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .drawBehind {
            drawRect(color = IvoryBackground)
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(IvoryGlow, IvoryBackground.copy(alpha = 0f)),
                  center = Offset(size.width * 0.5f, size.height * 0.15f),
                  radius = size.maxDimension * 0.7f,
                ),
            )
          }
          .padding(innerPadding)
    ) {
      when (uiState) {
        SeniorHealthUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              modifier = Modifier.size(56.dp),
              color = MedTakenGreen,
              strokeWidth = 5.dp,
            )
          }
        }

        is SeniorHealthUiState.Ready -> {
          BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
          ) {
            val isExpandedTablet = maxWidth >= 840.dp
            val scrollState = rememberScrollState()

            if (isExpandedTablet) {
              Column(
                modifier =
                  Modifier.fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
              ) {
                TodayDateHeader(
                  displayDate = uiState.displayDate,
                  modifier = Modifier.widthIn(max = 1040.dp).fillMaxWidth(),
                )
                Row(
                  modifier = Modifier.widthIn(max = 1040.dp).fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(28.dp),
                  verticalAlignment = Alignment.Top,
                ) {
                  MedicationSection(
                    record = uiState.record,
                    onToggleMedication = { slot ->
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      onToggleMedication(slot)
                    },
                    modifier = Modifier.weight(1f),
                  )
                  WaterTrackingSection(
                    waterCups = uiState.record.waterCups,
                    dailyWaterGoal = uiState.dailyWaterGoal,
                    onAddWaterCup = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      onAddWaterCup()
                    },
                    onRemoveWaterCup = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      onRemoveWaterCup()
                    },
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            } else {
              Column(
                modifier =
                  Modifier.fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp),
              ) {
                TodayDateHeader(
                  displayDate = uiState.displayDate,
                  modifier = Modifier.fillMaxWidth(),
                )

                MedicationSection(
                  record = uiState.record,
                  onToggleMedication = { slot ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleMedication(slot)
                  },
                  modifier = Modifier.fillMaxWidth(),
                )

                WaterTrackingSection(
                  waterCups = uiState.record.waterCups,
                  dailyWaterGoal = uiState.dailyWaterGoal,
                  onAddWaterCup = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddWaterCup()
                  },
                  onRemoveWaterCup = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemoveWaterCup()
                  },
                  modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TodayDateHeader(
  displayDate: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.testTag("today_date_header"),
    shape = RoundedCornerShape(24.dp),
    color = IvoryElevatedBanner,
    border = BorderStroke(1.5.dp, IvoryCardBorder),
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.CalendarToday,
          contentDescription = null,
          tint = WarmSlateInk,
          modifier = Modifier.size(24.dp),
        )
        Text(
          text = stringResource(R.string.today_label),
          style = MaterialTheme.typography.labelMedium,
          color = WarmSlateInk,
          fontSize = 22.sp,
        )
      }
      Text(
        text = displayDate,
        style = MaterialTheme.typography.headlineLarge,
        color = EspressoInk,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("today_date_text"),
      )
    }
  }
}

@Composable
private fun MedicationSection(
  record: DailyHealthRecord,
  onToggleMedication: (MedicationSlot) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Text(
      text = stringResource(R.string.medication_section_title),
      style = MaterialTheme.typography.titleLarge,
      color = EspressoInk,
      fontSize = 25.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 4.dp),
    )

    MedicationButton(
      title = stringResource(R.string.med_morning),
      subtitle = stringResource(R.string.med_morning_time),
      icon = Icons.Filled.WbTwilight,
      accentBg = MorningAmberBg,
      accentInk = MorningAmberInk,
      isTaken = record.morningTaken,
      onClick = { onToggleMedication(MedicationSlot.MORNING) },
      testTag = "med_button_morning",
    )

    MedicationButton(
      title = stringResource(R.string.med_lunch),
      subtitle = stringResource(R.string.med_lunch_time),
      icon = Icons.Filled.WbSunny,
      accentBg = LunchCoralBg,
      accentInk = LunchCoralInk,
      isTaken = record.lunchTaken,
      onClick = { onToggleMedication(MedicationSlot.LUNCH) },
      testTag = "med_button_lunch",
    )

    MedicationButton(
      title = stringResource(R.string.med_dinner),
      subtitle = stringResource(R.string.med_dinner_time),
      icon = Icons.Filled.NightsStay,
      accentBg = DinnerIndigoBg,
      accentInk = DinnerIndigoInk,
      isTaken = record.dinnerTaken,
      onClick = { onToggleMedication(MedicationSlot.DINNER) },
      testTag = "med_button_dinner",
    )

    AnimatedVisibility(
      visible = record.allMedicationsTaken,
      enter =
        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
          scaleIn(
            initialScale = 0.92f,
            animationSpec =
              spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
              ),
          ) +
          expandVertically(),
      exit = fadeOut() + scaleOut(targetScale = 0.95f) + shrinkVertically(),
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth().testTag("all_meds_done_banner"),
        shape = RoundedCornerShape(24.dp),
        color = MedDoneBannerBg,
        border = BorderStroke(2.5.dp, MedTakenGreen),
        shadowElevation = 4.dp,
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier =
              Modifier.size(56.dp)
                .background(color = MedTakenGreen, shape = CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Celebration,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(34.dp),
            )
          }
          Text(
            text = stringResource(R.string.all_meds_done),
            style = MaterialTheme.typography.headlineLarge,
            color = MedDoneBannerText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("all_meds_done_text"),
          )
          Text(
            text = stringResource(R.string.all_meds_done_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MedDoneBannerText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun MedicationButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentBg: Color,
  accentInk: Color,
  isTaken: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) MedTakenGreen else IvoryCardSurface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "medContainerColor",
    )
  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) MedTakenGreenBorder else IvoryCardBorder,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "medBorderColor",
    )
  val titleColor by
    animateColorAsState(
      targetValue = if (isTaken) Color.White else EspressoInk,
      label = "medTitleColor",
    )

  val statusAccessibility =
    if (isTaken) {
      stringResource(R.string.cd_med_taken, title)
    } else {
      stringResource(R.string.cd_med_pending, title)
    }

  Surface(
    onClick = onClick,
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 98.dp)
        .testTag(testTag)
        .semantics { contentDescription = statusAccessibility },
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(width = 2.dp, color = borderColor),
    shadowElevation = if (isTaken) 4.dp else 2.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier =
            Modifier.size(56.dp)
              .background(
                color = if (isTaken) Color.White.copy(alpha = 0.22f) else accentBg,
                shape = CircleShape,
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isTaken) Color.White else accentInk,
            modifier = Modifier.size(32.dp),
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = titleColor,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isTaken) Color.White.copy(alpha = 0.92f) else WarmSlateInk,
            fontSize = 22.sp,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      if (isTaken) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MedTakenBadgeBg,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = MedTakenGreen,
              modifier = Modifier.size(26.dp),
            )
            Text(
              text = stringResource(R.string.status_taken),
              style = MaterialTheme.typography.labelMedium,
              color = MedTakenGreen,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = WarmSlateInk,
            modifier = Modifier.size(26.dp),
          )
          Text(
            text = stringResource(R.string.status_not_taken),
            style = MaterialTheme.typography.labelMedium,
            color = WarmSlateInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun WaterTrackingSection(
  waterCups: Int,
  dailyWaterGoal: Int,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.testTag("water_tracking_card"),
    shape = RoundedCornerShape(28.dp),
    color = IvoryCardSurface,
    border = BorderStroke(2.dp, WaterBlueBorder),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      // Section header + huge numeric cup counter
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier =
              Modifier.size(48.dp)
                .background(color = WaterBlueSurface, shape = CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.LocalDrink,
              contentDescription = null,
              tint = WaterBluePrimary,
              modifier = Modifier.size(28.dp),
            )
          }
          Text(
            text = stringResource(R.string.water_section_title),
            style = MaterialTheme.typography.titleLarge,
            color = EspressoInk,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
          )
        }

        Surface(
          shape = RoundedCornerShape(18.dp),
          color = WaterBlueSurface,
          border = BorderStroke(1.5.dp, WaterBlueBorder),
        ) {
          Text(
            text = stringResource(R.string.water_count_format, waterCups),
            style = MaterialTheme.typography.displayLarge,
            color = WaterBlueDark,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            modifier =
              Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("water_count_text"),
          )
        }
      }

      // 8 large animated water cup icons arranged in 2 rows of 4 for effortless visibility
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        for (rowIndex in 0 until 2) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            for (colIndex in 0 until 4) {
              val cupIndex = rowIndex * 4 + colIndex
              WaterCupIcon(
                cupIndex = cupIndex,
                isFilled = cupIndex < waterCups,
                modifier = Modifier.testTag("water_cup_$cupIndex"),
              )
            }
          }
        }
      }

      // Clear large status sentence (>= 22.sp)
      val goalMessage =
        if (waterCups >= dailyWaterGoal) {
          stringResource(R.string.water_goal_reached)
        } else {
          stringResource(R.string.water_goal_format, dailyWaterGoal, waterCups)
        }
      Text(
        text = goalMessage,
        style = MaterialTheme.typography.bodyLarge,
        color = if (waterCups >= dailyWaterGoal) MedTakenGreen else WarmSlateInk,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )

      // Giant primary "+1 cup" button
      Button(
        onClick = onAddWaterCup,
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 78.dp)
            .testTag("water_add_button"),
        shape = RoundedCornerShape(22.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = WaterBluePrimary,
            contentColor = Color.White,
          ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.LocalDrink,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.water_button_add),
          style = MaterialTheme.typography.labelLarge,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      // Accessible undo (-1) button only shown when count > 0
      AnimatedVisibility(
        visible = waterCups > 0,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        OutlinedButton(
          onClick = onRemoveWaterCup,
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 58.dp)
              .testTag("water_remove_button"),
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, IvoryCardBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmSlateInk),
        ) {
          Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.water_button_undo),
            style = MaterialTheme.typography.labelMedium,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}
