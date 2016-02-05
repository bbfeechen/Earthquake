package andrew.cmu.edu.earthquake;

import android.support.v4.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class EarthquakeListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EarthquakeListFragment.class.getSimpleName();

    private ArrayList<Quake> earthquakes = new ArrayList<>();
    private SimpleCursorAdapter adapter;

    private Handler handler = new Handler();

    public EarthquakeListFragment() {
        // Required empty public constructor
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        String[] projection = new String[]{
                EarthquakeProvider.KEY_ID,
                EarthquakeProvider.KEY_SUMMARY
        };

        EarthquakeActivity earthquakeActivity = (EarthquakeActivity) getActivity();
        String selection = EarthquakeProvider.KEY_MAGNITUDE + " > " +
                earthquakeActivity.minimumMagnitude;

        CursorLoader loader = new CursorLoader(getActivity(),
                EarthquakeProvider.CONTENT_URI, projection, selection, null, null);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");

        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");

        adapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY},
                new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
        refreshEarthquakes();
    }

    public void refreshEarthquakes() {
        Log.d(TAG, "refreshEarthquakes");

        handler.post(new Runnable() {
            public void run() {
                getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
            }
        });
        getActivity().startService(new Intent(getActivity(), EarthquakeUpdateService.class));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ContentResolver cr = getActivity().getContentResolver();
        Cursor result = cr.query(ContentUris.withAppendedId(EarthquakeProvider.CONTENT_URI, id),
                null, null, null, null);
        if (result.moveToFirst()) {
            Date date = new Date(result.getLong(result.getColumnIndex(
                    EarthquakeProvider.KEY_DATE)));
            String details = result.getString(result.getColumnIndex(
                    EarthquakeProvider.KEY_DETAILS));
            double magnitude = result.getDouble(result.getColumnIndex(
                    EarthquakeProvider.KEY_MAGNITUDE));
            String linkString = result.getString(result.getColumnIndex(
                    EarthquakeProvider.KEY_LINK));
            double lat = result.getDouble(result.getColumnIndex(
                    EarthquakeProvider.KEY_LOCATION_LAT));
            double lng = result.getDouble(result.getColumnIndex(
                    EarthquakeProvider.KEY_LOCATION_LNG));

            Location location = new Location("db");
            location.setLatitude(lat);
            location.setLongitude(lng);

            Quake quake = new Quake(date, details, location, magnitude, linkString);
            DialogFragment newFragment = EarthquakeDialog.newInstance(getActivity(), quake);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }
}
