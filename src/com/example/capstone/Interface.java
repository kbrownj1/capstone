package com.example.capstone;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.connection.IXmppTransportService;

import edu.sumb.mygooglemap.MyGoogleMapActivity;

public class Interface extends TabActivity implements Runnable {
	/**
     * Logging tag, ChatActivity.
     */
    private static final String TAG = "MMMC - "  + Chat.class.getSimpleName();
	
	/**
	 * Application specific context
	 */
	private Context context;
	
	/**
	 * Used to get the username of the logged in account, AccountManager
	 */
	private AccountManager accountManager;
	
    /**
     * XMLPullParser factory to generate a parser for messages.
     */
    private XmlPullParserFactory xmlPullParserFactory;
    
    /**
     * The system wide notification manager.
     */
    private NotificationManager notificationManager;
    
    /**
     * The remote jid of this chat.
     */
    private String to;

    /**
     * The local account jid of this chat.
     */
    private String from;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.e(TAG, "Get context");
        this.context = getApplicationContext();

        //TODO this is simulating the BOOT_COMPLETED broadcast, which never seems to
        // be received by this application.  It will start the IChatService.
        startIChatService();
        
        
        // Gets account information about the currently logged in user
		this.accountManager = AccountManager.get(this.context);
		Account accounts[] = this.accountManager.getAccountsByType("com.googlecode.asmack");
		
		Log.e(TAG, "The number of acounts is: " + accounts.length);
		
		// Assumes only 1 XMPP com.google.asmack account and will only use the first on in the list
		this.from = (accounts.length > 0) ? accounts[0].name : "Nobody";
		Log.i(TAG, "Logged in as" + this.from);
        
		// The name of the chat room
		//this.to = "123@conference.jabber.ferrobyte.com";
		//this.to = "test-miao@conference.peggy-pc";
		this.to = this.getResources().getString(R.string.xmpp_server_url);
		
		// used to send to and from information in the intents
		String toFrom = this.to + "/" + this.from;
		
        
        this.notificationManager = (NotificationManager)
        getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

        try {
        	this.xmlPullParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
        	Log.e(TAG, "Can't intatiate xmlPullParser");
        	finish();
        	return;
        }
        this.xmlPullParserFactory.setNamespaceAware(true);
        this.xmlPullParserFactory.setValidating(false);

        Intent serviceIntent =
        	new Intent(IXmppTransportService.class.getCanonicalName());
        startService(serviceIntent);
        
        StringWriter xml2 = new StringWriter();
        try {
            XmlSerializer serializer = this.xmlPullParserFactory.newSerializer();
            serializer.setOutput(xml2);
            serializer.startTag(null, "presence");
            serializer.startTag("xmlns='http://jabber.org/protocol/muc'", "x");
            
            
            serializer.endTag("xmlns='http://jabber.org/protocol/muc'", "x");
            serializer.endTag(null, "presence");
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

        ArrayList<Attribute> attributes2 = new ArrayList<Attribute>();
        attributes2.add(new Attribute("to", "", this.to + "/" + XMPPUtils.getUser(this.from)));
        
        
        Stanza stanza2 =
            new Stanza("presence", "", this.from, xml2.toString(), attributes2);
        Intent intent2 = new Intent();
        intent2.setAction("com.googlecode.asmack.intent.XMPP.STANZA.SEND");
        intent2.putExtra("stanza", stanza2);
        intent2.addFlags(Intent.FLAG_FROM_BACKGROUND);
        getApplicationContext().sendBroadcast(intent2, "com.googlecode.asmack.intent.XMPP.STANZA.SEND");
        
        //end of chat room initialization.............

        
        
        
        
        setContentView(R.layout.main);
        
        Resources res = getResources(); // Resources object get Drawable
        final TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        
                
        
        // Initializes the tabbed interface 
        
        //Members Tab
        intent = new Intent().setClass(this, Group.class);
        spec = tabHost.newTabSpec("members").setIndicator("Members", res.getDrawable(R.drawable.group_icon)).setContent(intent);
        tabHost.addTab(spec);
        
        // Encodes the to and from values to be used by the chat application. 
		Uri uri = Uri.parse("imto://jabber/" + URLEncoder.encode(toFrom));
		
		
       //Chat Tab 
        intent = new Intent(this, Chat.class);
        intent.setData(uri);
        spec = tabHost.newTabSpec("chat").setIndicator("Chat", res.getDrawable(R.drawable.chat_icon)).setContent(intent);
        tabHost.addTab(spec);
        
        // Encodes the to and from values to be used by the chat application. 
		Uri uri1 = Uri.parse("imto://jabber/" + URLEncoder.encode(toFrom));
        
		
		//Media Tab
        intent = new Intent().setClass(this, Media.class);
        intent.setData(uri1);
        spec = tabHost.newTabSpec("media").setIndicator("Media", res.getDrawable(R.drawable.media_icon)).setContent(intent);
        tabHost.addTab(spec);
    
        
        // Map tab 
        intent = new Intent().setClass(this, MyGoogleMapActivity.class);
        intent.setData(uri);
        spec = tabHost.newTabSpec("map").setIndicator("Map", res.getDrawable(R.drawable.map_icon)).setContent(intent);
        tabHost.addTab(spec);
        
        //GPS Tab 
//        intent = new Intent().setClass(this, GPSTrackingActivity.class);
//        spec = tabHost.newTabSpec("gps").setIndicator("GPS", res.getDrawable(R.drawable.gps_icon)).setContent(intent);
//        tabHost.addTab(spec);

        
        tabHost.setOnTabChangedListener(new OnTabChangeListener()
        {
	        @Override
			public void onTabChanged(String tabId)
            {
	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(tabHost.getApplicationWindowToken(), 0);
            }
        });
        
        tabHost.setCurrentTab(1);
        
        this.notificationManager.cancel(toFrom, 1);
        
        // Start thread to ping server with message periodically as alternative to presence.
        new Thread(this, "pingThread").start();

    }
    
    @Override
	public void onResume(){
		super.onResume();
	}
    
    /**
     * Code copied from BootCompletedReceiver -- to initialize the IChatService.
     */
    private void startIChatService() {
        Intent transportService = new Intent();
        transportService.setAction(IChatService.class.getCanonicalName());
        this.startService(transportService);
        Log.i("Interface", "Started IChatservice");
    }

    /*
     * This is run in its own thread and "pings" the server with a presence message 
     * at a fixed interval.
     * It also examines the database and updates any row with in_trouble set beyond a
     * given number of seconds since the emergency was issued.
     * 
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	@Override
	public void run() {
		String pingPeriodSeconds = this.getString(R.string.ping_period_seconds);
		long time = Long.valueOf(pingPeriodSeconds).longValue() * 1000;

//		String msg = this.getString(R.string.ping_message);
		
		while (true) {

//			Chat.sendMessage(this, msg, this.to, this.from);
	        
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// Assume interruption means end of app.
				return;
			}
			
		}
	}
}	