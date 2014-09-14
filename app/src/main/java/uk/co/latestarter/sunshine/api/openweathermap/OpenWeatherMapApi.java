package uk.co.latestarter.sunshine.api.openweathermap;

import android.content.ContentValues;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import uk.co.latestarter.sunshine.Utility;
import uk.co.latestarter.sunshine.api.DataFetcher;

/**
 * Connect to the OpenWeatherMap api to fetch data
 */
public class OpenWeatherMapApi extends DataFetcher {

//    private static final String LOG_TAG = OpenWeatherMapApi.class.getSimpleName();

    // These are the names of the JSON objects that need to be extracted.
    // Location information
    public static final String OWM_CITY = "city";
    public static final String OWM_CITY_NAME = "name";
    public static final String OWM_COORD = "coord";
    public static final String OWM_COORD_LAT = "lat";
    public static final String OWM_COORD_LONG = "lon";

    // Weather information.  Each day's forecast info is an element of the "list" array.
    public static final String OWM_LIST = "list";

    public static final String OWM_DATETIME = "dt";
    public static final String OWM_PRESSURE = "pressure";
    public static final String OWM_HUMIDITY = "humidity";
    public static final String OWM_WINDSPEED = "speed";
    public static final String OWM_WIND_DIRECTION = "deg";

    public static final String OWM_WEATHER = "weather";
    public static final String OWM_DESCRIPTION = "main";
    public static final String OWM_WEATHER_ID = "id";

    // All temperatures are children of the "temp" object.
    public static final String OWM_TEMPERATURE = "temp";
    public static final String OWM_MAX = "max";
    public static final String OWM_MIN = "min";

    public static JSONObject getWeatherForecast(String location, int days, String units) throws JSONException, IOException {
        /*
        Construct the URL for the OpenWeatherMap query
        Possible parameters are available at OWM's forecast API page, at http://openweathermap.org/API#forecast
        */
        final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(days))
                .build();
//        Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

        URL url = new URL(builtUri.toString());

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = fetchDataFromServer(url);

        return new JSONObject(forecastJsonStr);
    }

    public static ContentValues getLocationData(JSONObject forecastJson, String location) throws JSONException {

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
        double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

//        Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude + " " + cityLongitude);

        return Utility.createLocationValues(location, cityName, cityLatitude, cityLongitude);
    }

    public static Vector<ContentValues> getWeatherData(JSONObject forecastJson, long locationID) throws JSONException {

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // Get and insert the new weather information into the database
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(weatherArray.length());

        // These are the values that will be collected.
        long dateTime;
        double pressure;
        int humidity;
        double windSpeed;
        double windDirection;
        double high;
        double low;
        String description;
        int weatherId;

        for(int i = 0; i < weatherArray.length(); i++) {
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = Utility.createWeatherValues(locationID,
                    new Date(dateTime * 1000L), description,
                    weatherId, humidity, pressure, windSpeed, windDirection, low, high);

            contentValuesVector.add(weatherValues);
        }

        return contentValuesVector;
    }
}