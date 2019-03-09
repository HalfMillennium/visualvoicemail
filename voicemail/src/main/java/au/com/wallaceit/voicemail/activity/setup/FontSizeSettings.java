package cc.martin.vv.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.*;
import cc.martin.vv.*;
import cc.martin.vv.activity.VisualVoicemailPreferenceActivity;

import cc.martin.vv.R;

/**
 * Activity to configure the font size of the information displayed in the
 * account list, folder list, message list and in the message view.
 *
 * @see FontSizes
 */
public class FontSizeSettings extends VisualVoicemailPreferenceActivity {
    /*
     * Keys of the preferences defined in res/xml/font_preferences.xml
     */

    private static final String PREFERENCE_MESSAGE_LIST_SENDER_FONT = "message_list_sender_font";
    private static final String PREFERENCE_MESSAGE_LIST_DATE_FONT = "message_list_date_font";


    private ListPreference mMessageListSender;
    private ListPreference mMessageListDate;


    /**
     * Start the FontSizeSettings activity.
     *
     * @param context The application context.
     */
    public static void actionEditSettings(Context context) {
        Intent i = new Intent(context, FontSizeSettings.class);
        context.startActivity(i);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FontSizes fontSizes = VisualVoicemail.getFontSizes();
        addPreferencesFromResource(R.xml.font_preferences);


        mMessageListSender = setupListPreference(
                                 PREFERENCE_MESSAGE_LIST_SENDER_FONT,
                                 Integer.toString(fontSizes.getMessageListSender()));
        mMessageListDate = setupListPreference(
                               PREFERENCE_MESSAGE_LIST_DATE_FONT,
                               Integer.toString(fontSizes.getMessageListDate()));
    }

    /**
     * Update the global FontSize object and permanently store the (possibly
     * changed) font size settings.
     */
    private void saveSettings() {
        FontSizes fontSizes = VisualVoicemail.getFontSizes();

        fontSizes.setMessageListSender(Integer.parseInt(mMessageListSender.getValue()));
        fontSizes.setMessageListDate(Integer.parseInt(mMessageListDate.getValue()));

        SharedPreferences preferences = Preferences.getPreferences(this).getPreferences();
        Editor editor = preferences.edit();
        fontSizes.save(editor);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        saveSettings();
        super.onBackPressed();
    }
}
