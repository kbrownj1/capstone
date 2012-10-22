package com.example.capstone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaAdapter extends BaseAdapter{
	
	private Activity activity;
	private static LayoutInflater inflater=null;
	private File thumbs[];	
	
	public MediaAdapter (Activity a, File thumbs[]) {
		
		this.activity = a;
		this.thumbs = thumbs;
				
		MediaAdapter.inflater = (LayoutInflater)this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	@Override
	public int getCount() {
		if (this.thumbs == null){
			return 0;
		}
		return this.thumbs.length;
	}

	
	@Override
	public Object getItem(int position) {
		return this.thumbs[position];
	}

	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public static class ViewHolder{
		public TextView sender;
		public ImageView image;
		public String imageLocation;
		
		public String getImageLocation(){
			return this.imageLocation;
		}
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 View vi=convertView;
	     ViewHolder holder;
	     if(convertView==null){
	         vi = inflater.inflate(R.layout.media_item, null);
	         holder=new ViewHolder();
	         holder.sender=(TextView)vi.findViewById(R.id.sender);
	         holder.image=(ImageView)vi.findViewById(R.id.image);
	         vi.setTag(holder);
	     }
	     else
	         holder=(ViewHolder)vi.getTag();
	        
	     Log.i("MMMC", "Adding a image to the screen at position " + position);
	        
	     holder.sender.setText(this.thumbs[position].getName());
	     holder.image.setTag(this.thumbs[position]);
	     holder.imageLocation = this.thumbs[position].getPath();
	     DisplayImage(this.thumbs[position], this.activity, holder.image);
	     return vi;
	 }
	
	/**
	 * @param activity  
	 */
	public static void DisplayImage(File image, Activity activity, ImageView imageView){
		imageView.setImageBitmap(decodeFile(image));
	}
	
	// taken from lazyList
	//decodes image and scales it to reduce memory consumption
    private static Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        	//
        }
        return null;
    }
    // Done taking code from lazyList
}

