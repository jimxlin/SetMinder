/*
 * Copyright (c) 2015 Jim X. Lin
 *
 * This file is part of SetMinder.
 *
 *  SetMinder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SetMinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SetMinder.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.miljin.setminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class TimerService extends Service {

    public static final String EXTRA_TIMER_TICK = "com.miljin.setminder.TIMER_TICK";
    public static final String EXTRA_TIMER_DONE = "com.miljin.setminder.TIMER_DONE";

    public static boolean timerServiceTicking = false;

    long seconds;

    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    int notifyID = 1;

    CountDownTimer myCountDownTimer;
    public static MediaPlayer alarmPlayer;
    public static Vibrator vibrator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //keep CPU on while timer is ticking
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        seconds = intent.getLongExtra(MainActivity.EXTRA_START_TIME, 0);

        Intent getActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, getActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //make notification
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(getResources().getString(com.miljin.setminder.R.string.notification_title));
        if (seconds >= 120) {
            notificationBuilder.setContentText( Long.toString(seconds / 60) +
                    getResources().getString(com.miljin.setminder.R.string.notification_text_plural));
        } else {
            notificationBuilder.setContentText(
                    getResources().getString(com.miljin.setminder.R.string.notification_text_minute));
        }
        notificationBuilder.setSmallIcon(com.miljin.setminder.R.drawable.ic_notification);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyID, notificationBuilder.build());

        myCountDownTimer = new CountDownTimer(seconds * 1000, 100) {
            public void onTick(long millisUntilFinished) {
                timerServiceTicking = true;
                broadcastTime(millisUntilFinished);
                sendNotification(millisUntilFinished);
            }

            public void onFinish() {
                timerServiceTicking = false;
                broadcastDone();
                wakeLock.release();
            }
        };
        myCountDownTimer.start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        myCountDownTimer.cancel();
        super.onDestroy();
    }

    public void broadcastTime(long inputMillis) {
        //(re)start main activity in preparation to receive "timer done" intent
        //main activity will come to foreground whether it was paused or destroyed
        //putting this block in broadcastDone() will (re)start the activity BUT the broadcast...
        // ...will NOT get received, therefore finishTimer() will not get called (tested)
        if(inputMillis < 500) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        Intent tickIntent = new Intent(MainActivity.ACTION_RECEIVE_TIMER_TICK);
        tickIntent.putExtra(EXTRA_TIMER_TICK, inputMillis);
        sendBroadcast(tickIntent);
    }

    public void broadcastDone() {
        //Activate alarm when timer is finished, depending on settings
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (settingsPrefs.getBoolean("enable_alarm_vibrate", false)) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] vibratePattern = new long[] {500, 500, 500, 500,
                    2500, 500, 500, 500,
                    2500, 500, 500, 500};
            vibrator.vibrate(vibratePattern, -1);
        }
        if (settingsPrefs.getBoolean("enable_alarm_sound", false)) {
            alarmPlayer = MediaPlayer.create(this, com.miljin.setminder.R.raw.alarm1);
            alarmPlayer.start();
        }

        notificationManager.cancelAll();

        //send broadcast
        Intent tickIntent = new Intent(MainActivity.ACTION_RECEIVE_TIMER_DONE);
        tickIntent.putExtra(EXTRA_TIMER_DONE, true);
        sendBroadcast(tickIntent);

        stopSelf();
    }

    public void sendNotification(long inputMillis) {
        //send notifications at 1 minute intervals
        if ((inputMillis/1000) % 60 == 0) {    //millisUntilFinished aren't multiples of 100ms, need to round
            notificationBuilder.setContentTitle(getResources().
                    getString(com.miljin.setminder.R.string.notification_title));
            if (inputMillis >= 61000) {
                notificationBuilder.setContentText(Long.toString(inputMillis / 60000) +
                        getResources().getString(com.miljin.setminder.R.string.notification_text_plural));
            } else {
                notificationBuilder.setContentText(
                        getResources().getString(com.miljin.setminder.R.string.notification_text_minute));
            }
            notificationBuilder.setSmallIcon(com.miljin.setminder.R.drawable.ic_notification);
            notificationManager.notify(notifyID, notificationBuilder.build());
        }
    }
}
