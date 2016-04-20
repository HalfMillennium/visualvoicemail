package au.com.wallaceit.voicemail.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.activity.MessageReference;
import au.com.wallaceit.voicemail.controller.MessagingController;
import com.fsck.k9.mail.Flag;
import au.com.wallaceit.voicemail.mailstore.LocalMessage;
import au.com.wallaceit.voicemail.service.*;
import au.com.wallaceit.voicemail.service.CoreService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Service called by actions in notifications.
 * Provides a number of default actions to trigger.
 */
public class NotificationActionService extends CoreService {
    private final static String REPLY_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.REPLY_ACTION";
    private final static String READ_ALL_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.READ_ALL_ACTION";
    private final static String DELETE_ALL_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.DELETE_ALL_ACTION";
    private final static String ARCHIVE_ALL_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.ARCHIVE_ALL_ACTION";
    private final static String SPAM_ALL_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.SPAM_ALL_ACTION";
    private final static String ACKNOWLEDGE_ACTION = "au.com.wallaceit.voicemail.service.NotificationActionService.ACKNOWLEDGE_ACTION";

    private final static String EXTRA_ACCOUNT = "account";
    private final static String EXTRA_MESSAGE = "message";
    private final static String EXTRA_MESSAGE_LIST = "messages";

    public static PendingIntent getReplyIntent(Context context, final Account account, final MessageReference ref) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MESSAGE, ref);
        i.setAction(REPLY_ACTION);

        return PendingIntent.getService(context, account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getReadAllMessagesIntent(Context context, final Account account, final Serializable refs) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MESSAGE_LIST, refs);
        i.setAction(READ_ALL_ACTION);

        return PendingIntent.getService(context, account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getAcknowledgeIntent(Context context, final Account account) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.setAction(ACKNOWLEDGE_ACTION);

        return PendingIntent.getService(context, account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent getDeleteAllMessagesIntent(Context context, final Account account, final Serializable refs) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MESSAGE_LIST, refs);
        i.setAction(DELETE_ALL_ACTION);

        return i;
    }

    /**
     * Check if for the given parameters the ArchiveAllMessages intent is possible for Android Wear.
     * (No confirmation on the phone required and moving these messages to the spam-folder possible)<br/>
     * Since we can not show a toast like on the phone screen, we must not offer actions that can not be performed.
     * @see #getArchiveAllMessagesIntent(Context, Account, Serializable)
     * @param context the context to get a {@link MessagingController}
     * @param account the account (must allow moving messages to allow true as a result)
     * @param messages the messages to move to the spam folder (must be synchronized to allow true as a result)
     * @return true if the ArchiveAllMessages intent is available for the given messages
     */
    public static boolean isArchiveAllMessagesWearAvaliable(Context context, final Account account, final LinkedList<LocalMessage> messages) {
        final MessagingController controller = MessagingController.getInstance(context);
        return (account.getArchiveFolderName() != null && !(account.getArchiveFolderName().equals(account.getSpamFolderName()) && VisualVoicemail.confirmSpam()) && isMovePossible(controller, account, account.getSentFolderName(), messages));
    }

    public static PendingIntent getArchiveAllMessagesIntent(Context context, final Account account, final Serializable refs) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MESSAGE_LIST, refs);
        i.setAction(ARCHIVE_ALL_ACTION);

        return PendingIntent.getService(context, account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * Check if for the given parameters the SpamAllMessages intent is possible for Android Wear.
     * (No confirmation on the phone required and moving these messages to the spam-folder possible)<br/>
     * Since we can not show a toast like on the phone screen, we must not offer actions that can not be performed.
     * @see #getSpamAllMessagesIntent(Context, Account, Serializable)
     * @param context the context to get a {@link MessagingController}
     * @param account the account (must allow moving messages to allow true as a result)
     * @param messages the messages to move to the spam folder (must be synchronized to allow true as a result)
     * @return true if the SpamAllMessages intent is available for the given messages
     */
    public static boolean isSpamAllMessagesWearAvaliable(Context context, final Account account, final LinkedList<LocalMessage> messages) {
        final MessagingController controller = MessagingController.getInstance(context);
        return (account.getSpamFolderName() != null && !VisualVoicemail.confirmSpam() && isMovePossible(controller, account, account.getSentFolderName(), messages));
    }

    public static PendingIntent getSpamAllMessagesIntent(Context context, final Account account, final Serializable refs) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.service.NotificationActionService.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MESSAGE_LIST, refs);
        i.setAction(SPAM_ALL_ACTION);

        return PendingIntent.getService(context, account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static boolean isMovePossible(MessagingController controller, Account account, String dstFolder, List<LocalMessage> messages) {
        if (!controller.isMoveCapable(account)) {
            return false;
        }
        if (VisualVoicemail.FOLDER_NONE.equalsIgnoreCase(dstFolder)) {
            return false;
        }
        for(LocalMessage messageToMove : messages) {
            if (!controller.isMoveCapable(messageToMove)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int startService(Intent intent, int startId) {
        if (VisualVoicemail.DEBUG)
            Log.i(VisualVoicemail.LOG_TAG, "NotificationActionService started with startId = " + startId);
        final Preferences preferences = Preferences.getPreferences(this);
        final MessagingController controller = MessagingController.getInstance(getApplication());
        final Account account = preferences.getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        final String action = intent.getAction();

        if (account != null) {
            if (READ_ALL_ACTION.equals(action)) {
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "NotificationActionService marking messages as read");

                List<MessageReference> refs =
                        intent.getParcelableArrayListExtra(EXTRA_MESSAGE_LIST);
                for (MessageReference ref : refs) {
                    controller.setFlag(account, ref.getFolderName(), ref.getUid(), Flag.SEEN, true);
                }
            } else if (DELETE_ALL_ACTION.equals(action)) {
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "NotificationActionService deleting messages");

                List<MessageReference> refs =
                        intent.getParcelableArrayListExtra(EXTRA_MESSAGE_LIST);
                List<LocalMessage> messages = new ArrayList<LocalMessage>();

                for (MessageReference ref : refs) {
                    LocalMessage m = ref.restoreToLocalMessage(this);
                    if (m != null) {
                        messages.add(m);
                    }
                }

                controller.deleteMessages(messages, null);
            } else if (ARCHIVE_ALL_ACTION.equals(action)) {
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "NotificationActionService archiving messages");

                List<MessageReference> refs =
                        intent.getParcelableArrayListExtra(EXTRA_MESSAGE_LIST);
                List<LocalMessage> messages = new ArrayList<LocalMessage>();

                for (MessageReference ref : refs) {
                    LocalMessage m = ref.restoreToLocalMessage(this.getApplicationContext());
                    if (m != null) {
                        messages.add(m);
                    }
                }

                String dstFolder = account.getArchiveFolderName();
                if (dstFolder != null
                        && !(dstFolder.equals(account.getSpamFolderName()) && VisualVoicemail.confirmSpam())
                        && isMovePossible(controller, account, dstFolder, messages)) {
                    for(LocalMessage messageToMove : messages) {
                        if (!controller.isMoveCapable(messageToMove)) {
                            //Toast toast = Toast.makeText(getActivity(), R.string.move_copy_cannot_copy_unsynced_message, Toast.LENGTH_LONG);
                            //toast.show();
                            continue;
                        }
                        String srcFolder = messageToMove.getFolder().getName();
                        controller.moveMessage(account, srcFolder, messageToMove, dstFolder, null);
                    }
                }
            } else if (SPAM_ALL_ACTION.equals(action)) {
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "NotificationActionService moving messages to spam");

                List<MessageReference> refs =
                        intent.getParcelableArrayListExtra(EXTRA_MESSAGE_LIST);
                List<LocalMessage> messages = new ArrayList<LocalMessage>();

                for (MessageReference ref : refs) {
                    LocalMessage m = ref.restoreToLocalMessage(this);
                    if (m != null) {
                        messages.add(m);
                    }
                }

                String dstFolder = account.getSpamFolderName();
                if (dstFolder != null
                        && !VisualVoicemail.confirmSpam()
                        && isMovePossible(controller, account, dstFolder, messages)) {
                    for(LocalMessage messageToMove : messages) {
                        String srcFolder = messageToMove.getFolder().getName();
                        controller.moveMessage(account, srcFolder, messageToMove, dstFolder, null);
                    }
                }
            } else if (ACKNOWLEDGE_ACTION.equals(action)) {
                // nothing to do here, we just want to cancel the notification so the list
                // of unseen messages is reset
            }

        } else {
            Log.w(VisualVoicemail.LOG_TAG, "Could not find account for notification action.");
        }

        return START_NOT_STICKY;
    }
}
