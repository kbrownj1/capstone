
package edu.sumb.mygooglemap;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.example.capstone.Constants;
import com.example.capstone.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * 
 * 
 *
 */
public class MyOverlays extends ItemizedOverlay<OverlayItem> {

	private static int maxNum = 5;
	private OverlayItem overlays[] = new OverlayItem[maxNum];
	private int index = 0;
	private boolean full = false;
	Context context;
	//private OverlayItem previousoverlay;

	/**
	 * 
	 * @param context
	 * @param defaultMarker
	 */
	public MyOverlays(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return this.overlays[i];
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
		if (this.full) {
			return this.overlays.length;
		}
		return this.index;

	}

	/**
	 * 
	 * @param overlay  item to add as an overlay.  
	 * 					If value is null, simply redrwas the map
	 * 					 without adding any new overlay.
	 */
	public void addOverlay(OverlayItem overlay) {
		if (overlay != null) {
			if (this.index < maxNum) {
				//
			} else {
				this.index = 0;
				this.full = true;
			}
			this.overlays[this.index++] = overlay;
		}
		populate();


		/*
		if (previousoverlay != null) {
			if (index < maxNum) {
				overlays[index] = previousoverlay;
			} else {
				index = 0;
				full = true;
				overlays[index] = previousoverlay;
			}
			index++;
			populate();
		}
		this.previousoverlay = overlay;
		*/
	}
	
	/**
	 * 
	 */
	public void clearOverlays() {
	    for (int i = 0; i < maxNum; i++) {
	    	this.overlays[i] = null;
	    }
	    this.index = 0;
	    this.full = false;
	}
	
	/*
	 * Try drawing our own map view marker
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
	 */

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

//		if (true) {
//			super.draw(canvas, mapView, shadow);
//			return;
//		}
		
		Drawable marker1 = this.context.getResources().getDrawable(R.drawable.ic_launcher);
		Drawable marker2 = this.context.getResources().getDrawable(R.drawable.red_pin2);
		Bitmap bmp_normal    = ((BitmapDrawable) marker1).getBitmap();
		Bitmap bmp_emergency = ((BitmapDrawable) marker2).getBitmap();
		
		Paint textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(16f);
		textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 1));
		textPaint.setStyle(Paint.Style.FILL);
		
		Paint markerPaint = new Paint();

		Point screenPts = new Point();

		float left;
		float top;
		
		GeoPoint gp;

		int total = this.size();
		for (int index1 = 0; index1 < total; index1++) {

			gp = this.overlays[index1].getPoint();
			mapView.getProjection().toPixels(gp, screenPts);

			// Determine which image to use depending on emergency flag
			Bitmap bmp = Constants.EMERGENCY.equals(this.overlays[index1].getSnippet()) ? bmp_emergency : bmp_normal;
			
			
			float bmpWidth = bmp.getWidth();
			float bmpHeight = bmp.getHeight();

			left = screenPts.x - (bmpWidth / 2);
			top = screenPts.y - bmpHeight;

			// draw icon
			canvas.drawBitmap(bmp, screenPts.x-(bmp.getWidth()/2), screenPts.y-(bmp.getHeight()), markerPaint);

			// draw text
			String title = this.overlays[index1].getTitle();
			canvas.drawText(title, left + 8, top + 30, textPaint);
			

	    }

	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	@Override
	protected boolean onTap(int index1) {
		OverlayItem overlayItem = this.overlays[index1];
		Builder builder = new AlertDialog.Builder(this.context);
		builder.setMessage("Do you want to contact " + overlayItem.getTitle() + "?");
		builder.setCancelable(true);
		builder.setPositiveButton("I agree", new OkOnClickListener(overlayItem));
		builder.setNegativeButton("No, no", new CancelOnClickListener(overlayItem));
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	}

	/**
	 * 
	 * 
	 *
	 */
	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		private final OverlayItem item;
		
		/**
		 * 
		 * @param item The tapped overlay item that contains name and lat/lon
		 */
		CancelOnClickListener(OverlayItem item) {
			// By defining default scope constructor, allows classes defined within
			// the outer class to access onClick, yet keep the class private so no
			// other class can access.
			this.item = item;
		}

		/*
		 * (non-Javadoc)
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(MyOverlays.this.context, "You did not agree to connect to" + this.item.getTitle(), Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * 
	 * 
	 *
	 */
	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		private final OverlayItem item;
		
		/**
		 * 
		 * @param item The tapped overlay item that contains name and lat/lon
		 */
		OkOnClickListener(OverlayItem item) {
			// By defining default scope constructor, allows classes defined within
			// the outer class to access onClick, yet keep the class private so no
			// other class can access.
			this.item = item;
		}

		/*
		 * (non-Javadoc)
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(MyOverlays.this.context, "You agreed to connect to " + this.item.getTitle(), Toast.LENGTH_LONG).show();
		}
	}
}

