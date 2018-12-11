package reboja.com.alphafitness;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.security.Provider;
import java.util.HashMap;

public class MyContentProvider extends ContentProvider {

    // Tag for debugging purposes.
    private final static String TAG = MyContentProvider.class.getSimpleName();

    private final static String PROVIDER = "com.wearable.alphafitness.provider";
    static final String URL = "content://" + PROVIDER + "/Workouts";
    static final String URL2 = "content://" + PROVIDER + "/Profiles";
    static final Uri URI = Uri.parse(URL);
    static final Uri URI2 = Uri.parse(URL2);

    // Database stuff
    SQLiteDatabase database;
    DB dbhelper;


    static final String DB_NAME = "myprovider";
    static final String WORKOUTS_TABLE = "Workouts";
    static final String PROFILE_TABLE = "Profiles";
    static final int DB_VERSION = 1;

    // Column names for the PROFILE_TABLE
    static final String P_ID = "ID";
    static final String NAME = "UserName";
    static final String GENDER = "Gender";
    static final String WEIGHT = "Weight";
    static final String NUM_WORKOUTS = "Workouts";


    // Column names for the WORKOUTS_TABLE.
    static final String W_ID = "ID";
    static final String DURATION = "Duration";
    static final String DISTANCE = "Distance";
    static final String CALORIES = "Calories";
    static final String STEPS = "Steps";
    static final String WEEK = "Week";


    Context mContext;

    private static HashMap<String, String> WORKOUTS_MAP;

    static final int WORKOUTS = 1;
    static final int PROFILES = 2;
    static final int PROFILE_ID = 3;
    static final int WORKOUT_ID = 4;

    static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // To the tables.
        uriMatcher.addURI(PROVIDER, "Workouts", WORKOUTS);
        uriMatcher.addURI(PROVIDER, "Profiles", PROFILES);

        // For selection of a row in either table.
        uriMatcher.addURI(PROVIDER, "Profiles/#", PROFILE_ID);
        uriMatcher.addURI(PROVIDER, "Workouts/#", WORKOUT_ID);
    }

    private SQLiteDatabase db;

    // Workouts table, tracks workouts per user if multiple.
    static final String CREATE_TABLE =
            " CREATE TABLE " + WORKOUTS_TABLE +
                    "(ID INTEGER, " +
                    DURATION +" DECIMAL NOT NULL, " +
                    DISTANCE + " DECIMAL NOT NULL, " +
                    CALORIES + " DECIMAL NOT NULL, " +
                    STEPS + " INTEGER NOT NULL," +
                    WEEK + " INTEGER NOT NULL," +
                    "FOREIGN KEY" + "(" + W_ID  + ") REFERENCES " + PROFILE_TABLE + "(" + P_ID + "));";


      // Profile Table, to keep track of multiple profiles.
      static final String CREATE_SECOND_TABLE =
              " CREATE TABLE " + PROFILE_TABLE +
                      "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                      "UserName TEXT NOT NULL, " +
                      "Gender TEXT NOT NULL, " +
                      "Weight DECIMAL NOT NULL," +
                      "Workouts INTEGER NOT NULL);";




    // This class will be responsible for querying.
     static class DB extends SQLiteOpenHelper
    {

        DB(Context context)
        {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // This will create the two tables.
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_SECOND_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + WORKOUTS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE);
            onCreate(db);

        }
    }

    public MyContentProvider() {

    }



    // Don't really need to worry about this method, we won't be deleting much in
    // this application.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.


        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Method that inserts values into the database when the workout has ended
     * --- Half done, just need to test ---
     *
     * @param uri The content that you want to access.
     * @param values Values to insert into the content provider
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Uri nUri;
        int i = uriMatcher.match(uri);
        Log.v(TAG, String.valueOf(i));

       switch(uriMatcher.match(uri))
       {
           case WORKOUTS:
           {
              long row =  db.insert(WORKOUTS_TABLE, null, values);
              if (row > 0) {
                  // This will be the new URi containing the ID.
                  nUri = ContentUris.withAppendedId(URI, row);
                  notifyChange(nUri);
                  return nUri;
              }

           }
           case PROFILES:
           {
              long row = db.insert(PROFILE_TABLE, null, values);
              if (row > 0) {
                  nUri = ContentUris.withAppendedId(URI2, row);
                  notifyChange(nUri);
                  return nUri;
              }

           }
           default:
               throw new IllegalArgumentException("URI not supported: " + uri);
       }
    }

    /**
     * This will provide the initialization for the datable.
     * Note: getWriteableDatabase() method allows to create and/or open a db
     * used for reading and writing
     * @return boolean on whether the operation was a success or not.
     */
    @Override
    public boolean onCreate() {
        Log.v(TAG, "Content provider: onCreate()");
        mContext = getContext();
        if (mContext == null) {
            Log.e(TAG, "Failed to retrieve the context.");
            return false;
        }
        dbhelper = new DB(mContext);
        db =  dbhelper.getWritableDatabase();
        if (db == null)
        {
            Log.e(TAG, "Failed to create a writable Database ");
            return false;
        }
        return true;
    }

    /**
     * This will be used for querying a record.
     * Note: to query records, use 'SQLiteQueryBuilder'
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "Content provider: query()");
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //sqLiteQueryBuilder.setTables(TABLE_NAME);

        // Do operations based on the Content URI.
        switch (uriMatcher.match(uri))
        {
            case WORKOUTS:
            {
                sortOrder = "ID ASC";
                sqLiteQueryBuilder.setProjectionMap(null);
                sqLiteQueryBuilder.setTables(WORKOUTS_TABLE);
                break;
            }
            case PROFILES:
            {
                sortOrder = "ID ASC";
                sqLiteQueryBuilder.setProjectionMap(null);
                sqLiteQueryBuilder.setTables(PROFILE_TABLE);
                break;
            }

            default:
                // Error handling.
                throw new IllegalArgumentException("Uri not supported: " + uri);
        }

    /**
        if (getMatchedID(uri) == WORKOUTS)
        {
            sqLiteQueryBuilder.setProjectionMap(null);
        }

     **/

        /**
        // This may determine the sort order of the database.
        if (sortOrder == null || sortOrder == "") {
            sortOrder = NAME;
        }

         **/

        Cursor c = sqLiteQueryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.v(TAG, "Content provider: update()");

        int count = 0;
        int matchedID = getMatchedID(uri);

        //String sel_str = (matchedID == WORKOUTS);
        return count;
    }

    /**
     * Tbus method will notify register observers that a row in the SQLite database
     * was updated and attempt to sync changes to the network.
     * @param uri
     */
    private void notifyChange(Uri uri) {
        ContentResolver resolver = mContext.getContentResolver();
        if (resolver != null) resolver.notifyChange(uri, null);
    }



    /**
     * Checks whether or not the current URI matches any of the current URIs.
     * @param uri
     * @return
     */
    private int getMatchedID(Uri uri) {
        int matchedID = uriMatcher.match(uri);
        if (!(matchedID == WORKOUTS)) throw new IllegalArgumentException("Unsupported URI: " + uri);
        return matchedID;
    }

    private String getIdString(Uri uri) {
        return (DB_NAME + " = " + uri.getPathSegments().get(1));
    }

    private String getSelectionWithId(Uri uri, String selection) {
        String sel_str = getIdString(uri);
        if (!TextUtils.isEmpty(selection))
            sel_str +=" AND (" + selection + ')';
        return sel_str;
    }
}
