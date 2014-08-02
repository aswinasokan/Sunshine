package uk.co.latestarter.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

import uk.co.latestarter.sunshine.data.WeatherContract;

public class Utility {

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static String getPreferredUnits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric_value));
    }

    public static boolean isMetric(Context context) {
        return getPreferredUnits(context).equals(context.getString(R.string.pref_units_metric_value));
    }

    public static ContentValues createLocationValues(String locationSetting, String cityName,
                                                     double cityLatitude, double cityLongitude) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, cityLatitude);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, cityLongitude);

        return values;
    }

    public static ContentValues createWeatherValues(long locationRowId, String date, String description,
            int id, double humidity, double pressure, double speed, double degrees, double min, double max) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        values.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, date);
        values.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
        values.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, id);
        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, speed);
        values.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, degrees);
        values.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, min);
        values.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, max);

        return values;
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDbString(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
