package au.com.wallaceit.voicemail.service;

/*
 * Copyright 2016 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of visualvoicemail.
 *
 * visualvoicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * visualvoicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with visualvoicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by michael on 10/12/16.
 *
 * Process type 0 SMS messages sent from the voicemail server and broadcast by the xposed framwork VvmSmsDetector.class
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.NotificationSetting;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.setup.AccountSettings;
import au.com.wallaceit.voicemail.activity.setup.AccountSetup;

public class Type0SmsReceiver extends BroadcastReceiver {

    public static final String ACTION_TYPE0_SMS_RECEIVED = "au.com.wallaceit.voicemail.TYPE0_SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!ACTION_TYPE0_SMS_RECEIVED.equals(intent.getAction()))
            return;

        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "Type 0 SMS broadcast received...");

        String sender = intent.getStringExtra("sender");
        String body = intent.getStringExtra("body");

        // process message data
        String[] parts = body.split("\\?");
        String cmd = parts[0];
        if (parts.length>1){
            String[] paramArr = parts[1].split(";");
            for (String aParamArr : paramArr) {
                String[] param = aParamArr.split("=");
                if (param.length > 1)
                    intent.putExtra(param[0], param[1]);
            }
        }

        if (cmd.equals("STATE") && intent.hasExtra("pw")){
            Log.w(context.getPackageName(), "SMS contains Voicemail account settings");

            String server = intent.getStringExtra("server");
            String user = intent.getStringExtra("name");
            // Check for existing accounts and try to update
            List<Account> accounts = Preferences.getPreferences(context).getAccounts();
            for (int i =0; i<accounts.size(); i++){
                Account account = accounts.get(i);
                try {
                    URI uri = new URI(account.getStoreUri());
                    if (uri.getHost().equals(server) || uri.getUserInfo().indexOf(user)==0){
                        Log.w(context.getPackageName(), "Existing account found for the specified settings, updating account.");

                        uri = new URI(	uri.getScheme(),
                                user + ":" + intent.getStringExtra("pw"),
                                server,
                                Integer.parseInt(intent.getStringExtra("port")),
                                "",
                                "",
                                "");
                        account.setStoreUri(uri.toString());
                        account.save(Preferences.getPreferences(context));

                        return;
                    }}
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            // No account found, check if activity is open
            if (((VisualVoicemail) context.getApplicationContext()).setupActive){
                autoProvisioningAccountPrompt(context, intent);
            } else {
                Intent i = new Intent(context, AccountSetup.class);
                i.setAction(intent.getAction());
                i.putExtras(intent.getExtras());
                addNotification(context, body, i);
            }

        } else if (cmd.equals("MBOXUPDATE")){
            Log.w(context.getPackageName(), "SMS contains new voicemail notification");
            MailService.actionCheck(context, null, true);
        } else {
            // Display unknown SMS command
            addNotification(context, body, null);
        }
    }

    private void addNotification(Context context, String body, Intent intent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notify_new_voicemail);


        if (intent!=null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent)
            .setContentTitle("VVM Settings SMS received")
            .setContentText("Tap to add an account with these settings.")
            .setSubText(body);
        } else {
            builder.setContentTitle("VVM Type0 SMS detected")
            .setContentText(body)
            .setSubText("This command is unknown");
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(64, builder.build());
    }

    private void autoProvisioningAccountPrompt(Context context, Intent intent){
        Intent i = new Intent(context, AccountSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.setAction(intent.getAction());
        i.putExtras(intent.getExtras());
        context.startActivity(i);
    }
}
