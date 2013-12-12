package com.gingerio.activitydetect;

//--------------------------------------------------------------------------------------------------
//
//--------------------------------------------------------------------------------------------------


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.google.common.primitives.Floats;
import com.gingerio.activitydetect.R;

public class MainActivity extends Activity implements OnClickListener, DialogInterface.OnClickListener{
	private static final String TAG = "GingerIO::Activity";
	private SensorManager mSensorManager;
	Button info, start, train, test;
	SensorEventListener accelerationListener;
    int sensorType1 = Sensor.TYPE_ACCELEROMETER;
    int sensorType2 = Sensor.TYPE_MAGNETIC_FIELD;
    float x = 0, y = 0, z = 0;
    TextView valuesText;
    List<Float> accData;
    private List<Float> accX; 
	private List<Float> accY;
	private List<Float> accZ;
    long timeStamp;
    List<Long> time;
    private AccDeviceView graphView;
    long totalTime = 2000; // total amount of data collection time in milliseconds. The accelerometer stops sensing after this time has elapsed.
    AlertDialog.Builder alertDialogBuilder;
	LayoutInflater li;
	EditText userInput;
	AlertDialog alertDialog;
	View promptsView;
	float label;
	DetectActivity act;
	
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    act = new DetectActivity();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		graphView = new AccDeviceView(this); // ensures that the streaming accelerometer data can be visualized.
		setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.accelerometer_ui);
		layout.setBackgroundColor(Color.WHITE);
		//-----------------------------------------------------------------------------------------------
		// Initialize all the UI widgets
		start=(Button)findViewById(R.id.startSensor);
        start.setOnClickListener(this);
        info=(Button)findViewById(R.id.infoButton);
        info.setOnClickListener(this);
        train=(Button)findViewById(R.id.trainButton);
        train.setOnClickListener(this);
        test=(Button)findViewById(R.id.testButton);
        test.setOnClickListener(this);
        valuesText = (TextView) findViewById(R.id.accValuesText);
        graphView = (AccDeviceView) findViewById(R.id.accelGraph);
        resetAxisText(); 
        //-----------------------------------------------------------------------------------------------
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        //------------------------------------------------------------------------------------------------
        // Dialog inflation when train button is pressed
        li = LayoutInflater.from(this);
		promptsView = li.inflate(R.layout.prompts, null);
		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setPositiveButton("Ok", this);
		alertDialogBuilder.setNegativeButton("Cancel", this);
		userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
		
		alertDialogBuilder.setView(promptsView);
		alertDialog = alertDialogBuilder.create();
		//------------------------------------------------------------------------------------------------
		// initialize the activity detection class.
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
//------------------------------------------------------------------------------------------------------
	// Response to button clicks by the user
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == start){
			startRecording();
			mTimer.start();
		}else if(v == info){
			
		}else if(v == train){
			alertDialogBuilder.setCancelable(false);
			alertDialog.show();			
		}else if(v == test){
			
		}
	}
//------------------------------------------------------------------------------------------------------
	// Count down timer to ensure accelerometer data is sensed only for 'totalTime' number of seconds.
	private final CountDownTimer mTimer = new CountDownTimer(totalTime, 1000) {
		@Override
		public void onTick(final long millisUntilFinished) {
		}
		@Override
		public void onFinish() {
			stopRecording();
		}
    };
//-------------------------------------------------------------------------------------------------------
 // get data from the accelerometer and store the x, y and z-axis values, and the signal magnitude vector.
	public void startRecording(){
		time = new ArrayList<Long>();
		accX = new ArrayList<Float>();
		accY =new ArrayList<Float>();
		accZ = new ArrayList<Float>();
		accData = new ArrayList<Float>();
		accelerationListener = new SensorEventListener() {
	    	
	    	@Override    
	        public void onSensorChanged(SensorEvent event) {
	            float accMag = 0;
	            graphView.setValues(event);
	            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
	            x = event.values[0];
	            y = event.values[1];
	            z = event.values[2];
	            timeStamp = event.timestamp;
            	time.add(timeStamp);
	            float sum = (float) (Math.pow(x, 2)+Math.pow(y, 2)+Math.pow(z, 2));
            	accMag = (float) (Math.sqrt(sum)-9.8); 
            	accData.add(accMag);
            	accX.add(x);
            	accY.add(y);
            	accZ.add(z);
            	updateText(); // update the UI to display the acceleration values.
	           }
	    }
	    	@Override
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        }
	    };
	    
        mSensorManager.registerListener(accelerationListener,mSensorManager.getDefaultSensor(sensorType1),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accelerationListener,mSensorManager.getDefaultSensor(sensorType2),
                SensorManager.SENSOR_DELAY_FASTEST);
	}
//------------------------------------------------------------------------------------------------------------------
	public void stopRecording(){
		mSensorManager.unregisterListener(accelerationListener);
		
	}
//------------------------------------------------------------------------------------------------------------------
	public void startTraining(){
		float[] x = Floats.toArray(accX);
		float[] y = Floats.toArray(accY);
		float[] z = Floats.toArray(accZ);
		float[] mag = Floats.toArray(accData);
		
		act.train(x, y, z, mag, label);
		Toast toast1 = Toast.makeText(this, "Done!", Toast.LENGTH_SHORT);
		toast1.show();
	}
//-------------------------------------------------------------------------------------------------------------------
	// Update the UI with the x, y and z axis instantaneous acceleration values
	public void updateText(){
		DecimalFormat twoDForm = new DecimalFormat("#.###");
		SpannableString text = new SpannableString("X-axis:    " +twoDForm.format(x)+ " m/s\u00B2  \n\nY-axis:    "+twoDForm.format(y)+ " m/s\u00B2 \n\nZ-axis:    "+twoDForm.format(z)+ " m/s\u00B2");
		int len = text.length();
		text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 7, 0); // x-axis color
		text.setSpan(new ForegroundColorSpan(Color.BLACK), 8, 21, 0);
		text.setSpan(new ForegroundColorSpan(Color.RED), 23, 33, 0);  // y-axis color
		text.setSpan(new ForegroundColorSpan(Color.BLACK), 35, 41, 0);
		text.setSpan(new ForegroundColorSpan(Color.GREEN), 48, 58, 0);// z-axis color
		valuesText.setText(text, BufferType.SPANNABLE);
	}
//-------------------------------------------------------------------------------------------------------------------
	// Reset axis text in the UI
	public void resetAxisText(){
		 DecimalFormat twoDForm = new DecimalFormat("#.###");
			SpannableString text = new SpannableString("X-axis:    " +twoDForm.format(x)+ " m/s\u00B2  \n\nY-axis:    "+twoDForm.format(y)+ " m/s\u00B2 \n\nZ-axis:    "+twoDForm.format(z)+ " m/s\u00B2");
			int len = text.length();
			text.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 7, 0); // x-axis color
			text.setSpan(new ForegroundColorSpan(Color.RED), 21, 31, 0);  // y-axis color
			text.setSpan(new ForegroundColorSpan(Color.GREEN), 38, 48, 0);// z-axis color
			valuesText.setText(text, BufferType.SPANNABLE);
			
	 }
//-------------------------------------------------------------------------------------------------------------------
	// Dialog for user to enter activity labels
	 @Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
				switch(which){
				case DialogInterface.BUTTON_POSITIVE:
					label = Float.parseFloat(userInput.getText().toString());
					startTraining();
					break;
				
				case DialogInterface.BUTTON_NEGATIVE:
				dialog.cancel();
				break;
				
				default:
					break;
				}
			
		}
//-------------------------------------------------------------------------------------------------------------------
	// Android activity life cycle managing methods  
	@Override
	    protected void onStop() {
	        mSensorManager.unregisterListener(accelerationListener);
	        super.onStop();
	    }
    
    @Override
	    protected void onPause() {
	        mSensorManager.unregisterListener(accelerationListener);
	        super.onPause();
	    }
	    public void onDestroy() {
	        super.onDestroy();
	        mSensorManager.unregisterListener(accelerationListener);
	      }
//----------------------------------------------------------------------------------------------------------------------
	 @Override
	  	public void onBackPressed() {  
	  	   	finish();
	  		super.onBackPressed();     
	  	}

}
