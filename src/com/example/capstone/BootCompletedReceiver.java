package com.example.capstone;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A boot receiver to ensure that the chat service is running.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    /**
     * Start the chat service on boot.
     * @param context The current context.
     * @param intent The boot completed intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent transportService = new Intent();
        transportService.setAction(IChatService.class.getCanonicalName());
        context.startService(transportService);
        Log.i("BootCompletedReciever", "Started IChatservice");
    }

}
