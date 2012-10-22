package edu.sumb.mygooglemap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.capstone.Constants;
import com.example.capstone.Database;
import com.example.capstone.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

/**
 * 
 *
 *
 */
public class MyGoogleMapActivity extends MapActivity {
	static final String TAG = MyGoogleMapActivity.class.getSimpleName();
	
	/**
	 * Minimum time (in ms) between checks for changes to location
	 */
	private static long UPDATE_INTERVAL = 60 * 1000;	// one minute

	MapController mapController;
	MapView mapView;
	private LocationManager locationManager;
	private MyOverlays itemizedoverlay;
	MyLocationOverlay myLocationOverlay;
	private Button emergencyButton;

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.mapactivity); // bind the layout to the activity

		// Configure the Map
		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setSatellite(true);
		this.mapController = this.mapView.getController();
		this.mapController.setZoom(14); // Zoon 1 is world view
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL,
				0, new GeoUpdateHandler());

		this.myLocationOverlay = new MyLocationOverlay(this, this.mapView);
		this.mapView.getOverlays().add(this.myLocationOverlay);

		this.myLocationOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				MyGoogleMapActivity.this.mapView.getController().animateTo(
						MyGoogleMapActivity.this.myLocationOverlay.getMyLocation());
			}
		});
		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		this.itemizedoverlay = new MyOverlays(this, drawable);
		createMarker();
		
		// Handle emergency button
		this.emergencyButton = (Button)this.findViewById(R.id.emergency_button);
		this.emergencyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("MyGoogleMapActivity", "Tapped on EMERGENCY");
				
				Builder builder = new AlertDialog.Builder(MyGoogleMapActivity.this);
				builder.setMessage("Do you want to send EMERGENCY status to everyone?");
				builder.setCancelable(true);
				builder.setPositiveButton("YES, DO IT NOW!", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					
				});
				builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					
				});
				AlertDialog dialog = builder.create();
				dialog.show();

				// At this point, we need to update the user_info for the user and
				// send this to the XMPP server.
			}
			
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
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
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			MyGoogleMapActivity.this.mapController.animateTo(point); // mapController.setCenter(point);
			createMarker();
		
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

	/*
	 * 
	 */
	void createMarker() {
		// Start with fresh overlay, since this method adds all points on the map.
		this.itemizedoverlay.clearOverlays();

		GeoPoint p = this.mapView.getMapCenter();
		
		/*itemizedoverlay.clearOverlays();
		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint(p.getLatitudeE6()-10000, p.getLongitudeE6()-10000), "", ""));
		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint(p.getLatitudeE6()-10000, p.getLongitudeE6()+10000), "", ""));
		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint(p.getLatitudeE6()+10000, p.getLongitudeE6()+10000), "", ""));
		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint(p.getLatitudeE6()+10000, p.getLongitudeE6()-10000), "", ""));
		if (itemizedoverlay.size() > 0) {
			mapView.getOverlays().add(itemizedoverlay);
		}
		*/
		
		OverlayItem overlayitem = new OverlayItem(p, "Me", "");
		addOtherUsersToMap();
		
		this.itemizedoverlay.addOverlay(overlayitem);
		if (this.itemizedoverlay.size() > 0) {
			this.mapView.getOverlays().add(this.itemizedoverlay);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (this.myLocationOverlay != null) {
			this.myLocationOverlay.enableMyLocation();
			this.myLocationOverlay.enableCompass();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onResume();
		this.myLocationOverlay.disableMyLocation();
		this.myLocationOverlay.disableCompass();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.emergencyButton != null) {
			this.emergencyButton.setOnClickListener(null);
		}
		
	}
	
	/**
	 * Add members listed in the user_info database table to the map.
	 */
	private void addOtherUsersToMap() {
		SQLiteDatabase database = Database.getDatabase(this, null);
		// Columns to query from the user_info table
		String columns[] = {"name", "lat", "long", "in_trouble"};

		//run the query
		Cursor resultsCursor = database.query("user_info", columns, null, null, null, null, null);
		
		// Parse the results and create a group member for each entry
		Log.i(TAG, ": number of members in the group " + resultsCursor.getCount());
		
		if (resultsCursor.moveToFirst()){
			do {
				String name = resultsCursor.getString(0);
				Double lat = resultsCursor.getDouble(1);
				Double lng = resultsCursor.getDouble(2);
				boolean isEmergency = resultsCursor.getInt(3) != 0;
				Log.i(TAG, " adding [" + name + "] to map at location " + lat + " " + lng);
				
				addPointToMap(lat, lng, name, isEmergency);
				
			} while (resultsCursor.moveToNext());
		} else {
			Log.i(TAG, " there are no members present in the group");
		}
		resultsCursor.close();

	}
	
	/**
	 * Add a single point on the map, given lat/lon and a name to display
	 * @param lat		Latitude
	 * @param lon		Longitude
	 * @param title		Name to display next to map pin icon.
	 */
	private void addPointToMap(double lat, double lon, String title, boolean isEmergency) {
		String emergency = isEmergency ? Constants.EMERGENCY : "";
		this.itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint((int)(lat * 1000000), (int)(lon * 1000000)), title, emergency));
	}
}