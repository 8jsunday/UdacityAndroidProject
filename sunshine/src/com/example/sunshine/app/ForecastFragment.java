package com.example.sunshine.app;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ForecastFragment extends Fragment {

	private ArrayAdapter<String> forecastAdapter;

	public ForecastFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.forecastfragment, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			updateWeather();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);

		forecastAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item_forecast, R.id.list_item_forecast_textview,
				new ArrayList<String>());

		ListView listView = (ListView) rootView
				.findViewById(R.id.listview_forecast);
		listView.setAdapter(forecastAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String forecastDetail = parent.getItemAtPosition(position)
						.toString();

				// Toast sample

				/*
				 * Toast toast = Toast.makeText(getActivity(), forecastDetail,
				 * 2); toast.show();
				 */

				Intent intent = new Intent(getActivity(), DetailActivity.class);
				intent.putExtra(Intent.EXTRA_TEXT, forecastDetail);
				startActivity(intent);
			}
		});
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateWeather();
	}

	private void updateWeather() {

		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(),
				forecastAdapter);

		fetchWeatherTask.execute(Utility.getPrefferedLocation(getActivity()));
	}

}
