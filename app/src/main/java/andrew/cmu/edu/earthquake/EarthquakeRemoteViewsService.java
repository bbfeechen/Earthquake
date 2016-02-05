package andrew.cmu.edu.earthquake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class EarthquakeRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new EarthquakeRemoteViewsFactory(getApplicationContext());
    }

    class EarthquakeRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;
        private Cursor c;

        public EarthquakeRemoteViewsFactory(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if (c != null) {
                return c.getCount();
            } else {
                return 0;
            }
        }

        @Override
        public void onCreate() {
            c = executeQuery();
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
            c.close();
        }

        @Override
        public RemoteViews getViewAt(int index) {
            c.moveToPosition(index);
            int idIdx = c.getColumnIndex(EarthquakeProvider.KEY_ID);
            int magnitudeIdx = c.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE);
            int detailsIdx = c.getColumnIndex(EarthquakeProvider.KEY_DETAILS);

            String id = c.getString(idIdx);
            String magnitude = c.getString(magnitudeIdx);
            String details = c.getString(detailsIdx);

            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.quake_widget);
            rv.setTextViewText(R.id.widget_magnitude, magnitude);
            rv.setTextViewText(R.id.widget_details, details);

            Intent fillInIntent = new Intent();
            Uri uri = Uri.withAppendedPath(EarthquakeProvider.CONTENT_URI, id);
            fillInIntent.setData(uri);

            rv.setOnClickFillInIntent(R.id.widget_magnitude, fillInIntent);
            rv.setOnClickFillInIntent(R.id.widget_details, fillInIntent);
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int index) {
            if (c != null) {
                return c.getLong(c.getColumnIndex(EarthquakeProvider.KEY_ID));
            } else {
                return index;
            }
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private Cursor executeQuery() {
            String[] projection = new String[] {
                    EarthquakeProvider.KEY_ID,
                    EarthquakeProvider.KEY_MAGNITUDE,
                    EarthquakeProvider.KEY_DETAILS
            };

            Context appContext = getApplicationContext();
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(appContext);
            int minimumMagnitude = Integer.parseInt(prefs.getString(
                    PreferencesActivity.PREF_MIN_MAG, "3"));
            String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + minimumMagnitude;
            return context.getContentResolver().query(EarthquakeProvider.CONTENT_URI,
                    projection, where, null, null);
        }
    }
}
