package com.bigreddoglabs.pibot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;


public class DriverActivity extends Activity {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "MjpegActivity";
	
	public SharedPreferences sharedPref;
	public AtomicReference<String> prefPiAddress;
	public AtomicReference<String> prefRestPort;
	String prefMjpegPort;
	
	String MjpegURL;
	String RestURL;
	
	String videoHTML;
	
	private WebView wv;
	
	JoystickView joystickMotor;
	JoystickView joystickCam;
	
	Controller c;
	
	final ArrayBlockingQueue<Controller> outQueue = new ArrayBlockingQueue<Controller>(50);
	
	final Handler receivedDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
		}
	};
	
	Thread httpSendThread;
	Thread httpReceiveThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefPiAddress = new AtomicReference<String>();
        prefPiAddress.set(sharedPref.getString("pref_pi_ip", ""));
        prefRestPort = new AtomicReference<String>();
        prefRestPort.set(sharedPref.getString("pref_pi_rest_port", ""));
        prefMjpegPort = sharedPref.getString("pref_pi_video_port", "");
        
		MjpegURL = "http://" + prefPiAddress.get() + ":" + prefMjpegPort + "/?action=stream";
		
		videoHTML = "<html><body><img src=\"" + MjpegURL + "\" width=\"750px\"/></body></html>";
		
//		alertRouterNotConnected = buildRouterDialog();
		
		setContentView(R.layout.activity_driver);
		
		wv = (WebView)findViewById(R.id.wv);
		
		c = new Controller();
		
		joystickMotor = (JoystickView)findViewById(R.id.jsMotor);
        joystickMotor.setOnJoystickMovedListener(_motorListener);
        
        joystickCam = (JoystickView)findViewById(R.id.jsCam);
        joystickCam.setOnJoystickMovedListener(_camListener);
        
        httpSendThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		        try {
		        	String URL = "http://" + prefPiAddress.get() + ":" + prefRestPort.get() + "/devices/serial0";
		        	HttpClient httpClient = new DefaultHttpClient();		        	
		            while (true) {
		            	String sendString = packageString(outQueue.take());
		                HttpPost post = new HttpPost(URL + sendString);
		                try {
							httpClient.execute(post);
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		    }
		    
		    public String packageString(Controller c)
		    {
		    	return "$" + c.getlMotorDirection() + "," +
		    			c.getlMotorSpeed() + "," +
		    			c.getrMotorDirection() + "," +
		    			c.getrMotorSpeed() + "," +
		    			c.getCamPan() + "," +
		    			c.getCamTilt() + "#";
		    }
		});
        
        httpReceiveThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		        String URL = "http://" + prefPiAddress.get() + ":" + prefRestPort.get() + "/devices/serial0";
				HttpClient httpClient = new DefaultHttpClient();		        	
				while (true) {
				    HttpGet get = new HttpGet(URL);
				    try {
						HttpResponse response = httpClient.execute(get);
						HttpEntity entity = response.getEntity();
						String text = getASCIIContentFromEntity(entity);
						Sensor s = unpackData(getDataFromString(text));
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		    }
		   
		    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
		        InputStream in = entity.getContent();
		        StringBuffer out = new StringBuffer();
		        int n = 1;
		        while (n > 0) {
		            byte[] b = new byte[4096];
		            n = in.read(b);
		            if (n > 0)
		                out.append(new String(b, 0, n));
		        }
		        return out.toString();
		    }
		    
		    protected String getDataFromString(String t)
		    {
		    	try
		    	{
		    		return t.substring(t.indexOf('$') + 1, t.indexOf('#', t.indexOf('$')) - 1);
		    	}
		    	catch(Exception e)
		    	{
		    		return "";
		    	}
		    }

		    protected Sensor unpackData(String t)
		    {
		    	Sensor sensor = new Sensor();
		    	try
		    	{
		    		String[] data = t.split(",");
		    		sensor.setLongitude(Integer.parseInt(data[0]));
		    		sensor.setLatitude(Integer.parseInt(data[1]));
		    		sensor.setHeading(Integer.parseInt(data[2]));
		    		sensor.setTemperature(Integer.parseInt(data[3]));
		    		sensor.setPressure(Integer.parseInt(data[4]));
		    		sensor.setAltitude(Integer.parseInt(data[5]));
		    	}
		    	catch(Exception e)
		    	{
		    		
		    	}
		    	return sensor;
		    }
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wv.loadData(videoHTML, "text/html", null);
		
		httpSendThread.start();
	}
	
	public void onPause()
	{
		super.onPause();
		httpSendThread.interrupt();
	}

//	public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
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
//
//            return null;
//        }
//
//        protected void onPostExecute(MjpegInputStream result) {
//            mv.setSource(result);
//            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
//            mv.showFps(true);
//        }
//    }
	
	private JoystickMovedListener _motorListener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int x, int y) {
                if (x == 0)
                {
                	c.setMotorDirection(1);
                }
                else if (y < 0)
                {
                	c.setMotorDirection(2);
                }
                else
                {
                	c.setMotorDirection(0);
                }
                
                int absY = Math.abs(y);
                int absX = Math.abs(x);
                
                int initSpeed = 100 + (int)(15.5 * (double)absY);
                
                int subAmount = (int)((initSpeed - 100) / 10 * absX);
                
                if (x == 0)
                {
                	c.setlMotorSpeed(initSpeed);
                }
                else if (x < 0)
                {
                	c.setlMotorSpeed(initSpeed - subAmount);
                	c.setrMotorSpeed(initSpeed);
                	
                }
                else
                {
                	c.setlMotorSpeed(initSpeed);
                	c.setrMotorSpeed(initSpeed - subAmount);
                }
                
                addToOutQueue(c);
        }

        @Override
        public void OnReleased() {
            c.setMotorDirection(1);
            c.setMotorSpeed(100);
            addToOutQueue(c);
        }
        
        public void OnReturnedToCenter() {
        	c.setMotorDirection(1);
            c.setMotorSpeed(100);
            addToOutQueue(c);
        }
	}; 
	
	private JoystickMovedListener _camListener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
        	c.setCamPan(pan);
        	c.setCamTilt(tilt * -1);
        	addToOutQueue(c);
        }

        @Override
        public void OnReleased() {
        	c.setCamPan(0);
        	c.setCamTilt(0);
        	addToOutQueue(c);
        }
        
        public void OnReturnedToCenter() {
        	c.setCamPan(0);
        	c.setCamTilt(0);
        	addToOutQueue(c);
        };
	}; 
	
	private void addToOutQueue(Controller c)
	{
		try {
			if (outQueue.remainingCapacity() == 0)
			{
				outQueue.removeAll(outQueue);
				Log.i("addToOutQueue", "CLEARED QUEUE");
			}
			outQueue.put(c);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
