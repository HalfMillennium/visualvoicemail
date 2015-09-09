/**
 *
 */

package au.com.wallaceit.voicemail.activity.setup;

import android.widget.Spinner;

public class SpinnerOption {
    public Object value;

    public String label;

    public static void setSpinnerOptionValue(Spinner spinner, Object value) {
        for (int i = 0, count = spinner.getCount(); i < count; i++) {
            com.fsck.k9.activity.setup.SpinnerOption so = (com.fsck.k9.activity.setup.SpinnerOption)spinner.getItemAtPosition(i);
            if (so.value.equals(value)) {
                spinner.setSelection(i, true);
                return;
            }
        }
    }

    public SpinnerOption(Object value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
