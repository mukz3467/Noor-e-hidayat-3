package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.AppLocationManager
import com.example.data.location.CityPreset
import com.example.data.model.AppLanguage
import com.example.data.repository.PrayerCalculator
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Location state from shared location manager
    val userLocation by AppLocationManager.currentLocation.collectAsState()
    val isLocating by AppLocationManager.isLocating.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            AppLocationManager.requestLocationUpdate(context, forceFresh = true) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission not granted. You can search your city manually.", Toast.LENGTH_SHORT).show()
        }
    }

    var showCityPicker by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }
    var showManualCoordsDialog by remember { mutableStateOf(false) }

    // Calculate exact mathematical Great Circle bearing to Kaaba
    val qiblaBearing = remember(userLocation.latitude, userLocation.longitude) {
        PrayerCalculator.calculateQiblaBearing(userLocation.latitude, userLocation.longitude)
    }

    // Magnetic Declination calculation via Android World Magnetic Model
    val magneticDeclination = remember(userLocation.latitude, userLocation.longitude) {
        try {
            val field = GeomagneticField(
                userLocation.latitude.toFloat(),
                userLocation.longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            field.declination
        } catch (_: Exception) {
            0f
        }
    }

    // Sensor State
    var rawMagneticHeading by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasCompassSensor by remember { mutableStateOf(true) }
    var sensorTypeUsed by remember { mutableStateOf("Rotation Vector (Gyro + Magnetometer)") }

    // Register Android Sensors
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var listener: SensorEventListener? = null

        if (rotationSensor != null) {
            sensorTypeUsed = "Rotation Vector (Fused High Accuracy)"
            listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientationAngles = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        val normalized = (azimuthDegrees + 360f) % 360f
                        // Low pass filter smoothing
                        rawMagneticHeading = lowPass(normalized, rawMagneticHeading)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    sensorAccuracy = accuracy
                }
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null && magSensor != null) {
            sensorTypeUsed = "Accelerometer + Magnetic Field Fallback"
            listener = object : SensorEventListener {
                private val gravity = FloatArray(3)
                private val geomagnetic = FloatArray(3)
                private val rotationMatrix = FloatArray(9)
                private val orientationAngles = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            System.arraycopy(it.values, 0, gravity, 0, 3)
                        } else if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                            System.arraycopy(it.values, 0, geomagnetic, 0, 3)
                        }

                        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            val normalized = (azimuth + 360f) % 360f
                            rawMagneticHeading = lowPass(normalized, rawMagneticHeading)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    sensorAccuracy = accuracy
                }
            }
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            hasCompassSensor = false
        }

        onDispose {
            listener?.let { sensorManager.unregisterListener(it) }
        }
    }

    // Correct magnetic heading with declination to obtain True Geographic Heading
    val trueHeading = ((rawMagneticHeading + magneticDeclination) + 360f) % 360f

    // Relative difference between user's phone orientation and the Kaaba
    val relativeAngle = ((qiblaBearing.toFloat() - trueHeading) + 360f) % 360f

    // Smooth compass animated rotation
    val animatedCompassRotation by animateFloatAsState(
        targetValue = -trueHeading,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "compass_rotation"
    )

    // Check if user is aligned with Kaaba (within ±3 degrees)
    val isAligned = hasCompassSensor && sensorAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE && sensorAccuracy != SensorManager.SENSOR_STATUS_ACCURACY_LOW && abs(((relativeAngle + 180f) % 360f) - 180f) <= 3.5f

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("qibla", language),
                language = language,
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            AppLocationManager.requestLocationUpdate(context, forceFresh = true)
                        }
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Update GPS Location",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val isGpsActive = AppLocationManager.isGpsEnabled(context)
            val hasPerm = AppLocationManager.hasLocationPermission(context)

            // Alert 1: Permission Needed for Auto Worldwide Location
            if (!hasPerm) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationSearching,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Worldwide Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Allow location to calculate the exact Qibla bearing from your current position anywhere in the world.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Allow", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else if (!isGpsActive) {
                // Alert 2: GPS is turned OFF on mobile device
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationDisabled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mobile Location (GPS) is OFF",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Turn ON mobile location in settings so the compass can compute your exact angle to the Holy Kaaba.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { AppLocationManager.openLocationSettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Turn ON", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Location Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (userLocation.isGpsFix) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${userLocation.cityName}${if (userLocation.countryName.isNotBlank()) ", " + userLocation.countryName else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "Lat: %.4f° • Lon: %.4f°", userLocation.latitude, userLocation.longitude),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showCityPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Change", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accuracy / Calibration Warning if needed
            if (!hasCompassSensor || sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFFFC107), Color(0xFFFF9800)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (!hasCompassSensor) "Compass Sensor Not Available" else "Compass Calibration Recommended",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404)
                            )
                            Text(
                                text = if (!hasCompassSensor) "This device does not expose a compatible compass sensor. Use the calculated Qibla bearing; compass alignment cannot be verified." else "Wave phone in a figure-8 motion away from metal covers or magnetic objects to restore sensor precision.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Aligned Confirmation Banner
            AnimatedVisibility(visible = isAligned) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFFE6A100)))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🕋 Aligned with Kaaba! You are facing Qibla",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (hasCompassSensor) {
                // Compass Dial View
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(310.dp)
                        .padding(8.dp)
                ) {
                    // Outer rotating compass rose with North, South, East, West markers
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedCompassRotation)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 12.dp.toPx()

                        // Outer rim circle
                        drawCircle(
                            color = Color(0xFF006D44).copy(alpha = 0.15f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw 72 graduation ticks around dial (every 5 degrees)
                        for (deg in 0 until 360 step 5) {
                            val angleRad = Math.toRadians((deg - 90).toDouble())
                            val isMajor = (deg % 30 == 0)
                            val isCardinal = (deg % 90 == 0)
                            val tickLength = if (isCardinal) 16.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                            val strokeWidth = if (isCardinal) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                            val tickColor = if (deg == 0) Color(0xFFE53935) else if (isMajor) Color(0xFF006D44) else Color.Gray.copy(alpha = 0.5f)

                            val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
                            val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                            val endX = (center.x + radius * cos(angleRad)).toFloat()
                            val endY = (center.y + radius * sin(angleRad)).toFloat()

                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeWidth
                            )
                        }
                    }

                    // Static Cardinal Direction Labels (N, S, E, W) overlay inside rotating rose
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedCompassRotation)
                    ) {
                        Text(
                            text = "N",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                        )
                        Text(
                            text = "S",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)
                        )
                        Text(
                            text = "E",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)
                        )
                        Text(
                            text = "W",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp)
                        )
                    }

                    // Rotating Qibla Indicator Arrow pointing to Kaaba
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedCompassRotation + qiblaBearing.toFloat())
                    ) {
                        // Kaaba icon needle at Qibla angle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAligned) Color(0xFFE6A100) else Color(0xFF006D44),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🕋", fontSize = 20.sp)
                                }
                            }
                            Canvas(modifier = Modifier.size(16.dp, 28.dp)) {
                                val p = Path().apply {
                                    moveTo(size.width / 2, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(p, color = if (isAligned) Color(0xFFE6A100) else Color(0xFF006D44))
                            }
                        }
                    }

                    // Center Hub showing angle
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFFE6A100)))),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${qiblaBearing.roundToInt()}°",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Qibla Angle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Map / Vector Direction Fallback when hardware sensor is missing
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExploreOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Magnetic Compass Sensor Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This device lacks physical magnetometer/rotation sensors. Displaying mathematical bearing to Kaaba:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Qibla Bearing: ${qiblaBearing.roundToInt()}° from True North",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Metrics Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real-Time Directional Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Kaaba Bearing (True)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%.1f°", qiblaBearing),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text("Current Heading", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%.1f°", trueHeading),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("Magnetic Declination", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%+.1f°", magneticDeclination),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC88A00)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sensor: $sensorTypeUsed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Kaaba: 21.4225° N, 39.8262° E",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityPicker) {
        val filteredCities = remember(citySearchQuery) {
            if (citySearchQuery.isBlank()) {
                AppLocationManager.PRESET_CITIES
            } else {
                AppLocationManager.PRESET_CITIES.filter {
                    it.nameEn.contains(citySearchQuery, ignoreCase = true) ||
                            it.nameUr.contains(citySearchQuery) ||
                            it.country.contains(citySearchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showCityPicker = false
                citySearchQuery = ""
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Location Worldwide", fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        showCityPicker = false
                        showManualCoordsDialog = true
                    }) {
                        Icon(Icons.Default.PinDrop, contentDescription = "Custom Coordinates", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        placeholder = { Text("Search city or country worldwide...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (citySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { citySearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(filteredCities) { city ->
                            val isSel = (userLocation.cityName == city.nameEn)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AppLocationManager.selectPresetCity(context, city)
                                        showCityPicker = false
                                        citySearchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${city.nameEn} (${city.country})",
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${city.nameUr} • Lat: ${city.latitude}, Lon: ${city.longitude}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCityPicker = false
                    citySearchQuery = ""
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Manual Coordinates Dialog
    if (showManualCoordsDialog) {
        var latInput by remember { mutableStateOf(userLocation.latitude.toString()) }
        var lonInput by remember { mutableStateOf(userLocation.longitude.toString()) }
        var cityInput by remember { mutableStateOf(userLocation.cityName) }

        AlertDialog(
            onDismissRequest = { showManualCoordsDialog = false },
            title = { Text("Manual Coordinate Entry", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("City Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text("Latitude (-90 to +90)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lonInput,
                        onValueChange = { lonInput = it },
                        label = { Text("Longitude (-180 to +180)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = latInput.toDoubleOrNull()
                        val lon = lonInput.toDoubleOrNull()
                        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                            AppLocationManager.setManualCoordinates(
                                context,
                                lat,
                                lon,
                                cityInput,
                                "Custom",
                                userLocation.timeZoneId
                            )
                            showManualCoordsDialog = false
                        }
                    }
                ) {
                    Text("Apply Coordinates")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualCoordsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Low-pass filter for smooth compass rotation
private fun lowPass(current: Float, previous: Float): Float {
    val alpha = 0.25f
    var diff = current - previous
    while (diff < -180f) diff += 360f
    while (diff > 180f) diff -= 360f
    return (previous + alpha * diff + 360f) % 360f
}
