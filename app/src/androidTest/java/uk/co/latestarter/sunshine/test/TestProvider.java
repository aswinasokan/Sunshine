package uk.co.latestarter.sunshine.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import uk.co.latestarter.sunshine.data.WeatherContract.LocationEntry;
import uk.co.latestarter.sunshine.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {

        ContentValues locationValues = TestDb.createLocationValues();
        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
        long locationRowId = ContentUris.parseId(locationInsertUri);
        Log.d(LOG_TAG, "New location row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, locationValues);
        cursor.close();

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, locationValues);
        cursor.close();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);
        Uri insertWeatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weatherRowId = ContentUris.parseId(insertWeatherUri);
        Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, weatherValues);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, weatherValues);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, weatherValues);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // Columns to be fetched, null will bring all
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, weatherValues);
        cursor.close();
    }

    public void testGetType() {
        // content://uk.co.latestarter.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/uk.co.latestarter.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_DIR_TYPE, type);

        // content://uk.co.latestarter.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION));
        // vnd.android.cursor.dir/uk.co.latestarter.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_DIR_TYPE, type);

        // content://uk.co.latestarter.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE));
        // vnd.android.cursor.item/uk.co.latestarter.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://uk.co.latestarter.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/uk.co.latestarter.sunshine/location
        assertEquals(LocationEntry.CONTENT_DIR_TYPE, type);

        // content://uk.co.latestarter.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/uk.co.latestarter.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    // brings our database to an empty state
    public void deleteAllRecords() {
        // Weather entry has to be deleted before Location entries due to foreign key constraints
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, updatedValues);
        cursor.close();
    }
}
