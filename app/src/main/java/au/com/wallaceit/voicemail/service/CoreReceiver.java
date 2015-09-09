
package au.com.wallaceit.voicemail.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import au.com.wallaceit.voicemail.VisualVoicemail;
import com.fsck.k9.mail.power.TracingPowerManager;
import com.fsck.k9.mail.power.TracingPowerManager.TracingWakeLock;

public class CoreReceiver extends BroadcastReceiver {

    public static final String WAKE_LOCK_RELEASE = "au.com.wallaceit.voicemail.service.CoreReceiver.wakeLockRelease";

    public static final String WAKE_LOCK_ID = "au.com.wallaceit.voicemail.service.CoreReceiver.wakeLockId";

    private static ConcurrentHashMap<Integer, TracingWakeLock> wakeLocks = new ConcurrentHashMap<Integer, TracingWakeLock>();
    private static AtomicInteger wakeLockSeq = new AtomicInteger(0);

    private static Integer getWakeLock(Context context) {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(context);
        TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreReceiver getWakeLock");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(VisualVoicemail.BOOT_RECEIVER_WAKE_LOCK_TIMEOUT);
        Integer tmpWakeLockId = wakeLockSeq.getAndIncrement();
        wakeLocks.put(tmpWakeLockId, wakeLock);
        if (VisualVoicemail.DEBUG)
            Log.v(VisualVoicemail.LOG_TAG, "CoreReceiver Created wakeLock " + tmpWakeLockId);
        return tmpWakeLockId;
    }

    private static void releaseWakeLock(Integer wakeLockId) {
        if (wakeLockId != null) {
            TracingWakeLock wl = wakeLocks.remove(wakeLockId);
            if (wl != null) {
                if (VisualVoicemail.DEBUG)
                    Log.v(VisualVoicemail.LOG_TAG, "CoreReceiver Releasing wakeLock " + wakeLockId);
                wl.release();
            } else {
                Log.w(VisualVoicemail.LOG_TAG, "BootReceiver WakeLock " + wakeLockId + " doesn't exist");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer tmpWakeLockId = au.com.wallaceit.voicemail.service.CoreReceiver.getWakeLock(context);
        try {
            if (VisualVoicemail.DEBUG)
                Log.i(VisualVoicemail.LOG_TAG, "CoreReceiver.onReceive" + intent);
            if (au.com.wallaceit.voicemail.service.CoreReceiver.WAKE_LOCK_RELEASE.equals(intent.getAction())) {
                Integer wakeLockId = intent.getIntExtra(WAKE_LOCK_ID, -1);
                if (wakeLockId != -1) {
                    if (VisualVoicemail.DEBUG)
                        Log.v(VisualVoicemail.LOG_TAG, "CoreReceiver Release wakeLock " + wakeLockId);
                    au.com.wallaceit.voicemail.service.CoreReceiver.releaseWakeLock(wakeLockId);
                }
            } else {
                tmpWakeLockId = receive(context, intent, tmpWakeLockId);
            }
        } finally {
            au.com.wallaceit.voicemail.service.CoreReceiver.releaseWakeLock(tmpWakeLockId);
        }
    }

    public Integer receive(Context context, Intent intent, Integer wakeLockId) {
        return wakeLockId;
    }

    public static void releaseWakeLock(Context context, int wakeLockId) {
        if (VisualVoicemail.DEBUG)
            Log.v(VisualVoicemail.LOG_TAG, "CoreReceiver Got request to release wakeLock " + wakeLockId);
        Intent i = new Intent();
        i.setClass(context, au.com.wallaceit.voicemail.service.CoreReceiver.class);
        i.setAction(WAKE_LOCK_RELEASE);
        i.putExtra(WAKE_LOCK_ID, wakeLockId);
        context.sendBroadcast(i);
    }
}
