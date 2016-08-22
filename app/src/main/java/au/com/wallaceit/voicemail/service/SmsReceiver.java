package au.com.wallaceit.voicemail.service;

/**
 * Created by michael on 8/03/15.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.setup.AccountSettings;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "SMS broadcast received...");
        boolean isEnabled = Preferences.getPreferences(context).getStorage().getInt(AccountSettings.PREFERENCE_AUTO_CHECK, 1) == AccountSettings.PREFERENCE_AUTO_CHECK_SMS;
        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "SMS detection enabled: " + isEnabled);
        if (isEnabled) {
            String providerSms = Preferences.getPreferences(context).getAccounts().get(0).getProvider().notifySmsNumber;

            // Get sender number
            final Bundle bundle = intent.getExtras();

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                String phoneNumber;

                for (Object aPdusObj : pdusObj) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    if (phoneNumber != null && providerSms.equals(phoneNumber)) {
                        if (VisualVoicemail.DEBUG)
                            Log.i(VisualVoicemail.LOG_TAG, "SMS provider match, checking for voicemails: " + phoneNumber);
                        MailService.actionCheck(context, null, true);
                    } else {
                        if (VisualVoicemail.DEBUG)
                            Log.i(VisualVoicemail.LOG_TAG, "No SMS provider match: " + phoneNumber);
                    }
                }
            }
        }
    }
}
