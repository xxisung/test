package com.example.google

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.google.ui.theme.GoogleTheme
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectClient: HealthConnectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HealthConnectClient 초기화
        healthConnectClient = HealthConnectClient.getOrCreate(this)

        // 권한 요청
        requestPermissions()

        // UI 설정
        setContent {
            GoogleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    private fun requestPermissions() {
        // 필요한 권한 설정
        val permissions = setOf(
            HealthPermission.getReadPermission(SleepRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsGranted ->
            if (permissionsGranted.all { it.value }) {
                // 권한이 승인되었을 때의 처리
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                readSleepData() // 수면 데이터 읽기
            } else {
                // 권한이 거부되었을 때의 처리
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        // 권한 요청
        requestPermissionLauncher.launch(permissions.map { it.toString() }.toTypedArray())
    }

    private fun readSleepData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 최근 7일간의 수면 데이터 읽기
                val sleepData = healthConnectClient.readRecords(
                    ReadRecordsRequest(SleepRecord::class).setTimeRangeFilter(
                        TimeRangeFilter.between(
                            LocalDate.now().minusDays(7).atStartOfDay(),
                            LocalDate.now().atStartOfDay()
                        )
                    )
                )

                // 가져온 데이터 처리
                if (sleepData.records.isNotEmpty()) {
                    // 첫 번째 수면 기록의 시간 출력
                    val firstSleepRecord = sleepData.records.first()
                    println("First sleep record: ${firstSleepRecord.startTime} to ${firstSleepRecord.endTime}")
                } else {
                    println("No sleep records found.")
                }
            } catch (e: Exception) {
                // 예외 처리
                Toast.makeText(this@MainActivity, "Failed to read sleep data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoogleTheme {
        Greeting("Android")
    }
}
