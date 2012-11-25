package com.example.capstone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GPSTrackingActivity extends Activity {
	 
	Button btnShowLocation;
	
	// GPSTracker class
	GPSTracker gps;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps);
        
        this.btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        
        // show location button click event
        this.btnShowLocation.setOnClickListener(new View.OnClickListener() 
        {

			@Override
			public void onClick(View arg0)
			{		
				// create class object
		        GPSTrackingActivity.this.gps = new GPSTracker(GPSTrackingActivity.this);

				// check if GPS enabled		
		        if(GPSTrackingActivity.this.gps.canGetLocation())
		        {
		        	
		        	double latitude = GPSTrackingActivity.this.gps.getLatitude();
		        	double longitude = GPSTrackingActivity.this.gps.getLongitude();
		        	
		        	// \n is for new line
		        	Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();	
		        	//System.out.println("Your Location is - \nLat: " + latitude + "\nLong: " + longitude);
		        }
		        else
		        {
		        	// can't get location
		        	// GPS or Network is not enabled
		        	// Ask user to enable GPS/network in settings
		        	GPSTrackingActivity.this.gps.showSettingsAlert();
		        }
				
			}
		});
    }
    
}