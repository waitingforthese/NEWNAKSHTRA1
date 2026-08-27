package com.mahaesuvidha.chandrapanchangalarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import com.mahaesuvidha.chandrapanchangalarm.location.LiveLocationProvider

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mahaesuvidha.chandrapanchangalarm.alarm.AlarmScheduler
import com.mahaesuvidha.chandrapanchangalarm.model.JyotishInfo
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfile
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfileStore
import com.mahaesuvidha.chandrapanchangalarm.model.GhatChakra
import com.mahaesuvidha.chandrapanchangalarm.model.GhatChakraCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.LifeAlarmStateCache
import com.mahaesuvidha.chandrapanchangalarm.model.JyotishMaster
import com.mahaesuvidha.chandrapanchangalarm.model.LiveMoonCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.LiveSunCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.MoonState
import com.mahaesuvidha.chandrapanchangalarm.model.LivePanchangCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.NakshatraGuidanceCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.PanchangState
import com.mahaesuvidha.chandrapanchangalarm.model.SunState
import com.mahaesuvidha.chandrapanchangalarm.settings.AlarmPrefs
import com.mahaesuvidha.chandrapanchangalarm.settings.LocationPrefs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    private lateinit var scheduler: AlarmScheduler

    private companion object {
        const val STARTUP_PERMISSION_PREFS = "startup_permission_flow"
        const val KEY_PERMISSION_FLOW_REQUESTED = "requested_once"
    }

    private fun shouldRequestStartupPermissions(): Boolean =
        !getSharedPreferences(STARTUP_PERMISSION_PREFS, MODE_PRIVATE)
            .getBoolean(KEY_PERMISSION_FLOW_REQUESTED, false)

    private fun markStartupPermissionFlowRequested() {
        getSharedPreferences(STARTUP_PERMISSION_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_FLOW_REQUESTED, true)
            .apply()
    }

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            requestLocationPermissionIfNeeded()
        }

    private val locationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            requestBackgroundLocationIfNeeded()
        }

    private val backgroundLocationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            requestBatteryOptimizationExemptionIfNeeded()
        }

    private fun requestLocationPermissionIfNeeded() {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            requestBackgroundLocationIfNeeded()
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                backgroundLocationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                return
            }
        }
        requestBatteryOptimizationExemptionIfNeeded()
    }

    private fun requestBatteryOptimizationExemptionIfNeeded() {
        markStartupPermissionFlowRequested()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val powerManager = getSystemService(PowerManager::class.java)
        val packageName = packageName
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        scheduler =
            AlarmScheduler(this)


        // Ask for startup/background permissions only once after installation.
        // The completion flag is persisted so reopening the app does not launch
        // the permission/battery-optimization flow again. Users can change these
        // permissions later from Android Settings.
        if (shouldRequestStartupPermissions()) {
            if (
                android.os.Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                requestLocationPermissionIfNeeded()
            }
        }


        setContent {

            MaterialTheme {

                AppRoot(


                    onTestRashi = {

                        scheduler.scheduleTest(
                            "राशी"
                        )
                    },


                    onTestNakshatra = {

                        scheduler.scheduleTest(
                            "नक्षत्र"
                        )
                    },


                    onTestCharan = {

                        scheduler.scheduleTest(
                            "चरण"
                        )
                    },

                    onTestPanchang = {
                        scheduler.schedulePanchangTestSequence()
                    }
                )
            }
        }
    }
}



@Composable
private fun AppRoot(
    onTestRashi: () -> Unit,
    onTestNakshatra: () -> Unit,
    onTestCharan: () -> Unit,
    onTestPanchang: () -> Unit
) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(BirthProfileStore.load(context.applicationContext)?.let { p ->
        if (p.birthNakshatra.isBlank()) p.copy(birthNakshatra = runCatching { NakshatraGuidanceCalculator.calculateBirthNakshatra(p.birthDate, p.birthTime) }.getOrDefault("")) else p
    }) }

    if (profile == null) {
        BirthLoginScreen { newProfile ->
            BirthProfileStore.save(context.applicationContext, newProfile)
            profile = newProfile
            Thread {
                runCatching { AlarmScheduler(context.applicationContext).scheduleAll() }
            }.start()
        }
    } else {
        ChandraSuryaHome(
            profile = profile!!,
            onLogout = {
                // Remove all alarms tied to the old birth profile before logout
                // so the next person's guidance can never use the previous profile.
                runCatching { AlarmScheduler(context.applicationContext).cancelAll() }
                BirthProfileStore.clear(context.applicationContext)
                profile = null
            },
            onTestRashi = onTestRashi,
            onTestNakshatra = onTestNakshatra,
            onTestCharan = onTestCharan,
            onTestPanchang = onTestPanchang
        )
    }
}

@Composable
private fun BirthLoginScreen(onSave: (BirthProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var birthTime by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07111F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("🌙", fontSize = 52.sp)
        Text("Life Alarm", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("जन्ममाहिती Login", color = Color.LightGray, fontSize = 16.sp)
        Spacer(Modifier.height(20.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            focusedLabelColor = Color(0xFFBB86FC),
            unfocusedLabelColor = Color(0xFFD0D5DD),
            focusedBorderColor = Color(0xFF7E57C2),
            unfocusedBorderColor = Color(0xFF7A7F8A),
            cursorColor = Color(0xFFBB86FC),
            focusedPlaceholderColor = Color(0xFF8F96A3),
            unfocusedPlaceholderColor = Color(0xFF8F96A3)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("नाव") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = birthDate,
            onValueChange = {
                birthDate = formatBirthDate(it)
                dateError = if (birthDate.length == 10) validateBirthDate(birthDate) else null
                error = null
            },
            label = { Text("जन्मतारीख") },
            placeholder = { Text("DD/MM/YYYY") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = dateError != null,
            supportingText = { dateError?.let { Text(it, color = Color(0xFFFF7777)) } },
            colors = fieldColors
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = birthTime,
            onValueChange = {
                birthTime = formatBirthTime(it)
                timeError = if (birthTime.length == 5) validateBirthTime(birthTime) else null
                error = null
            },
            label = { Text("जन्मवेळ") },
            placeholder = { Text("00:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = timeError != null,
            supportingText = { timeError?.let { Text(it, color = Color(0xFFFF7777)) } },
            colors = fieldColors
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = birthPlace,
            onValueChange = { birthPlace = it },
            label = { Text("जन्मठिकाण") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors
        )

        Spacer(Modifier.height(12.dp))
        Text("Gender", color = Color.White, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Male" to "पुरुष", "Female" to "स्त्री", "Other" to "इतर").forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == value, onClick = { gender = value })
                    Text(label, color = Color.White, fontSize = 13.sp)
                }
            }
        }

        if (error != null) {
            Text(error!!, color = Color(0xFFFF7777), fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Button(
            onClick = {
                try {
                    require(name.trim().isNotEmpty()) { "नाव भरा" }
                    require(birthDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) { "जन्मतारीख DD/MM/YYYY मध्ये भरा" }
                    require(birthTime.matches(Regex("\\d{2}:\\d{2}"))) { "जन्मवेळ 00:00 या पद्धतीने भरा" }
                    require(validateBirthDate(birthDate) == null) { validateBirthDate(birthDate)!! }
                    require(validateBirthTime(birthTime) == null) { validateBirthTime(birthTime)!! }
                    require(birthPlace.trim().isNotEmpty()) { "जन्मठिकाण भरा" }
                    val rashi = GhatChakraCalculator.calculateBirthMoonRashi(birthDate, birthTime)
                    val nakshatra = NakshatraGuidanceCalculator.calculateBirthNakshatra(birthDate, birthTime)
                    onSave(BirthProfile(name.trim(), birthDate.trim(), gender, birthTime.trim(), birthPlace.trim(), rashi, nakshatra))
                } catch (t: Throwable) {
                    error = t.message ?: "जन्ममाहिती तपासा"
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("LOGIN / घटचक्र तयार करा", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "ही माहिती save राहील. Logout केल्यावर पुढील व्यक्तीची नवीन जन्ममाहिती भरता येईल.",
            color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center
        )
    }
}

private fun validateBirthDate(value: String): String? {
    if (!value.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
        return "जन्मतारीख DD/MM/YYYY मध्ये भरा"
    }
    return try {
        val parts = value.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
        cal.isLenient = false
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
        if (year !in 1900..java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) {
            "जन्मवर्ष योग्य नाही"
        } else null
    } catch (_: Exception) {
        "जन्मतारीख अस्तित्वात नाही"
    }
}

private fun validateBirthTime(value: String): String? {
    if (!value.matches(Regex("\\d{2}:\\d{2}"))) {
        return "जन्मवेळ 00:00 या पद्धतीने भरा"
    }
    val parts = value.split(":")
    val hour = parts[0].toIntOrNull() ?: return "जन्मवेळ चुकीची आहे"
    val minute = parts[1].toIntOrNull() ?: return "जन्मवेळ चुकीची आहे"
    return if (hour !in 0..23 || minute !in 0..59) {
        "जन्मवेळ 00:00 ते 23:59 मध्ये भरा"
    } else null
}

private fun formatBirthDate(raw: String): String {
    val digits = raw.filter(Char::isDigit).take(8)
    return buildString {
        digits.forEachIndexed { index, ch ->
            if (index == 2 || index == 4) append('/')
            append(ch)
        }
    }
}

private fun formatBirthTime(raw: String): String {
    val digits = raw.filter(Char::isDigit).take(4)
    return buildString {
        digits.forEachIndexed { index, ch ->
            if (index == 2) append(':')
            append(ch)
        }
    }
}

// ==========================================================
// HOME SCREEN
// ==========================================================

@Composable
private fun ChandraSuryaHome(

    profile: BirthProfile,

    onLogout: () -> Unit,

    onTestRashi: () -> Unit,

    onTestNakshatra: () -> Unit,

    onTestCharan: () -> Unit,

    onTestPanchang: () -> Unit

) {

    val context = LocalContext.current
    val cachedState = remember(context) {
        LifeAlarmStateCache.load(context.applicationContext)
    }

    var moonState by remember(cachedState) {
        mutableStateOf<MoonState?>(cachedState?.moon)
    }

    var sunState by remember(cachedState) {
        mutableStateOf<SunState?>(cachedState?.sun)
    }

    var panchangState by remember(cachedState) {
        mutableStateOf<PanchangState?>(cachedState?.panchang)
    }

    var loadError by remember {
        mutableStateOf<String?>(null)
    }

    var refreshRequest by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshMillis by remember { mutableLongStateOf(0L) }

    var liveLocation by remember {
        mutableStateOf("स्थान मिळवत आहे…")
    }

    var liveLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var liveLongitude by remember {
        mutableStateOf<Double?>(null)
    }

    var calculationLocationVersion by remember {
        mutableIntStateOf(0)
    }

    var locationProvider by remember {
        mutableStateOf<LiveLocationProvider?>(null)
    }

    DisposableEffect(Unit) {
        val provider =
            LiveLocationProvider(context.applicationContext)

        locationProvider = provider

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            provider.start(
                onLocation = { location ->
                    liveLatitude = location.latitude
                    liveLongitude = location.longitude

                    val locationPrefs =
                        LocationPrefs(
                            context.applicationContext
                        )

                    val movedEnough =
                        kotlin.math.abs(
                            locationPrefs.latitude -
                                location.latitude
                        ) >= 0.01 ||
                        kotlin.math.abs(
                            locationPrefs.longitude -
                                location.longitude
                        ) >= 0.01 ||
                        !locationPrefs.hasLiveLocation

                    locationPrefs.latitude =
                        location.latitude

                    locationPrefs.longitude =
                        location.longitude

                    locationPrefs.hasLiveLocation =
                        true

                    liveLocation =
                        "📍 %.5f°, %.5f°".format(
                            java.util.Locale.US,
                            location.latitude,
                            location.longitude
                        )

                    if (movedEnough) {
                        calculationLocationVersion++
                        Thread {
                            try {
                                AlarmScheduler(
                                    context.applicationContext
                                ).scheduleAll()
                            } catch (t: Throwable) {
                                android.util.Log.e(
                                    "LifeAlarm",
                                    "Location-based alarm reschedule failed",
                                    t
                                )
                            }
                        }.start()
                    }
                },
                onError = { message ->
                    liveLocation = "📍 $message"
                }
            )
        } else {
            liveLocation = "📍 स्थानाची परवानगी आवश्यक आहे"
        }

        onDispose {
            provider.stop()
        }
    }

LaunchedEffect(calculationLocationVersion, refreshRequest) {
        isRefreshing = true
        try {
            val savedLocation = LocationPrefs(context.applicationContext)

            val latitude =
                liveLatitude
                    ?: if (savedLocation.hasLiveLocation) savedLocation.latitude else 18.5204

            val longitude =
                liveLongitude
                    ?: if (savedLocation.hasLiveLocation) savedLocation.longitude else 73.8567

            val result =
                withContext(Dispatchers.Default) {
                    Triple(
                        LiveMoonCalculator.getCurrentMoonState(),
                        LiveSunCalculator.getCurrentSunState(),
                        LivePanchangCalculator.getCurrentPanchangState(
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                }

            LifeAlarmStateCache.save(
                context.applicationContext,
                result.first,
                result.second,
                result.third
            )

            moonState = result.first
            sunState = result.second
            panchangState = result.third
            loadError = null
            lastRefreshMillis = System.currentTimeMillis()
        } catch (t: kotlinx.coroutines.CancellationException) {
            throw t
        } catch (t: Throwable) {
            loadError = t.message ?: t.javaClass.simpleName
        } finally {
            isRefreshing = false
        }
    }

    // The screen refreshes automatically every 5 minutes while the app is open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(5 * 60 * 1000L)
            refreshRequest++
        }
    }

    if (
        moonState == null ||
        sunState == null ||
        panchangState == null
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF07111F)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🌙",
                    fontSize = 54.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                CircularProgressIndicator()

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text = if (loadError == null)
                        "पंचांग लोड होत आहे…"
                    else
                        "पंचांग गणनेत त्रुटी",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text = loadError ?: "LIVE गणना सुरू आहे",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        return
    }

    val ghatChakra = remember(profile.birthMoonRashi) {
        GhatChakraCalculator.fromBirthMoonRashi(profile.birthMoonRashi)
    }

    ChandraSuryaHomeContent(
        profile = profile,
        ghatChakra = ghatChakra,
        onLogout = onLogout,
        moonState = moonState!!,
        sunState = sunState!!,
        panchangState = panchangState!!,
        onTestRashi = onTestRashi,
        onTestNakshatra = onTestNakshatra,
        onTestCharan = onTestCharan,
        onTestPanchang = onTestPanchang,
        liveLocation = liveLocation,
        isRefreshing = isRefreshing,
        lastRefreshMillis = lastRefreshMillis,
        onRefresh = { refreshRequest++ }
    )
}

private data class NextAlarmInfo(
    val title: String,
    val timeMillis: Long
)

private fun findNextAlarm(
    prefs: AlarmPrefs,
    moon: MoonState,
    sun: SunState,
    panchang: PanchangState
): NextAlarmInfo? {
    val now = System.currentTimeMillis()
    val candidates = mutableListOf<NextAlarmInfo>()

    if (prefs.moonRashi && moon.nextRashiMillis > now) candidates += NextAlarmInfo("🌙 चंद्र राशी बदल", moon.nextRashiMillis)
    if (prefs.moonNakshatra && moon.nextNakshatraMillis > now) candidates += NextAlarmInfo("🌙 चंद्र नक्षत्र बदल", moon.nextNakshatraMillis)
    if (prefs.moonCharan && moon.nextCharanMillis > now) candidates += NextAlarmInfo("🌙 चंद्र चरण बदल", moon.nextCharanMillis)
    if (prefs.sunRashi && sun.nextRashiMillis > now) candidates += NextAlarmInfo("☀️ सूर्य राशी बदल", sun.nextRashiMillis)
    if (prefs.sunNakshatra && sun.nextNakshatraMillis > now) candidates += NextAlarmInfo("☀️ सूर्य नक्षत्र बदल", sun.nextNakshatraMillis)
    if (prefs.sunCharan && sun.nextCharanMillis > now) candidates += NextAlarmInfo("☀️ सूर्य चरण बदल", sun.nextCharanMillis)
    if (prefs.tithiAlarm && panchang.nextTithiMillis > now) candidates += NextAlarmInfo("🔔 तिथी बदल", panchang.nextTithiMillis)
    if (prefs.yogaAlarm && panchang.nextYogaMillis > now) candidates += NextAlarmInfo("✨ योग बदल", panchang.nextYogaMillis)
    if (prefs.karanaAlarm && panchang.nextKaranaMillis > now) candidates += NextAlarmInfo("🔔 करण बदल", panchang.nextKaranaMillis)
    if (prefs.pakshaAlarm && panchang.nextPakshaMillis > now) candidates += NextAlarmInfo("🌗 पक्ष बदल", panchang.nextPakshaMillis)
    if (prefs.praharAlarm && panchang.nextPraharMillis > now) candidates += NextAlarmInfo("⌛ प्रहर बदल", panchang.nextPraharMillis)
    if (prefs.lagnaAlarm && panchang.nextLagnaMillis > now) candidates += NextAlarmInfo("⭐ लग्न बदल", panchang.nextLagnaMillis)

    return candidates.minByOrNull { it.timeMillis }
}

private fun formatNextAlarmTime(millis: Long): String =
    java.text.SimpleDateFormat(
        "dd-MM-yyyy HH:mm",
        java.util.Locale.getDefault()
    ).format(java.util.Date(millis))

@Composable
private fun ChandraSuryaHomeContent(

    profile: BirthProfile,

    ghatChakra: GhatChakra,

    onLogout: () -> Unit,

    moonState: MoonState,

    sunState: SunState,

    panchangState: PanchangState,

    onTestRashi: () -> Unit,

    onTestNakshatra: () -> Unit,

    onTestCharan: () -> Unit,

    onTestPanchang: () -> Unit,

    liveLocation: String,

    isRefreshing: Boolean,

    lastRefreshMillis: Long,

    onRefresh: () -> Unit

) {

    val backgroundColor =
        Color(0xFF07111F)

    val moonCardColor =
        Color(0xFF0B2038)

    val sunCardColor =
        Color(0xFF211A08)

    val gold =
        Color(0xFFFFC83D)

    val moonBlue =
        Color(0xFF4DA3FF)

    val white =
        Color(0xFFF5F7FA)

    val context = LocalContext.current
    val alarmPrefs = remember { AlarmPrefs(context) }
    val nextAlarm = remember(moonState, sunState, panchangState) {
        findNextAlarm(alarmPrefs, moonState, sunState, panchangState)
    }

    var showGuidance by remember { mutableStateOf(false) }
    var showBadTara by remember { mutableStateOf(false) }

    if (showBadTara) {
        BackHandler { showBadTara = false }
        UpcomingBadTaraScreen(
            birthNakshatra = profile.birthNakshatra,
            onBack = { showBadTara = false }
        )
        return
    }

    if (showGuidance) {
        BackHandler { showGuidance = false }
        NakshatraGuidanceScreen(
            birthNakshatra = profile.birthNakshatra,
            onBack = { showGuidance = false }
        )
        return
    }

    var showSettings by remember {

        mutableStateOf(false)
    }


    if (
        showSettings
    ) {

        SettingsDialog(

            onDismiss = {

                showSettings = false
            },

            profile = profile,

            onLogout = {
                showSettings = false
                onLogout()
            }
        )
    }


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    backgroundColor
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    12.dp
                )

    ) {

        Spacer(
            Modifier.height(6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("👤 ${profile.name}", color = white, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("जन्म: ${profile.birthDate} • ${profile.birthTime} • ${profile.birthPlace}", color = Color.LightGray, fontSize = 11.sp)
                Text("जन्म चंद्र राशी: ${profile.birthMoonRashi}", color = moonBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("घात चक्र: ${ghatChakra.birthRashi}", color = Color(0xFFFF7777), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = liveLocation,
            color = Color.LightGray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = { showGuidance = true },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC83D))
        ) {
            Text("⭐ नक्षत्र मार्गदर्शन", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = { showBadTara = true },
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
        ) {
            Text("🔴 विपत / प्रत्यारी / वध आगामी", fontWeight = FontWeight.Bold)
        }


        // HEADER

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Text(

                text = "🌙",

                fontSize =
                    38.sp
            )


            Spacer(

                modifier =
                    Modifier.width(
                        8.dp
                    )
            )


            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                Text(

                    text =
                        "Life Alarm",

                    color =
                        white,

                    fontSize =
                        23.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(

                    text =
                        "LIVE • AUTO • ACCURATE",

                    color =
                        Color.LightGray,

                    fontSize =
                        11.sp
                )
            }


            Text(

                text =
                    "⚙️",

                fontSize =
                    25.sp,

                modifier =
                    Modifier.clickable {

                        showSettings =
                            true
                    }
            )
        }



        // LOCATION

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 4.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Text(

                text =
                    "📍 ${moonState.location}",

                color =
                    white,

                fontSize =
                    14.sp
            )


            Spacer(

                modifier =
                    Modifier.width(
                        8.dp
                    )
            )


            Text(

                text =
                    "● LIVE",

                color =
                    Color(0xFF39D353),

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lastRefreshMillis > 0L) {
                    "अंतिम refresh: " + java.text.SimpleDateFormat(
                        "HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(lastRefreshMillis))
                } else {
                    "LIVE refresh"
                },
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onRefresh,
                enabled = !isRefreshing,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = if (isRefreshing) "⟳ Refreshing…" else "↻ Refresh",
                    fontSize = 12.sp
                )
            }
        }

        Spacer(

            modifier =
                Modifier.height(
                    10.dp
                )
        )



        if (nextAlarm != null) {
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF102A43)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "🔔 पुढील अलार्म",
                        color = gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = nextAlarm.title,
                        color = white,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatNextAlarmTime(nextAlarm.timeMillis),
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }


        // MOON + SUN

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),

            verticalAlignment =
                Alignment.Top

        ) {


            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                MoonColumn(

                    state =
                        moonState,

                    cardColor =
                        moonCardColor,

                    accentColor =
                        moonBlue,

                    textColor =
                        white
                )
            }



            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                SunColumn(

                    state =
                        sunState,

                    cardColor =
                        sunCardColor,

                    accentColor =
                        gold,

                    textColor =
                        white
                )
            }
        }



        Spacer(

            modifier =
                Modifier.height(
                    12.dp
                )
        )



        // PERSONAL GHAT CHAKRA

        GhatChakraCard(
            ghat = ghatChakra,
            gender = profile.gender
        )

        Spacer(Modifier.height(12.dp))

        // PANCHANG CARD

        PanchangCard(
            state = panchangState,
            moonState = moonState,
            ghatChakra = ghatChakra,
            gender = profile.gender
        )



        Spacer(

            modifier =
                Modifier.height(
                    12.dp
                )
        )



        // TEST BUTTONS

        Text(

            text =
                "🔔 अलार्म टेस्ट",

            color =
                white,

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    vertical = 6.dp
                )
        )



        TestButton(

            text =
                "🌙 राशी बदल Test",

            onClick =
                onTestRashi
        )



        TestButton(

            text =
                "⭐ नक्षत्र बदल Test",

            onClick =
                onTestNakshatra
        )



        TestButton(

            text =
                "🔔 चरण बदल Test",

            onClick =
                onTestCharan
        )


        TestButton(

            text =
                "📅 पंचांग Ringtone Test",

            onClick =
                onTestPanchang
        )



        Spacer(

            modifier =
                Modifier.height(
                    12.dp
                )
        )



        Text(

            text =
                "चंद्र सूर्य अलार्म\n" +
                        "LIVE Calculation • Auto Alarm",

            color =
                Color.Gray,

            fontSize =
                11.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 10.dp
                    )
        )

        Text(
            text = "Developed by Rahul Jagtap Patil",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}



// ==========================================================
// MOON COLUMN
// ==========================================================

@Composable
private fun MoonColumn(

    state: MoonState,

    cardColor: Color,

    accentColor: Color,

    textColor: Color

) {

    val jyotish =

        JyotishMaster.getInfo(

            state.rashi,

            state.nakshatra,

            state.pada
        )


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    cardColor
            )

    ) {

        Column(

            modifier =
                Modifier.padding(
                    10.dp
                )

        ) {

            Text(

                text =
                    "🌙 चंद्र",

                color =
                    textColor,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(

                text =
                    "● LIVE सध्याची स्थिती",

                color =
                    Color(0xFF39D353),

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                Modifier.height(10.dp)
            )


            SmallDataRow(

                "राशी",

                state.rashi.marathi,

                textColor
            )


            SmallDataRow(

                "नक्षत्र",

                state.nakshatra.marathi,

                textColor
            )


            SmallDataRow(

                "चरण",

                state.pada.toString(),

                textColor
            )


            HorizontalDivider(

                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    ),

                color =
                    Color.White.copy(
                        alpha = 0.15f
                    )
            )


            Text(

                text =
                    "ग्रह स्वामी",

                color =
                    accentColor,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )


            PlanetPanel(

                info =
                    jyotish,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            Spacer(
                Modifier.height(8.dp)
            )


            NextChangeBlock(

                title =
                    "🌙 पुढील राशी बदल",

                change =
                    state.nextRashi,

                time =
                    state.nextRashiTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            NextChangeBlock(

                title =
                    "⭐ पुढील नक्षत्र बदल",

                change =
                    state.nextNakshatra,

                time =
                    state.nextNakshatraTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            NextChangeBlock(

                title =
                    "🔔 पुढील चरण बदल",

                change =
                    state.nextCharan,

                time =
                    state.nextCharanTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )
        }
    }
}



// ==========================================================
// SUN COLUMN
// ==========================================================

@Composable
private fun SunColumn(

    state: SunState,

    cardColor: Color,

    accentColor: Color,

    textColor: Color

) {

    val jyotish =

        JyotishMaster.getInfo(

            state.rashi,

            state.nakshatra,

            state.pada
        )


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    cardColor
            )

    ) {

        Column(

            modifier =
                Modifier.padding(
                    10.dp
                )

        ) {

            Text(

                text =
                    "☀️ सूर्य",

                color =
                    textColor,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(

                text =
                    "● LIVE सध्याची स्थिती",

                color =
                    Color(0xFF39D353),

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                Modifier.height(10.dp)
            )


            SmallDataRow(

                "राशी",

                state.rashi.marathi,

                textColor
            )


            SmallDataRow(

                "नक्षत्र",

                state.nakshatra.marathi,

                textColor
            )


            SmallDataRow(

                "चरण",

                state.pada.toString(),

                textColor
            )


            HorizontalDivider(

                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    ),

                color =
                    Color.White.copy(
                        alpha = 0.15f
                    )
            )


            Text(

                text =
                    "ग्रह स्वामी",

                color =
                    accentColor,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )


            PlanetPanel(

                info =
                    jyotish,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            Spacer(
                Modifier.height(8.dp)
            )


            NextChangeBlock(

                title =
                    "☀️ पुढील राशी बदल",

                change =
                    state.nextRashi,

                time =
                    state.nextRashiTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            NextChangeBlock(

                title =
                    "⭐ पुढील नक्षत्र बदल",

                change =
                    state.nextNakshatra,

                time =
                    state.nextNakshatraTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )


            NextChangeBlock(

                title =
                    "🔔 पुढील चरण बदल",

                change =
                    state.nextCharan,

                time =
                    state.nextCharanTime,

                accent =
                    accentColor,

                textColor =
                    textColor
            )
        }
    }
}



// ==========================================================
// SMALL DATA ROW
// ==========================================================

@Composable
private fun SmallDataRow(

    label: String,

    value: String,

    color: Color

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        Text(

            text =
                label,

            color =
                Color.LightGray,

            fontSize =
                12.sp
        )


        Text(

            text =
                value,

            color =
                color,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}



// ==========================================================
// PLANET PANEL
// ==========================================================

@Composable
private fun PlanetPanel(

    info: JyotishInfo,

    accent: Color,

    textColor: Color

) {

    Text(
        "राशी: ${info.rashiLord}",
        color = textColor,
        fontSize = 12.sp
    )

    Text(
        "नक्षत्र: ${info.nakshatraLord}",
        color = textColor,
        fontSize = 12.sp
    )

    Text(
        "नवांश: ${info.navamshaRashi}",
        color = textColor,
        fontSize = 12.sp
    )

    Text(
        "नवांश स्वामी: ${info.navamshaLord}",
        color = textColor,
        fontSize = 12.sp
    )


    if (
        info.enemies.isNotEmpty()
    ) {

        Text(

            text =
                "⚠️ विरोधी ग्रह: " +
                        info.enemies.joinToString(
                            ", "
                        ),

            color =
                accent,

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}



// ==========================================================
// NEXT CHANGE BLOCK
// ==========================================================

@Composable
private fun NextChangeBlock(

    title: String,

    change: String,

    time: String,

    accent: Color,

    textColor: Color

) {

    Text(

        text =
            title,

        color =
            accent,

        fontSize =
            14.sp,

        fontWeight =
            FontWeight.Bold
    )


    Text(

        text =
            change,

        color =
            textColor,

        fontSize =
            12.sp
    )


    Text(

        text =
            "📅 $time",

        color =
            Color.LightGray,

        fontSize =
            11.sp
    )


    Spacer(
        Modifier.height(8.dp)
    )
}



// ==========================================================
// TEST BUTTON
// ==========================================================

@Composable
private fun TestButton(

    text: String,

    onClick: () -> Unit

) {

    Button(

        onClick =
            onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 3.dp
                ),

        shape =
            RoundedCornerShape(
                14.dp
            )

    ) {

        Text(

            text =
                text,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ==========================================================
// PERSONAL GHAT CHAKRA + PANCHANG CARDS
// ==========================================================

private fun currentTithiNumber(state: PanchangState): Int {
    val names = listOf("प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पौर्णिमा", "पूर्णिमा", "अमावस्या")
    val n = names.indexOf(state.tithi)
    if (state.tithi == "अमावस्या") return 30
    if (n < 0) return 1
    return if (n <= 14) n + 1 else 15
}

@Composable
private fun GhatChakraCard(ghat: GhatChakra, gender: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1015))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("⚠️ माझे घात चक्र", color = Color(0xFFFF7777), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("जन्म चंद्र राशी: ${ghat.birthRashi}", color = Color.White, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            val rows = listOf(
                "मास" to ghat.masa,
                "तिथी" to "${ghat.tithiGroup} (1/6/11, 2/7/12, 3/8/13, 4/9/14 किंवा 5/10/15)",
                "वार" to ghat.weekday,
                "नक्षत्र" to ghat.nakshatra,
                "योग" to ghat.yoga,
                "पक्ष" to "शुक्ल + कृष्ण (दोन्ही पक्ष)",
                "करण" to ghat.karana,
                "प्रहर" to ghat.prahar.toString(),
                "चंद्र राशी" to ghat.moonRashi,
                "लग्न" to ghat.lagnaFor(gender)
            )
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = Color.LightGray, fontSize = 12.sp)
                    Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 8.dp))
                }
            }
            Spacer(Modifier.height(5.dp))
            Text("पक्ष: शुक्ल + कृष्ण — घात तिथी-वर्ग दोन्ही पक्षांत लागू.", color = Color(0xFFFFB0B0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("पंचांगातील जुळणारे कार्ड RED होईल.", color = Color(0xFFFFB0B0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PanchangCard(
    state: PanchangState,
    moonState: MoonState,
    ghatChakra: GhatChakra,
    gender: String
) {
    val tithiNumber = currentTithiNumber(state)
    val ghatLagna = ghatChakra.lagnaFor(gender)
    val matches = linkedMapOf(
        "मास" to (state.masa == ghatChakra.masa),
        "तिथी" to ghatChakra.matchesTithi(tithiNumber),
        "वार" to (state.weekday == ghatChakra.weekday),
        "नक्षत्र" to (moonState.nakshatra.marathi == ghatChakra.nakshatra),
        // Ghat-Chakra Yoga is matched ONLY against the same Surya Siddhanta
        // Yoga calculated by the app's live Panchang (Sun + Moon sidereal
        // longitude / 27). Do not use a second Yoga table/calculator here.
        "योग" to ghatChakra.matchesSuryaSiddhantaYoga(state.yoga),
        // Ghat tithi-group (Nanda/Bhadra/Jaya/Rikta/Purna) repeats in both pakshas.
        // Therefore Paksha itself is not a separate Ghat-Chakra element; the Paksha
        // card is highlighted whenever the current tithi belongs to the Ghat group.
        "पक्ष" to ghatChakra.matchesTithi(tithiNumber),
        "करण" to (state.karana == ghatChakra.karana),
        "प्रहर" to (state.prahar == ghatChakra.prahar.toString()),
        "चंद्र राशी" to (moonState.rashi.marathi == ghatChakra.moonRashi),
        "लग्न" to (state.lagna == ghatLagna)
    )
    val matchCount = matches.values.count { it }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))) {
            Column(Modifier.padding(16.dp)) {
                Text("📅 आजचे पंचांग", color = Color.Black, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (matchCount == 0) "घात चक्र जुळणी: नाही" else "⚠️ घात चक्र जुळणी: $matchCount",
                    color = if (matchCount == 0) Color(0xFF2E9E44) else Color(0xFFC62828),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SimplePanchangValueCard("📅 दिनांक", state.date, false)
        SimplePanchangValueCard("🗓️ वार", state.weekday, matches["वार"] == true)
        PanchangChangeSection("तिथी", state.tithi, state.tithiStartTime, state.nextTithi, state.nextTithiTime, matches["तिथी"] == true)
        PanchangInfoCard("⭐ नक्षत्र", moonState.nakshatra.marathi, moonState.nakshatraStartTime, moonState.nextNakshatra, moonState.nextNakshatraTime, matches["नक्षत्र"] == true)
        PanchangChangeSection("योग (सूर्यसिद्धांत)", state.yoga, state.yogaStartTime, state.nextYoga, state.nextYogaTime, matches["योग"] == true)
        PanchangChangeSection("करण", state.karana, state.karanaStartTime, state.nextKarana, state.nextKaranaTime, matches["करण"] == true)
        PanchangChangeSection("पक्ष", state.paksha, state.pakshaStartTime, state.nextPaksha, state.nextPakshaTime, matches["पक्ष"] == true)
        SimplePanchangValueCard("🌙 घात मास / सध्याचा मास", state.masa, matches["मास"] == true)
        PanchangInfoCard("⏳ प्रहर", state.prahar, state.praharStartTime, state.nextPrahar, state.nextPraharTime, matches["प्रहर"] == true)
        PanchangInfoCard("🌙 चंद्र राशी", moonState.rashi.marathi, "", moonState.nextRashi, moonState.nextRashiTime, matches["चंद्र राशी"] == true)
        PanchangInfoCard("⭐ लग्न", state.lagna, state.lagnaStartTime, state.nextLagna, state.nextLagnaTime, matches["लग्न"] == true)
    }
}

@Composable
private fun GhatAwareCardContainer(active: Boolean, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFFFFD6D6) else Color(0xFFF7F7F7)),
        border = if (active) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFC62828)) else null
    ) { content() }
}

@Composable
private fun PanchangChangeSection(label: String, value: String, startTime: String, next: String, endTime: String, active: Boolean) {
    val icon = when (label) { "तिथी" -> "🌙"; "योग" -> "✨"; "करण" -> "🔔"; "पक्ष" -> "🌗"; else -> "📌" }
    GhatAwareCardContainer(active) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("$icon $label", color = if (active) Color(0xFFC62828) else Color(0xFF006CA8), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.Black, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(7.dp)); HorizontalDivider(color = Color.LightGray); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🟢 चालू आहे", color = Color(0xFF2E9E44), fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(value, color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🟢 प्रारंभ", color = Color(0xFF388E3C), fontSize = 16.sp); Text(startTime, color = Color.DarkGray, fontSize = 16.sp) }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🔔 पुढील बदल", color = Color(0xFF006CA8), fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(next, color = Color.Black, fontSize = 16.sp) }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🔴 बदलाची वेळ", color = Color(0xFFC62828), fontSize = 16.sp); Text(endTime, color = Color.DarkGray, fontSize = 16.sp) }
            if (active) { Spacer(Modifier.height(6.dp)); Text("⚠️ घात चक्रातील सध्याचा घटक", color = Color(0xFFC62828), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SimplePanchangValueCard(title: String, value: String, active: Boolean) {
    GhatAwareCardContainer(active) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(title, color = if (active) Color(0xFFC62828) else Color(0xFF006CA8), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PanchangInfoCard(title: String, current: String, startTime: String, next: String, nextTime: String, active: Boolean) {
    GhatAwareCardContainer(active) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = if (active) Color(0xFFC62828) else Color(0xFF006CA8), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp)); HorizontalDivider(color = Color.LightGray); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🟢 चालू आहे", color = Color(0xFF2E9E44), fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(current, color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            if (startTime.isNotBlank()) { Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🟢 प्रारंभ", color = Color(0xFF388E3C), fontSize = 16.sp); Text(startTime, color = Color.DarkGray, fontSize = 16.sp) } }
            Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🔔 पुढील बदल", color = Color(0xFF006CA8), fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(next, color = Color.Black, fontSize = 16.sp) }
            Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🔴 बदलाची वेळ", color = Color(0xFFC62828), fontSize = 16.sp); Text(nextTime, color = Color.DarkGray, fontSize = 16.sp) }
            if (active) { Spacer(Modifier.height(6.dp)); Text("⚠️ घात चक्रातील सध्याचा घटक — Card RED", color = Color(0xFFC62828), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ==========================================================
// PANCHANG ROW
// ==========================================================

@Composable
private fun PanchangRow(

    label: String,

    value: String

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 7.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        Text(

            text =
                label,

            color =
                Color.Gray,

            fontSize =
                15.sp
        )


        Text(

            text =
                value,

            color =
                Color.Black,

            fontSize =
                17.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}



// ==========================================================
// SETTINGS DIALOG
// ==========================================================


@Composable
private fun NakshatraGuidanceScreen(
    birthNakshatra: String,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val now = System.currentTimeMillis()
    val current = remember(birthNakshatra) { NakshatraGuidanceCalculator.currentGuidance(birthNakshatra, now) }
    val upcoming = remember(birthNakshatra) { NakshatraGuidanceCalculator.upcomingGuidance(birthNakshatra, 60, now) }
    val warning = Color(0xFFE53935)
    val bg = Color(0xFF07111F)
    val card = Color(0xFF10253A)
    val white = Color(0xFFF5F7FA)

    Column(Modifier.fillMaxSize().background(bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← मागे", color = white) }
            Text("नक्षत्र मार्गदर्शन", color = white, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("जन्म नक्षत्र", color = Color.LightGray, fontSize = 13.sp)
                Text(if (birthNakshatra.isBlank()) "—" else birthNakshatra, color = Color(0xFF4DA3FF), fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("🌙 सध्या चालू असलेले नक्षत्र", color = white, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(current.nakshatra, color = if (current.tara.isWarning) warning else white, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("तारा: ${current.tara.marathi}", color = if (current.tara.isWarning) warning else Color(0xFFFFC83D), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("सुरुवात: ${NakshatraGuidanceCalculator.format(current.startMillis)}", color = Color.LightGray, fontSize = 12.sp)
                Text("समाप्ती: ${NakshatraGuidanceCalculator.format(current.endMillis)}", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("काय करावे", color = white, fontWeight = FontWeight.Bold)
                Text(current.doText, color = Color(0xFFB9E6FF), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("काय टाळावे", color = white, fontWeight = FontWeight.Bold)
                Text(current.avoidText, color = if (current.tara.isWarning) warning else Color(0xFFFFC0C0), fontSize = 13.sp, fontWeight = if (current.tara.isWarning) FontWeight.Bold else FontWeight.Normal)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("📅 पुढील 60 दिवसांचे नक्षत्र", color = white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        upcoming.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(11.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.nakshatra, color = if (item.tara.isWarning) warning else white, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(item.tara.marathi + if (item.tara.isWarning) " 🔴" else "", color = if (item.tara.isWarning) warning else Color(0xFFFFC83D), fontWeight = FontWeight.Bold)
                    }
                    Text("${NakshatraGuidanceCalculator.format(item.startMillis)}  →  ${NakshatraGuidanceCalculator.format(item.endMillis)}", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("🔴 विपत / प्रत्यारी / वध — आगामी", color = warning, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        upcoming.filter { it.tara.isWarning }.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF35151A)), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(11.dp)) {
                    Text("${item.tara.marathi} — ${item.nakshatra}", color = warning, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${NakshatraGuidanceCalculator.format(item.startMillis)}  →  ${NakshatraGuidanceCalculator.format(item.endMillis)}", color = white, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("काय टाळावे: ${item.avoidText}", color = warning, fontSize = 12.sp)
                }
            }
        }
    }
}
@Composable
private fun UpcomingBadTaraScreen(
    birthNakshatra: String,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val now = System.currentTimeMillis()
    val upcoming = remember(birthNakshatra) {
        NakshatraGuidanceCalculator.upcomingGuidance(birthNakshatra, 60, now)
            .filter { it.tara.isWarning }
    }
    val warning = Color(0xFFE53935)
    val bg = Color(0xFF07111F)
    val card = Color(0xFF35151A)
    val white = Color(0xFFF5F7FA)
    Column(Modifier.fillMaxSize().background(bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← मागे", color = white) }
            Text("विपत / प्रत्यारी / वध आगामी", color = warning, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("जन्म नक्षत्र: $birthNakshatra", color = white, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("पुढील 60 दिवस", color = Color.LightGray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (upcoming.isEmpty()) {
            Text("पुढील 60 दिवसांत विपत / प्रत्यारी / वध आढळले नाही.", color = white, fontSize = 14.sp)
        } else {
            upcoming.forEach { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(11.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${item.tara.marathi} 🔴", color = warning, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("नक्षत्र: ${item.nakshatra}", color = white, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("सुरुवात: ${NakshatraGuidanceCalculator.format(item.startMillis)}", color = Color.LightGray, fontSize = 12.sp)
                        Text("समाप्ती: ${NakshatraGuidanceCalculator.format(item.endMillis)}", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("काय करावे: ${item.doText}", color = white, fontSize = 12.sp)
                        Text("काय टाळावे: ${item.avoidText}", color = warning, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(

    onDismiss: () -> Unit,
    profile: BirthProfile,
    onLogout: () -> Unit

) {

    val context = LocalContext.current
    val prefs = remember { AlarmPrefs(context) }

    var moonRashi by remember { mutableStateOf(prefs.moonRashi) }
    var moonNakshatra by remember { mutableStateOf(prefs.moonNakshatra) }
    var moonCharan by remember { mutableStateOf(prefs.moonCharan) }

    var sunRashi by remember { mutableStateOf(prefs.sunRashi) }
    var sunNakshatra by remember { mutableStateOf(prefs.sunNakshatra) }
    var sunCharan by remember { mutableStateOf(prefs.sunCharan) }

    var tithiAlarm by remember { mutableStateOf(prefs.tithiAlarm) }
    var yogaAlarm by remember { mutableStateOf(prefs.yogaAlarm) }
    var karanaAlarm by remember { mutableStateOf(prefs.karanaAlarm) }
    var pakshaAlarm by remember { mutableStateOf(prefs.pakshaAlarm) }
    var praharAlarm by remember { mutableStateOf(prefs.praharAlarm) }
    var lagnaAlarm by remember { mutableStateOf(prefs.lagnaAlarm) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ अलार्म सेटिंग्स") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("🌙 चंद्र अलार्म")
                SwitchRow("चंद्र राशी बदल", moonRashi) {
                    moonRashi = it
                    prefs.moonRashi = it
                }
                SwitchRow("चंद्र नक्षत्र बदल", moonNakshatra) {
                    moonNakshatra = it
                    prefs.moonNakshatra = it
                }
                SwitchRow("चंद्र चरण बदल", moonCharan) {
                    moonCharan = it
                    prefs.moonCharan = it
                }

                Spacer(Modifier.height(10.dp))
                Text("☀️ सूर्य अलार्म")
                SwitchRow("सूर्य राशी बदल", sunRashi) {
                    sunRashi = it
                    prefs.sunRashi = it
                }
                SwitchRow("सूर्य नक्षत्र बदल", sunNakshatra) {
                    sunNakshatra = it
                    prefs.sunNakshatra = it
                }
                SwitchRow("सूर्य चरण बदल", sunCharan) {
                    sunCharan = it
                    prefs.sunCharan = it
                }

                Spacer(Modifier.height(10.dp))
                Text("📅 पंचांग अलार्म")
                SwitchRow("तिथी बदल", tithiAlarm) {
                    tithiAlarm = it
                    prefs.tithiAlarm = it
                }
                SwitchRow("योग बदल", yogaAlarm) {
                    yogaAlarm = it
                    prefs.yogaAlarm = it
                }
                SwitchRow("करण बदल", karanaAlarm) {
                    karanaAlarm = it
                    prefs.karanaAlarm = it
                }
                SwitchRow("पक्ष बदल", pakshaAlarm) {
                    pakshaAlarm = it
                    prefs.pakshaAlarm = it
                }
                SwitchRow("प्रहर बदल", praharAlarm) {
                    praharAlarm = it
                    prefs.praharAlarm = it
                }
                SwitchRow("लग्न बदल", lagnaAlarm) {
                    lagnaAlarm = it
                    prefs.lagnaAlarm = it
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Text("👤 ${profile.name}", fontWeight = FontWeight.Bold)
                Text("जन्म चंद्र राशी: ${profile.birthMoonRashi}", fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text("Logout / दुसऱ्या व्यक्तीसाठी Login")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    val appContext = context.applicationContext
                    Thread {
                        try {
                            AlarmScheduler(appContext).scheduleAll()
                        } catch (t: Throwable) {
                            android.util.Log.e(
                                "LifeAlarm",
                                "Alarm scheduling failed after Settings Save",
                                t
                            )
                        }
                    }.start()
                }
            ) {
                Text("सेव्ह करा")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करा")
            }
        }
    )
}


// ==========================================================
// SWITCH ROW
// ==========================================================

@Composable
private fun SwitchRow(

    text: String,

    checked: Boolean,

    onCheckedChange:
        (Boolean) -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        Text(

            text =
                text,

            modifier =
                Modifier.weight(
                    1f
                )
        )


        Switch(

            checked =
                checked,

            onCheckedChange =
                onCheckedChange
        )
    }
}
