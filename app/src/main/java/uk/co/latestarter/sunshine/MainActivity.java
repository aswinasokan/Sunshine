package uk.co.latestarter.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.latestarter.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends Activity implements ForecastFragment.Callback {

    //TODO: Add control centre on left side, showing on swipe to right
    //TODO: Investigate on Material design
    //TODO: Add setting to change from Dark & Light Theme
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if view has 2 panes
        if (findViewById(R.id.weather_detail_container) != null) {
            // Detailed container only for large views (sw600dp)
            mTwoPane = true;

            // In two pane mode, show detail view in this activity by add/replace the detail fragment
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = ((ForecastFragment)getFragmentManager().findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_view_location_on_map:
                openPreferredLocationInMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openPreferredLocationInMap() {
        String location = Utility.getPreferredLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent viewOnMapIntent = new Intent(Intent.ACTION_VIEW);
        viewOnMapIntent.setData(geoLocation);
        if (viewOnMapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(viewOnMapIntent);
        } else {
            Log.e(LOG_TAG, "Couldn't open map to location: " + location + ", no available Intent");
        }
    }

    @Override
    public void onItemSelected(String date) {
        // If two pane view, then replace the detailed view
        // If single view, then launch detailed view
        if (mTwoPane) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, DetailFragment.getInstance(date))
                    .commit();
        } else {
            Intent detailWeatherIntent = new Intent(this, DetailActivity.class);
            detailWeatherIntent.putExtra(DetailActivity.DATE_KEY, date);
            startActivity(detailWeatherIntent);
        }
    }
}