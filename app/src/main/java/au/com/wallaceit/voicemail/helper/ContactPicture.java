package au.com.wallaceit.voicemail.helper;

import android.content.Context;
import android.util.TypedValue;

import au.com.wallaceit.voicemail.VisualVoicemail;
import com.fsck.k9.R;
import au.com.wallaceit.voicemail.activity.misc.ContactPictureLoader;

public class ContactPicture {

    public static ContactPictureLoader getContactPictureLoader(Context context) {
        final int defaultBgColor;
        if (!VisualVoicemail.isColorizeMissingContactPictures()) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.contactPictureFallbackDefaultBackgroundColor,
                    outValue, true);
            defaultBgColor = outValue.data;
        } else {
            defaultBgColor = 0;
        }

        return new ContactPictureLoader(context, defaultBgColor);
    }
}
