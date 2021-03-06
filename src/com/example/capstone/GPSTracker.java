package com.example.capstone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener, Runnable {

	final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	volatile Location location; // location
	volatile double latitude; // latitude
	volatile double longitude; // longitude
	
	private String locationProvider;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	public Location getLocation() {
		try {
			this.locationManager = (LocationManager) this.mContext.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			this.isGPSEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			this.isNetworkEnabled = this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			this.locationProvider = null;
			if (!this.isGPSEnabled && !this.isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (this.isNetworkEnabled) {
					this.locationProvider = LocationManager.NETWORK_PROVIDER;
				}
				// if GPS Enabled get lat/long using GPS Services
				if (this.isGPSEnabled) {
					if (this.location == null) {
						this.locationProvider = LocationManager.GPS_PROVIDER;
					}
				}
			}
			if (this.locationProvider != null) {
				((Activity)this.mContext).runOnUiThread(this);
				
				while (this.location == null) {
					Thread.sleep(200);
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.location != null) {
			Log.i("GPSTracker", "getLocation returns lat=" + this.location.getLatitude() + ", lon=" + this.location.getLongitude());
		} else {
			Log.i("GPSTracker", "location is null!!!!!!!, isGPSEnable=" + this.isGPSEnabled + ", isNetworkEnable=" + this.isNetworkEnabled);
		}
		return this.location;
	}
	
	
	@Override
	public void onLocationChanged(Location location1) {
		//
	}

	
	@Override
	public void onProviderDisabled(String provider) {
		//
	}

	
	@Override
	public void onProviderEnabled(String provider) {
		//
	}

	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS(){
		if(this.locationManager != null){
			this.locationManager.removeUpdates(GPSTracker.this);
		}		
	}
	
	/**
	 * Function to get latitude
	 * */
	public double getLatitude(){
		if(this.location != null){
			this.latitude = this.location.getLatitude();
		}
		
		// return latitude
		return this.latitude;
	}
	
	/**
	 * Function to get longitude
	 * */
	public double getLongitude(){
		if(this.location != null){
			this.longitude = this.location.getLongitude();
		}
		
		// return longitude
		return this.longitude;
	}
	
	
	/**
	 * Function to check GPS/wifi enabled
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}
	
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.mContext);
   	 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	GPSTracker.this.mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}

	/*
	 * This method runs in the main ui thread and obtains the lat/long
	 * Side effects:
	 * 		If the location is successfully retrieved, the "location"
	 * 		property is set and "latitude" and "longitude" are likewise set.
	 */
	@Override
	public void run() {
		this.locationManager.requestLocationUpdates(
				this.locationProvider,
				MIN_TIME_BW_UPDATES,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Log.d("GPSTracker", "Network");
		if (this.locationManager != null) {
			this.location = this.locationManager.getLastKnownLocation(this.locationProvider);
			if (this.location != null) {
				this.latitude  = this.location.getLatitude();
				this.longitude = this.location.getLongitude();
			}
		}
		Log.d("GPSTracker", "At end of run method, location=" + this.location);
		
	}

}
