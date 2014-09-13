package uk.co.latestarter.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.Vector;

import uk.co.latestarter.sunshine.api.openweathermap.OpenWeatherMapApi;
import uk.co.latestarter.sunshine.data.WeatherProviderHelper;

//TODO: Is this class no longer required, after implementing SunshineSyncService/Adapter?
public class WeatherUpdateService extends IntentService {

    private static final String LOG_TAG = WeatherUpdateService.class.getSimpleName();
    public static final String LOCATION_PARAM = "location";
    private WeatherProviderHelper helper;

    public WeatherUpdateService() {
        super("WeatherUpdateService");
    }

    @Override
    public void onCreate() {
        helper = new WeatherProviderHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String locationSetting = intent.getStringExtra(WeatherUpdateService.LOCATION_PARAM);
        if (null == locationSetting || locationSetting.isEmpty()) {
            return;
        }

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
    }

    static public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent updateWeatherService = new Intent(context, WeatherUpdateService.class);
            updateWeatherService.putExtra(WeatherUpdateService.LOCATION_PARAM,
                    intent.getStringExtra(WeatherUpdateService.LOCATION_PARAM));
            context.startService(updateWeatherService);
        }
    }
}
