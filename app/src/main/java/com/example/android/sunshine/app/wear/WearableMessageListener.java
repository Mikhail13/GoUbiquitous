package com.example.android.sunshine.app.wear;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearableMessageListener extends WearableListenerService {
    private static final String START_PATH = "/start";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (START_PATH.equals(messageEvent.getPath())) {
            startService(new Intent(this, WearableIntentService.class));
        }
    }
}
