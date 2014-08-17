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
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import uk.co.latestarter.sunshine.data.WeatherContract.WeatherEntry;
import uk.co.latestarter.sunshine.view.CompassView;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private String mLocation;
    private String mForecastStr;

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
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES
    };

    public static DetailFragment getInstance(String date) {
        Bundle bundle = new Bundle();
        bundle.putString(DetailActivity.DATE_KEY, date);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private boolean isValidBundle() {
        Bundle bundle = getArguments();
        return (null != bundle && bundle.containsKey(DetailActivity.DATE_KEY));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mLocation) {
            outState.putString(getString(R.string.pref_location_key), mLocation);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            mLocation = savedInstanceState.getString(getString(R.string.pref_location_key));
        }

        if (isValidBundle()) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isValidBundle() && null != mLocation && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
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
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        String weatherDate = getArguments().getString(DetailActivity.DATE_KEY);

        // Filter the query to return weather only for selected date and location
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithDate(mLocation, weatherDate);

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

        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        String dbDate = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));

        viewHolder.dayView.setText(Utility.getDayName(getActivity(), dbDate));
        viewHolder.dateView.setText(Utility.getFormattedMonthDay(dbDate));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(getActivity());

        float high = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        viewHolder.highView.setText(Utility.formatTemperature(getActivity(), high, isMetric));

        float low = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
        viewHolder.lowView.setText(Utility.formatTemperature(getActivity(), low, isMetric));

        // Read weather icon ID from cursor
        int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
        // Use placeholder image for now
        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        String weatherDescription = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
        viewHolder.descriptionView.setText(weatherDescription);
        viewHolder.descriptionView.setContentDescription(getString(R.string.access_forecast) + weatherDescription + ".");

        double humidity = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
        viewHolder.humidityView.setText(Utility.getFormattedHumidity(getActivity(), humidity));

        double pressure = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
        viewHolder.pressureView.setText(Utility.getFormattedPressure(getActivity(), pressure));

        float wind = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
        float degrees = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
        viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), wind, degrees));
        viewHolder.compassView.updateDirection(degrees);

        mForecastStr = String.format("%s - %s - %s/%s",
                Utility.getFriendlyDayString(getActivity(), dbDate), weatherDescription, high, low);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    /**
     * Cache of the children views for a detail weather.
     */
    public static class ViewHolder {
        public final TextView dayView;
        public final TextView dateView;
        public final TextView highView;
        public final TextView lowView;
        public final ImageView iconView;
        public final TextView descriptionView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;
        public final CompassView compassView;

        public ViewHolder(View view) {
            dayView = (TextView) view.findViewById(R.id.detail_day_textview);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            highView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowView = (TextView) view.findViewById(R.id.detail_low_textview);
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            descriptionView = (TextView) view.findViewById(R.id.detail_description_textview);
            humidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
            windView = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
            compassView = (CompassView) view.findViewById(R.id.detail_wind_compassview);
        }
    }
}