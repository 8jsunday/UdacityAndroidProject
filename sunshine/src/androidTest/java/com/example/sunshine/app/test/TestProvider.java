package com.example.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.sunshine.app.data.WeatherDbHelper;

public class TestProvider extends AndroidTestCase {
	static public String TEST_CITY_NAME = "North Pole";
	static public String TEST_LOCATION = "99705";
	static public String TEST_DATE = "20141205";

	public static final String LOG_TAG = TestDb.class.getSimpleName();

	public void testDeleteDb() throws Throwable {

		mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);

	}

	public void testInsertReadDb() {

		ContentValues values = TestDb.getLocationContentValues();

	
		Uri weatherUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
		long locationRowId = ContentUris.parseId(weatherUri);
		assertTrue(locationRowId != -1);
		Log.v(LOG_TAG, "New row id: " + locationRowId);
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					LocationEntry.buildLocationUri(locationRowId), // Table to
																	// Query
					null, // all columns
					null, // Columns for the "where" clause
					null, // Values for the "where" clause
					null // sort order
					);

			TestDb.validateCursor(cursor, values);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		ContentValues weatherValues = TestDb
				.getWeatherContentValues(locationRowId);

		Uri insertUri = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);
		long weatherRowId = ContentUris.parseId(insertUri);
		Cursor weatherCursor;
		try {
			weatherCursor = mContext.getContentResolver().query(
					WeatherEntry.CONTENT_URI, // Table to
					// Query
					null, // leaving "columns" null just returns all the
							// columns.
					null, // cols for "where" clause
					null, // values for "where" clause
					null // sort order
					);
			if (weatherCursor.moveToFirst()) {
				TestDb.validateCursor(weatherCursor, weatherValues);
			} else {
				fail("No weather data returned!");
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		/* TESTING FOR CONTENT URI WITH LOCATION */
		try {
			weatherCursor = mContext.getContentResolver().query(
					WeatherEntry.buildWeatherLocation(TEST_LOCATION), null,
					null, null, null);

			if (weatherCursor.moveToFirst()) {
				TestDb.validateCursor(weatherCursor, weatherValues);
			} else {
				fail("No weather data returned!");
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		/* TESTING FOR CONTENT URI WITH LOCATION AND DATE */
		
		try {
			weatherCursor = mContext.getContentResolver().query(
					WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION,TEST_DATE), null,
					null, null, null);

			if (weatherCursor.moveToFirst()) {
				TestDb.validateCursor(weatherCursor, weatherValues);
			} else {
				fail("No weather data returned!");
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		

	}

	public void testGetType() {

		// content://com.example.android.sunshine.app/weather/
		String type = mContext.getContentResolver().getType(
				WeatherEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testLocation = "94074";
		// content://com.example.android.sunshine.app/weather/94074
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocation(testLocation));
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testDate = "20140612";
		// content://com.example.android.sunshine.app/weather/94074/20140612
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocationWithDate(testLocation,
						testDate));
		// vnd.android.cursor.item/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

		// content://com.example.android.sunshine.app/location/
		type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_TYPE, type);

		// content://com.example.android.sunshine.app/location/1
		type = mContext.getContentResolver().getType(
				LocationEntry.buildLocationUri(1L));
		// vnd.android.cursor.item/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);

	}

}
