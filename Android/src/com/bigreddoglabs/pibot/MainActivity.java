package com.bigreddoglabs.pibot;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_activity_settings:
    		Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
    		MainActivity.this.startActivity(intent_settings);
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    public void startDriving(View view) {
    	Intent i = new Intent(MainActivity.this, DriverActivity.class);
    	startActivity(i);
    }
    
}
