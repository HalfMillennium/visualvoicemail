package cc.martin.vv.activity.setup;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import cc.martin.vv.Account;
import cc.martin.vv.Preferences;
import cc.martin.vv.VisualVoicemail;
import cc.martin.vv.VisualVoicemail.NotificationHideCaller;
import cc.martin.vv.activity.VisualVoicemailPreferenceActivity;
import cc.martin.vv.helper.DateFormatter;
import cc.martin.vv.helper.FileBrowserHelper;
import cc.martin.vv.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import cc.martin.vv.service.MailService;

// CSM import cc.martin.vv.preferences.CheckBoxListPreference;
// CSM import cc.martin.vv.preferences.TimePickerPreference;
// CSM import cc.martin.vv.view.MessageWebView;

import cc.martin.vv.R;



public class Prefs extends VisualVoicemailPreferenceActivity {

    /**
     * Immutable empty {@link CharSequence} array
     */
//    private static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];

    /*
     * Keys of the preferences defined in res/xml/global_preferences.xml
     */
//    private static final String PREFERENCE_LANGUAGE = "language";
    private static final String PREFERENCE_THEME = "theme";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_DATE_FORMAT = "dateFormat";
//    private static final String PREFERENCE_ANIMATIONS = "animations";
    private static final String PREFERENCE_GESTURES = "gestures";
// CSM    private static final String PREFERENCE_VOLUME_NAVIGATION = "volumeNavigation";
//    private static final String PREFERENCE_START_INTEGRATED_INBOX = "start_integrated_inbox";
// CSM    private static final String PREFERENCE_CONFIRM_ACTIONS = "confirm_actions";
    private static final String PREFERENCE_NOTIFICATION_HIDE_CALLER = "notification_hide_caller";
//    private static final String PREFERENCE_MEASURE_ACCOUNTS = "measure_accounts";
//    private static final String PREFERENCE_COUNT_SEARCH = "count_search";
//    private static final String PREFERENCE_HIDE_SPECIAL_ACCOUNTS = "hide_special_accounts";
    private static final String PREFERENCE_MESSAGELIST_CHECKBOXES = "messagelist_checkboxes";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES = "messagelist_show_correspondent_names";
//    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME = "messagelist_show_contact_name";
//    private static final String PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR = "messagelist_contact_name_color";
// CSM    private static final String PREFERENCE_MESSAGEVIEW_FIXEDWIDTH = "messageview_fixedwidth_font";

//    private static final String PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST = "messageview_return_to_list";
//    private static final String PREFERENCE_MESSAGEVIEW_SHOW_NEXT = "messageview_show_next";
// CSM    private static final String PREFERENCE_QUIET_TIME_ENABLED = "quiet_time_enabled";
// CSM    private static final String PREFERENCE_QUIET_TIME_STARTS = "quiet_time_starts";
// CSM    private static final String PREFERENCE_QUIET_TIME_ENDS = "quiet_time_ends";
    private static final String PREFERENCE_BATCH_BUTTONS_MARK_READ = "batch_buttons_mark_read";
    private static final String PREFERENCE_BATCH_BUTTONS_DELETE = "batch_buttons_delete";
    private static final String PREFERENCE_BATCH_BUTTONS_ARCHIVE = "batch_buttons_archive";
    private static final String PREFERENCE_BATCH_BUTTONS_MOVE = "batch_buttons_move";
    private static final String PREFERENCE_BATCH_BUTTONS_FLAG = "batch_buttons_flag";
    private static final String PREFERENCE_BATCH_BUTTONS_UNSELECT = "batch_buttons_unselect";

// CSM    private static final String PREFERENCE_MESSAGEVIEW_MOBILE_LAYOUT = "messageview_mobile_layout";
    private static final String PREFERENCE_BACKGROUND_OPS = "background_ops";
// CSM    private static final String PREFERENCE_GALLERY_BUG_WORKAROUND = "use_gallery_bug_workaround";
//    private static final String PREFERENCE_DEBUG_LOGGING = "debug_logging";
//    private static final String PREFERENCE_SENSITIVE_LOGGING = "sensitive_logging";

    private static final String PREFERENCE_ATTACHMENT_DEF_PATH = "attachment_default_path";
    private static final String PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR = "messagelist_background_as_unread_indicator";
//    private static final String PREFERENCE_THREADED_VIEW = "threaded_view";

    private static final int ACTIVITY_CHOOSE_FOLDER = 1;


//    private ListPreference mLanguage;
    private ListPreference mTheme;
    private ListPreference mDateFormat;
//    private CheckBoxPreference mAnimations;
    private CheckBoxPreference mGestures;
//CSM    private CheckBoxListPreference mVolumeNavigation;
//    private CheckBoxPreference mStartIntegratedInbox;
// TODO - add back confirm actions:    private CheckBoxListPreference mConfirmActions;
    private ListPreference mNotificationHideSubject;
//    private CheckBoxPreference mMeasureAccounts;
//    private CheckBoxPreference mCountSearch;
//    private CheckBoxPreference mHideSpecialAccounts;
    private CheckBoxPreference mCheckboxes;
    private CheckBoxPreference mShowCorrespondentNames;
//    private CheckBoxPreference mShowContactName;
//    private CheckBoxPreference mChangeContactNameColor;
// CSM    private CheckBoxPreference mFixedWidth;
//    private CheckBoxPreference mReturnToList;
//    private CheckBoxPreference mShowNext;
// CSM    private CheckBoxPreference mMobileOptimizedLayout;
    private ListPreference mBackgroundOps;
// CSM    private CheckBoxPreference mUseGalleryBugWorkaround;
//    private CheckBoxPreference mDebugLogging;
//    private CheckBoxPreference mSensitiveLogging;
    
// CSM - Get rid of the quite time parameters
/*
    private CheckBoxPreference mQuietTimeEnabled;
    private cc.martin.vv.preferences.TimePickerPreference mQuietTimeStarts;
    private cc.martin.vv.preferences.TimePickerPreference mQuietTimeEnds;
*/
    private Preference mAttachmentPathPreference;

    private CheckBoxPreference mBatchButtonsMarkRead;
    private CheckBoxPreference mBatchButtonsDelete;
    private CheckBoxPreference mBatchButtonsArchive;
    private CheckBoxPreference mBatchButtonsMove;
    private CheckBoxPreference mBatchButtonsFlag;
    private CheckBoxPreference mBatchButtonsUnselect;
    private CheckBoxPreference mBackgroundAsUnreadIndicator;
//    private CheckBoxPreference mThreadedView;


    public static void actionPrefs(Context context) {
        Intent i = new Intent(context, Prefs.class);
        context.startActivity(i);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_preferences);

//        mLanguage = (ListPreference) findPreference(PREFERENCE_LANGUAGE);
//        List<CharSequence> entryVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntries()));
//        List<CharSequence> entryValueVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntryValues()));
//        String supportedLanguages[] = getResources().getStringArray(R.array.supported_languages);
//        HashSet<String> supportedLanguageSet = new HashSet<String>(Arrays.asList(supportedLanguages));
//        for (int i = entryVector.size() - 1; i > -1; --i) {
//            if (!supportedLanguageSet.contains(entryValueVector.get(i))) {
//                entryVector.remove(i);
//                entryValueVector.remove(i);
//            }
//        }
//        initListPreference(mLanguage, VisualVoicemail.getK9Language(),
//                           entryVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY),
//                           entryValueVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY));

        final String theme = (VisualVoicemail.getK9Theme() == VisualVoicemail.THEME_DARK) ? "dark" : "light";
        mTheme = setupListPreference(PREFERENCE_THEME, theme);

        findPreference(PREFERENCE_FONT_SIZE).setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onFontSizeSettings();
                return true;
            }
        });

        mDateFormat = (ListPreference) findPreference(PREFERENCE_DATE_FORMAT);
        String[] formats = DateFormatter.getFormats(this);
        CharSequence[] entries = new CharSequence[formats.length];
        CharSequence[] values = new CharSequence[formats.length];
        for (int i = 0 ; i < formats.length; i++) {
            String format = formats[i];
            entries[i] = DateFormatter.getSampleDate(this, format);
            values[i] = format;
        }
        initListPreference(mDateFormat, DateFormatter.getFormat(this), entries, values);

//        mAnimations = (CheckBoxPreference)findPreference(PREFERENCE_ANIMATIONS);
//        mAnimations.setChecked(VisualVoicemail.showAnimations());

// MW       mGestures = (CheckBoxPreference)findPreference(PREFERENCE_GESTURES);
// MW       mGestures.setChecked(VisualVoicemail.gesturesEnabled());
/* CSM - getting rid of volume navigation
        mVolumeNavigation = (CheckBoxListPreference)findPreference(PREFERENCE_VOLUME_NAVIGATION);
        mVolumeNavigation.setItems(new CharSequence[] {getString(R.string.volume_navigation_message), getString(R.string.volume_navigation_list)});
        mVolumeNavigation.setCheckedItems(new boolean[] {VisualVoicemail.useVolumeKeysForNavigationEnabled(), VisualVoicemail.useVolumeKeysForListNavigationEnabled()});
*/
//        mStartIntegratedInbox = (CheckBoxPreference)findPreference(PREFERENCE_START_INTEGRATED_INBOX);
//        mStartIntegratedInbox.setChecked(VisualVoicemail.startIntegratedInbox());
        
// TODO - add back confirm action
/*
        mConfirmActions = (CheckBoxListPreference) findPreference(PREFERENCE_CONFIRM_ACTIONS);
        mConfirmActions.setItems(new CharSequence[] {
                                     getString(R.string.global_settings_confirm_action_delete),
                                     getString(R.string.global_settings_confirm_action_delete_starred),
// CSM                                     getString(R.string.global_settings_confirm_action_spam),
                                 });
        mConfirmActions.setCheckedItems(new boolean[] {
                                            VisualVoicemail.confirmDelete(),
                                            VisualVoicemail.confirmDeleteStarred(),
// CSM                                            VisualVoicemail.confirmSpam(),
                                        });
*/
        
        mNotificationHideSubject = setupListPreference(PREFERENCE_NOTIFICATION_HIDE_CALLER,
                VisualVoicemail.getNotificationHideCaller().toString());

//        mMeasureAccounts = (CheckBoxPreference)findPreference(PREFERENCE_MEASURE_ACCOUNTS);
//        mMeasureAccounts.setChecked(VisualVoicemail.measureAccounts());

//        mCountSearch = (CheckBoxPreference)findPreference(PREFERENCE_COUNT_SEARCH);
//        mCountSearch.setChecked(VisualVoicemail.countSearchMessages());

//        mHideSpecialAccounts = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_SPECIAL_ACCOUNTS);
//        mHideSpecialAccounts.setChecked(VisualVoicemail.isHideSpecialAccounts());

// MW     mCheckboxes = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CHECKBOXES);
// MW     mCheckboxes.setChecked(VisualVoicemail.messageListCheckboxes());

// MW     mShowCorrespondentNames = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES);
// MW     mShowCorrespondentNames.setChecked(VisualVoicemail.showCorrespondentNames());

//        mShowContactName = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME);
//        mShowContactName.setChecked(VisualVoicemail.showContactName());

        mBackgroundAsUnreadIndicator = (CheckBoxPreference)findPreference(PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR);
        mBackgroundAsUnreadIndicator.setChecked(VisualVoicemail.useBackgroundAsUnreadIndicator());

//        mChangeContactNameColor = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR);
//        mChangeContactNameColor.setChecked(VisualVoicemail.changeContactNameColor());

//        mThreadedView = (CheckBoxPreference) findPreference(PREFERENCE_THREADED_VIEW);
//        mThreadedView.setChecked(VisualVoicemail.isThreadedViewEnabled());


// CSM        
//        mFixedWidth = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGEVIEW_FIXEDWIDTH);
//        mFixedWidth.setChecked(VisualVoicemail.messageViewFixedWidthFont());

//        mReturnToList = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST);
//        mReturnToList.setChecked(VisualVoicemail.messageViewReturnToList());

//        mShowNext = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_SHOW_NEXT);
//        mShowNext.setChecked(VisualVoicemail.messageViewShowNext());
// CSM - remove mobilised setting
/*
        mMobileOptimizedLayout = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_MOBILE_LAYOUT);
        if (!MessageWebView.isSingleColumnLayoutSupported())
        {
            mMobileOptimizedLayout.setEnabled(false);
            mMobileOptimizedLayout.setChecked(false);
        }
        else
        {
            mMobileOptimizedLayout.setChecked(VisualVoicemail.mobileOptimizedLayout());
        }
*/
        
// CSM - Get rid of the quite time parameters        
/*        mQuietTimeEnabled = (CheckBoxPreference) findPreference(PREFERENCE_QUIET_TIME_ENABLED);
        mQuietTimeEnabled.setChecked(VisualVoicemail.getQuietTimeEnabled());

        mQuietTimeStarts = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_STARTS);
        mQuietTimeStarts.setDefaultValue(VisualVoicemail.getQuietTimeStarts());
        mQuietTimeStarts.setSummary(VisualVoicemail.getQuietTimeStarts());
        mQuietTimeStarts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeStarts.setSummary(time);
                return false;
            }
        });

        mQuietTimeEnds = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_ENDS);
        mQuietTimeEnds.setSummary(VisualVoicemail.getQuietTimeEnds());
        mQuietTimeEnds.setDefaultValue(VisualVoicemail.getQuietTimeEnds());
        mQuietTimeEnds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeEnds.setSummary(time);
                return false;
            }
        });
*/

        mBackgroundOps = setupListPreference(PREFERENCE_BACKGROUND_OPS, VisualVoicemail.getBackgroundOps().toString());
        // In ICS+ there is no 'background data' setting that apps can chose to ignore anymore. So
        // we hide that option for "Background Sync".
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            CharSequence[] oldEntries = mBackgroundOps.getEntries();
//            CharSequence[] newEntries = new CharSequence[3];
//            // Use "When 'Auto-sync' is checked" instead of "When 'Background data' & 'Auto-sync'
//            // are checked" as description.
//            newEntries[0] = getString(R.string.background_ops_auto_sync_only);
//            newEntries[1] = oldEntries[2];
//            newEntries[2] = oldEntries[3];
//
//            CharSequence[] oldValues = mBackgroundOps.getEntryValues();
//            CharSequence[] newValues = new CharSequence[3];
//            newValues[0] = oldValues[1];
//            newValues[1] = oldValues[2];
//            newValues[2] = oldValues[3];
//
//            mBackgroundOps.setEntries(newEntries);
//            mBackgroundOps.setEntryValues(newValues);
//
//        }

// CSM        
//        mUseGalleryBugWorkaround = (CheckBoxPreference)findPreference(PREFERENCE_GALLERY_BUG_WORKAROUND);
//        mUseGalleryBugWorkaround.setChecked(VisualVoicemail.useGalleryBugWorkaround());

//        mDebugLogging = (CheckBoxPreference)findPreference(PREFERENCE_DEBUG_LOGGING);
//        mSensitiveLogging = (CheckBoxPreference)findPreference(PREFERENCE_SENSITIVE_LOGGING);

//        mDebugLogging.setChecked(VisualVoicemail.DEBUG);
//        mSensitiveLogging.setChecked(VisualVoicemail.DEBUG_SENSITIVE);

        mAttachmentPathPreference = findPreference(PREFERENCE_ATTACHMENT_DEF_PATH);
        mAttachmentPathPreference.setSummary(VisualVoicemail.getAttachmentDefaultPath());
        mAttachmentPathPreference
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileBrowserHelper
                .getInstance()
                .showFileBrowserActivity(Prefs.this,
                                         new File(VisualVoicemail.getAttachmentDefaultPath()),
                                         ACTIVITY_CHOOSE_FOLDER, callback);

                return true;
            }

            FileBrowserFailOverCallback callback = new FileBrowserFailOverCallback() {

                @Override
                public void onPathEntered(String path) {
                    mAttachmentPathPreference.setSummary(path);
                    VisualVoicemail.setAttachmentDefaultPath(path);
                }

                @Override
                public void onCancel() {
                    // canceled, do nothing
                }
            };
        });

        /* MW // mBatchButtonsMarkRead = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_MARK_READ);
        mBatchButtonsDelete = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_DELETE);
        mBatchButtonsArchive = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_ARCHIVE);
        mBatchButtonsMove = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_MOVE);
        mBatchButtonsFlag = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_FLAG);
        mBatchButtonsUnselect = (CheckBoxPreference)findPreference(PREFERENCE_BATCH_BUTTONS_UNSELECT);
        mBatchButtonsMarkRead.setChecked(VisualVoicemail.batchButtonsMarkRead());
        mBatchButtonsDelete.setChecked(VisualVoicemail.batchButtonsDelete());
        mBatchButtonsArchive.setChecked(VisualVoicemail.batchButtonsArchive());
        mBatchButtonsMove.setChecked(VisualVoicemail.batchButtonsMove());
        mBatchButtonsFlag.setChecked(VisualVoicemail.batchButtonsFlag());
        mBatchButtonsUnselect.setChecked(VisualVoicemail.batchButtonsUnselect());*/

        // If we don't have any accounts with an archive folder, then don't enable the preference.
        boolean hasArchiveFolder = false;
        for (final Account acct : Preferences.getPreferences(this).getAccounts()) {
            if (acct.hasArchiveFolder()) {
                hasArchiveFolder = true;
                break;
            }
        }
        if (!hasArchiveFolder) {
            mBatchButtonsArchive.setEnabled(false);
            mBatchButtonsArchive.setSummary(R.string.global_settings_archive_disabled_reason);
        }
    }

    private void saveSettings() {
        SharedPreferences preferences = Preferences.getPreferences(this).getPreferences();

//        VisualVoicemail.setK9Language(mLanguage.getValue());

        int newTheme = mTheme.getValue().equals("dark") ? VisualVoicemail.THEME_DARK : VisualVoicemail.THEME_LIGHT;
        if (VisualVoicemail.getK9Theme() != newTheme) {
            // Reset the message view theme when the app theme changes
            VisualVoicemail.setK9MessageViewTheme(newTheme);
        }
        VisualVoicemail.setK9Theme(newTheme);

//        VisualVoicemail.setAnimations(mAnimations.isChecked());
//        VisualVoicemail.setGesturesEnabled(mGestures.isChecked());
// CSM        VisualVoicemail.setUseVolumeKeysForNavigation(mVolumeNavigation.getCheckedItems()[0]);
// CSM        VisualVoicemail.setUseVolumeKeysForListNavigation(mVolumeNavigation.getCheckedItems()[1]);
//        VisualVoicemail.setStartIntegratedInbox(!mHideSpecialAccounts.isChecked() && mStartIntegratedInbox.isChecked());
// TODO - add back confirmation:        VisualVoicemail.setConfirmDelete(mConfirmActions.getCheckedItems()[0]);
// TODO - add back confirmation:        VisualVoicemail.setConfirmDeleteStarred(mConfirmActions.getCheckedItems()[1]);
// CSM        VisualVoicemail.setConfirmSpam(mConfirmActions.getCheckedItems()[2]);
        VisualVoicemail.setNotificationHideSubject(NotificationHideCaller.valueOf(mNotificationHideSubject.getValue()));

//        VisualVoicemail.setMeasureAccounts(mMeasureAccounts.isChecked());
//        VisualVoicemail.setCountSearchMessages(mCountSearch.isChecked());
//        VisualVoicemail.setHideSpecialAccounts(mHideSpecialAccounts.isChecked());
//        VisualVoicemail.setMessageListCheckboxes(mCheckboxes.isChecked());
//        VisualVoicemail.setShowCorrespondentNames(mShowCorrespondentNames.isChecked());
//        VisualVoicemail.setShowContactName(mShowContactName.isChecked());
        VisualVoicemail.setUseBackgroundAsUnreadIndicator(mBackgroundAsUnreadIndicator.isChecked());
//        VisualVoicemail.setThreadedViewEnabled(mThreadedView.isChecked());
//        VisualVoicemail.setChangeContactNameColor(mChangeContactNameColor.isChecked());
// CSM        VisualVoicemail.setMessageViewFixedWidthFont(mFixedWidth.isChecked());
//        VisualVoicemail.setMessageViewReturnToList(mReturnToList.isChecked());
//        VisualVoicemail.setMessageViewShowNext(mShowNext.isChecked());
// CSM        VisualVoicemail.setMobileOptimizedLayout(mMobileOptimizedLayout.isChecked());
        
// CSM get rid of quite time parameters
/*
        VisualVoicemail.setQuietTimeEnabled(mQuietTimeEnabled.isChecked());
        VisualVoicemail.setQuietTimeStarts(mQuietTimeStarts.getTime());
        VisualVoicemail.setQuietTimeEnds(mQuietTimeEnds.getTime());
*/
        /*VisualVoicemail.setBatchButtonsMarkRead(mBatchButtonsMarkRead.isChecked());
        VisualVoicemail.setBatchButtonsDelete(mBatchButtonsDelete.isChecked());
        VisualVoicemail.setBatchButtonsArchive(mBatchButtonsArchive.isChecked());
        VisualVoicemail.setBatchButtonsMove(mBatchButtonsMove.isChecked());
        VisualVoicemail.setBatchButtonsFlag(mBatchButtonsFlag.isChecked());
        VisualVoicemail.setBatchButtonsUnselect(mBatchButtonsUnselect.isChecked());*/

        VisualVoicemail.setAttachmentDefaultPath(mAttachmentPathPreference.getSummary().toString());
        boolean needsRefresh = VisualVoicemail.setBackgroundOps(mBackgroundOps.getValue());
// CSM        VisualVoicemail.setUseGalleryBugWorkaround(mUseGalleryBugWorkaround.isChecked());

//        if (!VisualVoicemail.DEBUG && mDebugLogging.isChecked()) {
//            Toast.makeText(this, R.string.debug_logging_enabled, Toast.LENGTH_LONG).show();
//        }
//        VisualVoicemail.DEBUG = mDebugLogging.isChecked();
//        VisualVoicemail.DEBUG_SENSITIVE = mSensitiveLogging.isChecked();

        Editor editor = preferences.edit();
        VisualVoicemail.save(editor);
        DateFormatter.setDateFormat(editor, mDateFormat.getValue());
        editor.apply();

        if (needsRefresh) {
            MailService.actionReset(this, null);
        }
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }

    private void onFontSizeSettings() {
        FontSizeSettings.actionEditSettings(this);
    }

 

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CHOOSE_FOLDER:
            if (resultCode == RESULT_OK && data != null) {
                // obtain the filename
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String filePath = fileUri.getPath();
                    if (filePath != null) {
                        mAttachmentPathPreference.setSummary(filePath.toString());
                        VisualVoicemail.setAttachmentDefaultPath(filePath.toString());
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
