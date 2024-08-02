package com.vte.timetable

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class WearMessageListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == OPEN_PHONE_APP_PATH) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

    companion object {
        private const val TAG = "WearMessageListener"
        private const val OPEN_PHONE_APP_PATH = "/open-phone-app"
    }
}
