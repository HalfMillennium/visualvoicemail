package au.com.wallaceit.voicemail.account;


import android.content.Context;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.Account.DeletePolicy;
import au.com.wallaceit.voicemail.R;

import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings.Type;


/**
 * Deals with logic surrounding account creation.
 * <p/>
 * TODO Move much of the code from au.com.wallaceit.voicemail.activity.setup.* into here
 */
public class AccountCreator {

    public static Account initialVisualVoicemailSetup(Context context, Account account){

        account.setDraftsFolderName(context.getString(R.string.special_mailbox_name_drafts));
        account.setTrashFolderName(context.getString(R.string.special_mailbox_name_trash));
        account.setArchiveFolderName(context.getString(R.string.special_mailbox_name_archive));
        account.setSpamFolderName(context.getString(R.string.special_mailbox_name_spam));
        account.setSentFolderName(context.getString(R.string.special_mailbox_name_sent));

        account.setMaximumAutoDownloadMessageSize(0);
        account.setMaximumPolledMessageAge(-1);
        account.setSubscribedFoldersOnly(false);
        account.setDeletePolicy(Account.DeletePolicy.ON_DELETE);

        return account;
    }

    public static DeletePolicy getDefaultDeletePolicy(Type type) {
        switch (type) {
            case IMAP: {
                return DeletePolicy.ON_DELETE;
            }
            case POP3: {
                return DeletePolicy.NEVER;
            }
            case WebDAV: {
                return DeletePolicy.ON_DELETE;
            }
            case SMTP: {
                throw new IllegalStateException("Delete policy doesn't apply to SMTP");
            }
        }

        throw new AssertionError("Unhandled case: " + type);
    }

    public static int getDefaultPort(ConnectionSecurity securityType, Type storeType) {
        switch (securityType) {
            case NONE:
            case STARTTLS_REQUIRED: {
                return storeType.defaultPort;
            }
            case SSL_TLS_REQUIRED: {
                return storeType.defaultTlsPort;
            }
        }

        throw new AssertionError("Unhandled ConnectionSecurity type encountered: " + securityType);
    }
}
