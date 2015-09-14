package au.com.wallaceit.voicemail.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.fsck.k9.mail.Address;

import au.com.wallaceit.voicemail.VisualVoicemail;

/**
 * Helper class to access the contacts stored on the device.
 */

public class VvmContacts {
    public static final String NUMBER_WITHHELD = "Unknown";
    /**
     * The order in which the search results are returned by
     * {@link #searchContacts(CharSequence)}.
     */
    protected static final String SORT_ORDER = Email.TIMES_CONTACTED + " DESC, " + Contacts.DISPLAY_NAME + ", " + Email._ID;

    /**
     * Array of columns to load from the database.
     *
     * Important: The _ID field is needed by
     * {@link android.widget.ResourceCursorAdapter}.
     */
    protected static final String PROJECTION[] = { Email._ID, Contacts.DISPLAY_NAME, Email.DATA, Email.CONTACT_ID };

    /**
     * Index of the name field in the projection. This must match the order in
     * {@link #PROJECTION}.
     */
    protected static final int NAME_INDEX = 1;

    /**
     * Index of the email address field in the projection. This must match the
     * order in {@link #PROJECTION}.
     */
    protected static final int EMAIL_INDEX = 2;

    /**
     * Index of the contact id field in the projection. This must match the order in
     * {@link #PROJECTION}.
     */
    protected static final int CONTACT_ID_INDEX = 3;


    protected ContentResolver mContentResolver;

    /**
     * Constructor
     *
     */
    public VvmContacts(Context context)
    {
        mContentResolver = context.getContentResolver();
    }

    public String extractPhoneFromVoicemailAddress(Address address) {
        String email = address.getAddress();
        String phone = email; // fallback to full email address

        // Now extract the phone number, if we have one from the address
        // TODO: This Needs improving.  It is only OK for Vodafone
        // get the string before the "@" in the address.  This is the phone number
        // e.g. VOICE=+61292014596@vm.vodafone.net.au
        // TODO: Also should check if the extracted phone number is Chars.
        // Some systems set the CID to a name or other string
        // we may be better off using a regex like "^VOICE=([\\+0-9\\(\\)\\.\\-]*)@"
        // or perhaps use a regex match like "^VOICE=(.+)$"  as there could be text as well
        int idx = -1;
        if (email != null) {
            idx = email.indexOf('@');
        }
        if (email!=null && idx >= 0) {
            if (email.startsWith("VOICE="))
                phone = email.substring(6, idx);
            else
                phone = email.substring(0, idx);
        }

        // Note the phone number may not be numeric
        if (!isPhoneNumberValid(phone))
            return email;

        return phone;
    }

    public String getDisplayName(String phoneNumber)
    {

        String displayName = null;
        if (VisualVoicemail.showCorrespondentNames()) {
            if (isPhoneNumberValid(phoneNumber)){
                String contactName = getNameFromPhoneNumber(phoneNumber);
                if (!isNullOrEmpty(contactName))
                    displayName = contactName;
            }
        }
        if (displayName == null) {
            if (!isNullOrEmpty(phoneNumber))
                displayName = phoneNumber;
            else
                displayName = NUMBER_WITHHELD;
        }

        return displayName;
    }

    public SpannableStringBuilder getFormattedDisplayName(String phoneNumber, boolean bold){
        SpannableStringBuilder displayName = new SpannableStringBuilder();
        String name = getDisplayName(phoneNumber);
        SpannableString ss = new SpannableString(name);
        if (bold)
            ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        displayName.append(ss);
        return displayName;
    }

    public SpannableStringBuilder getFormattedPhone(String phoneNumber, boolean bold){
        SpannableStringBuilder phoneNum = new SpannableStringBuilder();
        SpannableString ss = new SpannableString(phoneNumber);
        ss.setSpan(new RelativeSizeSpan(0.8f), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold)
            ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        phoneNum.append(ss);
        return phoneNum;
    }

    public boolean isPhoneNumberValid(String phone)
    {
        if (isNullOrEmpty(phone))
            return false;

        return phone.matches("^\\+\\d+$");
    }

    public static boolean isNullOrEmpty(String string){
        return string == null || string.length() == 0;
    }

    /**
     * Start the activity to add a phone number to an existing contact or add a new one.
     *
     * @param phoneNumber
     *         The phone number to add to a contact, or to use when creating a new contact.
     */
    public void addPhoneContact(Context context, final String phoneNumber) {
        Intent addIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        addIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        addIntent.putExtra(Insert.PHONE, Uri.decode(phoneNumber));
        addIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(addIntent);
    }

    /**
     * Check whether the provided email address belongs to one of the contacts.
     *
     * @param phoneNumber The phone number to look for.
     * @return <tt>true</tt>, if the email address belongs to a contact.
     *         <tt>false</tt>, otherwise.
     */
    public boolean isInContacts(final String phoneNumber) {
        boolean result = false;

        final Cursor c = getContactByPhoneNumber(phoneNumber);

        if (c != null) {
            if (c.getCount() > 0) {
                result = true;
            }
            c.close();
        }

        return result;
    }

    /**
     * Filter the contacts matching the given search term.
     *
     * @param constraint The search term to filter the contacts.
     * @return A {@link Cursor} instance that can be used to get the
     *         matching contacts.
     */
    public Cursor searchContacts(final CharSequence constraint) {
        final String filter = (constraint == null) ? "" : constraint.toString();
        final Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(filter));
        final Cursor c = mContentResolver.query(
                             uri,
                             PROJECTION,
                             null,
                             null,
                             SORT_ORDER);

        if (c != null)
        {
            /*
             * To prevent expensive execution in the UI thread:
             * Cursors get lazily executed, so if you don't call anything on
             * the cursor before returning it from the background thread you'll
             * have a complied program for the cursor, but it won't have been
             * executed to generate the data yet. Often the execution is more
             * expensive than the compilation...
             */
            c.getCount();
        }

        return c;
    }

    /**
     * Get the name of the contact a phone number belongs to.
     *
     * @param phoneNumber The phone number to search for.
     * @return The name of the contact the phone number belongs to. Or
     *      <tt>null</tt> if there's no matching contact.
     */
    public String getNameFromPhoneNumber(String phoneNumber) {
        if (phoneNumber == null)
        {
            return null;
        }

        final Cursor c = getContactByPhoneNumber(phoneNumber);

        String name = null;
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                name = getName(c);
            }
            c.close();
        }

        return name;
    }

    /**
     * Extract the name from a {@link Cursor} instance returned by
     * {@link #searchContacts(CharSequence)}.
     *
     * @param cursor The {@link Cursor} instance.
     * @return The name of the contact in the {@link Cursor}'s current row.
     */

    public String getName(Cursor cursor)
    {
        return cursor.getString(NAME_INDEX);
    }

    /**
     * Extract the email address from a {@link Cursor} instance returned by
     * {@link #searchContacts(CharSequence)}.
     *
     * @param cursor The {@link Cursor} instance.
     * @return The email address of the contact in the {@link Cursor}'s current
     *         row.
     */
    public String getEmail(Cursor cursor)
    {
        return cursor.getString(EMAIL_INDEX);
    }

    /**
     * Return a {@link Cursor} instance that can be used to fetch information
     * about the contact with the given phone number.
     *
     * @param phoneNumber The phone number to search for.
     * @return A {@link Cursor} instance that can be used to fetch information
     *         about the contact with the given email address
     */

    private Cursor getContactByPhoneNumber(final String phoneNumber)
    {
        final Uri uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        final Cursor c = mContentResolver.query(
                             uri,
                             PROJECTION,
                             null,
                             null,
                             SORT_ORDER);
        return c;
    }
}
    
