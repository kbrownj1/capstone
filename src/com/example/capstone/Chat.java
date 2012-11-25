/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.example.capstone;

import static android.content.DialogInterface.BUTTON_POSITIVE;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;

import edu.sumb.mygooglemap.MyGoogleMapActivity;

/**
 * Main chat activity.
 */
public class Chat extends Activity implements OnClickListener {

    /**
     * Logging tag, ChatActivity.
     */
    private static final String TAG = Chat.class.getSimpleName();

    /**
     * ID string used as message stanza prefix (app + random).
     */
    static final String ID =
        TAG + "-" +
        Integer.toHexString((int)(Math.random() * 255.9999));

    /**
     * Stanza unique id.
     */
    static final AtomicInteger atomicInt = new AtomicInteger();

    /**
     * The text input field.
     */
    private EditText input;

    /**
     * Full local user jid, if available.
     */
    public String fullJid;
    
    public boolean isDestroyed ;
    

    /**
     * XMLPullParser factory to generate a parser for messages.
     */
    static XmlPullParserFactory xmlPullParserFactory;
    static {
        try {
         	xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            xmlPullParserFactory.setValidating(false);
         } catch (XmlPullParserException e) {
         	Log.e(TAG, "Can't intatiate xmlPullParser");
         }
   	
    }
    
    /**
     * The remote jid of this chat.
     */
    private String to;

    /**
     * The local account jid of this chat.
     */
    String from;

    // Holds latitude and longitude; if not set will be null
    Double mLat;
    Double mLon;

    /**
     * Initialize the members of this activity and bind to the xmpp transport
     * service.
     * @param savedInstanceState The save state of this activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        Intent intent = getIntent();
        String toFrom = intent.getData().getPathSegments().get(0);
        Log.i("MMMC","message to/from: "+ toFrom);
        String splitToFrom[] = toFrom.split("/");
         this.to = splitToFrom[0];
         this.from = splitToFrom[1];
         
        
        //to = splitToFrom[0] + "/" + XMPPUtils.getUser(from);
        
        
        
        setContentView(R.layout.chat);

        TextView fromTextView = (TextView) findViewById(R.id.chat_FromTextView);
        fromTextView.setText(this.from);
        TextView toTextView = (TextView) findViewById(R.id.chat_ToTextView);
        toTextView.setText(this.to);
        Database.getDatabase(getApplicationContext(), null);
        ListView history = (ListView) findViewById(R.id.chat_ChatHistoryList);
        history.setItemsCanFocus(false);
        history.setAdapter(
            new HistoryCursorAdapter(
                this,
                this.from,
                HistoryCursorAdapter.query(this, this.from, this.to),
                true
            )
        );

        //start chat room initialization.  Send a presence stanza to the room when you open it.
        
       
        sendLocation();
		
		
        //start roster get.  Send a IQ stanza to the room when you open it.
        /**/

        ArrayList<Attribute> attributes3 = new ArrayList<Attribute>();
        attributes3.add(new Attribute("type", "", "get"));
//        attributes3.add(new Attribute("to", "", "lowney-emulator@jabber.ferrobyte.com"));
        attributes3.add(new Attribute("to", "", this.getResources().getString(R.string.xmpp_server_url)));
        attributes3.add(new Attribute("from", "", this.from));
        
        attributes3.add(new Attribute(
                "id",
                "",
                ID + "-" + Integer.toHexString(atomicInt.incrementAndGet()))
        ); 


        StringWriter xml3 = new StringWriter();
        try {
        	if (xmlPullParserFactory != null) {
	            XmlSerializer serializer = xmlPullParserFactory.newSerializer();
	            serializer.setOutput(xml3);
	
	            String iqString = "iq";
	            serializer.startTag(null, iqString);
	            for (Attribute attr : attributes3) {
	            	serializer.attribute(null, attr.getName(), attr.getValue());
	            }
	            
	            String queryString = "query xmlns='jabber:iq:roster'";
	            serializer.startTag(null, queryString); 
	            serializer.endTag(null, queryString);
	            
	            serializer.endTag(null, iqString);
	            serializer.flush();
        	}
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        
        Stanza stanza3 =
            new Stanza("iq", "", this.from, xml3.toString(), attributes3);
        
//        Packet myPacket = null;
//        
//        myPacket.setFrom(from);
//        myPacket.setTo("lowney-emulator@jabber.ferrobyte.com");
//        myPacket.setProperty("iq", "query xmlns='jabber:iq:roster'");
        
        Log.i("Chat", "Requesting Roster, xml=" + xml3.toString() + "");
        Intent intent3 = new Intent();
        intent3.setAction("com.googlecode.asmack.intent.XMPP.STANZA.SEND");
        intent3.putExtra("stanza", stanza3);
        intent3.addFlags(Intent.FLAG_FROM_BACKGROUND);
        getApplicationContext().sendBroadcast(intent3, "com.googlecode.asmack.intent.XMPP.STANZA.SEND");
        
        //end of roster.............
        /**/
        
        Button send = (Button) findViewById(R.id.chat_sendMessageButton);
        send.setOnClickListener(this);
        this.input = (EditText) findViewById(R.id.chat_myMessage);
        this.input.setHint("Send " + XMPPUtils.getUser(this.to) + " a message");
        this.input.requestFocus();

    }

    @Override
    public void onDestroy() {
    	this.isDestroyed = true;
    	super.onDestroy();
    }
    /**
     * Clicked on button click, creating a new xmpp stanza and sending it
     * to the remote jid.
     * @param v The view catching the click, ignored.
     */
    
    @Override
	public void onClick(View v) {
        if (this.input == null) {
            Log.d(TAG, "input is null");
            return;
        }
        
        /*mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        GeoLoc test = new GeoLoc();
        test.setLat(lat)  */        
        
        //
        // Get message from the text field and create a message to send to Asmack service
        //
        String msg = this.input.getEditableText().toString();
        sendMessage(this, msg, this.to, this.from);
        
        //
        // Clear out text for the next message
        //
        this.input.setText("");
    }

    GeoLocation geo;
    /**
     * This sends location information to the XMPP server.
     */
    private void sendLocation() {
    	
    	// Put dialog up to indicate it may take some time to 
    	// obtain geo coordinates.
		AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
		builder.setMessage(R.string.wait_for_geo);
		builder.setCancelable(false);
		
		final AlertDialog dialog = builder.create();
		dialog.show();

    	
    	
    	this.geo = new GeoLocation(this, new GeoLocation.GLocationListener() {
    		
    		/*
    		 * (non-Javadoc)
    		 * @see com.example.capstone.GeoLocation.GLocationListener#locationSet(double, double)
    		 */
			@Override
			public void locationSet(double lat, double lon) {
				
				// Send lat/lon in an iq stanza to server
				
				Chat.this.mLat = Double.valueOf(lat);
				Chat.this.mLon = Double.valueOf(lon);
				
				// don't bother dismissing if the activity is already destroyed, as
				// doing so will throw an exception.
				if (!isDestroyed) {
					dialog.dismiss();
				}

//				Chat.this.runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						dialog.dismiss();
//					}
//					
//				});
				
				// Disable the getLocation once gps coordinates are obtained.
				Chat.this.geo.setGLocationListener(null);
				
				if (xmlPullParserFactory == null) {
					return;
				}
				
		        StringWriter xml3 = new StringWriter();
		        try {
		            XmlSerializer serializer = xmlPullParserFactory.newSerializer();
		            serializer.setOutput(xml3);

		            String iqString = "iq";
		            serializer.startTag(null, iqString);
		            serializer.attribute(null, "type", "set");
		            serializer.attribute(null, "from", Chat.this.from);
		            serializer.attribute(null, "id", ID + "-" + Integer.toHexString(atomicInt.incrementAndGet()));
		            
		            serializer.startTag(null, "pubsub");
		            serializer.attribute(null, "xmlns", "http://jabber.org/protocol/pubsub");
		            
		            serializer.startTag(null, "publish");
		            serializer.attribute(null, "node", "http://jabber.org/protocol/geoloc");
		            
		            serializer.startTag(null, "item");
		            
		            serializer.startTag(null, "geoloc");
		            serializer.attribute(null, "xmlns", "http://jabber.org/protocol/geoloc");
		            serializer.attribute(null, "xml:lang", "en");
		            
		            serializer.startTag(null, "lat");
		            serializer.text(""+lat);
		            serializer.endTag(null, "lat");
		            
		            serializer.startTag(null, "lon");
		            serializer.text(""+lon);
		            serializer.endTag(null, "lon");
		            
		            serializer.endTag(null, "geoloc");
		            
		            serializer.endTag(null, "item");
		            
		            serializer.endTag(null, "publish");
		            
		            serializer.endTag(null, "pubsub");
		            
		            serializer.endTag(null, "iq");
		            
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

		        
		        
		        Stanza stanza3 =
		            new Stanza("iq", null, Chat.this.from, xml3.toString(), null);
		        
		        Log.i("Chat", "Send Location, xml=" + xml3.toString() + "");
		        Intent intent3 = new Intent();
		        intent3.setAction("com.googlecode.asmack.intent.XMPP.STANZA.SEND");
		        intent3.putExtra("stanza", stanza3);
		        intent3.addFlags(Intent.FLAG_FROM_BACKGROUND);
		        getApplicationContext().sendBroadcast(intent3, "com.googlecode.asmack.intent.XMPP.STANZA.SEND");
				
			}
		});
    	

    }
    
    /**
     * Send a message to server
     * 
     * @param context
     * @param to
     * @param from
     * @param message
     */
    public static void sendMessage(Context context, String msg, String to, String from ) {
    	if (xmlPullParserFactory == null) {
    		return;
    	}

		// create class object
        GPSTracker gps = new GPSTracker(context);

		// check if GPS enabled		
        if(!gps.canGetLocation())
        {
        	Log.w("Chat", "sendMessage, cannot obtain GPS location");
        	return;
        }
        	
    	double latitude  = gps.getLatitude();
    	double longitude = gps.getLongitude();
        	
    	
    	StringWriter xml = new StringWriter();
        try {
            XmlSerializer serializer = xmlPullParserFactory.newSerializer();
            serializer.setOutput(xml);
            serializer.startTag(null, "message");
            serializer.startTag("http://jabber.org/protocol/muc#user", "x");
            serializer.endTag("http://jabber.org/protocol/muc#user", "x");
            serializer.startTag(null, "body");
            serializer.text(msg);
            
            serializer.endTag(null, "body");

            serializer.startTag(null, "location");
            
        	serializer.startTag(null, "lat");
        	serializer.text("" + latitude);
        	serializer.endTag(null, "lat");
        	
        	serializer.startTag(null, "lon");
        	serializer.text("" + longitude);
        	serializer.endTag(null, "lon");
        	
        	serializer.endTag(null, "location");
           
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
        attributes.add(new Attribute("to", "", to));
        attributes.add(new Attribute(
                "id",
                "",
                ID + "-" + Integer.toHexString(atomicInt.incrementAndGet()))
        );        
        
        Stanza stanza =
            new Stanza("message", "", from, xml.toString(), attributes);
        
        Log.i("Chat", "about send message=" + xml.toString());
        //
        // Code to send message to Asmack service via intent broadcast
        //
        Intent intent = new Intent();
        intent.setAction("com.googlecode.asmack.intent.XMPP.STANZA.SEND");
        intent.putExtra("stanza", stanza);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        context.getApplicationContext().sendBroadcast(intent, "com.googlecode.asmack.intent.XMPP.STANZA.SEND");

        //
        // Save key values in database
        //
        ContentValues values = new ContentValues();
        values.put("ts", System.currentTimeMillis());
        values.put("via", from);
        values.put("jid", to);
        values.put("dst", to);
        values.put("src", from);
        values.put("msg", msg);
        Database.getDatabase(null, null).insert("msg", "_id", values);
        
        //
        // ?? Notifies cursor adapter object of change 
        //
        Builder builder = new Uri.Builder();
        builder.scheme("content");
        builder.authority("jabber-chat-db");
        builder.appendPath(from);
        builder.appendPath(to);
        Uri uri = builder.build();
        Log.d("Chat", "Uri=" + uri.toString());
        context.getContentResolver().notifyChange(builder.build(), null);
        
 
    }
}
