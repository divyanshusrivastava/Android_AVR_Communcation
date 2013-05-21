package com.example.bluetoof;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Callback  {
	
	private static final String TAG = "THINBTCLIENT";
	private static final boolean D = true;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	TextView tv, debug_tv, status_tv;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
	ProgressDialog ShowProgress;
    Thread workerThread;
    volatile boolean stopWorker;
    ScrollView sv;
    private boolean isLighOn = false;

	Camera mCamera;
	public static SurfaceView preview;
	public static SurfaceHolder mHolder;
	ImageView status_img;
    
	
	private static final UUID MY_UUID = 
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// ==> hardcode your server's MAC address here <==
	private static String address = "00:06:66:4E:DE:50";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView)findViewById(R.id.textView1);
		status_tv = (TextView)findViewById(R.id.status_tv);
		debug_tv = (TextView)findViewById(R.id.debug_tv);
		
		status_img = (ImageView)findViewById(R.id.status_img);
        sv = (ScrollView)findViewById(R.id.terminSV);
        
        
        
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, 
				"Bluetooth is not available.", 
				Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, 
				"Please enable your BT and re-run this program.", 
				Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		
		
		
		
		
		Button on_btn = (Button) findViewById(R.id.on_btn);
        // show location button click event
		on_btn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {		
				trasnmit_data("b");
			}
		});
		
		
		Button off_btn = (Button) findViewById(R.id.off_btn);
        // show location button click event
		off_btn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {		
				trasnmit_data("a");
			}
		});
		
		Button temp_btn = (Button) findViewById(R.id.temp_btn);
        // show location button click event
		temp_btn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {		
				trasnmit_data("c");
			}
		});
		
		


		
		
		
	}


	
	public void trasnmit_data(String message){
		// Create a data stream so we can talk to server.

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
		}

		//String message = "b";
		byte[] msgBuffer = message.getBytes();
		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Exception during write.", e);
		}
	}
	

	
	
	
	
    void recieve_data()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        
        try {
            inStream = btSocket.getInputStream();
	    } catch (IOException e) {
	            Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
	    }
        
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                    	if (btSocket.isConnected()){
 	                        int bytesAvailable = inStream.available();                        
	                        if(bytesAvailable > 0)
	                        {
	                            byte[] packetBytes = new byte[bytesAvailable];
	                            inStream.read(packetBytes);
	                            Log.d("serial", "This: "+packetBytes);
	                            for(int i=0;i<bytesAvailable;i++)
	                            {
	                                byte b = packetBytes[i];
	                                if(b == delimiter)
	                                {
	                                    byte[] encodedBytes = new byte[readBufferPosition];
	                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
	                                    final String data = new String(encodedBytes, "US-ASCII");
	                                    readBufferPosition = 0;
	                                    
	                                    Log.d("serial", "This: "+data);
	                                    
	                                    handler.post(new Runnable()
	                                    {
	                                        public void run()
	                                        {
	                                        	if(data.contains("fart")){
	                                        		MediaPlayer mp;
	                                        	     mp = MediaPlayer.create(MainActivity.this, R.raw.fart);   
	                                                 mp.start();
	                                                
	                                        	}
	                                        	
	                                        	if(data.contains("flashlight")){
	                                        		if (isLighOn) {
	                                        			
	                                        			tv.append("Turning off flashlight...\n");	
	                                        			sv.fullScroll(View.FOCUS_DOWN);
	                                					Parameters params = mCamera.getParameters();
	                                					params.setFlashMode(Parameters.FLASH_MODE_OFF);
	                                					  mCamera.setParameters(params);    
	                                					isLighOn = false;
	
	                                				} else {
	                                					tv.append("Turning on flashlight...\n");
	                                					sv.fullScroll(View.FOCUS_DOWN);
	                                					
	                                					Parameters params = mCamera.getParameters();
	                            			            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
	                            			            mCamera.setParameters(params);      
	                            			            mCamera.startPreview();
	                                					isLighOn = true;
	                                				}
	                                        		
	                                        	}else if(data.contains("Light")){
	                                        		debug_tv.setText(data);
	                                        		
	                                        	}else
	                                        	{
	                                        		tv.append(data+"\n");
	                                        		sv.fullScroll(View.FOCUS_DOWN);
	                                        	}
	                                        }
	                                    });
	                                }
	                                else
	                                {
	                                    readBuffer[readBufferPosition++] = b;
	                                }
	                            }
	                        }
                    	}else{
                    		
                    	}
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });

        workerThread.start();
    }
	
	
	
	private void init_bt(){
		
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Socket creation failed.", e);
		}

		mBluetoothAdapter.cancelDiscovery();

		try {
			btSocket.connect();
			Log.e(TAG, "ON RESUME: BT connection established, data transfer link open.");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.e(TAG, 
					"ON RESUME: Unable to close socket during connection failure", e2);
			}
		}

		
	}
	
	
	class loadingTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
	        
			ShowProgress = ProgressDialog.show(MainActivity.this, "",
					"Connecting to BlueSmirf. Please wait...", true);
		}
		
		protected String doInBackground(String... urls) {

			init_bt();
			
			return "";

		}

		protected void onPostExecute(String s) {
		
	        if (ShowProgress != null) { 
	        	ShowProgress.dismiss();
	       }
			if(btSocket.isConnected()){
				status_img.setImageDrawable(getResources().getDrawable(R.drawable.green));
				status_tv.setText("Connected");
				status_tv.setTextColor(Color.GREEN);
				recieve_data();
			}else{
				status_img.setImageDrawable(getResources().getDrawable(R.drawable.red));
				status_tv.setText("Connected");
				status_tv.setTextColor(Color.RED);
				err_pop_up();
			}


		}
	}
	
	
	private void err_pop_up(){
	    	LayoutInflater inflater = LayoutInflater.from(this);		
	        Builder builder			= new AlertDialog.Builder(this);
		  	builder.setTitle("Error Connecting")
		  	.setMessage("Could not connect to BlueSmirf." )
		  	.setPositiveButton("Reconnect",  new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					new loadingTask().execute("");
				}
		  	})
		  	.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
						System.exit(0);
				}
		  	}); 
		  	builder.create().show();
	    
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		new loadingTask().execute("");
		
		
	    mCamera = Camera.open();
		preview = (SurfaceView) findViewById(R.id.PREVIEW);
		
	    mHolder = preview.getHolder();
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        mHolder.setSizeFromLayout();
	    mHolder.addCallback(this);


	}

	@Override
	public void onPause() {
		super.onPause();


		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				Log.e(TAG, "ON PAUSE: Couldn't flush output stream.", e);
			}
		}

		try	{
			btSocket.close();
		} catch (IOException e2) {
			Log.e(TAG, "ON PAUSE: Unable to close socket.", e2);
		}
		if(mCamera != null){
		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);    
		isLighOn = false;
		}
		
	}

	@Override
	public void onStop() {
		super.onStop();
		
		
		if(mCamera != null){
		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);    
		isLighOn = false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(mCamera != null){
		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);    
		isLighOn = false;
		}
	}



	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.startPreview();
	}



	public void surfaceCreated(SurfaceHolder holder) {
		
	}



	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
}