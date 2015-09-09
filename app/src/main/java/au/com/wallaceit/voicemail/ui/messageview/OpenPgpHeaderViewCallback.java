package au.com.wallaceit.voicemail.ui.messageview;


import android.app.PendingIntent;


interface OpenPgpHeaderViewCallback {
    void onPgpSignatureButtonClick(PendingIntent pendingIntent);
}
