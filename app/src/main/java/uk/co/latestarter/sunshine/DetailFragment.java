package uk.co.latestarter.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import uk.co.latestarter.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by Aswin Asokan on 01/08/2014.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private String mWeatherDate;
    private String mlocation;
    private String mForecastStr;

    private SimpleCursorAdapter mDetailAdapter;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mlocation) {
            outState.putString(getString(R.string.pref_location_key), mlocation);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            mlocation = savedInstanceState.getString(getString(R.string.pref_location_key));
        }

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mlocation && !mlocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) shareMenuItem.getActionProvider();
        shareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detailed_weather, container, false);

        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // Prevent the activity that we are share to, from being placed on to the activity stack
        // Else, return back to our app by clicking on icon will have the the other app on top
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + getString(R.string.HASH_TAG));
        return shareIntent;

    }

    private String formatDate(String value) {
        return Utility.formatDate(value);
    }

    private String formatTemperature(double value) {
        boolean isMetric = Utility.isMetric(getActivity());
        return Utility.formatTemperature(value, isMetric);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        Intent intent = getActivity().getIntent();
        if (intent == null || !intent.hasExtra(DetailActivity.DATE_KEY)) {
            return null;
        }
        mWeatherDate = intent.getStringExtra(DetailActivity.DATE_KEY);

        // Filter the query to return weather only for selected date and location
        mlocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithDate(mlocation, mWeatherDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        if (null == data || !data.moveToFirst()) {
            return;
        }

        String dateString = Utility.formatDate(data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
        String weatherDescription = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
        String low = Utility.formatTemperature(data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);

        ((TextView) getView().findViewById(R.id.detail_date_textview)).setText(dateString);
        ((TextView) getView().findViewById(R.id.detail_description_textview)).setText(weatherDescription);
        ((TextView) getView().findViewById(R.id.detail_high_textview)).setText(high + "\u00B0");
        ((TextView) getView().findViewById(R.id.detail_low_textview)).setText(low + "\u00B0");

        mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }
}