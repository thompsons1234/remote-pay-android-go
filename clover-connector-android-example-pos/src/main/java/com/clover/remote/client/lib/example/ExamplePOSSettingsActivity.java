package com.clover.remote.client.lib.example;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

public class ExamplePOSSettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	//private TextView endpointTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		initSummary(getPreferenceScreen());

		CustomEditTextPreference prefDialog = (CustomEditTextPreference) findPreference(ExamplePOSActivity.EXAMPLE_POS_SERVER_KEY);
		prefDialog.show(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
										  String key) {
		updatePrefSummary(findPreference(key));
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceGroup) {
			PreferenceGroup pGrp = (PreferenceGroup) p;
			for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
				initSummary(pGrp.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {

		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;

			String text = editTextPref.getText();
			if(text == null || "".equals(text)) {
				text = "<None>";
			}
			p.setSummary(text);
		}
	}

}
