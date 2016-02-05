package andrew.cmu.edu.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/14/16
 */
public class UserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreference);
    }
}
