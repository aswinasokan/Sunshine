package uk.co.latestarter.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Date;

import uk.co.latestarter.sunshine.data.WeatherContract;
import uk.co.latestarter.sunshine.data.WeatherContract.LocationEntry;
import uk.co.latestarter.sunshine.data.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    private ListView mListView;
    private ForecastAdapter mForecastAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private final static String SELECTED_KEY = "position_selection";
    private boolean mUseTodayLayout;

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

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
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    // TODO: Check for unused columns and remove
//    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_ART_ID = 5;
//    public static final int COL_LOCATION_SETTING = 6;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume()");
        super.onResume();

        // TODO: When is mLocation used? Check for scenarios
        if (null != mLocation && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            return;
        }

        // TODO: Trace the usage of this position being set on Orientation change
        if (ListView.INVALID_POSITION != mPosition) {
            // Scroll back to previous selection
            mListView.setSelection(mPosition);
        }

        //TODO: Auto-select the today item in the list when launching the app
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // ListView displaying the forecast
        mListView = (ListView) rootView.findViewById(R.id.listView_forecast);

        mForecastAdapter = new ForecastAdapter(getActivity());
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        // Attach the custom adapter to view
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ForecastAdapter adapter = (ForecastAdapter)adapterView.getAdapter();
                Cursor cursor =  adapter.getCursor();
                if (null != cursor && cursor.moveToPosition(position)) {
                    ((Callback)getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
                mPosition = position;
            }
        });

        if (null != savedInstanceState && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                Utility.updateWeather(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(outState);

        if (ListView.INVALID_POSITION != mPosition) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (null != mForecastAdapter) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader()");
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished()");
        // onLoadFinished() is called Twice
        // TODO: Monitor defect: https://code.google.com/p/android/issues/detail?id=63179

        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "onLoaderReset()");
        mForecastAdapter.swapCursor(null);
    }
}
