package com.causq.praxis

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import com.causq.praxis.util.CrashHandler
import com.causq.praxis.util.SocketManager
import com.causq.praxis.util.StandbyDetector

/**
 * Application class for Praxis.
 * Manages app-wide resources and lifecycle events, including standby detection
 * to optimize battery usage by pausing background operations when appropriate.
 */
class PraxisApplication : Application(), StandbyDetector.StandbyListener {
    
    private val tag = "PraxisApplication"
    private var standbyDetector: StandbyDetector? = null
    
    // List of listeners that want to be notified of standby changes
    private val standbyListeners = mutableListOf<StandbyListener>()

    lateinit var runtime: PraxisRuntime private set
    lateinit var socketManager: SocketManager private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize crash handler first to capture any crashes during initialization
        CrashHandler.initialize(this)
        
        Log.d(tag, "Praxis application starting")

        // Persist the Settings screen's defaults before anything reads them. Otherwise a
        // setting the user never opened Settings for is read with the code's fallback,
        // which can differ from what the Settings screen shows.
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        // Android creates Application before a system-started VpnService, so this graph is
        // available even when no Activity has been opened in the current process.
        runtime = PraxisRuntime(this)
        socketManager = runtime.socketManager

        // Initialize standby detector
        standbyDetector = StandbyDetector(this, this)
        standbyDetector?.start()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        Log.d(tag, "Praxis application terminating")
        standbyDetector?.stop()
        standbyDetector = null
    }
    
    override fun onEnterStandby() {
        Log.i(tag, "Device entered standby mode - pausing background operations")
        synchronized(standbyListeners) {
            standbyListeners.forEach { it.onEnterStandby() }
        }
    }
    
    override fun onExitStandby() {
        Log.i(tag, "Device exited standby mode - resuming background operations")
        synchronized(standbyListeners) {
            standbyListeners.forEach { it.onExitStandby() }
        }
    }
    
    /**
     * Register a listener to be notified of standby state changes.
     */
    fun registerStandbyListener(listener: StandbyListener) {
        synchronized(standbyListeners) {
            if (!standbyListeners.contains(listener)) {
                standbyListeners.add(listener)
                Log.d(tag, "Registered standby listener: ${listener.javaClass.simpleName}")
            }
        }
    }
    
    /**
     * Unregister a listener from standby state changes.
     */
    fun unregisterStandbyListener(listener: StandbyListener) {
        synchronized(standbyListeners) {
            standbyListeners.remove(listener)
            Log.d(tag, "Unregistered standby listener: ${listener.javaClass.simpleName}")
        }
    }
    
    /**
     * Interface for components that want to be notified of standby state changes.
     */
    interface StandbyListener {
        fun onEnterStandby()
        fun onExitStandby()
    }
}