package au.com.wallaceit.voicemail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.setup.AccountSettings;

/**
 * Created by michael on 23/02/15.
 */
public class MissedCallReceiver extends BroadcastReceiver {

    static boolean isRinging=false;
    static boolean isReceived=false;

    @Override
    public void onReceive(Context context, Intent intent){

        // Get current phone state
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "Call state broadcast received: " + state);

        if(state==null)
            return;

        //phone is ringing
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            isRinging =true;
        }

        //phone is received
        if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            isReceived=true;
        }

        // phone is idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            // detect missed call
            if(isRinging && !isReceived){
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "Missed call detected...");
                boolean isEnabled = Preferences.getPreferences(context).getAccounts().get(0).getAutomaticCheckMethod() == AccountSettings.PREFERENCE_AUTO_CHECK_MISSED_CALL;
                if (isEnabled) {
                    if (VisualVoicemail.DEBUG)
                        Log.i(VisualVoicemail.LOG_TAG, "Missed call check enabled, scheduling check");
                    PollTask pollTask = new PollTask(context);
                    Timer timer = new Timer();
                    timer.schedule(pollTask, 180000);
                }

                isRinging = false;
                isReceived = false;
            }
        }
    }

    class PollTask extends TimerTask {
        private Context mContext;

        public PollTask(Context context){
            mContext = context;
        }

        @Override
        public void run() {
            if (VisualVoicemail.DEBUG)
                Log.i(VisualVoicemail.LOG_TAG, "***** Missed Call Receiver *****: checking mail");
            MailService.actionCheck(mContext, null, true);
        }
    }
}