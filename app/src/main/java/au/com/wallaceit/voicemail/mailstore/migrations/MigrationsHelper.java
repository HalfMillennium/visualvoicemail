package au.com.wallaceit.voicemail.mailstore.migrations;


import java.util.List;

import android.content.Context;

import au.com.wallaceit.voicemail.Account;
import com.fsck.k9.mail.Flag;
import au.com.wallaceit.voicemail.mailstore.LocalStore;
import au.com.wallaceit.voicemail.preferences.Storage;


/**
 * Helper to allow accessing classes and methods that aren't visible or accessible to the 'migrations' package
 */
public interface MigrationsHelper {
    LocalStore getLocalStore();
    Storage getStorage();
    Account getAccount();
    Context getContext();
    String serializeFlags(List<Flag> flags);
}
