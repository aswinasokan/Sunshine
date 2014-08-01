package uk.co.latestarter.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import uk.co.latestarter.sunshine.Utility;
import uk.co.latestarter.sunshine.data.WeatherContract.LocationEntry;
import uk.co.latestarter.sunshine.data.WeatherContract.WeatherEntry;
import uk.co.latestarter.sunshine.data.WeatherDbHelper;

/**
 * Created by Aswin Asokan on 28/07/2014.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();
    public static final String TEST_CITY_NAME = "North Pole";
    public static final String TEST_LOCATION = "99705";
    public static final String TEST_DATE = "20141205";

    public void testCreateDb() throws Throwable {

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues locationValues = createLocationValues();
        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);
        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New location row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, locationValues);
        cursor.close();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = createWeatherValues(locationRowId);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        // Verify we got a row back.
        assertTrue(weatherRowId != -1);
        Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        cursor = db.query(
                WeatherEntry.TABLE_NAME,  // Table to Query
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, weatherValues);
        cursor.close();

        db.close();
    }

    static ContentValues createLocationValues() {
        return Utility.createLocationValues(TEST_LOCATION, TEST_CITY_NAME, 23.7488, -147.353);
    }

    static ContentValues createWeatherValues(long locationRowId) {
        return Utility.createWeatherValues(locationRowId, TEST_DATE, "Asteroids", 321, 1.2, 1.3, 5.5, 1.1, 32.1, 39.5);
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
