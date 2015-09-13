package au.com.wallaceit.voicemail.search;

import android.content.Context;

import au.com.wallaceit.voicemail.BaseAccount;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.search.SearchSpecification.Attribute;
import au.com.wallaceit.voicemail.search.SearchSpecification.SearchField;


/**
 * This class is basically a wrapper around a LocalSearch. It allows to expose it as
 * an account. This is a meta-account containing all the e-mail that matches the search.
 */
public class SearchAccount implements BaseAccount {
    public static final String ALL_MESSAGES = "all_messages";
    public static final String UNIFIED_INBOX = "unified_inbox";


    // create the all messages search ( all accounts is default when none specified )
    public static au.com.wallaceit.voicemail.search.SearchAccount createAllMessagesAccount(Context context) {
        String name = context.getString(R.string.search_all_messages_title);

        au.com.wallaceit.voicemail.search.LocalSearch tmpSearch = new au.com.wallaceit.voicemail.search.LocalSearch(name);
        tmpSearch.and(SearchField.SEARCHABLE, "1", Attribute.EQUALS);

        return new au.com.wallaceit.voicemail.search.SearchAccount(ALL_MESSAGES, tmpSearch, name,
                context.getString(R.string.search_all_messages_detail));
    }


    // create the unified inbox meta account ( all accounts is default when none specified )
    public static au.com.wallaceit.voicemail.search.SearchAccount createUnifiedInboxAccount(Context context) {
        String name = context.getString(R.string.integrated_inbox_title);
        au.com.wallaceit.voicemail.search.LocalSearch tmpSearch = new au.com.wallaceit.voicemail.search.LocalSearch(name);
        tmpSearch.and(SearchField.INTEGRATE, "1", Attribute.EQUALS);
        return new au.com.wallaceit.voicemail.search.SearchAccount(UNIFIED_INBOX, tmpSearch, name,
                context.getString(R.string.integrated_inbox_detail));
    }

    private String mId;
    private String mPhoneNumber;
    private String mDescription;
    private au.com.wallaceit.voicemail.search.LocalSearch mSearch;

    public SearchAccount(String id, au.com.wallaceit.voicemail.search.LocalSearch search, String description, String email)
            throws IllegalArgumentException {

        if (search == null) {
            throw new IllegalArgumentException("Provided LocalSearch was null");
        }

        mId = id;
        mSearch = search;
        mDescription = description;
        mPhoneNumber = email;
    }

    public String getId() {
        return mId;
    }

    @Override
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public void setDescription(String description) {
        this.mDescription = description;
    }

    public LocalSearch getRelatedSearch() {
        return mSearch;
    }

    /**
     * Returns the ID of this {@code SearchAccount} instance.
     *
     * <p>
     * This isn't really a UUID. But since we don't expose this value to other apps and we only
     * use the account UUID as opaque string (e.g. as key in a {@code Map}) we're fine.<br>
     * Using a constant string is necessary to identify the same search account even when the
     * corresponding {@link au.com.wallaceit.voicemail.search.SearchAccount} object has been recreated.
     * </p>
     */
    @Override
    public String getUuid() {
        return mId;
    }
}
