package com.example.capstone;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Returns the lat/lon in a listener using the Android GPS.
 * @author Ken
 *
 */
public class GeoLocation {
	private static final String TAG = "GeoLocation";
	
	private LocationManager locationManager;
	
	GLocationListener l;
	
	/**
	 * Minimum time (in ms) between checks for changes to location
	 */
	private static long UPDATE_INTERVAL = 60 * 1000;	// one minute

	/**
	 * Implement this interface for receiving a call when lat/lon is avaialble.
	 * @author Ken
	 *
	 */
	interface GLocationListener {
		/**
		 * Returns the lat and lon when it becomes available from the GPS.
		 * @param lat
		 * @param lon
		 */
		void locationSet(double lat, double lon);
	}

	/**
	 * Provides lat lon asynchronously via the provided listener
	 * @param context	The activity context in which this is called.
	 * @param listener	The GLocationListener to be called when lat/lon is available.
	 */
	public GeoLocation(Context context, GLocationListener listener) {
		this.l = listener;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL,
				0, new GeoUpdateHandler());

	}
	
	public void setGLocationListener( GLocationListener listener ) {
		this.l = listener;
	}
		
	/**
	 * Class contains a listener which is called periodically to
	 * update the location
	 *
	 */
	public class GeoUpdateHandler implements LocationListener {

		/*
		 * Each time a location is obtained, update the marker on the map.
		 * 
		 * (non-Javadoc)
		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
		 */
		@Override
		public void onLocationChanged(Location location) {
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			if (GeoLocation.this.l != null) {
				GeoLocation.this.l.locationSet(lat, lon);
			}
		}

		
		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "Location provider disabled");
		}

		
		@Override
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "Location provider enabled");
		}

		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "Location provider " + provider + " status changed to " + status);
		}
	}

}
