package io.github.fleetc0m.ttyl.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Icarus on 8/24/14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            Intent backgroundServiceIntent = new Intent(context, BackgroundService.class);
            context.startService(backgroundServiceIntent);
        }
    }
}
