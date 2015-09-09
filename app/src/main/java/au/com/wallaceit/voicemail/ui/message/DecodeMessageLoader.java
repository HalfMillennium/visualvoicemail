package au.com.wallaceit.voicemail.ui.message;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import au.com.wallaceit.voicemail.VisualVoicemail;
import com.fsck.k9.mail.Message;
import au.com.wallaceit.voicemail.mailstore.LocalMessageExtractor;
import au.com.wallaceit.voicemail.mailstore.MessageViewInfo;
import au.com.wallaceit.voicemail.ui.crypto.MessageCryptoAnnotations;


public class DecodeMessageLoader extends AsyncTaskLoader<MessageViewInfo> {
    private final Message message;
    private MessageViewInfo messageViewInfo;
    private MessageCryptoAnnotations annotations;

    public DecodeMessageLoader(Context context, Message message, MessageCryptoAnnotations annotations) {
        super(context);
        this.message = message;
        this.annotations = annotations;
    }

    @Override
    protected void onStartLoading() {
        if (messageViewInfo != null) {
            super.deliverResult(messageViewInfo);
        }

        if (takeContentChanged() || messageViewInfo == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(MessageViewInfo messageViewInfo) {
        this.messageViewInfo = messageViewInfo;
        super.deliverResult(messageViewInfo);
    }

    @Override
    public MessageViewInfo loadInBackground() {
        try {
            return LocalMessageExtractor.decodeMessageForView(getContext(), message, annotations);
        } catch (Exception e) {
            Log.e(VisualVoicemail.LOG_TAG, "Error while decoding message", e);
            return null;
        }
    }
}
