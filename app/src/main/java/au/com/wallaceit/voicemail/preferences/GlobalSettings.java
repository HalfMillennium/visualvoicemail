package au.com.wallaceit.voicemail.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.SharedPreferences;
import android.os.Environment;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.FontSizes;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.VisualVoicemail.NotificationHideSubject;
import au.com.wallaceit.voicemail.VisualVoicemail.NotificationQuickDelete;
import au.com.wallaceit.voicemail.VisualVoicemail.SplitViewMode;
import au.com.wallaceit.voicemail.VisualVoicemail.Theme;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.Account.SortType;
import au.com.wallaceit.voicemail.preferences.*;
import au.com.wallaceit.voicemail.preferences.Settings;
import au.com.wallaceit.voicemail.preferences.Settings.*;
import au.com.wallaceit.voicemail.preferences.TimePickerPreference;

import static au.com.wallaceit.voicemail.VisualVoicemail.LockScreenNotificationVisibility;

public class GlobalSettings {
    public static final Map<String, TreeMap<Integer, SettingsDescription>> SETTINGS;
    public static final Map<Integer, SettingsUpgrader> UPGRADERS;

    static {
        Map<String, TreeMap<Integer, SettingsDescription>> s =
            new LinkedHashMap<String, TreeMap<Integer, SettingsDescription>>();

        /**
         * When adding new settings here, be sure to increment {@link au.com.wallaceit.voicemail.preferences.Settings.VERSION}
         * and use that for whatever you add here.
         */

        s.put("animations", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("attachmentdefaultpath", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new DirectorySetting(Environment.getExternalStorageDirectory().toString()))
        ));
        s.put("backgroundOperations", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new EnumSetting<VisualVoicemail.BACKGROUND_OPS>(
                        VisualVoicemail.BACKGROUND_OPS.class, VisualVoicemail.BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC))
        ));
        s.put("changeRegisteredNameColor", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("confirmDelete", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("confirmDeleteStarred", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(2, new BooleanSetting(false))
        ));
        s.put("confirmSpam", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("countSearchMessages", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("enableDebugLogging", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("enableSensitiveLogging", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("fontSizeAccountDescription", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeAccountName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeFolderName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeFolderStatus", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageComposeInput", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(5, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageListDate", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageListPreview", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageListSender", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageListSubject", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewAdditionalHeaders", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewCC", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewContent", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new WebFontSizeSetting(3)),
                new V(31, null)
        ));
        s.put("fontSizeMessageViewDate", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewSender", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewSubject", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewTime", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("fontSizeMessageViewTo", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
        ));
        s.put("gesturesEnabled", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true)),
                new V(4, new BooleanSetting(false))
        ));
        s.put("hideSpecialAccounts", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("keyguardPrivacy", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false)),
                new V(12, null)
        ));
        s.put("language", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new LanguageSetting())
        ));
        s.put("measureAccounts", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("messageListCheckboxes", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("messageListPreviewLines", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new IntegerRangeSetting(1, 100, 2))
        ));
        s.put("messageListStars", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("messageViewFixedWidthFont", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("messageViewReturnToList", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("messageViewShowNext", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("quietTimeEnabled", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("quietTimeEnds", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new TimeSetting("7:00"))
        ));
        s.put("quietTimeStarts", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new TimeSetting("21:00"))
        ));
        s.put("registeredNameColor", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new ColorSetting(0xFF00008F))
        ));
        s.put("showContactName", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("showCorrespondentNames", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(true))
        ));
        s.put("sortTypeEnum", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(10, new EnumSetting<SortType>(SortType.class, Account.DEFAULT_SORT_TYPE))
        ));
        s.put("sortAscending", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(10, new BooleanSetting(Account.DEFAULT_SORT_ASCENDING))
        ));
        s.put("startIntegratedInbox", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("theme", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new ThemeSetting(VisualVoicemail.Theme.LIGHT))
        ));
        s.put("messageViewTheme", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(16, new ThemeSetting(VisualVoicemail.Theme.LIGHT)),
                new V(24, new SubThemeSetting(VisualVoicemail.Theme.USE_GLOBAL))
        ));
        s.put("useVolumeKeysForListNavigation", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("useVolumeKeysForNavigation", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(1, new BooleanSetting(false))
        ));
        s.put("wrapFolderNames", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(22, new BooleanSetting(false))
        ));
        s.put("notificationHideSubject", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(12, new EnumSetting<NotificationHideSubject>(
                        NotificationHideSubject.class, NotificationHideSubject.NEVER))
        ));
        s.put("useBackgroundAsUnreadIndicator", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(19, new BooleanSetting(true))
        ));
        s.put("threadedView", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(20, new BooleanSetting(true))
        ));
        s.put("splitViewMode", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(23, new EnumSetting<SplitViewMode>(SplitViewMode.class, SplitViewMode.NEVER))
        ));
        s.put("messageComposeTheme", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(24, new SubThemeSetting(VisualVoicemail.Theme.USE_GLOBAL))
        ));
        s.put("fixedMessageViewTheme", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(24, new BooleanSetting(true))
        ));
        s.put("showContactPicture", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(25, new BooleanSetting(true))
        ));
        s.put("autofitWidth", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(28, new BooleanSetting(true))
        ));
        s.put("colorizeMissingContactPictures", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(29, new BooleanSetting(true))
        ));
        s.put("messageViewDeleteActionVisible", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(30, new BooleanSetting(true))
        ));
        s.put("messageViewArchiveActionVisible", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(30, new BooleanSetting(false))
        ));
        s.put("messageViewMoveActionVisible", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(30, new BooleanSetting(false))
        ));
        s.put("messageViewCopyActionVisible", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(30, new BooleanSetting(false))
        ));
        s.put("messageViewSpamActionVisible", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(30, new BooleanSetting(false))
        ));
        s.put("fontSizeMessageViewContentPercent", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(31, new IntegerRangeSetting(40, 250, 100))
        ));
        s.put("hideUserAgent", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(32, new BooleanSetting(false))
        ));
        s.put("hideTimeZone", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(32, new BooleanSetting(false))
        ));
        s.put("lockScreenNotificationVisibility", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(37, new EnumSetting<LockScreenNotificationVisibility>(LockScreenNotificationVisibility.class,
                        LockScreenNotificationVisibility.MESSAGE_COUNT))
        ));
        s.put("confirmDeleteFromNotification", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(38, new BooleanSetting(true))
        ));
        s.put("messageListSenderAboveSubject", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(38, new BooleanSetting(false))
        ));
        s.put("notificationQuickDelete", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(38, new EnumSetting<NotificationQuickDelete>(NotificationQuickDelete.class,
                        NotificationQuickDelete.NEVER))
        ));
        s.put("notificationDuringQuietTimeEnabled", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(39, new BooleanSetting(true))
        ));
        s.put("confirmDiscardMessage", au.com.wallaceit.voicemail.preferences.Settings.versions(
                new V(40, new BooleanSetting(true))
        ));

        SETTINGS = Collections.unmodifiableMap(s);

        Map<Integer, SettingsUpgrader> u = new HashMap<Integer, SettingsUpgrader>();
        u.put(12, new SettingsUpgraderV12());
        u.put(24, new SettingsUpgraderV24());
        u.put(31, new SettingsUpgraderV31());

        UPGRADERS = Collections.unmodifiableMap(u);
    }

    public static Map<String, Object> validate(int version, Map<String, String> importedSettings) {
        return au.com.wallaceit.voicemail.preferences.Settings.validate(version, SETTINGS, importedSettings, false);
    }

    public static Set<String> upgrade(int version, Map<String, Object> validatedSettings) {
        return au.com.wallaceit.voicemail.preferences.Settings.upgrade(version, UPGRADERS, SETTINGS, validatedSettings);
    }

    public static Map<String, String> convert(Map<String, Object> settings) {
        return Settings.convert(settings, SETTINGS);
    }

    public static Map<String, String> getGlobalSettings(SharedPreferences storage) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : SETTINGS.keySet()) {
            String value = storage.getString(key, null);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Upgrades the settings from version 11 to 12
     *
     * Map the 'keyguardPrivacy' value to the new NotificationHideSubject enum.
     */
    public static class SettingsUpgraderV12 implements SettingsUpgrader {

        @Override
        public Set<String> upgrade(Map<String, Object> settings) {
            Boolean keyguardPrivacy = (Boolean) settings.get("keyguardPrivacy");
            if (keyguardPrivacy != null && keyguardPrivacy.booleanValue()) {
                // current setting: only show subject when unlocked
                settings.put("notificationHideSubject", NotificationHideSubject.WHEN_LOCKED);
            } else {
                // always show subject [old default]
                settings.put("notificationHideSubject", NotificationHideSubject.NEVER);
            }
            return new HashSet<String>(Arrays.asList("keyguardPrivacy"));
        }
    }

    /**
     * Upgrades the settings from version 23 to 24.
     *
     * <p>
     * Set <em>messageViewTheme</em> to {@link VisualVoicemail.Theme#USE_GLOBAL} if <em>messageViewTheme</em> has
     * the same value as <em>theme</em>.
     * </p>
     */
    public static class SettingsUpgraderV24 implements SettingsUpgrader {

        @Override
        public Set<String> upgrade(Map<String, Object> settings) {
            VisualVoicemail.Theme messageViewTheme = (VisualVoicemail.Theme) settings.get("messageViewTheme");
            VisualVoicemail.Theme theme = (VisualVoicemail.Theme) settings.get("theme");
            if (theme != null && messageViewTheme != null && theme == messageViewTheme) {
                settings.put("messageViewTheme", VisualVoicemail.Theme.USE_GLOBAL);
            }

            return null;
        }
    }

    /**
     * Upgrades the settings from version 30 to 31.
     *
     * <p>
     * Convert value from <em>fontSizeMessageViewContent</em> to
     * <em>fontSizeMessageViewContentPercent</em>.
     * </p>
     */
    public static class SettingsUpgraderV31 implements SettingsUpgrader {

        @Override
        public Set<String> upgrade(Map<String, Object> settings) {
            int oldSize = ((Integer) settings.get("fontSizeMessageViewContent")).intValue();

            int newSize = convertFromOldSize(oldSize);

            settings.put("fontSizeMessageViewContentPercent", newSize);

            return new HashSet<String>(Arrays.asList("fontSizeMessageViewContent"));
        }

        public static int convertFromOldSize(int oldSize) {
            switch (oldSize) {
                case 1: {
                    return 40;
                }
                case 2: {
                    return 75;
                }
                case 4: {
                    return 175;
                }
                case 5: {
                    return 250;
                }
                case 3:
                default: {
                    return 100;
                }
            }
        }
    }

    /**
     * The language setting.
     *
     * <p>
     * Valid values are read from {@code settings_language_values} in
     * {@code res/values/arrays.xml}.
     * </p>
     */
    public static class LanguageSetting extends PseudoEnumSetting<String> {
        private final Map<String, String> mMapping;

        public LanguageSetting() {
            super("");

            Map<String, String> mapping = new HashMap<String, String>();
            String[] values = VisualVoicemail.app.getResources().getStringArray(R.array.settings_language_values);
            for (String value : values) {
                if (value.length() == 0) {
                    mapping.put("", "default");
                } else {
                    mapping.put(value, value);
                }
            }
            mMapping = Collections.unmodifiableMap(mapping);
        }

        @Override
        protected Map<String, String> getMapping() {
            return mMapping;
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            if (mMapping.containsKey(value)) {
                return value;
            }

            throw new InvalidSettingValueException();
        }
    }

    /**
     * The theme setting.
     */
    public static class ThemeSetting extends SettingsDescription {
        private static final String THEME_LIGHT = "light";
        private static final String THEME_DARK = "dark";

        public ThemeSetting(VisualVoicemail.Theme defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                Integer theme = Integer.parseInt(value);
                if (theme == VisualVoicemail.Theme.LIGHT.ordinal() ||
                        // We used to store the resource ID of the theme in the preference storage,
                        // but don't use the database upgrade mechanism to update the values. So
                        // we have to deal with the old format here.
                        theme == android.R.style.Theme_Light) {
                    return VisualVoicemail.Theme.LIGHT;
                } else if (theme == VisualVoicemail.Theme.DARK.ordinal() || theme == android.R.style.Theme) {
                    return VisualVoicemail.Theme.DARK;
                }
            } catch (NumberFormatException e) { /* do nothing */ }

            throw new InvalidSettingValueException();
        }

        @Override
        public Object fromPrettyString(String value) throws InvalidSettingValueException {
            if (THEME_LIGHT.equals(value)) {
                return VisualVoicemail.Theme.LIGHT;
            } else if (THEME_DARK.equals(value)) {
                return VisualVoicemail.Theme.DARK;
            }

            throw new InvalidSettingValueException();
        }

        @Override
        public String toPrettyString(Object value) {
            switch ((VisualVoicemail.Theme) value) {
                case DARK: {
                    return THEME_DARK;
                }
                default: {
                    return THEME_LIGHT;
                }
            }
        }

        @Override
        public String toString(Object value) {
            return Integer.toString(((VisualVoicemail.Theme) value).ordinal());
        }
    }

    /**
     * The message view theme setting.
     */
    public static class SubThemeSetting extends ThemeSetting {
        private static final String THEME_USE_GLOBAL = "use_global";

        public SubThemeSetting(Theme defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                Integer theme = Integer.parseInt(value);
                if (theme == VisualVoicemail.Theme.USE_GLOBAL.ordinal()) {
                    return VisualVoicemail.Theme.USE_GLOBAL;
                }

                return super.fromString(value);
            } catch (NumberFormatException e) {
                throw new InvalidSettingValueException();
            }
        }

        @Override
        public Object fromPrettyString(String value) throws InvalidSettingValueException {
            if (THEME_USE_GLOBAL.equals(value)) {
                return VisualVoicemail.Theme.USE_GLOBAL;
            }

            return super.fromPrettyString(value);
        }

        @Override
        public String toPrettyString(Object value) {
            if (((VisualVoicemail.Theme) value) == VisualVoicemail.Theme.USE_GLOBAL) {
                return THEME_USE_GLOBAL;
            }

            return super.toPrettyString(value);
        }
    }

    /**
     * A time setting.
     */
    public static class TimeSetting extends SettingsDescription {
        public TimeSetting(String defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            if (!value.matches(TimePickerPreference.VALIDATION_EXPRESSION)) {
                throw new InvalidSettingValueException();
            }
            return value;
        }
    }

    /**
     * A directory on the file system.
     */
    public static class DirectorySetting extends SettingsDescription {
        public DirectorySetting(String defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                if (new File(value).isDirectory()) {
                    return value;
                }
            } catch (Exception e) { /* do nothing */ }

            throw new InvalidSettingValueException();
        }
    }
}
