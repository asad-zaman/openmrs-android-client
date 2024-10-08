package org.openmrs.mobile.webrtc.activity

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity

/**
 * Created by Vaghela Mithun R. on 10-10-2023 - 18:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallNotificationTestActivity : ACBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_notification_test)

//        RtcConfig.Builder()
//            .callUrl("wss://" + sessionManager.getServerUrl() + ":9090")
//            .socketUrl(
//                "https://" + sessionManager.getServerUrl() + ":3004" + "?userId="
//                        + sessionManager.getProviderID()
//                        + "&name=" + sessionManager.getChwname()
//            )
//            .callIntentClass(EkalVideoActivity::class.java)
//            .chatIntentClass(EkalChatActivity::class.java)
//            .callLogIntentClass(EkalCoreCallLogActivity::class.java)
//            .build().saveConfig(this)
        findViewById<MaterialButton>(R.id.btnStartCallNotification).setOnClickListener {
            startCall()
        }
    }

    private fun startCall() {
        CallHandlerUtils.operateIncomingCall(
            this,
            RtcArgs.dummy().apply { patientName = "Test User" }
        )
    }
}