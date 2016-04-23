package au.com.wallaceit.voicemail.service;
/*
 * Copyright 2013 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of Visual Voicemail.
 *
 * Visual Voicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Visual Voicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Visual Voicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.setup.AccountSettings;

public class MissedCallReceiver extends BroadcastReceiver {

    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent){

        // Get current phone state
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (VisualVoicemail.DEBUG)
            Log.w(VisualVoicemail.LOG_TAG, "Call state broadcast received: " + state);

        if(state==null)
            return;

        // phone is idle, if last state is ringing we have a missed call
        if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) && state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            if (VisualVoicemail.DEBUG)
                Log.w(VisualVoicemail.LOG_TAG, "Missed call detected...");
            // TODO: If the broadcast receivers do what they are suppose to (ie activate/deactivate) this may be redundant.
            List<Account> accounts = Preferences.getPreferences(context).getAccounts();
            if (accounts.size()<1)
                return;
            boolean isEnabled = accounts.get(0).getAutomaticCheckMethod() == AccountSettings.PREFERENCE_AUTO_CHECK_MISSED_CALL;
            if (isEnabled) {
                if (VisualVoicemail.DEBUG)
                    Log.w(VisualVoicemail.LOG_TAG, "Missed call check enabled, scheduling check");
                // setup intent for mail check, alarm service triggers the check directly
                Intent mintent = new Intent(context, MailService.class);
                mintent.setAction(MailService.ACTION_CHECK_MAIL);
                mintent.putExtra(MailService.FLAG_FORCE_CHECK, true);
                PendingIntent pintent = PendingIntent.getService(context, 0, mintent, 0);

                AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+240000, pintent); // takes a while for voicemail to come through.
            }
        }
        lastState = state;
    }
}