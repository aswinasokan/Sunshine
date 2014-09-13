package uk.co.latestarter.sunshine.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Vector;

public class WeatherProviderHelper {

    private static final String LOG_TAG = WeatherProviderHelper.class.getSimpleName();

    private final Context mContext;

    public WeatherProviderHelper(Context context) {
        mContext = context;
    }

    public void insertWeatherInDatabase(Vector<ContentValues> cVVector) {
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);

        Log.v(LOG_TAG, "Bulk insert of weather data");
        mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
    }

    public long insertLocationInDatabase(String locationSetting, ContentValues locationValues) {
        // Check if location already exists
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[] {WeatherContract.LocationEntry._ID}, // fetch the ID to be returned out
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ", // match the location
                new String[] {locationSetting}, // with the value passes
                null // sort order
        );

        long locationIndex;
        if (cursor.moveToFirst()) {
            // Already present in database, extract the ID
            Log.v(LOG_TAG, "Found location in the database");
            locationIndex = cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
        } else {
            Log.v(LOG_TAG, "Didn't find location in the database, inserting now");
            Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            locationIndex = ContentUris.parseId(uri);
        }

        cursor.close();
        return locationIndex;
    }
}
