package andrew.cmu.edu.earthquake;

import android.preference.PreferenceActivity;

import java.util.List;

public class FragmentPreferences extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
