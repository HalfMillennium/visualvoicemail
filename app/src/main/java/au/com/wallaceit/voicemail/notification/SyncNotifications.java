package au.com.wallaceit.voicemail.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.R;
import com.fsck.k9.mail.Folder;

import static au.com.wallaceit.voicemail.notification.NotificationController.NOTIFICATION_LED_BLINK_FAST;


class SyncNotifications {
    private static final boolean NOTIFICATION_LED_WHILE_SYNCING = false;


    private final NotificationController controller;
    private final NotificationActionCreator actionBuilder;


    public SyncNotifications(NotificationController controller, NotificationActionCreator actionBuilder) {
        this.controller = controller;
        this.actionBuilder = actionBuilder;
    }

    public void clearSendingNotification(Account account) {
        int notificationId = NotificationIds.getFetchingMailNotificationId(account);
        getNotificationManager().cancel(notificationId);
    }

    public void showFetchingMailNotification(Account account, Folder folder) {
        String accountName = account.getDescription();
        String folderName = folder.getName();

        Context context = controller.getContext();
        String tickerText = context.getString(R.string.notification_bg_sync_ticker, accountName);
        String title = context.getString(R.string.notification_bg_sync_title);
        //TODO: Use format string from resources
        String text = accountName + context.getString(R.string.notification_bg_title_separator) + folderName;

        int notificationId = NotificationIds.getFetchingMailNotificationId(account);
        PendingIntent showMessageListPendingIntent = actionBuilder.createViewFolderPendingIntent(
                account, folderName, notificationId);

        NotificationCompat.Builder builder = controller.createNotificationBuilder()
                .setSmallIcon(R.drawable.ic_notify_check_mail)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setTicker(tickerText)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(showMessageListPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (NOTIFICATION_LED_WHILE_SYNCING) {
            controller.configureNotification(builder, null, null,
                    account.getNotificationSetting().getLedColor(),
                    NOTIFICATION_LED_BLINK_FAST, true);
        }

        getNotificationManager().notify(notificationId, builder.build());
    }

    public void clearFetchingMailNotification(Account account) {
        int notificationId = NotificationIds.getFetchingMailNotificationId(account);
        getNotificationManager().cancel(notificationId);
    }

    private NotificationManagerCompat getNotificationManager() {
        return controller.getNotificationManager();
    }
}