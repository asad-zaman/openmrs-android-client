package org.openmrs.mobile.webrtc.service

import com.github.ajalt.timberkt.Timber
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.openmrs.mobile.webrtc.receiver.FCMNotificationReceiver
import org.intelehealth.fcm.FBMessageService
import org.intelehealth.klivekit.utils.FirebaseUtils
import org.openmrs.mobile.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMMessageService : FBMessageService(FCMNotificationReceiver::class.java) {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d { "onNewToke ---> $token" }
        // save fcm reg. token for chat (Video)
        val sessionManager = SessionManager(this)
        FirebaseUtils.saveToken(
            this,
            sessionManager.providerID,
            token,
            sessionManager.appLanguage
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d { "Remote message ${Gson().toJson(message)}" }
    }
}