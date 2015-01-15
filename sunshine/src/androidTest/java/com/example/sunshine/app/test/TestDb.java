package com.example.sunshine.app.test;

import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.sunshine.app.data.WeatherDbHelper;

public class TestDb extends AndroidTestCase {

	static String testLocationSetting = "99705";
	static String testCityName = "North Pole";
	static double testLatitude = 64.7488;
	static double testLongitude = -147.353;

	public static final String LOG_TAG = TestDb.class.getSimpleName();

	public void testCreateDb() throws Throwable {

		mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
		SQLiteDatabase db = new WeatherDbHelper(this.mContext)
				.getWritableDatabase();
		assertEquals(true, db.isOpen());
		db.close();
	}

	public void testInsertReadDb() {

		WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues values = getLocationContentValues();

		long locationRowId;
		locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

		assertTrue(locationRowId != -1);
		Log.v(LOG_TAG, "New row id: " + locationRowId);

		Cursor cursor = db.query(LocationEntry.TABLE_NAME, // Table to Query
				null, // all columns
				null, // Columns for the "where" clause
				null, // Values for the "where" clause
				null, // columns to group by
				null, // columns to filter by row groups
				null // sort order
				);

		validateCursor(cursor, values);

		ContentValues weatherValues = getWeatherContentValues(locationRowId);

		long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null,
				weatherValues);

		assertTrue(weatherRowId != -1);

		Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME, // Table to
				// Query
				null, // leaving "columns" null just returns all the columns.
				null, // cols for "where" clause
				null, // values for "where" clause
				null, // columns to group by
				null, // columns to filter by row groups
				null // sort order
				);

		validateCursor(weatherCursor, weatherValues);
		dbHelper.close();

	}

	static ContentValues getLocationContentValues() {

		ContentValues values = new ContentValues();
		values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
		values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
		values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
		values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);
		return values;

	}

	static ContentValues getWeatherContentValues(long locationRowId) {

		ContentValues weatherValues = new ContentValues();
		weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
		weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
		weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
		weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
		weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
		weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
		weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
		weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
		weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
		weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

		return weatherValues;
	}

	static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

		assertTrue(valueCursor.moveToFirst());

		Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
		for (Map.Entry<String, Object> entry : valueSet) {
			String columnName = entry.getKey();
			int idx = valueCursor.getColumnIndex(columnName);
			assertFalse(idx == -1);
			String expectedValue = entry.getValue().toString();
			assertEquals(expectedValue, valueCursor.getString(idx));
		}
		valueCursor.close();
	}
}
