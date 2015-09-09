package au.com.wallaceit.voicemail.ui.crypto;


import com.fsck.k9.ui.crypto.MessageCryptoAnnotations;

public interface MessageCryptoCallback {
    void onCryptoOperationsFinished(MessageCryptoAnnotations annotations);
}
