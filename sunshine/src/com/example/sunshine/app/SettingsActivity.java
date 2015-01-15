package com.example.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private final String LOG_TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref_general);
		
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_unit_key)));
	}

	private void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(this);

		// Trigger the listener immediately with the preference's
		// current value.
		onPreferenceChange(preference, PreferenceManager
				.getDefaultSharedPreferences(preference.getContext())
				.getString(preference.getKey(), ""));

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		String stringValue = newValue.toString();

		if (preference instanceof ListPreference) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate
			// labels/values).
			ListPreference listPreference = (ListPreference) preference;

			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);

			}

		} else {
			// For other preferences, set the summary to the value's simple
			// string representation.
			preference.setSummary(stringValue);

		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplication());
		prefs.edit().putString(preference.getKey(), stringValue).apply();

		/*
		 * if (stringValue.length() < 1) { prefs.edit()
		 * .putString(preference.getKey(),
		 * getString(R.string.pref_location_default)).apply();
		 * preference.setSummary(getString(R.string.pref_location_default)); }
		 */

		return false;
	}
}
