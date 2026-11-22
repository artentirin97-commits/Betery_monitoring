package com.example.batterymonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BatteryState(
    val percent: Int = 0,
    val isCharging: Boolean = false,
    val voltageMilliVolts: Int = 0,
    val temperatureCelsius: Float = 0f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BatteryScreen()
                }
            }
        }
    }
}

@Composable
fun BatteryScreen() {
    var batteryState by remember { mutableStateOf(BatteryState()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent ?: return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 0

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

                batteryState = BatteryState(percent, charging, voltage, temp)
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${batteryState.percent}%",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (batteryState.isCharging) "Заряжается" else "От батареи",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Напряжение: ${batteryState.voltageMilliVolts} мВ",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "Температура: ${batteryState.temperatureCelsius} °C",
            fontSize = 16.sp
        )
    }
}
