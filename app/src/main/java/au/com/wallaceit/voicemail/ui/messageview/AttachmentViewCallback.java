package au.com.wallaceit.voicemail.ui.messageview;


import com.fsck.k9.mailstore.AttachmentViewInfo;


interface AttachmentViewCallback {
    void onViewAttachment(AttachmentViewInfo attachment);
    void onSaveAttachment(AttachmentViewInfo attachment);
    void onSaveAttachmentToUserProvidedDirectory(AttachmentViewInfo attachment);
}
