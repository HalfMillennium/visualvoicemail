package au.com.wallaceit.voicemail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import au.com.wallaceit.voicemail.VisualVoicemail;


class MigrationTo36 {
    public static void addAttachmentsContentIdColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE attachments ADD content_id TEXT");
        } catch (SQLiteException e) {
            Log.e(VisualVoicemail.LOG_TAG, "Unable to add content_id column to attachments");
        }
    }
}
