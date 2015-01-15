package com.example.sunshine.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.sunshine.app.data.WeatherContract.WeatherEntry;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

	private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
	private ArrayAdapter<String> mForecastAdapter;
	private final Context mContext;

	public FetchWeatherTask(Context context,
			ArrayAdapter<String> forecastAdapter) {
		mContext = context;
		mForecastAdapter = forecastAdapter;
	}

	@Override
	protected String[] doInBackground(String... params) {

		if (params.length == 0) {
			return null;
		}

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		String locationQuery = params[0];
		String forecastJsonStr = null;
		String format = "json";
		String unit = "metric";
		int numDays = 14;
		try {

			final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
			final String QUERY_PARAM = "q";
			final String FORMAT_PARAM = "mode";
			final String UNIT_PARAM = "unit";
			final String DAYS_PARAM = "cnt";

			Uri builtUri = Uri
					.parse(FORECAST_BASE_URL)
					.buildUpon()
					.appendQueryParameter(QUERY_PARAM, locationQuery)
					.appendQueryParameter(FORMAT_PARAM, format)
					.appendQueryParameter(UNIT_PARAM, unit)
					.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
					.build();

			URL url = new URL(builtUri.toString());

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if (inputStream == null) {
				return null;
			}

			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {

				buffer.append(line + "\n");
			}

			if (buffer.length() == 0) {
				return null;
			}

			forecastJsonStr = buffer.toString();

		} catch (IOException e) {
			Log.e(LOG_TAG, "Error ", e);
			return null;
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error closing stream", e);
				}
			}
		}
		try {
			return getWeatherFromJson(forecastJsonStr, numDays, locationQuery);

		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String[] result) {

		if (result != null) {
			mForecastAdapter.clear();
			for (String s : result) {
				mForecastAdapter.add(s);
			}
		}
		// forecastAdapter.addAll(Arrays.asList(result));
	}

	// HELPER METHODS

	private String getReadableDateString(long time) {
		Date date = new Date(time * 1000);
		SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
		return format.format(date).toString();
	}

	private String formatHighLows(double high, double low) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String unitType = prefs.getString(
				mContext.getString(R.string.pref_unit_key),
				mContext.getString(R.string.pref_unit_metric));

		if (unitType.equals(mContext.getString(R.string.pref_unit_imperial))) {
			high = (high * 1.8) + 32;
			low = (low * 1.8) + 32;
		}
		long roundedHigh = Math.round(high);
		long roundedLow = Math.round(low);

		String highLowStr = roundedHigh + "/" + roundedLow;
		return highLowStr;
	}

	private String[] getWeatherFromJson(String forecastJsonStr, int numDays,
			String locationSetting) throws JSONException {

		final String OWM_CITY = "city";
		final String OWM_CITY_NAME = "name";
		final String OWM_COORD = "coord";
		final String OWM_COORD_LAT = "lat";
		final String OWM_COORD_LONG = "lon";

		final String OWM_LIST = "list";

		final String OWM_DATETIME = "dt";
		final String OWM_PRESSURE = "pressure";
		final String OWM_HUMIDITY = "humidity";
		final String OWM_WINDSPEED = "speed";
		final String OWM_WIND_DIRECTION = "deg";

		// All temperatures are children of the "temp" object.
		final String OWM_TEMPERATURE = "temp";
		final String OWM_MAX = "max";
		final String OWM_MIN = "min";

		final String OWM_WEATHER = "weather";
		final String OWM_DESCRIPTION = "main";
		final String OWM_WEATHER_ID = "id";

		JSONObject forecastJSON = new JSONObject(forecastJsonStr);
		JSONArray weatherArray = forecastJSON.getJSONArray(OWM_LIST);

		JSONObject cityJSON = forecastJSON.getJSONObject(OWM_CITY);
		String cityName = cityJSON.getString(OWM_CITY_NAME);
		JSONObject coordJSON = cityJSON.getJSONObject(OWM_COORD);
		double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
		double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

		long locationID = addLocation(locationSetting, cityName, cityLatitude,
				cityLongitude);

		Vector<ContentValues> cVVector = new Vector<ContentValues>(
				weatherArray.length());

		String[] resultStrs = new String[numDays];
		for (int i = 0; i < weatherArray.length(); i++) {

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

			// Get the JSON object representing the day
			JSONObject dayForecast = weatherArray.getJSONObject(i);

			// The date/time is returned as a long. We need to convert that
			// into something human-readable, since most people won't read
			// "1400356800" as
			// "this saturday".

			dateTime = dayForecast.getLong(OWM_DATETIME);

			pressure = dayForecast.getDouble(OWM_PRESSURE);
			humidity = dayForecast.getInt(OWM_HUMIDITY);
			windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
			windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

			// description is in a child array called "weather", which is 1
			// element long.
			JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER)
					.getJSONObject(0);
			description = weatherObject.getString(OWM_DESCRIPTION);
			weatherId = weatherObject.getInt(OWM_WEATHER_ID);

			// Temperatures are in a child object called "temp". Try not to
			// name variables
			// "temp" when working with temperature. It confuses everybody.
			JSONObject temperatureObject = dayForecast
					.getJSONObject(OWM_TEMPERATURE);
			high = temperatureObject.getDouble(OWM_MAX);
			low = temperatureObject.getDouble(OWM_MIN);

			ContentValues weatherValues = new ContentValues();
			weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationID);
			weatherValues
					.put(WeatherEntry.COLUMN_DATETEXT, WeatherContract
							.getDbDateString(new Date(dateTime * 1000L)));
			weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
			weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
			weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
			weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
			weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
			weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
			weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
			weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

			cVVector.add(weatherValues);
			if(cVVector.size()>0){
				addWeatherEntries(cVVector);
			}

			String highAndLow = formatHighLows(high, low);
			String day = getReadableDateString(dateTime);
			resultStrs[i] = day + " - " + description + " - " + highAndLow;

		}

		return resultStrs;
	}

	private long addLocation(String locationSetting, String cityName,
			double lat, double lon) {

		Cursor cursor = mContext.getContentResolver()
				.query(WeatherContract.LocationEntry.CONTENT_URI,
						new String[] { LocationEntry._ID },
						WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
								+ " = ? ", new String[] { locationSetting },
						null);
		Uri retUri = null;
		if (cursor.moveToFirst()) {
			int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
			return cursor.getLong(locationIdIndex);

		} else {

			ContentValues values = new ContentValues();
			values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
					locationSetting);
			values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
			values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
			values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

			retUri = mContext.getContentResolver().insert(
					WeatherContract.LocationEntry.CONTENT_URI, values);
		}

		return ContentUris.parseId(retUri);

	}

	private int addWeatherEntries(Vector<ContentValues> values) {
		ContentValues[] cvArray = new ContentValues[values.size()];
		values.toArray(cvArray);
		return mContext.getContentResolver().bulkInsert(
				WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

	}

}
