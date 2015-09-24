package au.com.wallaceit.voicemail.activity.misc;/*
/*
 * Copyright 2015 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of Voicemail.
 *
 * Voicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Voicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Voicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by michael on 24/09/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.BinaryTempFileBody;
import com.fsck.k9.mail.internet.BinaryTempFileMessageBody;
import com.fsck.k9.mail.internet.MimeBodyPart;
import com.fsck.k9.mail.internet.MimeHeader;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.MimeMessageHelper;
import com.fsck.k9.mail.internet.MimeMultipart;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.util.MimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.controller.MessagingController;
import au.com.wallaceit.voicemail.mailstore.FileBackedBody;
import au.com.wallaceit.voicemail.mailstore.LocalFolder;

public class GreetingRecorderDialog extends Dialog implements View.OnClickListener {
    private Account mAccount;
    private MediaRecorder mRecorder;
    private boolean isRecording = false;
    private Button recordButton;
    private String outputPath;
    private ImageView micView;

    public GreetingRecorderDialog(Context context, Account account) {
        super(context);
        setOwnerActivity((Activity) context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.recordview);
        mAccount = account;
        outputPath = getContext().getCacheDir() + "/tempgreeting.amr";
        new File(outputPath).delete(); // remove last cached file; audio recorder appends
        recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnClickListener(this);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);
        micView = (ImageView) findViewById(R.id.microphone);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record:
                if (isRecording){
                    showPreview();
                } else {
                    micView.setImageResource(R.drawable.microphone_active);
                    recordButton.setText(getContext().getResources().getString(R.string.okay_action));
                    startRecording();
                }
                break;
            case R.id.cancel:
                if (isRecording){
                    stopRecording();
                    recordButton.setText(getContext().getResources().getString(R.string.record_action));
                    micView.setImageResource(R.drawable.microphone);
                } else {
                    dismiss();
                }
                break;
        }
    }

    private void showPreview(){
        AudioPlayerDialog playerDialog = new AudioPlayerDialog(getOwnerActivity(), Uri.parse(outputPath));
        playerDialog.setCanceledOnTouchOutside(false);
        playerDialog.setCancelButtonCallback(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GreetingRecorderDialog recorderDialog = new GreetingRecorderDialog(getOwnerActivity(), mAccount);
                recorderDialog.setCanceledOnTouchOutside(false);
                recorderDialog.show();
                dialog.dismiss();
            }
        });
        playerDialog.setOkButtonCallback(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showTypeSelectDialog();
                dialog.dismiss();
            }
        });
        playerDialog.show();
        dismiss();
    }

    private void showTypeSelectDialog(){
        CharSequence colors[] = new CharSequence[] {"Greeting", "Voice Signature"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Save As..");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                String typeHeader;
                if (which==1){
                    typeHeader = "voice-signature";
                } else {
                    typeHeader = "normal-greeting";
                }
                saveGreetingToMessage(typeHeader);
            }
        });
        builder.show();
    }

    private void saveGreetingToMessage(String type){
        MimeMessage message = new MimeMessage();
        String contentType = "audio/AMR";
        try {
            // create message
            message.generateMessageId();
            message.setSentDate(new Date(), true);
            message.setHeader("X-AppleVM-Message-Version", "1.0");
            message.setHeader("X-CNS-Greeting-Type", type);
            // add attachment
            Body body = new FileBackedBody(new File(outputPath), MimeUtil.ENC_BASE64);

            MimeBodyPart bp = new MimeBodyPart(body);
            bp.addHeader(MimeHeader.HEADER_CONTENT_TYPE, String.format("%s;\r\n name=\"%s\"", contentType, EncoderUtil.encodeIfNecessary("message.amr", EncoderUtil.Usage.WORD_ENTITY, 7)));
            bp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, String.format(Locale.US, "attachment;\r\n filename=\"%s\";\r\n size=%d", "message.amr", new File(outputPath).length()));

            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(bp);
            MimeMessageHelper.setBody(message, mp);
        } catch (MessagingException e) {
            e.printStackTrace();
            return;
        }
        // save the message
        List<Message> messages = Arrays.asList(new Message[]{message});
        try {
            LocalFolder folder = mAccount.getLocalStore().getFolder("Greetings");
            folder.appendMessages(messages);
            final MessagingController messagingController = MessagingController.getInstance(getContext().getApplicationContext());
            messagingController.saveGreeting(mAccount, message, -1);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(outputPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(VisualVoicemail.LOG_TAG, "record prepare() failed");
        }

        mRecorder.start();
        isRecording = true;
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording = false;
    }

    @Override
    public void onStop() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        super.onStop();
    }
}
