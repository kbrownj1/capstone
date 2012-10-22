package com.example.capstone;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;




public class Media extends Activity{


	private static final int PICTURE_TAKEN = 1;
	//private Uri outputFileUri;
	
	//private ImageButton cameraButton;
	
	private OutputStream fOut = null;
	
	//private ContextMenu imageMenu;
	//private PopupWindow viewImage;
	
	private String imagesPath;
	private String imageName;
	private SimpleDateFormat dateFormat;
	
	private File imageFile;
	private File images[];
	private File imageLocation;
	
	//private Toast toast;
	private Toast toast1;
	private Toast toast2;
		
	//private Location location;
	//private LocationManager locationManager;

	
	private ListView list;
	private MediaAdapter adapter;
	
	private int number_of_thumbs;
	
	//private AdapterContextMenuInfo info;
	
	//private int arrayAdapterPosition;
	
	PopupWindow popupImageWindow;
	
	/**
     * Logging tag, ChatActivity.
     */
    private static final String TAG = Chat.class.getSimpleName();
	
	
    /**
     * ID string used as message stanza prefix (app + random).
     */
    private static final String ID =
        TAG + "-" +
        Integer.toHexString((int)(Math.random() * 255.9999));

    /**
     * Stanza unique id.
     */
    private static final AtomicInteger atomicInt = new AtomicInteger();
	
	/**
     * XMLPullParser factory to generate a parser for messages.
     */
    private XmlPullParserFactory xmlPullParserFactory;
    
    /**
     * The remote jid of this chat.
     */
    private String to;

    /**
     * The local account jid of this chat.
     */
    private String from;
    
    private String username;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		 // Collects to and from information from the intent and sets up the necessary variables to send images.
        Intent intent = getIntent();
        String toFrom = intent.getData().getPathSegments().get(0);
        Log.i("MMMC","message to/from: "+ toFrom);
        String splitToFrom[] = toFrom.split("/");
        this.to = splitToFrom[0];
        this.from = splitToFrom[1];
        
        Log.i("MMMC", Media.class.getSimpleName() + " from: " + this.from);
        String splitJId[] = this.from.split("@");
        this.username = splitJId[0];
         
         try {
         	this.xmlPullParserFactory = XmlPullParserFactory.newInstance();
         } catch (XmlPullParserException e) {
         	Log.e("MMM", Media.class.getSimpleName() + " " + "Can't intatiate xmlPullParser");
         	finish();
         	return;
         }
         this.xmlPullParserFactory.setNamespaceAware(true);
         this.xmlPullParserFactory.setValidating(false);
         
         // end image sending initialization code
		
		setContentView(R.layout.media);
		
		//this.cameraButton = (ImageButton)findViewById(R.id.cameraButton);
		
		this.imagesPath = Environment.getExternalStorageDirectory() + "/mmmc/";
		
		
		Log.e("MMMC","images path = " + this.imagesPath);
		
		
		this.imageLocation = new File(this.imagesPath);
		
	
		
		if(!this.imageLocation.exists()){
			this.imageLocation.mkdir();
		}		
		
		//this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		
		// gets list of images from the applications folder
		this.images = this.imageLocation.listFiles();
		
		
		if (this.images == null){
			this.number_of_thumbs = 0;
		} else {
			this.number_of_thumbs = this.images.length;
		}
		Log.i("MMMC", Media.class.getSimpleName()  + " number of thumbnails is " + this.number_of_thumbs); 
				
		

		// loads images and populates the view
		this.list = (ListView)findViewById(R.id.mediaList);
		registerForContextMenu(this.list);
		this.adapter = new MediaAdapter(this, this.images);
		this.list.setAdapter(this.adapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.media_context_menu, menu);
		
		/*
		try {
			info = (AdapterContextMenuInfo) menuInfo;
		}catch (ClassCastException e){
			Log.e("MMMC", Media.class.getSimpleName() + " " + e.getMessage());
			return;
		}
		*/
		
		
	}
	
	@Override
	public boolean onContextItemSelected (MenuItem item){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int listLocation = (int)info.id;
		String selectedImageLocation = this.adapter.getItem(listLocation).toString();
		
		// Decides what context menu item was selected
		if(item.getItemId() == R.id.media_context_menu_view){
			Log.i("MMMC", "Viewing image" + selectedImageLocation);
			
			LayoutInflater inflater = (LayoutInflater) Media.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.full_size_image_view, (ViewGroup)findViewById(R.id.full_size_image_layout));
			this.popupImageWindow = new PopupWindow(layout, 300, 400, true);
			
			Button popupImageButton = (Button)layout.findViewById(R.id.full_image_close_button);
			popupImageButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Media.this.popupImageWindow.dismiss();
				}
			});
			
			ImageView popupImage = (ImageView)layout.findViewById(R.id.full_size_image);
			Bitmap fullImage = BitmapFactory.decodeFile(selectedImageLocation);
			popupImage.setImageBitmap(fullImage);
			
			this.popupImageWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
			
			
			
			
			return true;
		}else if(item.getItemId() == R.id.media_context_menu_send){
			Toast sendImageToast = Toast.makeText(getApplicationContext(), "Sending image", Toast.LENGTH_LONG);
			sendImageToast.show();
			// send the image
			sendImage(selectedImageLocation);
			Log.i("MMMC", Media.class.getSimpleName() + " sending image " + selectedImageLocation + " to group");
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * @param v  
	 */
	public void openCamera(View v){
		
		// Gets the users current location
		//this.location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		
		
		//SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		
		//Log.e("MMMC", "Image name: " + imageName);
		
		//toast = Toast.makeText(getApplicationContext(), imageName, Toast.LENGTH_LONG);
		//toast.show();
		
		
		
		//outputFileUri = Uri.fromFile(imageFile);
		
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, PICTURE_TAKEN);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		Log.e("MMMC", "Result Code " + resultCode );
		
		if (resultCode == -1){
			if (data != null) {
				if(data.hasExtra("data")){
					Log.e("MMMC", "Thumbnail found!");
					Bitmap thumbnail = data.getParcelableExtra("data");
					
					this.imageName = this.dateFormat.format(new Date()) +"-" + this.username + ".jpg";
					this.imageFile = new File(this.imagesPath, this.imageName);
					try {
						this.imageFile.createNewFile();
					} catch (IOException e1) {
						Log.e("MMMC", Media.class.getSimpleName() + " error creating dummy file");
						e1.printStackTrace();
					}
					try {
						this.fOut = new FileOutputStream(this.imageFile);
						thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, this.fOut);
						this.fOut.flush();
						this.fOut.close();


					} catch (FileNotFoundException e) {
						Log.e("MMMC", Media.class.getSimpleName() + " error opening image " + this.imagesPath + " " + this.imageName);
						Log.e("MMMC", e.getMessage());
					} catch (IOException e) {
						Log.e("MMMC", Media.class.getSimpleName() + " error writting image file");
						Log.e("MMMC", e.getMessage());
						e.printStackTrace();
					}
				}
			}
				
				//imageid = data.getData().getLastPathSegment();
				//ContentValues values = new ContentValues(1);
				//values.put(MediaStore.Images.Thumbnails.IMAGE_ID, imageid);
				
				
				
				Log.e("MMMC", "User took picture");
				this.toast1 = Toast.makeText(getApplicationContext(), "Picture taken", Toast.LENGTH_SHORT);
				this.toast1.show();
		} else if (resultCode == Activity.RESULT_CANCELED) {
				Log.e("MMMC", "User Canceled Picture");
				this.toast2 = Toast.makeText(getApplicationContext(), "No picture taken", Toast.LENGTH_SHORT);
				this.toast2.show();
		}
			
		
	}
	
	private void sendImage(String locationOfImageToSend){
	String image_location_array[] = locationOfImageToSend.split("/");
	int image_name_location = image_location_array.length - 1;
		// Picture sending code here.............................
		// create a data packet and send an intent........
    
		DataPacketExtension	dataPacket = new DataPacketExtension("123", 0, image2Base64String(locationOfImageToSend));

    
		StringWriter xml = new StringWriter();
		try {
			XmlSerializer serializer = this.xmlPullParserFactory.newSerializer();
			serializer.setOutput(xml);
			serializer.startTag(null, "message");
			serializer.startTag("http://jabber.org/protocol/ibb", "data");
			serializer.attribute("", "seq", Long.toString(dataPacket.getSeq()));
			serializer.attribute("", "sid", dataPacket.getSessionID());
			serializer.text(dataPacket.getData());
			//serializer.text("AIQAAAAAAAAA");
			serializer.endTag("http://jabber.org/protocol/ibb", "data");
			serializer.startTag(null, "body");
			serializer.text(image_location_array[image_name_location]);
			serializer.endTag(null, "body");
			serializer.endTag(null, "message");
			serializer.flush();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("type", "", "groupchat"));
		attributes.add(new Attribute("to", "", this.to));
		attributes.add(new Attribute(
				"id",
				"",
				ID + "-" + Integer.toHexString(atomicInt.incrementAndGet()))
		);
    
		/*String payload = "<message type='groupchat' to='" + this.to + "' id='" +
    					ID + "-" + Integer.toHexString(atomicInt.incrementAndGet()) + "'> " +
    						"<data xmlns='http://jabber.org/protocol/ibb' seq='0' sid='123'>" +
    						//	dataPacket.getData() +
    						"</data>" +
    					"</message>";*/
    
		Stanza stanza = new Stanza("message", "", this.from, xml.toString(), attributes);

		Intent intent = new Intent();
		intent.setAction("com.googlecode.asmack.intent.XMPP.STANZA.SEND");
		intent.putExtra("stanza", stanza);
		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		getApplicationContext().sendBroadcast(intent, "com.googlecode.asmack.intent.XMPP.STANZA.SEND");

		Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("jabber-chat-db");
		builder.appendPath(this.from);
		builder.appendPath(this.to);
		getApplicationContext()
        	.getContentResolver()
        	.notifyChange(builder.build(), null);
		}

//open the picture file and convert to base64

	public static String image2Base64String(String locationOfImageToSend)
	{
		Log.d("MMMC", Media.class.getSimpleName() + " " + "trying to open file and convert to string...");
	
		String base64String = null;
		InputStream buf = null;
	
		try {
			File f = new File(locationOfImageToSend);
			FileInputStream fis = new FileInputStream(f);
			buf = new BufferedInputStream(fis);
			byte[] imageBytes = new byte[(int) f.length()];
			buf.read(imageBytes);

			base64String = Base64.encodeToString(imageBytes, 3);
		}	
		catch (IOException e) {
			Log.e("MMMC", Media.class.getSimpleName() + " " + e.getMessage());
		}
		finally {
			if (buf != null)
				try {
					buf.close();
				} catch (IOException e) {
					Log.e("MMMC", Media.class.getSimpleName() + " " + e.getMessage());
					
				}
		}
    
		Log.d("MMMC", Media.class.getName() + "trying to open file and convert to string: " + ((base64String == null) ? "" : base64String.substring(5, 20)));
    
		return base64String;
	} 
	
	
	
	
	
/*		
	protected void onPhotoTaken(){
		if(location == null){
			Log.e("MMMC", "Location You are no where to be found");
		}else if(location != null){
			Log.e("MMMC", "Location You are some where");
		}
		
		if(location != null){
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			
			Double latd = new Double(lat);
			Double lngd = new Double(lng);
			  
			
			try {
				
				String path_to_image = imageFile.getCanonicalPath();
				
				ExifInterface exif = new ExifInterface(path_to_image);
				//exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latd.toString());
				//exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lngd.toString());
				exif.setAttribute(ExifInterface.TAG_MODEL, "Erik Burgess");
				Integer timestamp = new Integer(new Date().getDate());
				exif.setAttribute(ExifInterface.TAG_DATETIME, timestamp.toString());
				exif.saveAttributes();
				exif.
				
				// Create a thumbnail of the image
				createThumbnail();
			}catch (IOException e) {
					Log.d("MMMC EXIF", e.getMessage());
			}
				
		}	
		
	} */
	
	@Override
	public void onResume(){
		super.onResume();
		
		this.images = this.imageLocation.listFiles();
		
		// loads images and populates the view
		this.list = (ListView)findViewById(R.id.mediaList);
		registerForContextMenu(this.list);
		this.adapter = new MediaAdapter(this, this.images);
		this.list.setAdapter(this.adapter);
	}
/*
	
	// taken from lazyList
	//decodes image and scales it to reduce memory consumption
    private void createThumbnail(){
    	Log.i("MMMC", Media.class.getSimpleName() + " creating a thumbnail for image " + imageFile.getAbsolutePath());
    	OutputStream out = null;
    	FileInputStream image = null;
    	
    	try {
			image = new FileInputStream(imageFile);
		} catch (FileNotFoundException e1) {
			Log.e("MMMC" , Media.class.getSimpleName() + " " + imageFile.getName() + " was not found in the directory " + imageFile.getPath());
			e1.printStackTrace();
		}
    	
		if (image != null){
       
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            Log.i("MMMC", " loading image file " + imageFile.getAbsolutePath());
            BitmapFactory.decodeStream(image,null,o);
            Log.i("MMMC", "Image" + imageFile.getAbsolutePath() + " loaded succesfully");
            
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
            Bitmap thumbnail = BitmapFactory.decodeStream(image, null, o2);
            
            File thumbnailFile = new File(imagesPath + imageFile.getName());
            
            
            
            
			try {
				out = new FileOutputStream(thumbnailFile);
				out.write(buffer)
			} catch (FileNotFoundException e1) {
				Log.e("MMMC", "1 There was an error saving the image" + thumbnailFile);
				e1.printStackTrace();
			} catch (IOException e) {
				Log.e("MMMC", "There was an error saving the image" + thumbnailFile);
				e.printStackTrace();
			}finally {
				if (out != null){
					try {
						out.close();
					} catch (IOException e) {
						Log.e("MMMC", Media.class.getSimpleName() + " something has gone seriously wrong.");
						e.printStackTrace();
					}
				}
			}
           
            Log.i("MMMC", Media.class.getSimpleName() + " done creating thumbnail for " + imageFile.getAbsolutePath());
		}else {
			Log.e("MMMC", "There was an unknown error opening the image file");
		}
    }
    */
    // Done taking code from lazyList
}
