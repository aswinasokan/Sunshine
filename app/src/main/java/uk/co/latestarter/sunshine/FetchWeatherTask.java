package uk.co.latestarter.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.Vector;

import uk.co.latestarter.sunshine.api.openweathermap.OpenWeatherMapApi;
import uk.co.latestarter.sunshine.data.WeatherProviderHelper;

//TODO: Is this class no longer required, after implementing WeatherUpdateService?
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private final WeatherProviderHelper helper;

    public FetchWeatherTask(Context context) {
        helper = new WeatherProviderHelper(context);
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        String locationSetting = params[0];

        // TODO: Change the day count as a setting with min/max spinner
        int days = 14;
        String units="metric";

        try {
            JSONObject forecastJson = OpenWeatherMapApi.getWeatherForecast(locationSetting, days, units);

            ContentValues locationValues = OpenWeatherMapApi.getLocationData(forecastJson, locationSetting);
            // Insert the location into the database.
            long locationRowID = helper.insertLocationInDatabase(locationSetting, locationValues);

            Vector<ContentValues> weatherValues = OpenWeatherMapApi.getWeatherData(forecastJson, locationRowID);
            helper.insertWeatherInDatabase(weatherValues);

        } catch (Exception e) {
            // If the code didn't successfully get the weather data, there's no point in attempting to parse it.
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }
}
