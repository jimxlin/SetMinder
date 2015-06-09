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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import com.miljin.setminder.EditTimeDialogFragment.EditTimeDialogListener;
import com.miljin.setminder.EditSetsRepsDialogFragment.EditSetsRepsDialogListener;
import com.miljin.setminder.ResetAlertDialogFragment.ResetAlertListener;
import com.miljin.setminder.FinishAlertDialogFragment.FinishAlertListener;
import com.miljin.setminder.TimeUpAlertDialogFragment.TimeUpAlertListener;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener,
        EditTimeDialogListener, EditSetsRepsDialogListener, ResetAlertListener, FinishAlertListener,
        TimeUpAlertListener {

    public final static String EXTRA_START_TIME = "com.miljin.setminder.START_TIME";
    public final static String ACTION_RECEIVE_TIMER_TICK =
            "com.miljin.setminder.RECEIVE_TIMER_TICK";
    public final static String ACTION_RECEIVE_TIMER_DONE =
            "com.miljin.setminder.RECEIVE_TIMER_DONE";

    //values
    long seconds;
    long millisLeft;
    int[] setsReps = new int[2]; //setsReps[0] is sets, setsReps[1] is reps
    int completedSets;

    //Views that update
    ActionBar actionBar;
    PieButton pieButton;
    SetProgressBar setProgressBar;
    ImageButton resetTimeButton;
    TextView timeTextView;
    TextView setsRepsTextView;

    ///////////////////////
    //Initialize Activity//
    ///////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.miljin.setminder.R.layout.activity_main);

        //create SharedPreferences file for variable values and retrieve values if they exist
        SharedPreferences valuesPrefs = getSharedPreferences(
                getString(com.miljin.setminder.R.string.values_preference_file_key), Context.MODE_PRIVATE);
        seconds = valuesPrefs.getLong(getString(com.miljin.setminder.R.string.saved_time), (long) 0);
        setsReps[0] = valuesPrefs.getInt(getString(com.miljin.setminder.R.string.saved_sets), 0);
        setsReps[1] = valuesPrefs.getInt(getString(com.miljin.setminder.R.string.saved_reps), 0);
        completedSets = valuesPrefs.getInt(getString(com.miljin.setminder.R.string.saved_completed_sets), 0);

        //keep screen on as per settings
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (settingsPrefs.getBoolean("enable_keep_screen_on", true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //initialize views
        timeTextView = (TextView) findViewById(com.miljin.setminder.R.id.timeTextView);
        timeTextView.setText(String.format("%02d" + ":" + "%02d", seconds/60, seconds%60));
        setProgressBar = (SetProgressBar) findViewById(com.miljin.setminder.R.id.setProgressBar);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);
        pieButton = (PieButton) findViewById(com.miljin.setminder.R.id.pieButton);
        resetTimeButton = (ImageButton) findViewById(com.miljin.setminder.R.id.resetTimeButton);
        setsRepsTextView = (TextView) findViewById(com.miljin.setminder.R.id.setsRepsTextView);
        setsRepsTextView.setText(completedSets + "/" + setsReps[0] + " x " + setsReps[1]);

        //in case activity is destroyed but service continues, reset proper button state
        if (TimerService.timerServiceTicking) {
            pieButton.setEnabled(false);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.miljin.setminder.R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.miljin.setminder.R.id.action_settings:
                onShowPreferenceDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //save values to a shared preference file (to restore on activity re-create)
        SharedPreferences valuesPrefs = getSharedPreferences(
                getString(com.miljin.setminder.R.string.values_preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = valuesPrefs.edit();
        editor.putLong(getString(com.miljin.setminder.R.string.saved_time), seconds);
        editor.putInt(getString(com.miljin.setminder.R.string.saved_sets), setsReps[0]);
        editor.putInt(getString(com.miljin.setminder.R.string.saved_reps), setsReps[1]);
        editor.putInt(getString(com.miljin.setminder.R.string.saved_completed_sets), completedSets);
        editor.apply();

        unregisterReceiver(timerServiceReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_TIMER_TICK);
        intentFilter.addAction(ACTION_RECEIVE_TIMER_DONE);
        registerReceiver(timerServiceReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        //remove fragments
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() != 0) {
            fm.popBackStack();
            actionBar = getActionBar();
            assert actionBar != null;
            actionBar.setTitle(com.miljin.setminder.R.string.app_name);
        } else { super.onBackPressed();
            //moveTaskToBack(true); //back button imitates home button, instead of destroying activity
        }
    }

    ////////////////////
    //Start/stop timer//
    ////////////////////

    //Start countdown timer
    public void onClickStartTimer(View v) {
        //disable start timer button, enable reset timer while timer ticks
        if (seconds > 0) {
            pieButton.setEnabled(false);
        }

        //increment sets and update set display
        completedSets++;
        setsRepsTextView.setText(completedSets + "/" + setsReps[0] + " x " + setsReps[1]);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);

        //start timer service, but only for positive time inputs
        if (seconds > 0) {
            Intent timerIntent = new Intent(this, TimerService.class);
            timerIntent.putExtra(EXTRA_START_TIME, seconds);
            startService(timerIntent);
        }

        //Alert that all sets are completed, as per settings
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (settingsPrefs.getBoolean("enable_finish_sets_alert", true)) {
            if (completedSets == setsReps[0]) {
                FragmentManager fm = getFragmentManager();
                FinishAlertDialogFragment finishAlertDialog = FinishAlertDialogFragment.newInstance();
                finishAlertDialog.show(fm, "fragment_finish_alert");
            }
        }
    }

    //receive ticks from timer service
    private BroadcastReceiver timerServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_RECEIVE_TIMER_TICK)) {
                millisLeft = intent.getLongExtra(TimerService.EXTRA_TIMER_TICK, 0);
                updateTimer();
            }
            if(intent.getAction().equals(ACTION_RECEIVE_TIMER_DONE)) {
                finishTimer();
            }
        }
    };

    //Update timer displays
    public void updateTimer() {
        timeTextView.setText(String.format("%02d" + ":" + "%02d",
                (int) Math.floor(millisLeft/60000), (int) Math.floor((millisLeft/1000)%60)));
        pieButton.updatePie(((float) millisLeft / 1000) / seconds);
    }

    //End countdown timer
    public void finishTimer() {
        //enable start timer button
        pieButton.setEnabled(true);

        //reset time displays
        timeTextView.setText(String.format("%02d" + ":" + "%02d", seconds / 60, seconds % 60));
        pieButton.updatePie(1f);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);

        //display "Time's up!" alert dialog, as per settings
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (settingsPrefs.getBoolean("enable_alarm_dismissal", true)) {
            FragmentManager fm = getFragmentManager();
            TimeUpAlertDialogFragment timeUpAlertDialog = TimeUpAlertDialogFragment.newInstance();
            timeUpAlertDialog.show(fm, "fragment_time_up_alert");
        }

        //alarms initialized by the service
    }



    /////////////////
    //Small Buttons//
    /////////////////

    //Input time
    public void onShowTimePickerDialog(View v) {
        FragmentManager fm = getFragmentManager();
        EditTimeDialogFragment editTimeDialogFragment = EditTimeDialogFragment.newInstance(seconds);
        editTimeDialogFragment.show(fm, "fragment_edit_time_dialog");
    }

    //Reset countdown timer
    public void onClickResetTime(View v) {
        stopService(new Intent(this, TimerService.class));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        timeTextView.setText(String.format("%02d" + ":" + "%02d", seconds / 60, seconds % 60));
        pieButton.updatePie(1f);
        pieButton.setEnabled(true);
    }

    //Reset set counter alert
    public void onShowResetAlertDialog(View v) {
        FragmentManager fm = getFragmentManager();
        ResetAlertDialogFragment resetAlertDialog = ResetAlertDialogFragment.newInstance();
        resetAlertDialog.show(fm, "fragment_reset_alert");
    }

    //Input sets and reps
    public void onShowSetsRepsPickerDialog(View v) {
        FragmentManager fm = getFragmentManager();
        EditSetsRepsDialogFragment editSetsRepsDialogFragment =
                EditSetsRepsDialogFragment.newInstance(setsReps);
        editSetsRepsDialogFragment.show(fm, "fragment_edit_sets_reps_dialog");
    }


    //////////////////
    //Dialog Buttons//
    //////////////////

    //update displayed time with user input
    public void onFinishEditTimeDialog(long inputTime) {
        seconds = inputTime;
        timeTextView = (TextView) findViewById(com.miljin.setminder.R.id.timeTextView);
        timeTextView.setText(String.format("%02d" + ":" + "%02d", seconds/60, seconds%60));
    }

    //update displayed sets and reps with user input
    public void onFinishEditSetsRepsDialog(int[] inputSetsReps) {
        setsReps = inputSetsReps;
        setsRepsTextView = (TextView) findViewById(com.miljin.setminder.R.id.setsRepsTextView);
        setsRepsTextView.setText(completedSets + "/" + setsReps[0] + " x " + setsReps[1]);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);
    }

    //reset set counter
    public void onResetOKButton() {
        completedSets = 0;
        setsRepsTextView = (TextView) findViewById(com.miljin.setminder.R.id.setsRepsTextView);
        setsRepsTextView.setText(completedSets + "/" + setsReps[0] + " x " + setsReps[1]);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);
    }

    public void onResetCancelButton() {
    }

    //All sets are done; reset set counter and reset timer
    public void onFinishOKButton() {
        stopService(new Intent(this, TimerService.class));

        completedSets = 0;
        setsRepsTextView = (TextView) findViewById(com.miljin.setminder.R.id.setsRepsTextView);
        setsRepsTextView.setText(completedSets + "/" + setsReps[0] + " x " + setsReps[1]);
        setProgressBar.updateSetProgress(completedSets, setsReps[0]);

        timeTextView.setText(String.format("%02d" + ":" + "%02d", seconds / 60, seconds % 60));
        pieButton.updatePie(1f);

        pieButton.setEnabled(true);
    }

    public void onFinishCancelButton() {
    }

    //Dismiss "Time's up!" alert; will stop alarm/vibration
    public void onTimeUpButton() {
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (settingsPrefs.getBoolean("enable_alarm_vibrate", false)) {
            TimerService.vibrator.cancel();
        }
        if (settingsPrefs.getBoolean("enable_alarm_sound", false)) {
            TimerService.alarmPlayer.stop();
            TimerService.alarmPlayer.release();
        }
    }

    ////////////
    //Settings//
    ////////////

    //Show settings
    public void onShowPreferenceDialog() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        PrefsFragment prefsFragment = new PrefsFragment();
        ft.add(android.R.id.content, prefsFragment).addToBackStack(null).commit();

        actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setTitle(com.miljin.setminder.R.string.settings);
    }

    //preference change listener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //update keep-screen-on status when setting is changed
        if (key.equals("enable_keep_screen_on")) {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (settingsPrefs.getBoolean("enable_keep_screen_on", true)) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }
}
