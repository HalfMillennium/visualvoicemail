package au.com.wallaceit.voicemail.mailstore;


import au.com.wallaceit.voicemail.mailstore.LocalMessage;

public interface LocalPart {
    String getAccountUuid();
    long getId();
    String getDisplayName();
    long getSize();
    boolean isFirstClassAttachment();
    LocalMessage getMessage();
}
