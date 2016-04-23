package au.com.wallaceit.voicemail.notification;


import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.activity.MessageReference;
import au.com.wallaceit.voicemail.helper.Contacts;
import au.com.wallaceit.voicemail.helper.MessageHelper;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;

import au.com.wallaceit.voicemail.helper.VvmContacts;
import au.com.wallaceit.voicemail.mailstore.LocalMessage;
import au.com.wallaceit.voicemail.message.preview.PreviewResult.PreviewType;


class NotificationContentCreator {
    private final Context context;
    private TextAppearanceSpan emphasizedSpan;


    public NotificationContentCreator(Context context) {
        this.context = context;
    }

    public NotificationContent createFromMessage(Account account, LocalMessage message) {
        MessageReference messageReference = message.makeMessageReference();
        VvmContacts contacts = new VvmContacts(context);
        String sender = contacts.extractPhoneFromVoicemailAddress(message.getFrom()[0]);
        String displaySender = contacts.getDisplayName(sender);

        return new NotificationContent(messageReference, displaySender);
    }
}
