package au.com.wallaceit.voicemail.preferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.SharedPreferences;
import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.Account.DeletePolicy;
import au.com.wallaceit.voicemail.Account.Expunge;
import au.com.wallaceit.voicemail.Account.FolderMode;
import au.com.wallaceit.voicemail.Account.MessageFormat;
import au.com.wallaceit.voicemail.Account.QuoteStyle;
import au.com.wallaceit.voicemail.Account.Searchable;
import au.com.wallaceit.voicemail.Account.ShowPictures;
import au.com.wallaceit.voicemail.Account.SortType;
import au.com.wallaceit.voicemail.VisualVoicemail;
import com.fsck.k9.R;
import au.com.wallaceit.voicemail.mailstore.StorageManager;
import au.com.wallaceit.voicemail.preferences.*;
import au.com.wallaceit.voicemail.preferences.Settings;
import au.com.wallaceit.voicemail.preferences.Settings.*;

public class AccountSettings {
    public static final Map<String, TreeMap<Integer, SettingsDescription>> SETTINGS;
    public static final Map<Integer, SettingsUpgrader> UPGRADERS;

    static {
        Map<String, TreeMap<Integer, SettingsDescription>> s =
            new LinkedHashMap<String, TreeMap<Integer, SettingsDescription>>();

        /**
         * When adding new settings here, be sure to increment {@link au.com.wallaceit.voicemail.preferences.Settings.VERSION}
         * and use that for whatever you add here.
         */

        s.put("alwaysBcc", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(11, new StringSetting(""))
        ));
        s.put("alwaysShowCcBcc", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(13, new BooleanSetting(false))
        ));
        s.put("archiveFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("Archive"))
        ));
        s.put("autoExpandFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("INBOX"))
        ));
        s.put("automaticCheckIntervalMinutes", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(-1,
                        R.array.account_settings_check_frequency_values))
        ));
        s.put("chipColor", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new ColorSetting(0xFF0000FF))
        ));
        s.put("cryptoApp", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("apg")),
                new V(36, new StringSetting(Account.NO_OPENPGP_PROVIDER))
        ));
        s.put("defaultQuotedTextShown", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(Account.DEFAULT_QUOTED_TEXT_SHOWN))
        ));
        s.put("deletePolicy", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new DeletePolicySetting(DeletePolicy.NEVER))
        ));
        s.put("displayCount", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(VisualVoicemail.DEFAULT_VISIBLE_LIMIT,
                        R.array.account_settings_display_count_values))
        ));
        s.put("draftsFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("Drafts"))
        ));
        s.put("expungePolicy", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringResourceSetting(Expunge.EXPUNGE_IMMEDIATELY.name(),
                        R.array.account_setup_expunge_policy_values))
        ));
        s.put("folderDisplayMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<FolderMode>(FolderMode.class, FolderMode.NOT_SECOND_CLASS))
        ));
        s.put("folderPushMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<FolderMode>(FolderMode.class, FolderMode.FIRST_CLASS))
        ));
        s.put("folderSyncMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<FolderMode>(FolderMode.class, FolderMode.FIRST_CLASS))
        ));
        s.put("folderTargetMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<FolderMode>(FolderMode.class, FolderMode.NOT_SECOND_CLASS))
        ));
        s.put("goToUnreadMessageSearch", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("idleRefreshMinutes", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(24, R.array.idle_refresh_period_values))
        ));
        s.put("inboxFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("INBOX"))
        ));
        s.put("led", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("ledColor", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new ColorSetting(0xFF0000FF))
        ));
        s.put("localStorageProvider", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StorageProviderSetting())
        ));
        s.put("markMessageAsReadOnView", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(7, new BooleanSetting(true))
        ));
        s.put("maxPushFolders", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerRangeSetting(0, 100, 10))
        ));
        s.put("maximumAutoDownloadMessageSize", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(32768,
                        R.array.account_settings_autodownload_message_size_values))
        ));
        s.put("maximumPolledMessageAge", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(-1,
                        R.array.account_settings_message_age_values))
        ));
        s.put("messageFormat", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<MessageFormat>(
                        MessageFormat.class, Account.DEFAULT_MESSAGE_FORMAT))
        ));
        s.put("messageFormatAuto", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(2, new BooleanSetting(Account.DEFAULT_MESSAGE_FORMAT_AUTO))
        ));
        s.put("messageReadReceipt", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(Account.DEFAULT_MESSAGE_READ_RECEIPT))
        ));
        s.put("notifyMailCheck", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("notifyNewMail", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("folderNotifyNewMailMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(34, new EnumSetting<FolderMode>(FolderMode.class, FolderMode.ALL))
        ));
        s.put("notifySelfNewMail", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("pushPollOnConnect", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("quotePrefix", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting(Account.DEFAULT_QUOTE_PREFIX))
        ));
        s.put("quoteStyle", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<QuoteStyle>(
                        QuoteStyle.class, Account.DEFAULT_QUOTE_STYLE))
        ));
        s.put("replyAfterQuote", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(Account.DEFAULT_REPLY_AFTER_QUOTE))
        ));
        s.put("ring", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("ringtone", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new RingtoneSetting("content://settings/system/notification_sound"))
        ));
        s.put("searchableFolders", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<Searchable>(
                        Searchable.class, Searchable.ALL))
        ));
        s.put("sentFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("Sent"))
        ));
        s.put("sortTypeEnum", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(9, new EnumSetting<SortType>(SortType.class, Account.DEFAULT_SORT_TYPE))
        ));
        s.put("sortAscending", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(9, new BooleanSetting(Account.DEFAULT_SORT_ASCENDING))
        ));
        s.put("showPicturesEnum", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<ShowPictures>(
                        ShowPictures.class, ShowPictures.NEVER))
        ));
        s.put("signatureBeforeQuotedText", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("spamFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("Spam"))
        ));
        s.put("stripSignature", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(2, new BooleanSetting(Account.DEFAULT_STRIP_SIGNATURE))
        ));
        s.put("subscribedFoldersOnly", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("syncRemoteDeletions", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("trashFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new StringSetting("Trash"))
        ));
        s.put("useCompression.MOBILE", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("useCompression.OTHER", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("useCompression.WIFI", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("vibrate", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("vibratePattern", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(0,
                        R.array.account_settings_vibrate_pattern_values))
        ));
        s.put("vibrateTimes", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerResourceSetting(5,
                        R.array.account_settings_vibrate_times_label))
        ));
        s.put("allowRemoteSearch", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(18, new BooleanSetting(true))
        ));
        s.put("remoteSearchNumResults", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(18, new IntegerResourceSetting(Account.DEFAULT_REMOTE_SEARCH_NUM_RESULTS,
                        R.array.account_settings_remote_search_num_results_values))
        ));
        s.put("remoteSearchFullText", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(18, new BooleanSetting(false))
        ));

        SETTINGS = Collections.unmodifiableMap(s);

        Map<Integer, SettingsUpgrader> u = new HashMap<Integer, SettingsUpgrader>();
        UPGRADERS = Collections.unmodifiableMap(u);
    }

    public static Map<String, Object> validate(int version, Map<String, String> importedSettings,
            boolean useDefaultValues) {
        return au.com.wallaceit.voicemail.preferences.Settings.validate(version, SETTINGS, importedSettings, useDefaultValues);
    }

    public static Set<String> upgrade(int version, Map<String, Object> validatedSettings) {
        return au.com.wallaceit.voicemail.preferences.Settings.upgrade(version, UPGRADERS, SETTINGS, validatedSettings);
    }

    public static Map<String, String> convert(Map<String, Object> settings) {
        return Settings.convert(settings, SETTINGS);
    }

    public static Map<String, String> getAccountSettings(SharedPreferences storage, String uuid) {
        Map<String, String> result = new HashMap<String, String>();
        String prefix = uuid + ".";
        for (String key : SETTINGS.keySet()) {
            String value = storage.getString(prefix + key, null);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * An integer resource setting.
     *
     * <p>
     * Basically a {@link PseudoEnumSetting} that is initialized from a resource array containing
     * integer strings.
     * </p>
     */
    public static class IntegerResourceSetting extends PseudoEnumSetting<Integer> {
        private final Map<Integer, String> mMapping;

        public IntegerResourceSetting(int defaultValue, int resId) {
            super(defaultValue);

            Map<Integer, String> mapping = new HashMap<Integer, String>();
            String[] values = VisualVoicemail.app.getResources().getStringArray(resId);
            for (String value : values) {
                int intValue = Integer.parseInt(value);
                mapping.put(intValue, value);
            }
            mMapping = Collections.unmodifiableMap(mapping);
        }

        @Override
        protected Map<Integer, String> getMapping() {
            return mMapping;
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new InvalidSettingValueException();
            }
        }
    }

    /**
     * A string resource setting.
     *
     * <p>
     * Basically a {@link PseudoEnumSetting} that is initialized from a resource array.
     * </p>
     */
    public static class StringResourceSetting extends PseudoEnumSetting<String> {
        private final Map<String, String> mMapping;

        public StringResourceSetting(String defaultValue, int resId) {
            super(defaultValue);

            Map<String, String> mapping = new HashMap<String, String>();
            String[] values = VisualVoicemail.app.getResources().getStringArray(resId);
            for (String value : values) {
                mapping.put(value, value);
            }
            mMapping = Collections.unmodifiableMap(mapping);
        }

        @Override
        protected Map<String, String> getMapping() {
            return mMapping;
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            if (!mMapping.containsKey(value)) {
                throw new InvalidSettingValueException();
            }
            return value;
        }
    }

    /**
     * The notification ringtone setting.
     */
    public static class RingtoneSetting extends SettingsDescription {
        public RingtoneSetting(String defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) {
            //TODO: add validation
            return value;
        }
    }

    /**
     * The storage provider setting.
     */
    public static class StorageProviderSetting extends SettingsDescription {
        public StorageProviderSetting() {
            super(null);
        }

        @Override
        public Object getDefaultValue() {
            return StorageManager.getInstance(VisualVoicemail.app).getDefaultProviderId();
        }

        @Override
        public Object fromString(String value) {
            StorageManager storageManager = StorageManager.getInstance(VisualVoicemail.app);
            Map<String, String> providers = storageManager.getAvailableProviders();
            if (providers.containsKey(value)) {
                return value;
            }
            throw new RuntimeException("Validation failed");
        }
    }

    public static class DeletePolicySetting extends PseudoEnumSetting<Integer> {
        private Map<Integer, String> mMapping;

        public DeletePolicySetting(DeletePolicy defaultValue) {
            super(defaultValue);
            Map<Integer, String> mapping = new HashMap<Integer, String>();
            mapping.put(DeletePolicy.NEVER.setting, "NEVER");
            mapping.put(DeletePolicy.ON_DELETE.setting, "DELETE");
            mapping.put(DeletePolicy.MARK_AS_READ.setting, "MARK_AS_READ");
            mMapping = Collections.unmodifiableMap(mapping);
        }

        @Override
        protected Map<Integer, String> getMapping() {
            return mMapping;
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                Integer deletePolicy = Integer.parseInt(value);
                if (mMapping.containsKey(deletePolicy)) {
                    return deletePolicy;
                }
            } catch (NumberFormatException e) { /* do nothing */ }

            throw new InvalidSettingValueException();
        }
    }
}
