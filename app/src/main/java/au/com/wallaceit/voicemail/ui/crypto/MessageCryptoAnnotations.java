package au.com.wallaceit.voicemail.ui.crypto;


import java.util.HashMap;

import com.fsck.k9.mail.Part;
import au.com.wallaceit.voicemail.mailstore.OpenPgpResultAnnotation;


public class MessageCryptoAnnotations {
    private HashMap<Part, OpenPgpResultAnnotation> annotations = new HashMap<Part, OpenPgpResultAnnotation>();

    MessageCryptoAnnotations() {
        // Package-private constructor
    }

    void put(Part part, OpenPgpResultAnnotation annotation) {
        annotations.put(part, annotation);
    }

    public OpenPgpResultAnnotation get(Part part) {
        return annotations.get(part);
    }

    public boolean has(Part part) {
        return annotations.containsKey(part);
    }
}
