package com.bigreddoglabs.pibot;

import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class DriverActivity extends Activity {
	
	SharedPreferences sharedPref;
	String prefRouterSSID;
	
	ConnectivityManager connMgr;
	NetworkInfo netInfo;
	
	WifiManager wifiMgr;
	WifiInfo wifiInfo;
	
	AlertDialog alertRouterNotConnected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driver);
		
		wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		//get Preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefRouterSSID = sharedPref.getString("pref_routerSSID", "");
		
		alertRouterNotConnected = buildRouterDialog();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		wifiInfo = wifiMgr.getConnectionInfo();
		
		boolean isWifiEnabled = wifiMgr.isWifiEnabled();
		//Turn Wifi on, wait until enabled.
		if(!isWifiEnabled)
		{
			wifiMgr.setWifiEnabled(true);
			while(!wifiMgr.isWifiEnabled());
		}
		
		String ssid = wifiInfo.getSSID();
		ssid = ssid.replaceAll("^\"|\"$", "");
		if(ssid != prefRouterSSID)
		{
			try
			{
				String chosenSSID = "";
				int netID = -1;
				List<WifiConfiguration> wifiConfiged = wifiMgr.getConfiguredNetworks();
				for(WifiConfiguration w: wifiConfiged)
				{
					String trimmedSSID = w.SSID.toString().replaceAll("^\"|\"$", "");
					if(trimmedSSID.equals(prefRouterSSID)) {
						netID = w.networkId;
						chosenSSID = trimmedSSID;
						break;
					}
				}
				if(netID != -1) {
					wifiMgr.enableNetwork(netID, true);
					wifiInfo = wifiMgr.getConnectionInfo();
					while(wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
						wifiInfo = wifiMgr.getConnectionInfo();
					}
				}
				else {
					alertRouterNotConnected.show();
				}
			}
			catch(Exception e)
			{
				Toast t = Toast.makeText(this, "An error occured connecting to " + prefRouterSSID + "\n\n" + e.toString(), Toast.LENGTH_LONG);
				t.show();
				Intent i = new Intent(DriverActivity.this, MainActivity.class);
				startActivity(i);
			}
		}
	}
	
	public AlertDialog buildRouterDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.driver_dialog_router_title)
        	.setMessage(R.string.driver_dialog_router_message)
        	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			Intent i = new Intent(DriverActivity.this, SettingsActivity.class);
        			startActivity(i);
        		}
        	})
        	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			Intent i = new Intent(DriverActivity.this, MainActivity.class);
        			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        			startActivity(i);
        		}
        	});
        // Create the AlertDialog object and return it
        return builder.create();
	}

}
