package com.bigreddoglabs.pibot;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;


public class DriverActivity extends Activity {
	
	private static final boolean DEBUG=false;
	private static final String TAG = "MjpegActivity";
	
	SharedPreferences sharedPref;
	String prefRouterSSID;
	String prefPiAddress;
	String prefMjpegPort;
	
	String MjpegURL;
	String RestURL;
	
	String videoHTML;
	
//	ConnectivityManager connMgr;
//	NetworkInfo netInfo;
//	
//	WifiManager wifiMgr;
//	WifiInfo wifiInfo;
	
//	AlertDialog alertRouterNotConnected;
	
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
//		wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		//get Preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//		prefRouterSSID = sharedPref.getString("pref_routerSSID", "");
		prefPiAddress = sharedPref.getString("pref_pi_ip", "");
		prefMjpegPort = sharedPref.getString("pref_pi_video_port", "");	
		
		MjpegURL = "http://" + prefPiAddress + ":" + prefMjpegPort + "/?action=stream";
		
		videoHTML = "<html><body><img src=\"" + MjpegURL + "\" width=\"100%\"/></body></html>";
		
//		alertRouterNotConnected = buildRouterDialog();
		
		setContentView(R.layout.activity_driver);
		
		wv = (WebView)findViewById(R.id.wv);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wv.loadData(videoHTML, "text/html", null);

//		wifiInfo = wifiMgr.getConnectionInfo();
//		
//		boolean isWifiEnabled = wifiMgr.isWifiEnabled();
//		//Turn Wifi on, wait until enabled.
//		if(!isWifiEnabled)
//		{
//			wifiMgr.setWifiEnabled(true);
//			while(!wifiMgr.isWifiEnabled());
//		}
//		
//		String ssid = wifiInfo.getSSID();
//		ssid = ssid.replaceAll("^\"|\"$", "");
//		if(ssid != prefRouterSSID)
//		{
//			try
//			{
//				String chosenSSID = "";
//				int netID = -1;
//				List<WifiConfiguration> wifiConfiged = wifiMgr.getConfiguredNetworks();
//				for(WifiConfiguration w: wifiConfiged)
//				{
//					String trimmedSSID = w.SSID.toString().replaceAll("^\"|\"$", "");
//					if(trimmedSSID.equals(prefRouterSSID))
//					{
//						netID = w.networkId;
//						chosenSSID = trimmedSSID;
//						Log.d("DriverActivity",netID + " " + chosenSSID);
//						break;
//					}
//				}
//				if(netID != -1)
//				{
//					wifiMgr.enableNetwork(netID, true);
//					wifiInfo = wifiMgr.getConnectionInfo();
//					while(wifiInfo.getSupplicantState() != SupplicantState.COMPLETED)
//					{
//						wifiInfo = wifiMgr.getConnectionInfo();
//					}
//				}
//				else
//				{
//					alertRouterNotConnected.show();
//					Intent i = new Intent(DriverActivity.this, MainActivity.class);
//					startActivity(i);
//				}
//			}
//			catch(Exception e)
//			{
//				Toast t = Toast.makeText(this, "An error occured connecting to " + prefRouterSSID + "\n\n" + e.toString(), Toast.LENGTH_LONG);
//				t.show();
//				Intent i = new Intent(DriverActivity.this, MainActivity.class);
//				startActivity(i);
//			}
//		}
		
	}
	
//	public AlertDialog buildRouterDialog() {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.driver_dialog_router_title)
//        	.setMessage(R.string.driver_dialog_router_message)
//        	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//        		public void onClick(DialogInterface dialog, int id) {
//        			Intent i = new Intent(DriverActivity.this, SettingsActivity.class);
//        			startActivity(i);
//        		}
//        	})
//        	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//        		public void onClick(DialogInterface dialog, int id) {
//        			Intent i = new Intent(DriverActivity.this, MainActivity.class);
//        			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        			startActivity(i);
//        		}
//        	});
//        // Create the AlertDialog object and return it
//        return builder.create();
//	}

//    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
//        protected MjpegInputStream doInBackground(String... url) {
//            //TODO: if camera has authentication deal with it and don't just not work
//            HttpResponse res = null;
//            DefaultHttpClient httpclient = new DefaultHttpClient();     
//            Log.d(TAG, "1. Sending http request");
//            try {
//                res = httpclient.execute(new HttpGet(URI.create(url[0])));
//                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
//                if(res.getStatusLine().getStatusCode()==401){
//                    //You must turn off camera User Access Control before this will work
//                    return null;
//                }
//                return new MjpegInputStream(res.getEntity().getContent());  
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//                Log.d(TAG, "Request failed-ClientProtocolException", e);
//                //Error connecting to camera
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "Request failed-IOException", e);
//                //Error connecting to camera
//            }
//            return null;
//        }
//
//        protected void onPostExecute(MjpegInputStream result) {
//            mv.setSource(result);
//            if(result!=null) result.setSkip(1);
//            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
//            mv.showFps(true);
//        }
//    }
}
