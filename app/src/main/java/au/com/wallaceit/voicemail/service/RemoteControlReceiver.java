
package au.com.wallaceit.voicemail.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.remotecontrol.K9RemoteControl;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.service.*;
import au.com.wallaceit.voicemail.service.CoreReceiver;

import java.util.List;

import static au.com.wallaceit.voicemail.remotecontrol.K9RemoteControl.*;

public class RemoteControlReceiver extends CoreReceiver {
    @Override
    public Integer receive(Context context, Intent intent, Integer tmpWakeLockId) {
        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "RemoteControlReceiver.onReceive" + intent);

        if (K9RemoteControl.K9_SET.equals(intent.getAction())) {
            RemoteControlService.set(context, intent, tmpWakeLockId);
            tmpWakeLockId = null;
        } else if (K9RemoteControl.K9_REQUEST_ACCOUNTS.equals(intent.getAction())) {
            try {
                Preferences preferences = Preferences.getPreferences(context);
                List<Account> accounts = preferences.getAccounts();
                String[] uuids = new String[accounts.size()];
                String[] descriptions = new String[accounts.size()];
                for (int i = 0; i < accounts.size(); i++) {
                    //warning: account may not be isAvailable()
                    Account account = accounts.get(i);

                    uuids[i] = account.getUuid();
                    descriptions[i] = account.getDescription();
                }
                Bundle bundle = getResultExtras(true);
                bundle.putStringArray(K9_ACCOUNT_UUIDS, uuids);
                bundle.putStringArray(K9_ACCOUNT_DESCRIPTIONS, descriptions);
            } catch (Exception e) {
                Log.e(VisualVoicemail.LOG_TAG, "Could not handle K9_RESPONSE_INTENT", e);
            }

        }

        return tmpWakeLockId;
    }

}
