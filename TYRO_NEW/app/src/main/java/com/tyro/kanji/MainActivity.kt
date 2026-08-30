package com.tyro.kanji

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.tyro.kanji.data.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val Pink = Color(0xFFE56B88)
private val Ink = Color(0xFF17151B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TyroApp() }
    }
}

@Composable
fun TyroApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tyro", 0) }
    var theme by remember { mutableStateOf(prefs.getString("theme", "light") ?: "light") }
    var level by remember { mutableStateOf(prefs.getString("level", "ALL") ?: "ALL") }
    var tab by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) }
    var index by remember { mutableIntStateOf(0) }
    var reveal by remember { mutableStateOf(false) }
    var mysteryFlip by remember { mutableStateOf(false) }
    var quizAnswered by remember { mutableStateOf(false) }
    var quizCorrect by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) Reminder.schedule(context) }
    val dark = theme == "dark" || theme == "fireworks"
    val pool = remember(level) { KanjiCatalog.items.filter { val itemLevel = KanjiLevels.levelFor(it.kanji); level == "ALL" || itemLevel == level } }

    fun instantRefresh() {
        if (pool.isNotEmpty()) index = if (pool.size == 1) 0 else Random.nextInt(pool.size)
        reveal = false; mysteryFlip = false; quizAnswered = false; quizCorrect = false
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Pink, background = Color(0xFF101017), surface = Color(0xFF1A1920)) else lightColorScheme(primary = Pink, background = Color(0xFFFFF7F5))) {
        Box(Modifier.fillMaxSize()) {
            Live(theme, Modifier.matchParentSize())
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("TYRO KANJI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink)
                    Spacer(Modifier.weight(1f)); Text("${pool.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(::instantRefresh) { Icon(Icons.Default.Refresh, "Instant refresh") }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        0 -> Home(pool, index, ::instantRefresh, dark)
                        1 -> Learn(pool, prefs)
                        2 -> Review(pool, index, mode, reveal, mysteryFlip, quizAnswered, quizCorrect, dark, { mode = it; reveal = false; mysteryFlip = false; quizAnswered = false }, ::instantRefresh, { reveal = !reveal }, { mysteryFlip = !mysteryFlip }, { quizAnswered = true; quizCorrect = it })
                        3 -> Settings(theme, level, { theme = it; prefs.edit().putString("theme", it).apply() }, { level = it; prefs.edit().putString("level", it).apply(); index = 0 }, { if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else Reminder.schedule(context) }, { Reminder.test(context) })
                    }
                }
                NavigationBar { val labels = listOf("Home", "Learn", "Review", "Settings"); val icons = listOf(Icons.Default.Home, Icons.Default.MenuBook, Icons.Default.Replay, Icons.Default.Settings); labels.forEachIndexed { i, label -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i], label) }, label = { Text(label, fontSize = 10.sp) }) } }
            }
        }
    }
}

@Composable
fun Live(theme: String, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "live")
    val progress by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart), label = "progress")
    Canvas(modifier) {
        when (theme) {
            "winter" -> { drawRect(Brush.verticalGradient(listOf(Color(0xFF9ED7F0), Color(0xFFEAF7FF)))); val mountain = Path().apply { moveTo(0f, size.height * .65f); lineTo(size.width * .5f, size.height * .2f); lineTo(size.width, size.height * .65f); close() }; drawPath(mountain, Color(0xFF7188A0)); for (i in 0..70) { val x = ((i * 73) % 100) / 100f * size.width; val y = (((i * 41) % 100) / 100f + progress * .4f) % 1f * size.height; drawCircle(Color.White, 1.5f + (i % 3), Offset(x, y)) } }
            "fireworks" -> { drawRect(Brush.verticalGradient(listOf(Color(0xFF080B25), Color(0xFF2B2045)))); for (i in 0..7) { val q = (progress + i * .13f) % 1f; if (q > .3f) { val x = ((i * 29) % 90) / 100f * size.width; val y = size.height * (.15f + ((i * 17) % 30) / 100f); val radius = size.minDimension * .12f * q; for (j in 0..11) { val angle = (j * PI / 6.0).toFloat(); val dx = (cos(angle.toDouble()) * radius).toFloat(); val dy = (sin(angle.toDouble()) * radius).toFloat(); drawLine(Color(0xFFFFD166).copy(alpha = 1f - q), Offset(x, y), Offset(x + dx, y + dy), 2f) } } }; drawCircle(Color(0xFFFFC56E), size.minDimension * .10f, Offset(size.width * .75f, size.height * .7f), style = Stroke(width = 3f)) }
            "dark" -> { drawRect(Brush.verticalGradient(listOf(Color(0xFF080A19), Color(0xFF27203A)))); for (i in 0..50) { val x = ((i * 47) % 100) / 100f * size.width; val y = ((i * 71) % 100) / 100f * size.height; val alpha = .25f + .6f * (sin(progress * 2f * PI.toFloat() + i) * .5f + .5f); drawCircle(Color(0xFFFFD98A).copy(alpha), 1.5f + (i % 3), Offset(x, y)) } }
            else -> { drawRect(Brush.verticalGradient(listOf(Color(0xFFFFEAF3), Color(0xFFFFD7CC)))); for (i in 0..60) { val x = ((i * 37) % 100) / 100f * size.width; val y = (((i * 61) % 100) / 100f + progress * .35f) % 1f * size.height; drawCircle(Color(0xFFE889A4).copy(.7f), 2f + (i % 3), Offset(x, y)) } }
        }
    }
}

@Composable fun Home(pool: List<KanjiSeed>, index: Int, refresh: () -> Unit, dark: Boolean) { val k = pool.getOrNull(index); Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) { Text("Learn Kanji. Live Japan.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink); Spacer(Modifier.height(10.dp)); Card(Modifier.fillMaxWidth().height(260.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(k?.kanji ?: "一", fontSize = 92.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink); Text(k?.meaning ?: "one", fontSize = 18.sp, color = if (dark) Color.White else Ink) } }; Spacer(Modifier.height(12.dp)); Button(refresh, Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Instant New Kanji") }; Spacer(Modifier.height(10.dp)); Text("${pool.size} Kanji in selected level", color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable fun Learn(pool: List<KanjiSeed>, prefs: android.content.SharedPreferences) { Column(Modifier.fillMaxSize().padding(8.dp)) { Text("Learn", fontSize = 24.sp, fontWeight = FontWeight.Bold); LazyColumn { items(pool, key = { it.kanji }) { k -> val key = "m_${k.kanji}"; var mastered by remember(k.kanji) { mutableStateOf(prefs.getBoolean(key, false)) }; Row(Modifier.fillMaxWidth().padding(5.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Text(k.kanji, fontSize = 30.sp, modifier = Modifier.width(52.dp)); Column(Modifier.weight(1f)) { Text(k.reading, fontWeight = FontWeight.Bold); Text(k.meaning, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }; IconButton({ mastered = !mastered; prefs.edit().putBoolean(key, mastered).apply() }) { Text(if (mastered) "👑" else "○", fontSize = 21.sp) } } } } } }

@Composable fun Review(pool: List<KanjiSeed>, index: Int, mode: Int, reveal: Boolean, mystery: Boolean, answered: Boolean, correct: Boolean, dark: Boolean, setMode: (Int) -> Unit, refresh: () -> Unit, toggleFlash: () -> Unit, toggleMystery: () -> Unit, answer: (Boolean) -> Unit) { val k = pool.getOrNull(index) ?: pool.firstOrNull() ?: KanjiSeed("一", "いち", "one"); Column(Modifier.fillMaxSize().padding(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { FilterChip(selected = mode == 0, onClick = { setMode(0) }, label = { Text("Flash") }); Spacer(Modifier.width(5.dp)); FilterChip(selected = mode == 1, onClick = { setMode(1) }, label = { Text("Quiz") }); Spacer(Modifier.width(5.dp)); FilterChip(selected = mode == 2, onClick = { setMode(2) }, label = { Text("Mystery") }); Spacer(Modifier.weight(1f)); IconButton(refresh) { Icon(Icons.Default.Refresh, "New Kanji") } }; Spacer(Modifier.height(10.dp)); when (mode) { 0 -> StudyCard(k, reveal, dark, toggleFlash); 2 -> Mystery(k, mystery, dark, toggleMystery); else -> Quiz(k, pool, answered, correct, dark, answer) } } }

@Composable fun StudyCard(k: KanjiSeed, reveal: Boolean, dark: Boolean, click: () -> Unit) { Card(Modifier.fillMaxWidth().height(360.dp).clickable { click() }, colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(k.kanji, fontSize = 84.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink); if (reveal) { Text(k.meaning, fontSize = 20.sp, color = if (dark) Color.White else Ink); Text(k.reading, color = if (dark) Color.White else Ink); Text("On'yomi: ${k.onyomi.ifBlank { "—" }}", color = if (dark) Color.White else Ink); Text("Kun'yomi: ${k.kunyomi.ifBlank { "—" }}", color = if (dark) Color.White else Ink) } else Text("Tap to reveal", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable fun Mystery(k: KanjiSeed, flip: Boolean, dark: Boolean, click: () -> Unit) { val rotation by animateFloatAsState(if (flip) 180f else 0f, tween(420), label = "flip"); Card(Modifier.fillMaxWidth().height(360.dp).clickable { click() }, colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { if (rotation <= 90f) Text("?", fontSize = 90.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink) else { Text(k.kanji, fontSize = 84.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink); Text(k.reading, color = if (dark) Color.White else Ink); Text(k.meaning, fontSize = 20.sp, color = if (dark) Color.White else Ink) } } } }

@Composable fun Quiz(k: KanjiSeed, pool: List<KanjiSeed>, answered: Boolean, correct: Boolean, dark: Boolean, answer: (Boolean) -> Unit) { val options = remember(k.kanji, pool.size) { (listOf(k) + pool.filter { it.kanji != k.kanji }.shuffled().take(3)).shuffled() }; Column(Modifier.fillMaxWidth().padding(12.dp)) { Text(k.kanji, fontSize = 84.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Ink, modifier = Modifier.align(Alignment.CenterHorizontally)); options.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { option -> Button({ if (!answered) answer(option.kanji == k.kanji) }, Modifier.weight(1f), enabled = !answered) { Text(option.meaning, fontSize = 12.sp) } } } }; if (answered) Text(if (correct) "Correct! 🎉" else "Not quite.", color = if (correct) Color(0xFF43A047) else Color(0xFFE53935), fontWeight = FontWeight.Bold) } }

@Composable fun Settings(theme: String, level: String, setTheme: (String) -> Unit, setLevel: (String) -> Unit, notify: () -> Unit, test: () -> Unit) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) { Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Kanji level", fontWeight = FontWeight.Bold); listOf("N5" to "N5 • 120", "N4" to "N4 • 200", "ALL" to "N5 + N4 • 320").forEach { (value, label) -> FilterChip(selected = level == value, onClick = { setLevel(value) }, label = { Text(label) }); Spacer(Modifier.height(6.dp)) }; Spacer(Modifier.height(10.dp)); Text("Theme", fontWeight = FontWeight.Bold); listOf("light" to "🌸 Cherry Blossom", "dark" to "✨ Fireflies + Stars", "winter" to "❄️ Winter + Fuji", "fireworks" to "🎆 Fireworks Festival").forEach { (value, label) -> Button({ setTheme(value) }, Modifier.fillMaxWidth()) { Text(label) } }; Spacer(Modifier.height(10.dp)); Text("Notifications", fontWeight = FontWeight.Bold); Button(notify, Modifier.fillMaxWidth()) { Text("Enable 2-hour reminders") }; OutlinedButton(test, Modifier.fillMaxWidth()) { Text("Send test notification") } } }
