package com.bigreddoglabs.pibot;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		Intent intent_home = new Intent(SettingsActivity.this, MainActivity.class);
    		intent_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent_home);
    		return true;
    	case R.id.action_activity_settings_help:
    		Intent intent_settings_help = new Intent(SettingsActivity.this, SettingsHelpActivity.class);
    		startActivity(intent_settings_help);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

}
