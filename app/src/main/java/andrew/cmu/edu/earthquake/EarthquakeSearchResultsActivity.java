package andrew.cmu.edu.earthquake;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class EarthquakeSearchResultsActivity extends ListActivity
            implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EarthquakeSearchResultsActivity.class.getSimpleName();

    private static String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

    private SimpleCursorAdapter adapter;

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        String query = "0";
        if (args != null) {
            query = args.getString(QUERY_EXTRA_KEY);
        }

        String[] projection = { EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_SUMMARY };
        String selection = EarthquakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
        String[] selectionArgs = null;
        String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

        return new CursorLoader(this, EarthquakeProvider.CONTENT_URI, projection, selection,
                selectionArgs, sortOrder);
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
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[] { EarthquakeProvider.KEY_SUMMARY },
                new int[] { android.R.id.text1 }, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
        parseIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        super.onNewIntent(intent);
        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        Log.d(TAG, "parseIntent");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            args.putString(QUERY_EXTRA_KEY, searchQuery);
            getLoaderManager().restartLoader(0, args, this);
        }
    }
}
