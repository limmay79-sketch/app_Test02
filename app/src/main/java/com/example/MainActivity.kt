package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DataStoreDailyHealthRepository
import com.example.ui.SeniorHealthScreen
import com.example.ui.SeniorHealthViewModel
import com.example.ui.theme.SeniorHealthTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SeniorHealthTheme {
        val context = LocalContext.current
        val viewModel: SeniorHealthViewModel =
          viewModel(
            factory = SeniorHealthViewModel.Factory(DataStoreDailyHealthRepository(context))
          )
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, context) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
              viewModel.refreshDateIfNeeded()
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)

          val dateChangeReceiver =
            object : BroadcastReceiver() {
              override fun onReceive(receiverContext: Context?, intent: Intent?) {
                viewModel.refreshDateIfNeeded()
              }
            }
          val dateIntentFilter =
            IntentFilter().apply {
              addAction(Intent.ACTION_DATE_CHANGED)
              addAction(Intent.ACTION_TIME_CHANGED)
              addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
          ContextCompat.registerReceiver(
            context,
            dateChangeReceiver,
            dateIntentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
          )

          onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.unregisterReceiver(dateChangeReceiver)
          }
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SeniorHealthScreen(
          uiState = uiState,
          onToggleMedication = viewModel::toggleMedication,
          onAddWaterCup = viewModel::addWaterCup,
          onRemoveWaterCup = viewModel::removeWaterCup,
        )
      }
    }
  }
}
