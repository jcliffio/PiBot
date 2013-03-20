package com.bigreddoglabs.pibot;

import android.preference.PreferenceFragment;
import com.bigreddoglabs.pibot.R;
import android.os.Bundle;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
