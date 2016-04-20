package au.com.wallaceit.voicemail.activity.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.VisualVoicemail.NotificationHideSubject;
import au.com.wallaceit.voicemail.VisualVoicemail.NotificationQuickDelete;
import au.com.wallaceit.voicemail.VisualVoicemail.SplitViewMode;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.activity.ColorPickerDialog;
import au.com.wallaceit.voicemail.activity.K9PreferenceActivity;
import au.com.wallaceit.voicemail.activity.setup.*;
import au.com.wallaceit.voicemail.activity.setup.FontSizeSettings;
import au.com.wallaceit.voicemail.controller.MessagingController;
import au.com.wallaceit.voicemail.helper.FileBrowserHelper;
import au.com.wallaceit.voicemail.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import au.com.wallaceit.voicemail.notification.NotificationController;
import au.com.wallaceit.voicemail.preferences.CheckBoxListPreference;
import au.com.wallaceit.voicemail.preferences.Storage;
import au.com.wallaceit.voicemail.preferences.StorageEditor;
import au.com.wallaceit.voicemail.preferences.TimePickerPreference;

import au.com.wallaceit.voicemail.service.MailService;


public class Prefs extends K9PreferenceActivity {

    /**
     * Immutable empty {@link CharSequence} array
     */
    private static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];

    /*
     * Keys of the preferences defined in res/xml/global_preferences.xml
     */
    private static final String PREFERENCE_LANGUAGE = "language";
    private static final String PREFERENCE_THEME = "theme";
    //private static final String PREFERENCE_MESSAGE_VIEW_THEME = "messageViewTheme";
    //private static final String PREFERENCE_FIXED_MESSAGE_THEME = "fixedMessageViewTheme";
    //private static final String PREFERENCE_COMPOSER_THEME = "messageComposeTheme";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_ANIMATIONS = "animations";
    private static final String PREFERENCE_GESTURES = "gestures";
    private static final String PREFERENCE_VOLUME_NAVIGATION = "volumeNavigation";
    //private static final String PREFERENCE_START_INTEGRATED_INBOX = "start_integrated_inbox";
    private static final String PREFERENCE_CONFIRM_ACTIONS = "confirm_actions";
    //private static final String PREFERENCE_NOTIFICATION_HIDE_SUBJECT = "notification_hide_subject";
    private static final String PREFERENCE_MEASURE_ACCOUNTS = "measure_accounts";
    private static final String PREFERENCE_COUNT_SEARCH = "count_search";
    //private static final String PREFERENCE_HIDE_SPECIAL_ACCOUNTS = "hide_special_accounts";
    private static final String PREFERENCE_MESSAGELIST_CHECKBOXES = "messagelist_checkboxes";
    //private static final String PREFERENCE_MESSAGELIST_PREVIEW_LINES = "messagelist_preview_lines";
    //private static final String PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT = "messagelist_sender_above_subject";
    private static final String PREFERENCE_MESSAGELIST_STARS = "messagelist_stars";
    //private static final String PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES = "messagelist_show_correspondent_names";
    //private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME = "messagelist_show_contact_name";
    //private static final String PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR = "messagelist_contact_name_color";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE = "messagelist_show_contact_picture";
    private static final String PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES =
            "messagelist_colorize_missing_contact_pictures";
    //private static final String PREFERENCE_MESSAGEVIEW_FIXEDWIDTH = "messageview_fixedwidth_font";
    //private static final String PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS = "messageview_visible_refile_actions";

    //private static final String PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST = "messageview_return_to_list";
    //private static final String PREFERENCE_MESSAGEVIEW_SHOW_NEXT = "messageview_show_next";
    private static final String PREFERENCE_QUIET_TIME_ENABLED = "quiet_time_enabled";
    private static final String PREFERENCE_DISABLE_NOTIFICATION_DURING_QUIET_TIME =
            "disable_notifications_during_quiet_time";
    private static final String PREFERENCE_QUIET_TIME_STARTS = "quiet_time_starts";
    private static final String PREFERENCE_QUIET_TIME_ENDS = "quiet_time_ends";
    private static final String PREFERENCE_NOTIF_QUICK_DELETE = "notification_quick_delete";
    private static final String PREFERENCE_LOCK_SCREEN_NOTIFICATION_VISIBILITY = "lock_screen_notification_visibility";
    private static final String PREFERENCE_HIDE_USERAGENT = "privacy_hide_useragent";
    private static final String PREFERENCE_HIDE_TIMEZONE = "privacy_hide_timezone";

    //private static final String PREFERENCE_AUTOFIT_WIDTH = "messageview_autofit_width";
    private static final String PREFERENCE_BACKGROUND_OPS = "background_ops";
    private static final String PREFERENCE_DEBUG_LOGGING = "debug_logging";
    private static final String PREFERENCE_SENSITIVE_LOGGING = "sensitive_logging";

    private static final String PREFERENCE_ATTACHMENT_DEF_PATH = "attachment_default_path";
    private static final String PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR = "messagelist_background_as_unread_indicator";
    //private static final String PREFERENCE_THREADED_VIEW = "threaded_view";
    //private static final String PREFERENCE_FOLDERLIST_WRAP_NAME = "folderlist_wrap_folder_name";
    //private static final String PREFERENCE_SPLITVIEW_MODE = "splitview_mode";

    private static final int ACTIVITY_CHOOSE_FOLDER = 1;

    // Named indices for the mVisibleRefileActions field
    /*private static final int VISIBLE_REFILE_ACTIONS_DELETE = 0;
    private static final int VISIBLE_REFILE_ACTIONS_ARCHIVE = 1;
    private static final int VISIBLE_REFILE_ACTIONS_MOVE = 2;
    private static final int VISIBLE_REFILE_ACTIONS_COPY = 3;
    private static final int VISIBLE_REFILE_ACTIONS_SPAM = 4;*/

    private ListPreference mLanguage;
    private ListPreference mTheme;
    /*private CheckBoxPreference mFixedMessageTheme;
    private ListPreference mMessageTheme;
    private ListPreference mComposerTheme;*/
    private CheckBoxPreference mAnimations;
    private CheckBoxPreference mGestures;
    private CheckBoxPreference mVolumeNavigation;
    //private CheckBoxPreference mStartIntegratedInbox;
    private CheckBoxListPreference mConfirmActions;
    //private ListPreference mNotificationHideSubject;
    private CheckBoxPreference mMeasureAccounts;
    private CheckBoxPreference mCountSearch;
    /*private CheckBoxPreference mHideSpecialAccounts;
    private ListPreference mPreviewLines;
    private CheckBoxPreference mSenderAboveSubject;*/
    private CheckBoxPreference mCheckboxes;
    private CheckBoxPreference mStars;
    /*private CheckBoxPreference mShowCorrespondentNames;
    private CheckBoxPreference mShowContactName;
    private CheckBoxPreference mChangeContactNameColor;*/
    private CheckBoxPreference mShowContactPicture;
    private CheckBoxPreference mColorizeMissingContactPictures;
    /*private CheckBoxPreference mFixedWidth;
    private CheckBoxPreference mReturnToList;
    private CheckBoxPreference mShowNext;
    private CheckBoxPreference mAutofitWidth;*/
    private ListPreference mBackgroundOps;
    private CheckBoxPreference mDebugLogging;
    private CheckBoxPreference mSensitiveLogging;
    private CheckBoxPreference mHideUserAgent;
    private CheckBoxPreference mHideTimeZone;
    //private CheckBoxPreference mWrapFolderNames;
    //private CheckBoxListPreference mVisibleRefileActions;

    private CheckBoxPreference mQuietTimeEnabled;
    private CheckBoxPreference mDisableNotificationDuringQuietTime;
    private TimePickerPreference mQuietTimeStarts;
    private TimePickerPreference mQuietTimeEnds;
    private ListPreference mNotificationQuickDelete;
    private ListPreference mLockScreenNotificationVisibility;
    private Preference mAttachmentPathPreference;

    private CheckBoxPreference mBackgroundAsUnreadIndicator;
    //private CheckBoxPreference mThreadedView;
    //private ListPreference mSplitViewMode;


    public static void actionPrefs(Context context) {
        Intent i = new Intent(context, au.com.wallaceit.voicemail.activity.setup.Prefs.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_preferences);

        mLanguage = (ListPreference) findPreference(PREFERENCE_LANGUAGE);
        List<CharSequence> entryVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntries()));
        List<CharSequence> entryValueVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntryValues()));
        String supportedLanguages[] = getResources().getStringArray(R.array.supported_languages);
        Set<String> supportedLanguageSet = new HashSet<String>(Arrays.asList(supportedLanguages));
        for (int i = entryVector.size() - 1; i > -1; --i) {
            if (!supportedLanguageSet.contains(entryValueVector.get(i))) {
                entryVector.remove(i);
                entryValueVector.remove(i);
            }
        }
        initListPreference(mLanguage, VisualVoicemail.getK9Language(),
                           entryVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY),
                           entryValueVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY));

        mTheme = setupListPreference(PREFERENCE_THEME, themeIdToName(VisualVoicemail.getK9Theme()));
        /*mFixedMessageTheme = (CheckBoxPreference) findPreference(PREFERENCE_FIXED_MESSAGE_THEME);
        mFixedMessageTheme.setChecked(VisualVoicemail.useFixedMessageViewTheme());
        mMessageTheme = setupListPreference(PREFERENCE_MESSAGE_VIEW_THEME,
                themeIdToName(VisualVoicemail.getK9MessageViewThemeSetting()));
        mComposerTheme = setupListPreference(PREFERENCE_COMPOSER_THEME,
                themeIdToName(VisualVoicemail.getK9ComposerThemeSetting()));*/

        findPreference(PREFERENCE_FONT_SIZE).setOnPreferenceClickListener(
        new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onFontSizeSettings();
                return true;
            }
        });

        mAnimations = (CheckBoxPreference)findPreference(PREFERENCE_ANIMATIONS);
        mAnimations.setChecked(VisualVoicemail.showAnimations());

        mGestures = (CheckBoxPreference)findPreference(PREFERENCE_GESTURES);
        mGestures.setChecked(VisualVoicemail.gesturesEnabled());

        mVolumeNavigation = (CheckBoxPreference)findPreference(PREFERENCE_VOLUME_NAVIGATION);
        //mVolumeNavigation.setItems(new CharSequence[] {getString(R.string.volume_navigation_message), getString(R.string.volume_navigation_list)});
        //mVolumeNavigation.setCheckedItems(new boolean[] {VisualVoicemail.useVolumeKeysForNavigationEnabled(), VisualVoicemail.useVolumeKeysForListNavigationEnabled()});

        /*mStartIntegratedInbox = (CheckBoxPreference)findPreference(PREFERENCE_START_INTEGRATED_INBOX);
        mStartIntegratedInbox.setChecked(VisualVoicemail.startIntegratedInbox());*/

        mConfirmActions = (CheckBoxListPreference) findPreference(PREFERENCE_CONFIRM_ACTIONS);

        boolean canDeleteFromNotification = NotificationController.platformSupportsExtendedNotifications();
        CharSequence[] confirmActionEntries = new CharSequence[canDeleteFromNotification ? 5 : 4];
        boolean[] confirmActionValues = new boolean[canDeleteFromNotification ? 5 : 4];
        int index = 0;

        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete);
        confirmActionValues[index++] = VisualVoicemail.confirmDelete();
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete_starred);
        confirmActionValues[index++] = VisualVoicemail.confirmDeleteStarred();
        if (canDeleteFromNotification) {
            confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete_notif);
            confirmActionValues[index++] = VisualVoicemail.confirmDeleteFromNotification();
        }
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_spam);
        confirmActionValues[index++] = VisualVoicemail.confirmSpam();
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_menu_discard);
        confirmActionValues[index++] = VisualVoicemail.confirmDiscardMessage();

        mConfirmActions.setItems(confirmActionEntries);
        mConfirmActions.setCheckedItems(confirmActionValues);

        //mNotificationHideSubject = setupListPreference(PREFERENCE_NOTIFICATION_HIDE_SUBJECT, VisualVoicemail.getNotificationHideSubject().toString());

        mMeasureAccounts = (CheckBoxPreference)findPreference(PREFERENCE_MEASURE_ACCOUNTS);
        mMeasureAccounts.setChecked(VisualVoicemail.measureAccounts());

        mCountSearch = (CheckBoxPreference)findPreference(PREFERENCE_COUNT_SEARCH);
        mCountSearch.setChecked(VisualVoicemail.countSearchMessages());

        //mHideSpecialAccounts = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_SPECIAL_ACCOUNTS);
        //mHideSpecialAccounts.setChecked(VisualVoicemail.isHideSpecialAccounts());


        //mPreviewLines = setupListPreference(PREFERENCE_MESSAGELIST_PREVIEW_LINES, Integer.toString(VisualVoicemail.messageListPreviewLines()));

        //mSenderAboveSubject = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT);
        //mSenderAboveSubject.setChecked(VisualVoicemail.messageListSenderAboveSubject());
        mCheckboxes = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CHECKBOXES);
        mCheckboxes.setChecked(VisualVoicemail.messageListCheckboxes());

        mStars = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_STARS);
        mStars.setChecked(VisualVoicemail.messageListStars());

        /*mShowCorrespondentNames = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES);
        mShowCorrespondentNames.setChecked(VisualVoicemail.showCorrespondentNames());

        mShowContactName = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME);
        mShowContactName.setChecked(VisualVoicemail.showContactName());*/

        mShowContactPicture = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE);
        mShowContactPicture.setChecked(VisualVoicemail.showContactPicture());

        mColorizeMissingContactPictures = (CheckBoxPreference)findPreference(
                PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES);
        mColorizeMissingContactPictures.setChecked(VisualVoicemail.isColorizeMissingContactPictures());

        mBackgroundAsUnreadIndicator = (CheckBoxPreference)findPreference(PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR);
        mBackgroundAsUnreadIndicator.setChecked(VisualVoicemail.useBackgroundAsUnreadIndicator());

        /*mChangeContactNameColor = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR);
        mChangeContactNameColor.setChecked(VisualVoicemail.changeContactNameColor());

        mThreadedView = (CheckBoxPreference) findPreference(PREFERENCE_THREADED_VIEW);
        mThreadedView.setChecked(VisualVoicemail.isThreadedViewEnabled());*/

        /*if (VisualVoicemail.changeContactNameColor()) {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
        } else {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
        }
        mChangeContactNameColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final Boolean checked = (Boolean) newValue;
                if (checked) {
                    onChooseContactNameColor();
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
                } else {
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
                }
                mChangeContactNameColor.setChecked(checked);
                return false;
            }
        });*/

        /*mFixedWidth = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGEVIEW_FIXEDWIDTH);
        mFixedWidth.setChecked(VisualVoicemail.messageViewFixedWidthFont());

        mReturnToList = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST);
        mReturnToList.setChecked(VisualVoicemail.messageViewReturnToList());

        mShowNext = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_SHOW_NEXT);
        mShowNext.setChecked(VisualVoicemail.messageViewShowNext());

        mAutofitWidth = (CheckBoxPreference) findPreference(PREFERENCE_AUTOFIT_WIDTH);
        mAutofitWidth.setChecked(VisualVoicemail.autofitWidth());*/

        mQuietTimeEnabled = (CheckBoxPreference) findPreference(PREFERENCE_QUIET_TIME_ENABLED);
        mQuietTimeEnabled.setChecked(VisualVoicemail.getQuietTimeEnabled());

        mDisableNotificationDuringQuietTime = (CheckBoxPreference) findPreference(
                PREFERENCE_DISABLE_NOTIFICATION_DURING_QUIET_TIME);
        mDisableNotificationDuringQuietTime.setChecked(!VisualVoicemail.isNotificationDuringQuietTimeEnabled());
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

        mNotificationQuickDelete = setupListPreference(PREFERENCE_NOTIF_QUICK_DELETE,
                VisualVoicemail.getNotificationQuickDeleteBehaviour().toString());
        if (!NotificationController.platformSupportsExtendedNotifications()) {
            PreferenceScreen prefs = (PreferenceScreen) findPreference("notification_preferences");
            prefs.removePreference(mNotificationQuickDelete);
            mNotificationQuickDelete = null;
        }

        mLockScreenNotificationVisibility = setupListPreference(PREFERENCE_LOCK_SCREEN_NOTIFICATION_VISIBILITY,
            VisualVoicemail.getLockScreenNotificationVisibility().toString());
        if (!NotificationController.platformSupportsLockScreenNotifications()) {
            ((PreferenceScreen) findPreference("notification_preferences"))
                .removePreference(mLockScreenNotificationVisibility);
            mLockScreenNotificationVisibility = null;
        }

        mBackgroundOps = setupListPreference(PREFERENCE_BACKGROUND_OPS, VisualVoicemail.getBackgroundOps().name());

        mDebugLogging = (CheckBoxPreference)findPreference(PREFERENCE_DEBUG_LOGGING);
        mSensitiveLogging = (CheckBoxPreference)findPreference(PREFERENCE_SENSITIVE_LOGGING);
        mHideUserAgent = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_USERAGENT);
        mHideTimeZone = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_TIMEZONE);

        mDebugLogging.setChecked(VisualVoicemail.DEBUG);
        mSensitiveLogging.setChecked(VisualVoicemail.DEBUG_SENSITIVE);
        mHideUserAgent.setChecked(VisualVoicemail.hideUserAgent());
        mHideTimeZone.setChecked(VisualVoicemail.hideTimeZone());

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

        /*mWrapFolderNames = (CheckBoxPreference)findPreference(PREFERENCE_FOLDERLIST_WRAP_NAME);
        mWrapFolderNames.setChecked(VisualVoicemail.wrapFolderNames());

        mVisibleRefileActions = (CheckBoxListPreference) findPreference(PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS);
        CharSequence[] visibleRefileActionsEntries = new CharSequence[5];
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_DELETE] = getString(R.string.delete_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_ARCHIVE] = getString(R.string.archive_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_MOVE] = getString(R.string.move_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_COPY] = getString(R.string.copy_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_SPAM] = getString(R.string.spam_action);

        boolean[] visibleRefileActionsValues = new boolean[5];
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_DELETE] = VisualVoicemail.isMessageViewDeleteActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_ARCHIVE] = VisualVoicemail.isMessageViewArchiveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_MOVE] = VisualVoicemail.isMessageViewMoveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_COPY] = VisualVoicemail.isMessageViewCopyActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_SPAM] = VisualVoicemail.isMessageViewSpamActionVisible();

        mVisibleRefileActions.setItems(visibleRefileActionsEntries);
        mVisibleRefileActions.setCheckedItems(visibleRefileActionsValues);

        mSplitViewMode = (ListPreference) findPreference(PREFERENCE_SPLITVIEW_MODE);
        initListPreference(mSplitViewMode, VisualVoicemail.getSplitViewMode().name(),
                mSplitViewMode.getEntries(), mSplitViewMode.getEntryValues());*/
    }

    private static String themeIdToName(VisualVoicemail.Theme theme) {
        switch (theme) {
            case DARK: return "dark";
            case USE_GLOBAL: return "global";
            default: return "light";
        }
    }

    private static VisualVoicemail.Theme themeNameToId(String theme) {
        if (TextUtils.equals(theme, "dark")) {
            return VisualVoicemail.Theme.DARK;
        } else if (TextUtils.equals(theme, "global")) {
            return VisualVoicemail.Theme.USE_GLOBAL;
        } else {
            return VisualVoicemail.Theme.LIGHT;
        }
    }

    private void saveSettings() {
        Storage preferences = Preferences.getPreferences(this).getStorage();

        VisualVoicemail.setK9Language(mLanguage.getValue());

        VisualVoicemail.setK9Theme(themeNameToId(mTheme.getValue()));
        /*VisualVoicemail.setUseFixedMessageViewTheme(mFixedMessageTheme.isChecked());
        VisualVoicemail.setK9MessageViewThemeSetting(themeNameToId(mMessageTheme.getValue()));
        VisualVoicemail.setK9ComposerThemeSetting(themeNameToId(mComposerTheme.getValue()));*/

        VisualVoicemail.setAnimations(mAnimations.isChecked());
        VisualVoicemail.setGesturesEnabled(mGestures.isChecked());
        //VisualVoicemail.setUseVolumeKeysForNavigation(mVolumeNavigation.getCheckedItems()[0]);
        VisualVoicemail.setUseVolumeKeysForListNavigation(mVolumeNavigation.isChecked());
        /*VisualVoicemail.setStartIntegratedInbox(!mHideSpecialAccounts.isChecked() && mStartIntegratedInbox.isChecked());
        VisualVoicemail.setNotificationHideSubject(NotificationHideSubject.valueOf(mNotificationHideSubject.getValue()));*/

        int index = 0;
        VisualVoicemail.setConfirmDelete(mConfirmActions.getCheckedItems()[index++]);
        VisualVoicemail.setConfirmDeleteStarred(mConfirmActions.getCheckedItems()[index++]);
        if (NotificationController.platformSupportsExtendedNotifications()) {
            VisualVoicemail.setConfirmDeleteFromNotification(mConfirmActions.getCheckedItems()[index++]);
        }
        VisualVoicemail.setConfirmSpam(mConfirmActions.getCheckedItems()[index++]);
        VisualVoicemail.setConfirmDiscardMessage(mConfirmActions.getCheckedItems()[index++]);

        VisualVoicemail.setMeasureAccounts(mMeasureAccounts.isChecked());
        VisualVoicemail.setCountSearchMessages(mCountSearch.isChecked());
        //VisualVoicemail.setHideSpecialAccounts(mHideSpecialAccounts.isChecked());
        //VisualVoicemail.setMessageListPreviewLines(Integer.parseInt(mPreviewLines.getValue()));
        VisualVoicemail.setMessageListCheckboxes(mCheckboxes.isChecked());
        VisualVoicemail.setMessageListStars(mStars.isChecked());
        //VisualVoicemail.setShowCorrespondentNames(mShowCorrespondentNames.isChecked());
        //VisualVoicemail.setMessageListSenderAboveSubject(mSenderAboveSubject.isChecked());
        //VisualVoicemail.setShowContactName(mShowContactName.isChecked());
        VisualVoicemail.setShowContactPicture(mShowContactPicture.isChecked());
        VisualVoicemail.setColorizeMissingContactPictures(mColorizeMissingContactPictures.isChecked());
        VisualVoicemail.setUseBackgroundAsUnreadIndicator(mBackgroundAsUnreadIndicator.isChecked());
        /*VisualVoicemail.setThreadedViewEnabled(mThreadedView.isChecked());
        VisualVoicemail.setChangeContactNameColor(mChangeContactNameColor.isChecked());
        VisualVoicemail.setMessageViewFixedWidthFont(mFixedWidth.isChecked());
        VisualVoicemail.setMessageViewReturnToList(mReturnToList.isChecked());
        VisualVoicemail.setMessageViewShowNext(mShowNext.isChecked());
        VisualVoicemail.setAutofitWidth(mAutofitWidth.isChecked());*/
        VisualVoicemail.setQuietTimeEnabled(mQuietTimeEnabled.isChecked());

        /*boolean[] enabledRefileActions = mVisibleRefileActions.getCheckedItems();
        VisualVoicemail.setMessageViewDeleteActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_DELETE]);
        VisualVoicemail.setMessageViewArchiveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_ARCHIVE]);
        VisualVoicemail.setMessageViewMoveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_MOVE]);
        VisualVoicemail.setMessageViewCopyActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_COPY]);
        VisualVoicemail.setMessageViewSpamActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_SPAM]);*/

        VisualVoicemail.setNotificationDuringQuietTimeEnabled(!mDisableNotificationDuringQuietTime.isChecked());
        VisualVoicemail.setQuietTimeStarts(mQuietTimeStarts.getTime());
        VisualVoicemail.setQuietTimeEnds(mQuietTimeEnds.getTime());
        //VisualVoicemail.setWrapFolderNames(mWrapFolderNames.isChecked());

        if (mNotificationQuickDelete != null) {
            VisualVoicemail.setNotificationQuickDeleteBehaviour(
                    NotificationQuickDelete.valueOf(mNotificationQuickDelete.getValue()));
        }

        if(mLockScreenNotificationVisibility != null) {
            VisualVoicemail.setLockScreenNotificationVisibility(
                    VisualVoicemail.LockScreenNotificationVisibility.valueOf(mLockScreenNotificationVisibility.getValue()));
        }

        //VisualVoicemail.setSplitViewMode(SplitViewMode.valueOf(mSplitViewMode.getValue()));
        VisualVoicemail.setAttachmentDefaultPath(mAttachmentPathPreference.getSummary().toString());
        boolean needsRefresh = VisualVoicemail.setBackgroundOps(mBackgroundOps.getValue());

        if (!VisualVoicemail.DEBUG && mDebugLogging.isChecked()) {
            Toast.makeText(this, R.string.debug_logging_enabled, Toast.LENGTH_LONG).show();
        }
        VisualVoicemail.DEBUG = mDebugLogging.isChecked();
        VisualVoicemail.DEBUG_SENSITIVE = mSensitiveLogging.isChecked();
        VisualVoicemail.setHideUserAgent(mHideUserAgent.isChecked());
        VisualVoicemail.setHideTimeZone(mHideTimeZone.isChecked());

        StorageEditor editor = preferences.edit();
        VisualVoicemail.save(editor);
        editor.commit();

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

    private void onChooseContactNameColor() {
        new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                VisualVoicemail.setContactNameColor(color);
            }
        },
        VisualVoicemail.getContactNameColor()).show();
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
