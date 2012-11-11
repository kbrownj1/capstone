package com.example.capstone;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.jivesoftware.smackx.packet.MUCUser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Environment;
import android.util.Log;

import com.buddycloud.jbuddycloud.packet.GeoLoc;

/**
 * The Chat notification listener updates the database, generates notifications
 * and enforces reloads of the UI.
 */
public class ChatNotificationListener implements PacketListener {

    /**
     * The internal database instance.
     */
    private final SQLiteDatabase database;

    /**
     * The system wide notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * The context of this listener.
     */
    private final Context context;

    /**
     * 
     * @param context
     */
    public ChatNotificationListener(Context context) {
        this.context = context;
        this.database = Database.getDatabase(context, null);
        this.notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        
		Log.d("CNL Constructor", "ERIK: RECEIVED BROADCAST.  Context name: " + context);

    }

    /*
     * Handle a single smack packet, discarding anything but Message.
     * @param packet The smack packet.
     * (non-Javadoc)
     * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
     */
    @Override
	public void processPacket(Packet packet) {
    	
    	Log.i("processPacket", packet.toXML());
    	if ( packet instanceof Presence ) {
    		
    		double inLat = 0, inLong = 0;
    		boolean isMUC = false, isGEO = false;
    		
    		Presence presence = (Presence)packet;
    		
    		// In a MUC, the "FROM" is the Resource
    		String presenceFrom = StringUtils.parseResource( presence.getFrom() );
    		String presenceTo = StringUtils.parseName( presence.getTo() );
    		
    		// if presence packet is from yourself, just bail
    		if( presenceFrom.equals(presenceTo) )
    			return;

    		for (PacketExtension extension: presence.getExtensions()) {
    			if ( extension instanceof GeoLoc ) {
    				GeoLoc loc = (GeoLoc) extension;
    				
    				inLat = loc.getLat();
    				inLong = loc.getLon();
    				
    				isGEO = true;
    				
        			Log.d("CNL", "ERIK: GEOLOC EXTENSION FOUND, LAT: " + inLat);

    			}
    			if ( extension instanceof MUCUser ) {
    				//MUCUser muc = (MUCUser) extension;   no need to create this object
    				isMUC = true;
    				
    				Log.d("CNL", "ERIK: MUC EXTENSION FOUND, presence type=" + presence.getType());
    			}
    		}
    		
    		//  If a MUC available presence packet comes in, add/update database
    		if( isMUC == true && presence.getType().toString().equals("available") && isGEO == true ) {
    			
    				updateDatabase(presenceFrom, inLat, inLong);
    			}
    	
    		// if a MUC Unavailable presence packet comes in, remove user from database
    		else if( isMUC == true && presence.getType().toString().equals("unavailable") ){
        		
    			if( this.database.delete("user_info", "name='" + presenceFrom + "'", null) > 0 ) {
        			Log.d("CNL", "ERIK: DATABASE UPDATED, USER " + presenceFrom + " DELETED");
        		}
        		else
        			Log.d("CNL", "ERIK: DATABASE SEARCHED, USER " + presenceFrom + " NOT FOUND");
    		}
        }
    	
    	if ( packet instanceof IQ ) {
    		IQ iq = (IQ)packet;
        	Log.d("CNL", "ERIK: IQ PACKET RECEIVED: " + iq.getExtensions());
        }	
    	
    	if( packet instanceof Message) {
    		Message msg = (Message)packet;
    		String text = msg.getBody();
    		
    		// Extract name
    		String messageFrom = StringUtils.parseResource( msg.getFrom() );

    		
    		// Extract lat and lon from message
    		double inLat = getLat(msg);
    		double inLon = getLon(msg);
    		
    		Log.i("CNL", "recovered name=[" + messageFrom + "], lat/lon=" + inLat + "," + inLon);
    		
    		updateDatabase(messageFrom, inLat, inLon);
    		
    		String bareFrom = XMPPUtils.getBareJid(msg.getFrom());
    		String msgFrom = StringUtils.parseResource(msg.getFrom());
    		String bareTo = XMPPUtils.getBareJid(msg.getTo());
    		String msgTo = StringUtils.parseName( msg.getTo() );
    		
    		if(msg.getType().toString().equals("groupchat")) {
        		Log.d("CNL", "ERIK: MUC MESSAGE PACKET RECEIVED, CONTAINS EXTENSIONS: " + msg.getExtensions());
        		if( msgFrom.equals(msgTo) )
        			return;
        		
        		// Picture receiving code here.............................!!!!!!!!!!
        		
        		for (PacketExtension extension: msg.getExtensions()) {
        			if ( extension instanceof DataPacketExtension ) {
        				DataPacketExtension data = (DataPacketExtension) extension;
        				
        				byte[] imageBytes = data.getDecodedData();
        				String imagePath = Environment.getExternalStorageDirectory() + "/mmmc/";
        				
        				//String imageName = text;
        				File f = new File(imagePath, text);
        				OutputStream out = null;
        				
        				try {
        				     out = new BufferedOutputStream(new FileOutputStream(f));
        				     out.write(imageBytes);
        				}
        		        catch (IOException ioe) {
        	                ioe.printStackTrace();
        		        }
       				    finally {
       				       if (out != null) {
       				         try { out.close(); } catch (IOException e) { e.printStackTrace(); }
       				       }
       				    }

       	       			//Log.d("CNL", "ERIK: MUC EXTENSION FOUND");
       	               	ContentValues values = new ContentValues();
       	              	values.put("ts", System.currentTimeMillis());
       	               	values.put("jid", bareFrom);
       	                		
       	               	//don't put who it's from, put the resource it came from (user in a MUC)
       	               	values.put("src", msgFrom);
       	                		
       	                values.put("dst", msg.getTo());
       	                values.put("via", bareTo);
       	               	values.put("msg", "Picture received: " + text.trim());
       	               	this.database.insert("msg", "_id", values);
       				    
       				    
            			Log.d("CNL", "ERIK: DATA EXTENSION FOUND, IMAGE SAVED");
            			return;
        			}
        		}
        				
        	   	if (text == null || text.trim().length() == 0) {
        	  		Log.d("CNL", "ERIK: MESSAGE PACKET LACKS A MESSAGE!!!");
        	  		return;
        	   	}	
        			
       			//Log.d("CNL", "ERIK: MUC EXTENSION FOUND");
               	ContentValues values = new ContentValues();
              	values.put("ts", System.currentTimeMillis());
               	values.put("jid", bareFrom);
                		
               	//don't put who it's from, put the resource it came from (user in a MUC)
               	values.put("src", msgFrom);
                		
                values.put("dst", msg.getTo());
                values.put("via", bareTo);
               	values.put("msg", text.trim());
               	this.database.insert("msg", "_id", values);
        	}
    		else
    			Log.d("CNL", "ERIK: NON-MUC MESSAGE PACKET RECEIVED: " + bareFrom);
    		
    		Builder builder = new Uri.Builder();
    		builder.scheme("content");
    		builder.authority("jabber-chat-db");
    		builder.appendPath(bareTo);
    		builder.appendPath(bareFrom);
    		this.context.getContentResolver().notifyChange(builder.build(), null);
    		setNotification(bareFrom, bareTo);
    	}
    }

    /**
     * Create a new notification for a given local/remote user pair.
     * @param from The local account jid.
     * @param to The remote account jid.
     */
    private final void setNotification(String from, String to) {
        String tag = from + "/" + to;
        Intent intent = new Intent("android.intent.action.SENDTO");
        Uri uri = Uri.parse("imto://jabber/" + URLEncoder.encode(tag));
        intent.setData(uri);
        Notification notify = new Notification(
            R.drawable.icon,
            "New chat",
            System.currentTimeMillis()
        );
        notify.setLatestEventInfo(
            this.context,
            "New chat message",
            "from " + from,
            PendingIntent.getActivity(this.context, 0, intent, 0)
        );
        this.notificationManager.notify(tag, 1, notify);
    }
    
    /**
     * Extract latitude from Message packet
     * @param msg
     * @return
     */
    private static double getLat(Message msg) {
    	String xmlString = msg.toXML();
    	
    	String latElement = "<lat>";
    	int iPos = xmlString.indexOf(latElement);
    	if (iPos >= 0) {
    		int iPosEnd = xmlString.indexOf("</lat>");
    		String latString = xmlString.substring(iPos + latElement.length(), iPosEnd);
    		return Double.parseDouble(latString);
    	}
    	return 0;
    }
    
    /**
     * Extract longitude from Message packet
     * @param msg
     * @return
     */
    private static double getLon(Message msg) {
    	String xmlString = msg.toXML();
    	
    	String lonElement = "<lon>";
    	int iPos = xmlString.indexOf(lonElement);
    	if (iPos >= 0) {
    		int iPosEnd = xmlString.indexOf("</lon>");
    		String lonString = xmlString.substring(iPos + lonElement.length(), iPosEnd);
    		return Double.parseDouble(lonString);
    	}
    	return 0;
    }
    
    /**
     * Insert new record or update the database 
     * @param name
     * @param lat
     * @param lon
     */
    private void updateDatabase(String name, double lat, double lon) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("lat", lat);
		values.put("long", lon);
		
		if (this.database.update("user_info", values, "name='" + name + "'", null) < 1 ) {
			this.database.insert("user_info", "_id", values);
			Log.d("CNL", "ERIK: DATABASE UPDATED, USER " + name + " ADDED");
		}
		else {
			Log.d("CNL", "ERIK: DATABASE UPDATED, USER " + name + " UPDATED");
		}

    }
}
