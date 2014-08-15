package uk.co.latestarter.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TODAY_LAYOUT = 0;
    private final int VIEW_FUTURE_LAYOUT = 1;
    private boolean mUseTodayLayout = false;

    public ForecastAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TODAY_LAYOUT : VIEW_FUTURE_LAYOUT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType == VIEW_TODAY_LAYOUT) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Get view references from the ViewHolder
        ViewHolder holder = (ViewHolder)view.getTag();

        int position = cursor.getPosition();
        int viewType = getItemViewType(position);

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ART_ID);

        int weatherResourceId;

        if (VIEW_TODAY_LAYOUT == viewType) {
            // Set weather icon id based on layout type
            weatherResourceId = Utility.getArtResourceForWeatherCondition(weatherId);

            // Set location text
            holder.locationView.setText(Utility.getPreferredLocation(context));
        } else {
            // Set weather icon based on layout type
            weatherResourceId = Utility.getIconResourceForWeatherCondition(weatherId);
        }

        // Set weather icon based on layout type
        holder.iconView.setImageResource(weatherResourceId);

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        String friendlyDateString = (position == 0 && !mUseTodayLayout) ?
                Utility.getDayName(context, dateString) :
                Utility.getFriendlyDayString(context, dateString);
        holder.dateView.setText(friendlyDateString);

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highView.setText(Utility.formatTemperature(context, high, isMetric));

        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView locationView;
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highView;
        public final TextView lowView;

        public ViewHolder(View view) {
            locationView = (TextView) view.findViewById(R.id.list_item_location_textview);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_description_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}