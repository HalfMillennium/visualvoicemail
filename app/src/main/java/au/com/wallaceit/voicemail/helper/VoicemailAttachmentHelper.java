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
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Part;
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
import java.util.List;
import java.util.Locale;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.BuildConfig;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.MessageReference;
import au.com.wallaceit.voicemail.controller.MessagingController;
import au.com.wallaceit.voicemail.controller.MessagingListener;
import au.com.wallaceit.voicemail.mailstore.BinaryMemoryBody;
import au.com.wallaceit.voicemail.mailstore.FileBackedBody;
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

    public boolean loadVoicemailAttachment(){
        attachment = getVoicemailAttachment(reference);
        if (attachment!=null && needsDownloading()){
            downloadAttachmentPart((LocalPart) attachment, new Runnable() {
                @Override
                public void run() {

                }
            });
            return false;
        }
        return attachment!=null;
    }

    private Part getVoicemailAttachment(MessageReference messageReference) {
        try {
            LocalMessage message = messageReference.restoreToLocalMessage(context);
            Account account = Preferences.getPreferences(context).getAccount(messageReference.getAccountUuid());
            LocalStore localStore = account.getLocalStore();
            LocalFolder folder = localStore.getFolder(messageReference.getFolderName());
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            List<LocalMessage> messages = Collections.singletonList(message);
            folder.fetch(messages, fp, null);
            folder.close();
            Log.i(VisualVoicemail.LOG_TAG, "Message MIME "+message.getMimeType());
            Part part = walkMessagePartsForRecording(message);
            Log.i(VisualVoicemail.LOG_TAG, "Attachment Returned " + (part == null ? "null" : "Valid Part: "+part.getDisposition()+" "+part.getMimeType()));

            return part;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Part walkMessagePartsForRecording(Part part) throws MessagingException
    {
        if (part.getBody() instanceof MimeMultipart) {
            Log.w(VisualVoicemail.LOG_TAG, part.getBody().getClass().toString() + " " + part.getMimeType());
            MimeMultipart mp = (MimeMultipart) part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                Log.i(VisualVoicemail.LOG_TAG, "multiPartCount = " + mp.getCount());
                Part rtn = walkMessagePartsForRecording(mp.getBodyPart(i));
                if (rtn!=null)
                    return rtn;
            }
        } else {
            Log.w(VisualVoicemail.LOG_TAG, part.getBody().getClass().toString() + " " + part.getMimeType());
            if (part.getBody() instanceof FileBackedBody || part.getBody() instanceof BinaryMemoryBody)
                return part;
        }
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
