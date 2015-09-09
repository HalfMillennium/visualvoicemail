package au.com.wallaceit.voicemail.mailstore;


import com.fsck.k9.mailstore.LocalMessage;

public interface LocalPart {
    String getAccountUuid();
    long getId();
    String getDisplayName();
    long getSize();
    boolean isFirstClassAttachment();
    LocalMessage getMessage();
}
