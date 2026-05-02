package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.elitedarkkaiser.redmagic.state.LedState

@Suppress("DEPRECATION")
class CallLightingService : Service() {

    private var telephonyManager: TelephonyManager? = null
    private var lastState: Int = TelephonyManager.CALL_STATE_IDLE

    private val phoneListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            handleCallState(state)
        }
    }

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        telephonyManager?.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleCallState(lastState)
        return START_STICKY
    }

    override fun onDestroy() {
        telephonyManager?.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleCallState(state: Int) {
        lastState = state

        if (!CallLightingState.isEnabled(this)) {
            CallLightingState.setActive(this, false)
            return
        }

        if (ChargingLedState.isActive(this)) {
            CallLightingState.setActive(this, false)
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                CallLightingState.setActive(this, true)
                applyIncomingProfile()
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                CallLightingState.setActive(this, true)
                applyConnectedProfile()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (CallLightingState.isActive(this)) {
                    CallLightingState.setActive(this, false)
                    restorePreviousLedOwner()
                }
            }
        }
    }

    private fun applyIncomingProfile() {
        applyProfile(
            fan = CallLightingState.readLed(
                this,
                CallLightingState.INCOMING_FAN_ENABLED_KEY,
                CallLightingState.INCOMING_FAN_EFFECT_KEY,
                CallLightingState.INCOMING_FAN_COLOR_KEY,
                true,
                "flashing",
                5
            ),
            logo = CallLightingState.readLed(
                this,
                CallLightingState.INCOMING_LOGO_ENABLED_KEY,
                CallLightingState.INCOMING_LOGO_EFFECT_KEY,
                CallLightingState.INCOMING_LOGO_COLOR_KEY,
                true,
                "flashing",
                1
            ),
            shoulder = CallLightingState.readLed(
                this,
                CallLightingState.INCOMING_SHOULDER_ENABLED_KEY,
                CallLightingState.INCOMING_SHOULDER_EFFECT_KEY,
                CallLightingState.INCOMING_SHOULDER_COLOR_KEY,
                true,
                "flashing",
                8
            )
        )
    }

    private fun applyConnectedProfile() {
        applyProfile(
            fan = CallLightingState.readLed(
                this,
                CallLightingState.CONNECTED_FAN_ENABLED_KEY,
                CallLightingState.CONNECTED_FAN_EFFECT_KEY,
                CallLightingState.CONNECTED_FAN_COLOR_KEY,
                true,
                "steady",
                5
            ),
            logo = CallLightingState.readLed(
                this,
                CallLightingState.CONNECTED_LOGO_ENABLED_KEY,
                CallLightingState.CONNECTED_LOGO_EFFECT_KEY,
                CallLightingState.CONNECTED_LOGO_COLOR_KEY,
                true,
                "steady",
                1
            ),
            shoulder = CallLightingState.readLed(
                this,
                CallLightingState.CONNECTED_SHOULDER_ENABLED_KEY,
                CallLightingState.CONNECTED_SHOULDER_EFFECT_KEY,
                CallLightingState.CONNECTED_SHOULDER_COLOR_KEY,
                true,
                "steady",
                8
            )
        )
    }

    private fun applyProfile(fan: LedState, logo: LedState, shoulder: LedState) {
        if (fan.enabled) {
            if (fan.effect.startsWith("preset:")) {
                HardwareController.setFanLedEnabled(true)
                HardwareController.setFanLedStockPreset(fan.effect.removePrefix("preset:"))
            } else {
                HardwareController.setFanLedEffect(fan.effect, fan.color)
            }
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (logo.enabled) {
            HardwareController.setLogoLedEffect(logo.effect, logo.color)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulder.enabled) {
            HardwareController.setShoulderLedEffect(shoulder.effect, shoulder.color)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }
    }

    private fun restorePreviousLedOwner() {
        if (ChargingLedState.isEnabled(this) && ChargingLedState.isChargingNow(this)) {
            ChargingLedState.setActive(this, true)
            ChargingLedState.applyChargingProfile(this)
            return
        }

        GameModeActions.startServiceSilentlyIfPermitted(this)
        HardwareServiceActions.startFanLed(this)
    }
}
