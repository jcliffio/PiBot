package com.bigreddoglabs.pibot;

import com.bigreddoglabs.pibot.MjpegInputStream;
import com.bigreddoglabs.pibot.MjpegView;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
import android.widget.TextView;


public class DriverActivity extends Activity {
	
	private static final String TAG = "MjpegActivity";
	
	public SharedPreferences sharedPref;
	public AtomicReference<String> prefPiAddress;
	public AtomicReference<String> prefRestPort;
	String prefMjpegPort;
	
	String MjpegURL;
	String RestURL;
	
	String videoHTML;
	
	private MjpegView videoFeed;
	
	JoystickView joystickMotor;
	JoystickView joystickCam;
	
	Controller c;
	
	final LinkedBlockingQueue<Controller> outQueue = new LinkedBlockingQueue<Controller>(10);
	
	static TextView longitude;
	static TextView latitude;
	static TextView heading;
	static TextView temperature;
	static TextView pressure;
	static TextView altitude;
	
	static final Handler receivedDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0)
			{
				Sensor s = (Sensor)msg.obj;
				
				longitude.setText(Integer.toString(s.getLongitude()));
				latitude.setText(Integer.toString(s.getLatitude()));
				heading.setText(Integer.toString(s.getHeading()));
				temperature.setText(Integer.toString(s.getTemperature()));
				pressure.setText(Integer.toString(s.getPressure()));
				//altitude.setText(Integer.toString(s.getAltitude()));
			}
			super.handleMessage(msg);
		}
	};
	
	Thread httpSendThread;
	Thread httpReceiveThread;
	boolean sendThreadGo;
	boolean receiveThreadGo;
	
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
		
		setContentView(R.layout.activity_driver);
		
		longitude = (TextView)findViewById(R.id.longitudeTextView);
		latitude = (TextView)findViewById(R.id.latitudeTextView);
		heading = (TextView)findViewById(R.id.headingTextView);
		temperature = (TextView)findViewById(R.id.temperatureTextView);
		pressure = (TextView)findViewById(R.id.pressureTextView);
		//altitude = (TextView)findViewById(R.id.altitudeTextView);
		
		videoFeed = (MjpegView)findViewById(R.id.videoFeed);
		
		c = new Controller();
		
		joystickMotor = (JoystickView)findViewById(R.id.jsMotor);
        joystickMotor.setOnJoystickMovedListener(_motorListener);
        
        joystickCam = (JoystickView)findViewById(R.id.jsCam);
        joystickCam.setOnJoystickMovedListener(_camListener);
        
        httpSendThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	Controller lastC = new Controller();
	        	Controller c = new Controller();
		        try {
		        	//URL to POST our serial string to
		        	String URL = "http://" + prefPiAddress.get() + ":" 
		        				+ prefRestPort.get() + "/api/pibot/";
		        	HttpClient httpClient = new DefaultHttpClient();
		        	//continuously send POST commands to the CherryPy server
		            while (sendThreadGo) {
		            	c.set(outQueue.take());
		            	Log.i("data", c.toString());
		            	if (lastC.equals(c))
		            	{
		            	}
		            	else
		            	{
		            		String sendString = c.toString();
			                HttpPost get = new HttpPost(URL + sendString + "/");
			                try {
								HttpResponse rp = httpClient.execute(get);
								//Monitor responses from the server
								Log.i("response", EntityUtils.toString(rp.getEntity()));
							} catch (ClientProtocolException e) {
								Log.i("error", "ERROR");
								e.printStackTrace();
							} catch (IOException e) {
								Log.i("error", "ERROR");
								e.printStackTrace();
							}
			                lastC.set(c);
		            	}
		            }
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		    }
		});
        
        httpReceiveThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	//URL to GET data from
		        String URL = "http://" + prefPiAddress.get() + ":" + prefRestPort.get() + "/api/pibot/";
		        HttpClient httpClient = new DefaultHttpClient();		        	
				while (receiveThreadGo) {
				    HttpGet get = new HttpGet(URL);
//				    try {
//						HttpResponse response = httpClient.execute(get);
//						Log.i("receivedData", EntityUtils.toString(response.getEntity()));
//						HttpEntity entity = response.getEntity();
//						String text = getASCIIContentFromEntity(entity);
//						
//						Sensor s = unpackData(getDataFromString(text));
//						sendMessageToUI(s);
//						Thread.sleep(1000);
//					}				    
//				    catch (ClientProtocolException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (Exception e) {
//						Log.i("recieveThread", e.toString());
//					}
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
		    
		    protected void sendMessageToUI(Sensor s)
		    {
		    	Message msg = receivedDataHandler.obtainMessage();
				msg.what = 0;
				msg.obj = s;
				receivedDataHandler.sendMessage(msg);
		    }
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sendThreadGo = true;
		httpSendThread.start();
		receiveThreadGo = true;
		httpReceiveThread.start();
		
		new DoRead().execute(MjpegURL);
	}
	
	public void onPause()
	{
		super.onPause();
		sendThreadGo = false;
		httpSendThread.interrupt();
		receiveThreadGo = false;
		httpReceiveThread.interrupt();
		Log.i("driver", "THREAD PAUSED");
		
		videoFeed.stopPlayback();
	}

	public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();     
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            videoFeed.setSource(result);
            videoFeed.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            videoFeed.showFps(true);
        }
    }
	
	private JoystickMovedListener _motorListener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int x, int y) {
                if (y == 0)
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
        	if (pan == 0 && tilt == 0)
        	{
        		
        	}
        	else
        	{
        		c.setCamPan(pan);
            	c.setCamTilt(tilt * -1);
            	addToOutQueue(c);
        	}
        }

        @Override
        public void OnReleased() {
        	
        }
        
        public void OnReturnedToCenter() {
        	c.setCamPan(0);
        	c.setCamTilt(0);
        	addToOutQueue(c);
        };
	}; 
	
	private void addToOutQueue(Controller c)
	{
		if (outQueue.remainingCapacity() == 0)
		{
			outQueue.removeAll(outQueue);
			Log.i("addToOutQueue", "CLEARED QUEUE");
		}
		outQueue.add(c);
	}
}
