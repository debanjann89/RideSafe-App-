package com.example.ridesafeautoreply.service

import android.app.*
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telecom.TelecomManager
import android.content.pm.PackageManager
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ridesafeautoreply.MainActivity
import com.example.ridesafeautoreply.data.Contact
import com.example.ridesafeautoreply.data.RideSession
import com.example.ridesafeautoreply.data.SettingsRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlin.math.sqrt

class RideSafeService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "RideSafeService"
        private const val NOTIFICATION_CHANNEL_ID = "ridesafe_protection_channel"
        private const val NOTIFICATION_ID = 8847
        
        // Static State Flows for Compose UI to bind to instantly without binder boilerplate
        val currentSpeedFlow = MutableStateFlow(0f)          // Speed in km/h
        val isRidingFlow = MutableStateFlow(false)           // Is currently moving above threshold
        val isServiceRunningFlow = MutableStateFlow(false)   // Is the background protection service active
        val sessionDistanceFlow = MutableStateFlow(0.0)      // Current ride distance in km
        val sessionDurationFlow = MutableStateFlow(0L)        // Current ride duration in seconds
    }

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var statsJob: Job? = null
    
    // Call intercepting variables
    private var phoneCallReceiver: PhoneCallReceiver? = null
    private var pendingReplyJob: Job? = null
    private val lastRepliedNumbers = mutableMapOf<String, Long>() // Duplicate prevention map: Number -> timestamp
    
    // Speed smoothing window
    private val speedWindow = mutableListOf<Float>()
    private var lastLocation: Location? = null
    private var lastStatsUpdateTime = 0L
    
    // Emergency tracking
    private var lastEmergencySentTime = 0L
    
    // AI accelerometer profile variables
    private var accelVariance = 0f
    private val accelWindow = mutableListOf<Float>()
    private var isBluetoothConnected = false
    private var stationaryTicks = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service starting...")
        settingsRepository = SettingsRepository(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Sensor setup for AI Smart Mode
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        createNotificationChannel()
        isServiceRunningFlow.value = true
        lastStatsUpdateTime = System.currentTimeMillis()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Protection service active")
        
        val notification = createNotification("RideSafe protection active", "Speed monitoring active.")
        
        // Start foreground with appropriate type for Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        startLocationUpdates()
        registerPhoneCallReceiver()
        startSensorsAndBluetoothChecks()
        startStatsTimer()
        
        // Send immediate test emergency SMS if in Test Mode and Emergency Tracking is active
        serviceScope.launch {
            try {
                val isTest = settingsRepository.isTestModeEnabled.first()
                val isEmergency = settingsRepository.isEmergencyTrackingEnabled.first()
                if (isTest && isEmergency) {
                    val familyContacts = settingsRepository.emergencyContacts.first()
                    if (familyContacts.isNotEmpty()) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                serviceScope.launch {
                                    val lat = location?.latitude ?: 0.0
                                    val lng = location?.longitude ?: 0.0
                                    
                                    val mapsLink = if (lat != 0.0 && lng != 0.0) {
                                        "https://maps.google.com/?q=$lat,$lng"
                                    } else {
                                        "https://maps.google.com/?q=12.9716,77.5946 (Initializing GPS...)"
                                    }
                                    
                                    val message = "RideSafe Test Alert: This is an immediate test of Emergency GPS Tracking. Current location: $mapsLink"
                                    sendSmsToContacts(familyContacts, message)
                                    Log.d(TAG, "Sent test emergency tracking SMS to ${familyContacts.size} contacts: $mapsLink")
                                }
                            }.addOnFailureListener {
                                serviceScope.launch {
                                    val message = "RideSafe Test Alert: This is an immediate test of Emergency GPS Tracking. Current location link: https://maps.google.com/?q=12.9716,77.5946 (GPS Failure)"
                                    sendSmsToContacts(familyContacts, message)
                                }
                            }
                        } catch (se: SecurityException) {
                            Log.e(TAG, "Location permission missing during test emergency tracking: ${se.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send immediate test emergency SMS: ${e.message}")
            }
        }
        
        // Keep service running unless explicitly stopped
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "RideSafe Protection Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active protection status of RideSafe Auto Reply."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action button to stop protection directly from notification
        val stopIntent = Intent(this, RideSafeService::class.java).apply {
            action = "ACTION_STOP_PROTECTION"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Standard system icon, beautiful M3 style
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Protection", stopPendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification("RideSafe protection active", text))
    }

    private fun startLocationUpdates() {
        // High accuracy with 1 second updates (fully real-time tracking)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    processLocationUpdate(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing: ${e.message}")
            stopSelf()
        }
    }

    private fun processLocationUpdate(location: Location) {
        // If accuracy is very poor (>35m) AND there's no Doppler speed signal, ignore coordinates
        if (location.accuracy > 35f && !location.hasSpeed()) {
            Log.d(TAG, "processLocationUpdate: Bad GPS accuracy and no speed signal, ignoring.")
            return
        }

        var speedMs = if (location.hasSpeed() && location.speed > 0.05f) location.speed else -1f

        // FALLBACK: Calculate speed from distance & time difference if Doppler GPS speed is missing
        if (speedMs < 0f) {
            val prevLoc = lastLocation
            if (prevLoc != null) {
                val timeDiffMs = location.time - prevLoc.time
                // Only compute if time gap is reasonable (0.5s to 5s) to avoid huge division noise
                if (timeDiffMs in 500..5000) {
                    val distanceM = location.distanceTo(prevLoc)
                    // Only trust distance if accuracy of both samples is decent (< 30m)
                    if (location.accuracy < 30f && prevLoc.accuracy < 30f) {
                        speedMs = distanceM / (timeDiffMs / 1000f)
                        Log.d(TAG, "processLocationUpdate: Calculated speed: $speedMs m/s ($distanceM meters in $timeDiffMs ms)")
                    }
                }
            }
        }

        // Default to 0 if still negative
        if (speedMs < 0f) {
            speedMs = 0f
        }

        val rawSpeedKmh = speedMs * 3.6f

        // Detect false GPS spikes (e.g. jump of > 120 km/h in 1 second is physically impossible for a bike)
        val previousSpeed = currentSpeedFlow.value
        if (previousSpeed > 0f && (rawSpeedKmh - previousSpeed) > 120f) {
            Log.d(TAG, "processLocationUpdate: GPS speed spike ignored ($rawSpeedKmh km/h)")
            return
        }

        // Exponential moving average: 85% current, 15% previous (instant response + minor jitter filtering)
        val smoothedSpeed = if (previousSpeed == 0f) {
            rawSpeedKmh
        } else {
            (rawSpeedKmh * 0.85f) + (previousSpeed * 0.15f)
        }

        currentSpeedFlow.value = smoothedSpeed
        Log.d(TAG, "processLocationUpdate: Raw: $rawSpeedKmh km/h, Smoothed: $smoothedSpeed km/h, HasSpeed: ${location.hasSpeed()}")

        serviceScope.launch {
            val threshold = settingsRepository.speedThreshold.first()
            val aiEnabled = settingsRepository.isAiSmartModeEnabled.first()
            
            // Riding decision logic
            val isRidingSignal = if (aiEnabled) {
                // If AI mode active, incorporate speed + vibration (smoothed via EMA) + Bluetooth connection
                val hasVibration = accelVariance > 1.0f // Highly stabilized threshold
                val bluetoothConnected = isBluetoothConnected
                
                if (smoothedSpeed >= threshold) {
                    true // High speed always indicates riding
                } else if (smoothedSpeed > (threshold * 0.5f) && (hasVibration || bluetoothConnected)) {
                    true // Marginally lower speed but vibrating or BT headset active
                } else {
                    false
                }
            } else {
                smoothedSpeed >= threshold
            }

            // Enforce a 5-second transition delay (debounce buffer) when stopping to prevent flicker
            if (isRidingSignal) {
                stationaryTicks = 0
                isRidingFlow.value = true
            } else {
                stationaryTicks++
                if (stationaryTicks >= 5) {
                    isRidingFlow.value = false
                }
            }

            val isRiding = isRidingFlow.value

            // Update stats if user is riding
            if (isRiding && lastLocation != null) {
                val distanceM = location.distanceTo(lastLocation!!)
                val distanceKm = distanceM / 1000.0
                
                sessionDistanceFlow.value += distanceKm
                
                val currentTime = System.currentTimeMillis()
                val elapsedMs = currentTime - lastStatsUpdateTime
                val elapsedHours = elapsedMs / (1000.0 * 60.0 * 60.0)
                
                settingsRepository.addRideStats(distanceKm, elapsedHours)
                lastStatsUpdateTime = currentTime
            } else {
                lastStatsUpdateTime = System.currentTimeMillis() // Reset timer anchor when stopped
            }

            lastLocation = location

            // Update persistent notification text
            val statusText = if (isRiding) {
                "Riding active at %.1f km/h.".format(smoothedSpeed)
            } else {
                "Speed is %.1f km/h (Stationary).".format(smoothedSpeed)
            }
            updateNotification(statusText)

            // Trigger Emergency family tracking updates if riding
            if (isRiding) {
                checkAndSendEmergencyTracking(location)
            }
        }
    }

    private suspend fun checkAndSendEmergencyTracking(location: Location) {
        val trackingEnabled = settingsRepository.isEmergencyTrackingEnabled.first()
        if (!trackingEnabled) return

        val currentTime = System.currentTimeMillis()
        // Send every 30 minutes (30 * 60 * 1000)
        if (currentTime - lastEmergencySentTime >= 30 * 60 * 1000) {
            val familyContacts = settingsRepository.emergencyContacts.first()
            if (familyContacts.isEmpty()) return

            val mapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            val message = "RideSafe Emergency Alert: I am riding my bike and sharing my live location: $mapsLink"

            sendSmsToContacts(familyContacts, message)
            lastEmergencySentTime = currentTime
            Log.d(TAG, "checkAndSendEmergencyTracking: GPS update sent to ${familyContacts.size} family contacts.")
        }
    }

    private fun sendSmsToContacts(contacts: List<Contact>, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            for (contact in contacts) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null)
                Log.d(TAG, "sendSmsToContacts: Sent SMS to ${contact.name} (${contact.phoneNumber})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendSmsToContacts failed: ${e.message}")
        }
    }

    private fun startSensorsAndBluetoothChecks() {
        serviceScope.launch {
            val aiEnabled = settingsRepository.isAiSmartModeEnabled.first()
            if (aiEnabled) {
                // Register Accelerometer
                accelerometer?.let {
                    sensorManager?.registerListener(
                        this@RideSafeService,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }

                // Check Bluetooth status periodically
                serviceScope.launch(Dispatchers.IO) {
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    while (isActive) {
                        isBluetoothConnected = audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn
                        delay(10000) // check every 10 seconds
                    }
                }
            }
        }
    }

    private fun startStatsTimer() {
        sessionDurationFlow.value = 0L
        sessionDistanceFlow.value = 0.0
        
        statsJob = serviceScope.launch {
            while (isActive) {
                delay(1000) // tick every second
                if (isRidingFlow.value) {
                    sessionDurationFlow.value += 1
                }
            }
        }
    }

    private fun registerPhoneCallReceiver() {
        phoneCallReceiver = PhoneCallReceiver()
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(phoneCallReceiver, filter)
        Log.d(TAG, "registerPhoneCallReceiver: Call receiver dynamically mounted.")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Service destroying, saving session.")
        isServiceRunningFlow.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        phoneCallReceiver?.let {
            unregisterReceiver(it)
        }
        
        sensorManager?.unregisterListener(this)
        statsJob?.cancel()

        // Compile final Ride Session stats and save to DataStore
        val distance = sessionDistanceFlow.value
        val duration = sessionDurationFlow.value
        if (distance > 0.02 || duration > 10L) { // Save session if moved > 20 meters or rode > 10 seconds
            val avgSpeed = if (duration > 0) (distance / (duration / 3600.0)) else 0.0
            val endLat = lastLocation?.latitude ?: 0.0
            val endLng = lastLocation?.longitude ?: 0.0
            
            runBlocking {
                val session = RideSession(
                    id = java.util.UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = duration,
                    distanceKm = distance,
                    avgSpeedKmh = avgSpeed,
                    endLatitude = endLat,
                    endLongitude = endLng
                )
                settingsRepository.saveRideSession(session)
                Log.d(TAG, "onDestroy: Logged completed ride session: $session")
            }
        }
        
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // SensorEventListener Overrides (AI Vibrations analysis)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // Acceleration vector magnitude
        val magnitude = sqrt(x * x + y * y + z * z)
        
        accelWindow.add(magnitude)
        if (accelWindow.size > 50) {
            accelWindow.removeAt(0)
        }
        
        if (accelWindow.size == 50) {
            val mean = accelWindow.average()
            val variance = accelWindow.map { (it - mean) * (it - mean) }.sum() / 50f
            val rawVariance = variance.toFloat()
            // Smooth variance using an EMA low-pass filter (15% raw sample, 85% smoothed history)
            accelVariance = if (accelVariance == 0f) rawVariance else (rawVariance * 0.15f) + (accelVariance * 0.85f)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Inner Phone Call Receiver class
    private inner class PhoneCallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            Log.d(TAG, "PhoneCallReceiver: Incoming State: $stateStr")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (incomingNumber != null) {
                        Log.d(TAG, "Ringing call from: $incomingNumber")
                        handleIncomingCall(incomingNumber)
                    } else {
                        Log.d(TAG, "Ringing call detected but number is null (due to Android security restrictions).")
                        // In some Android versions, incoming number is null in manifest receivers but populated in dynamic ones
                        // Check if we can extract it or log it
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call was answered, rejected, or missed
                    cancelAutoReply()
                }
            }
        }
    }

    private fun handleIncomingCall(phoneNumber: String) {
        cancelAutoReply()
        
        // Start 5-second timer. If call is still ringing after 5 seconds, trigger auto reply
        pendingReplyJob = serviceScope.launch {
            delay(5000)
            
            // Double-check if we are active and speed conditions are met
            val threshold = settingsRepository.speedThreshold.first()
            val speedKmh = currentSpeedFlow.value
            val isRiding = isRidingFlow.value
            val isTestMode = settingsRepository.isTestModeEnabled.first()
            
            Log.d(TAG, "Ringing timer finished. Speed: $speedKmh km/h, riding: $isRiding, testMode: $isTestMode")
            
            if (isTestMode || isRiding || speedKmh >= threshold) {
                evaluateAndTriggerSms(phoneNumber)
            } else {
                Log.d(TAG, "Auto SMS skipped: Speed is below threshold (%.1f / %.1f)".format(speedKmh, threshold))
            }
        }
    }

    private fun cancelAutoReply() {
        pendingReplyJob?.let {
            if (it.isActive) {
                Log.d(TAG, "Call answered or hung up before 5 seconds. Cancelling auto reply.")
                it.cancel()
            }
        }
        pendingReplyJob = null
    }

    private suspend fun evaluateAndTriggerSms(phoneNumber: String) {
        // 1. Duplicate Prevention: 30 minutes window (Bypassed in Test Mode)
        val isTestMode = settingsRepository.isTestModeEnabled.first()
        val lastSent = lastRepliedNumbers[phoneNumber] ?: 0L
        val currentTime = System.currentTimeMillis()
        
        if (!isTestMode && (currentTime - lastSent < 30 * 60 * 1000)) {
            Log.d(TAG, "Duplicate Prevention active: SMS recently sent to $phoneNumber (Skipping).")
            return
        }

        // 2. Contacts selection filter
        val replyToSelectedOnly = settingsRepository.isSelectedContactsOnly.first()
        val whitelisted = settingsRepository.whitelistedContacts.first()
        
        if (replyToSelectedOnly) {
            val isWhitelisted = whitelisted.any { contact ->
                PhoneNumberUtils.compare(phoneNumber, contact.phoneNumber)
            }
            if (!isWhitelisted) {
                Log.d(TAG, "SMS skipped: $phoneNumber is NOT in selected whitelisted contacts.")
                return
            }
        }

        // 3. Fetch custom message
        val customMsg = settingsRepository.autoReplyMessage.first()
                
        // 4. Send SMS
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val parts = smsManager.divideMessage(customMsg)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            
            // Record timestamp for duplicate prevention
            lastRepliedNumbers[phoneNumber] = currentTime
            
            Log.d(TAG, "Success! Automatically replied with SMS to $phoneNumber")
            
            // Auto Cut / Reject Call instantly
            endRingingCall()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending auto-reply SMS: ${e.message}")
        }
    }

    private fun endRingingCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                @Suppress("DEPRECATION")
                val ended = telecomManager?.endCall()
                Log.d(TAG, "endRingingCall: Call ended programmatically: $ended")
            } else {
                Log.d(TAG, "endRingingCall: ANSWER_PHONE_CALLS permission not granted.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "endRingingCall failed: ${e.message}")
        }
    }
}
