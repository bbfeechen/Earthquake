package andrew.cmu.edu.earthquake;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import android.database.SQLException;

import java.util.HashMap;

public class EarthquakeProvider extends ContentProvider {
    public static final String TAG = EarthquakeProvider.class.getSimpleName();

    public static final Uri CONTENT_URI = Uri.parse(
            "content://andrew.cmu.edu.earthquakeprovider/earthquakes");

    public static final Uri LIVE_FOLDER_URI = Uri.parse("" +
            "content://andrew.cmu.edu.earthquakeprovider/live_folder");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;
    private static final int LIVE_FOLDER = 4;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider", "earthquakes", QUAKES);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider", "earthquakes/#", QUAKE_ID);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider", "live_folder", LIVE_FOLDER);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI("andrew.cmu.edu.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;

    static {
        SEARCH_PROJECTION_MAP = new HashMap<>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_SUMMARY +
            " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
    }

    private static final HashMap<String, String> LIVE_FOLDER_PROJECTION;

    static {
        LIVE_FOLDER_PROJECTION = new HashMap<>();
        LIVE_FOLDER_PROJECTION.put(LiveFolders._ID, KEY_ID + " AS " + LiveFolders._ID);
        LIVE_FOLDER_PROJECTION.put(LiveFolders.NAME, KEY_DETAILS + " AS " + LiveFolders.NAME);
        LIVE_FOLDER_PROJECTION.put(LiveFolders.DESCRIPTION, KEY_DATE + " AS " + LiveFolders.DESCRIPTION);
    }

    private EarthquakeDatabaseHelper dbHelper;

    public EarthquakeProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new EarthquakeDatabaseHelper(context,
                EarthquakeDatabaseHelper.DATABASE_NAME, null,
                EarthquakeDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String segment = uri.getPathSegments().get(1);
                count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        KEY_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" +
                        selection + ')' : ""), selectionArgs);
                break;
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");

        switch (uriMatcher.match(uri)) {
            case QUAKES | LIVE_FOLDER: return "vnd.android.cursor.dir/vnd.cmu.earthquake";
            case QUAKE_ID: return "vnd.android.cursor.item/vnd.cmu.earthquake";
            case SEARCH:
                Log.d(TAG, "getType, SEARCH");
                return SearchManager.SUGGEST_MIME_TYPE;
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert");

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long rowID = database.insert(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                "quake", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query");

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE);
        switch (uriMatcher.match(uri)) {
            case QUAKE_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                qb.appendWhere(KEY_SUMMARY + " LIKE \"%" +
                        uri.getPathSegments().get(1) + "%\"");
                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            case LIVE_FOLDER:
                qb.setProjectionMap(LIVE_FOLDER_PROJECTION);
                break;
            default: break;
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_DATE;
        } else {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update");

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        values, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String segment = uri.getPathSegments().get(1);
                count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        values, KEY_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" +
                        selection + ')' : ""), selectionArgs);
                break;
            default: throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "EarthquakeProvider";

        private static final String DATABASE_NAME = "earthquakes.db";
        private static final int DATABASE_VERSION = 1;
        private static final String EARTHQUAKE_TABLE = "earthquakes";
        private static final String DATABASE_CREATE = "create table " +
                EARTHQUAKE_TABLE + " (" +
                KEY_ID + " integer primary key autoincrement, " +
                KEY_DATE + " INTEGER, " +
                KEY_DETAILS + " TEXT, " +
                KEY_SUMMARY + " TEXT, " +
                KEY_LOCATION_LAT + " FLOAT, " +
                KEY_LOCATION_LNG + " FLOAT, " +
                KEY_MAGNITUDE + " FLOAT, " +
                KEY_LINK + " TEXT);";
        private SQLiteDatabase sqLiteDatabase;

        public EarthquakeDatabaseHelper(Context context, String name,
                                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + EARTHQUAKE_TABLE);
            onCreate(db);
        }
    }
}
