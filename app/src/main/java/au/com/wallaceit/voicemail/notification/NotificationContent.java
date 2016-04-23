package au.com.wallaceit.voicemail.notification;


import au.com.wallaceit.voicemail.activity.MessageReference;


class NotificationContent {
    public final MessageReference messageReference;
    public final String sender;


    public NotificationContent(MessageReference messageReference, String sender) {
        this.messageReference = messageReference;
        this.sender = sender;
    }
}
