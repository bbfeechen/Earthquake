package andrew.cmu.edu.earthquake;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;


/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/13/16
 */
public class EarthquakeActivity extends AppCompatActivity {
    private static final String TAG = EarthquakeActivity.class.getSimpleName();

    private static final int MENU_PREFERENCES = Menu.FIRST + 1;
    private static final int MENU_UPDATE = Menu.FIRST + 2;

    private static final int SHOW_PREFERENCE = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    private TabListener<EarthquakeListFragment> listTabListener;
    private TabListener<EarthquakeMapFragment> mapTabListener;

    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";

    @Override
    protected void onResume() {
        super.onResume();
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;
        if (!tabletLayout) {
            SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getSupportActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            listTabListener.fragment = getSupportFragmentManager().findFragmentByTag(
                    EarthquakeListFragment.class.getName());
            mapTabListener.fragment = getSupportFragmentManager().findFragmentByTag(
                    EarthquakeListFragment.class.getName());
            SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            int actionBarIndex = getActionBar().getSelectedTab().getPosition();
            SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
            editor.putInt(ACTION_BAR_INDEX, actionBarIndex);
            editor.apply();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (mapTabListener.fragment != null) {
                ft.detach(mapTabListener.fragment);
            }
            if (listTabListener.fragment != null) {
                ft.detach(listTabListener.fragment);
            }
            ft.commit();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        updateFromPreferences();

        ActionBar actionBar = getSupportActionBar();
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;
        if (!tabletLayout) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        actionBar.setDisplayShowTitleEnabled(false);

        ActionBar.Tab listTab = actionBar.newTab();
        listTabListener = new TabListener<EarthquakeListFragment>(this, R.id.EarthquakeFragmentContainer,
                EarthquakeListFragment.class);
        listTab.setText("List")
                .setContentDescription("List of earthquakes")
                .setTabListener(listTabListener);
        actionBar.addTab(listTab);

        ActionBar.Tab mapTab = actionBar.newTab();
        mapTabListener = new TabListener<EarthquakeMapFragment>(this, R.id.EarthquakeFragmentContainer,
                EarthquakeMapFragment.class);
        mapTab.setText("Map")
                .setContentDescription("Map of earthquakes")
                .setTabListener(mapTabListener);
        actionBar.addTab(mapTab);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.menu_refresh): {
                startService(new Intent(this, EarthquakeUpdateService.class));
                return true;
            }
            case (R.id.menu_preference): {
                Class c = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                        PreferencesActivity.class : FragmentPreferences.class;

                Intent i = new Intent(this, c);
                startActivityForResult(i, SHOW_PREFERENCE);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCE) {
            updateFromPreferences();
            startService(new Intent(this, EarthquakeUpdateService.class));
        }
    }

    private void updateFromPreferences() {
        Log.d(TAG, "updateFromPreferences");

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        minimumMagnitude = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
        updateFreq = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
        autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
    }

    public static class TabListener<T extends Fragment>
        implements ActionBar.TabListener {

        private Fragment fragment;
        private Activity activity;
        private Class<T> fragmentClass;
        private int fragmentContainer;

        public TabListener(Activity activity, int fragmentContainer, Class<T> fragmentClass) {
            this.activity = activity;
            this.fragmentClass = fragmentClass;
            this.fragmentContainer = fragmentContainer;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            if (fragment == null) {
                String fragmentName = fragmentClass.getName();
                fragment = Fragment.instantiate(activity, fragmentName);
                ft.add(fragmentContainer, fragment, fragmentName);
            } else {
                ft.attach(fragment);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            if (fragment != null) {
                ft.detach(fragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            if (fragment != null) {
                ft.attach(fragment);
            }
        }
    }
}
