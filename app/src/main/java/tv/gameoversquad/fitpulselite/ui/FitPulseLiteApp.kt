package tv.gameoversquad.fitpulselite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fmt = SimpleDateFormat("d MMM • HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitPulseLiteApp() {
    val context = LocalContext.current

    var goal by remember { mutableStateOf(loadGoalMinutes(context)) }
    var logs by remember { mutableStateOf(loadLogs(context)) }

    // Timer
    var running by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            seconds += 1
        }
    }

    fun saveGoal() = saveGoalMinutes(context, goal)
    fun addLog(title: String, minutes: Int) {
        val updated = listOf(LogItem(title.trim(), minutes, System.currentTimeMillis())) + logs
        logs = updated
        saveLogs(context, updated)
    }
    fun deleteLog(ts: Long) {
        val updated = logs.filterNot { it.timestampMs == ts }
        logs = updated
        saveLogs(context, updated)
    }

    val todayMs = remember {
        val now = System.currentTimeMillis()
        // crude "today" start
        now - (now % (24L*60L*60L*1000L))
    }
    val minutesToday = logs.filter { it.timestampMs >= todayMs }.sumOf { it.minutes }
    val goalProgress = if (goal <= 0) 0f else (minutesToday.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = { TopAppBar(title = { Text("FitPulse Lite") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Daily goal: $goal min")
                    Slider(
                        value = goal.toFloat(),
                        onValueChange = { goal = it.toInt().coerceIn(5, 180) },
                        valueRange = 5f..180f
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = { saveGoal() }) {
                            Text("Save goal")
                        }
                        Button(modifier = Modifier.weight(1f), onClick = { goal = 30; saveGoal() }) {
                            Text("Reset")
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Today: $minutesToday min  •  ${"%.0f".format(goalProgress*100)}% of goal")
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Timer: ${"%02d:%02d".format(seconds/60, seconds%60)}")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = { running = true }) { Text("Start") }
                        Button(modifier = Modifier.weight(1f), onClick = { running = false }) { Text("Pause") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = { seconds = 0; running = false }) { Text("Reset") }
                        Button(modifier = Modifier.weight(1f), onClick = {
                            val mins = (seconds / 60).coerceAtLeast(1)
                            addLog("Workout", mins)
                            seconds = 0
                            running = false
                        }) { Text("Save as log") }
                    }
                }
            }

            var title by remember { mutableStateOf("Workout") }
            var minutes by remember { mutableStateOf("20") }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quick log")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(40) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                        label = { Text("Minutes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val m = minutes.toIntOrNull() ?: return@Button
                        addLog(title, m.coerceAtLeast(1))
                    }) { Text("Add") }
                }
            }

            Text("History")

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.timestampMs }) { e ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(e.title)
                            Text("${e.minutes} min • ${fmt.format(Date(e.timestampMs))}")
                            Button(onClick = { deleteLog(e.timestampMs) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
