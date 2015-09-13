
package au.com.wallaceit.voicemail;

import android.widget.AutoCompleteTextView.Validator;

import java.util.regex.Pattern;

public class MobilePhoneNumberValidator implements Validator
{
    public static final Pattern MOBILE_PHONE_NUMBER_PATTERN = Pattern.compile( "04[0-9]{8}|\\+64[1-9][0-9]{8}" );

    public CharSequence fixText(CharSequence invalidText)
    {
        return "";
    }

    public boolean isValid(CharSequence text)
    {
        return MOBILE_PHONE_NUMBER_PATTERN.matcher(text).matches();
    }
}
