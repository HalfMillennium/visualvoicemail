package au.com.wallaceit.voicemail.helper;
/*
 * Copyright 2013 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of Visual Voicemail.
 *
 * Visual Voicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Visual Voicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Visual Voicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by michael on 15/09/15.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.Store;
import com.fsck.k9.mail.internet.MimeHeader;
import com.fsck.k9.mail.internet.MimeMultipart;
import com.fsck.k9.mail.internet.MimeUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.MessageReference;
import au.com.wallaceit.voicemail.controller.MessagingController;
import au.com.wallaceit.voicemail.controller.MessagingListener;
import au.com.wallaceit.voicemail.mailstore.LocalFolder;
import au.com.wallaceit.voicemail.mailstore.LocalMessage;
import au.com.wallaceit.voicemail.mailstore.LocalPart;
import au.com.wallaceit.voicemail.mailstore.LocalStore;

public class VoicemailAttachmentHelper {
    private Context context;
    private final MessagingController controller;
    private final MessageReference reference;
    private Part attachment;
    private String phone = "";
    private Date date;

    public VoicemailAttachmentHelper(Context context, MessagingController controller, MessageReference reference) {
        this.context = context;
        this.controller = controller;
        this.reference = reference;
        LocalMessage message = reference.restoreToLocalMessage(context);
        if (message.getFrom().length>0) {
            VvmContacts vvmContacts = new VvmContacts(context);
            phone = vvmContacts.extractPhoneFromVoicemailAddress(message.getFrom()[0]);
        }
        date = message.getSentDate();
    }

    public Part getAttachment(){
        return attachment;
    }

    public Uri getAttachmentUriForSharing() {
        return FileProvider.getUriForFile(context, "au.com.wallaceit.voicemail", new File(getCacheUri().toString()));
    }

    public Uri getCacheUri(){
        File outFile = new File(context.getCacheDir()+"/voicemail/", getUniqueAttachmentFilename());
        if (!outFile.exists()){
            if (!outFile.getParentFile().exists())
                outFile.getParentFile().mkdir();
            try {
                FileOutputStream out = new FileOutputStream(outFile);
                LocalPart localPart = (LocalPart) attachment;
                InputStream in = getAttachmentInputStream(localPart.getAccountUuid(), String.valueOf(localPart.getId()));
                copyFile(in, out);
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        return Uri.parse(outFile.getPath());
    }

    public String getUniqueAttachmentFilename(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy_H-mm", Locale.ENGLISH);
        String dateStr = sdf.format(date);
        return reference.getUid().replaceAll("[^a-zA-Z0-9.-]", "_")+"_"+phone+"_"+dateStr+"."+MimeUtility.getExtensionByMimeType(attachment.getMimeType());
    }

    public InputStream getAttachmentInputStream() throws MessagingException {
        LocalPart localPart = (LocalPart) attachment;
        return getAttachmentInputStream(reference.getAccountUuid(), String.valueOf(localPart.getId()));
    }

    private InputStream getAttachmentInputStream(String accountUuid, String attachmentId) throws MessagingException {
        final Account account = Preferences.getPreferences(context).getAccount(accountUuid);
        LocalStore localStore = LocalStore.getInstance(account, context);
        return localStore.getAttachmentInputStream(attachmentId);
    }

    public void loadVoicemailAttachment(final Runnable callback){
        getVoicemailAttachment(reference, callback);
    }

    private void getVoicemailAttachment(final MessageReference messageReference, final Runnable callback) {
        try {
            final LocalMessage localMessage = messageReference.restoreToLocalMessage(context);
            final Account account = Preferences.getPreferences(context).getAccount(messageReference.getAccountUuid());
            final String uid = localMessage.getUid();
            LocalStore localStore = account.getLocalStore();
            final LocalFolder localFolder = localStore.getFolder(messageReference.getFolderName());
            final FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);

            if (VisualVoicemail.DEBUG)
                Log.w(VisualVoicemail.LOG_TAG, "Message load started");

            try {
                localFolder.fetch(Collections.singletonList(localMessage), fp, null);
            } catch (IllegalArgumentException ex){
                // TODO: Remove - This was added as a workaround for Vodafone's old server returning bad data and is probably not needed anymore
                if (VisualVoicemail.DEBUG) {
                    Log.w(VisualVoicemail.LOG_TAG, "Message has null MIME boundry, trying to download the message again");
                    Log.w(VisualVoicemail.LOG_TAG, TextUtils.join(", ", localMessage.getHeaderNames().toArray()));
                    Log.w(VisualVoicemail.LOG_TAG, localMessage.getMimeType());
                    Log.w(VisualVoicemail.LOG_TAG, "Body missing: " + String.valueOf(localMessage.isBodyMissing()));
                    Log.w(VisualVoicemail.LOG_TAG, "Attachments: " + String.valueOf(localMessage.getAttachmentCount()));
                }
                // force download the body of the message, for some reason using read only mode on the server prevents an imap response parsing error.
                localMessage.setFlag(Flag.X_DOWNLOADED_FULL, false);
                //controller.loadMessageForViewRemoteSynchronous(account, messageReference.getFolderName(), uid, null, true, false);
                class LoadMessageTask extends AsyncTask<String, Long, Boolean>{
                    @Override
                    protected Boolean doInBackground(String... params) {
                        try {
                            //message.destroy(); // clean corrupt message
                            Store remoteStore = account.getRemoteStore();
                            Folder remoteFolder = remoteStore.getFolder(localFolder.getName());
                            remoteFolder.open(Folder.OPEN_MODE_RO);

                            // Get the remote message and fully download it
                            final Message remoteMessage = remoteFolder.getMessage(uid);
                            remoteFolder.fetch(Collections.singletonList(remoteMessage), fp, null);

                            localMessage.setBody(remoteMessage.getBody());
                            localMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);

                            localFolder.open(Folder.OPEN_MODE_RW);
                            localFolder.appendMessages(Collections.singletonList(localMessage));
                            localFolder.close();

                            return true;
                        } catch (MessagingException ex1){
                            return false;
                        }
                    }

                    protected void onPostExecute(Boolean result) {
                        if (VisualVoicemail.DEBUG)
                            Log.w(VisualVoicemail.LOG_TAG, "Download complete, body still missing: " + String.valueOf(localMessage.isBodyMissing()));
                        if (result && !localMessage.isBodyMissing()) {
                            getVoicemailAttachment(messageReference, callback);
                        } else {
                            callback.run();
                        }
                    }
                }

                LoadMessageTask loadMessageTask = new LoadMessageTask();
                loadMessageTask.execute();

                return;
            }

            attachment = walkMessagePartsForRecording(localMessage);
            if (attachment!=null) {
                if (VisualVoicemail.DEBUG)
                    try {
                        Log.i(VisualVoicemail.LOG_TAG, "Attachment Content Type: " + TextUtils.join(";", attachment.getHeader(MimeHeader.HEADER_CONTENT_TYPE)));
                        Log.i(VisualVoicemail.LOG_TAG, "Attachment Disposition: " + TextUtils.join(";", attachment.getHeader(MimeHeader.HEADER_CONTENT_DISPOSITION)));
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }

                if (needsDownloading()) {
                    if (VisualVoicemail.DEBUG)
                        Log.w(VisualVoicemail.LOG_TAG, "Attachment part not loaded, starting download");
                    downloadAttachmentPart((LocalPart) attachment, new Runnable() {
                        @Override
                        public void run() {
                            callback.run();
                        }
                    });
                } else {
                    callback.run();
                }
                return;
            }

            if (VisualVoicemail.DEBUG)
                Log.w(VisualVoicemail.LOG_TAG, "Attachment was null");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private Part walkMessagePartsForRecording(Part part) {
        if (part.getBody() instanceof MimeMultipart) {
            if (VisualVoicemail.DEBUG)
                Log.w(VisualVoicemail.LOG_TAG, part.getBody().getClass().toString() + " " + part.getMimeType());
            MimeMultipart mp = (MimeMultipart) part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                if (VisualVoicemail.DEBUG)
                    Log.i(VisualVoicemail.LOG_TAG, "multiPartCount = " + mp.getCount());
                Part rtn = walkMessagePartsForRecording(mp.getBodyPart(i));
                if (rtn!=null)
                    return rtn;
            }
        } else {
            if (VisualVoicemail.DEBUG)
                Log.w(VisualVoicemail.LOG_TAG, part.getBody().getClass().toString() + " " + part.getMimeType());
            if (part.getMimeType().toLowerCase().indexOf("audio")==0)
                return part;
            /* A message transcript is usually placed in a text/plain attachment, but not many providers use it.
            if (part.getMimeType().equals("text/plain")){
                ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
                try {
                    part.getBody().writeTo(bos);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                try {
                    String result = new String(bos.toByteArray(), "utf-8");
                    System.out.println("RESULT: "+result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }*/
        }
        if (VisualVoicemail.DEBUG)
            Log.w(VisualVoicemail.LOG_TAG, part.getBody().getClass().toString() + " null: " + part.getMimeType());
        return null;
    }

    private boolean needsDownloading() {
        return isPartMissing() && isLocalPart();
    }

    private boolean isPartMissing() {
        return attachment.getBody() == null;
    }

    private boolean isLocalPart() {
        return attachment instanceof LocalPart;
    }

    private void downloadAttachmentPart(LocalPart localPart, final Runnable attachmentDownloadedCallback) {
        String accountUuid = localPart.getAccountUuid();
        Account account = Preferences.getPreferences(context).getAccount(accountUuid);
        LocalMessage message = localPart.getMessage();
        if (VisualVoicemail.DEBUG)
            Log.w(VisualVoicemail.LOG_TAG, "Downloading attachment part");
        //messageViewFragment.showAttachmentLoadingDialog();
        controller.loadAttachment(account, message, attachment, new MessagingListener() {
            @Override
            public void loadAttachmentFinished(Account account, Message message, Part part) {
                //messageViewFragment.hideAttachmentLoadingDialogOnMainThread();
                //messageViewFragment.runOnMainThread(attachmentDownloadedCallback);
                attachmentDownloadedCallback.run();
            }

            @Override
            public void loadAttachmentFailed(Account account, Message message, Part part, String reason) {
                //messageViewFragment.hideAttachmentLoadingDialogOnMainThread();
            }
        });
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
