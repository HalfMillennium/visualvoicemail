package au.com.wallaceit.voicemail.helper;

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
 * VvmSmsDetector is an xposed framework module to detect incoming type 0 SMS used by VVM servers for automatic provisioning and sync messages (sms push notifications).
 * Unfortunately Android does not have any native functionality to detect such messages.
 */

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class VvmSmsDetector implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //XposedBridge.log("Loaded app: "+loadPackageParam.packageName);
        if (!loadPackageParam.packageName.equals("com.android.phone"))
            return;

        XposedBridge.log("com.android.phone loaded, hooking SMS dispatch method...");

        String gsmClass, gsmDispatchMethod;
        if (Build.VERSION.SDK_INT < 19) {
            gsmClass = "com.android.internal.telephony.gsm.GsmSMSDispatcher";
            gsmDispatchMethod = "dispatchMessage";
        } else {
            gsmClass = "com.android.internal.telephony.gsm.GsmInboundSmsHandler";
            gsmDispatchMethod = "dispatchMessageRadioSpecific";
        }

        findAndHookMethod(gsmClass, loadPackageParam.classLoader, gsmDispatchMethod, SmsMessageBase.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // this will be called before the sms is processed
                SmsMessage message = (SmsMessage) param.args[0];
                if (message.isTypeZero()){
                    XposedBridge.log("TYPE 0 SMS DETECTED");
                    // Get context
                    Context context = AndroidAppHelper.currentApplication();
                    if (context != null) {
                        Intent smsIntent = new Intent("au.com.wallaceit.voicemail.TYPE0_SMS_RECEIVED");
                        smsIntent.setPackage("au.com.wallaceit.voicemail");
                        smsIntent.putExtra("sender", message.getOriginatingAddress());
                        smsIntent.putExtra("body", message.getMessageBody());
                        context.sendBroadcast(smsIntent);
                    } else {
                        XposedBridge.log("Could not get context for message broadcast");
                    }
                    XposedBridge.log("SMS is type 0 (class "+String.valueOf(message.getMessageClass())+", protocol "+String.valueOf(message.getProtocolIdentifier())+")");
                    XposedBridge.log("Sender: "+message.getOriginatingAddress());
                    XposedBridge.log("Body: "+message.getMessageBody());
                }
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // this will be called after the sms is processed
            }
        });
    }
}