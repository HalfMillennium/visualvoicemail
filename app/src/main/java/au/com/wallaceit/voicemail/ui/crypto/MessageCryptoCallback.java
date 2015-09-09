package au.com.wallaceit.voicemail.ui.crypto;


import au.com.wallaceit.voicemail.ui.crypto.MessageCryptoAnnotations;

public interface MessageCryptoCallback {
    void onCryptoOperationsFinished(MessageCryptoAnnotations annotations);
}
