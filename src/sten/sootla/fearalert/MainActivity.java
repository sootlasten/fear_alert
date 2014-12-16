package sten.sootla.fearalert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sten.sootla.fearalert.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;

public class MainActivity extends ActionBarActivity implements
		OnLongClickListener, OnTouchListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, 
		LocationListener {
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int UPDATE_INTERVAL = 5000;
	private final static int FASTEST_INTERVAL = 1000;
	
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	private TextView obtainingLocationText; 
	private ProgressBar obtainingLocationProgressBar; 
	
	boolean sendLocation; 
	boolean shownLocationSuccessToast;
	
	private ImageView phoneImg;
	private TextView textUp, textDown;
	private Animation rotatingAnimation;
	private Toast cancelledToast;
	private boolean fingerPressing, alert;
	private ArrayList<Contact> smsContacts;
	private Contact callContact;
	private HashMap<String, SmsStatus> smsContactStatus;
	private SmsStatusAdapter smsStatusAdapter;
	private boolean listenForSmsStatus;
	
	private Rect r;
	
	private final String SENT = "SMS_SENT";
	private final String DELIVERED = "SMS_DELIVERED";
	private PendingIntent sentPI, deliveredPI;
	private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;
	private ListView lv;

	private final String startAlert = "PRESS TO <font color='green'>START</font>";
	private final String releaseToAlert = "RELEASE TO <font color='green'>ALERT</font>";
	private final String cancelAlert = "RELEASE TO <font color='red'>CANCEL</font>";
	private final String dragToCancel = "DRAG FINGER <font color='red'>OFF </font>TO CANCEL";
	private final String noContacts = "<font color='red'>NO </font>CONTACTS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		listenForSmsStatus = false;
		
		sentPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SENT), 0);
		
		deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);
		
		setUpActionBar();
		
		phoneImg = (ImageView) findViewById(R.id.phone);
		textUp = (TextView) findViewById(R.id.text_up);
		textDown = (TextView) findViewById(R.id.text_down);
		
		obtainingLocationText = (TextView) findViewById(R.id.progress_text);
		obtainingLocationProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		
		// Load font
		Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/leaguegothic.ttf");
		textUp.setTypeface(typeface);
		textDown.setTypeface(typeface);
		
		Typeface leaguegothicRegular = Typeface.createFromAsset(getAssets(), "fonts/leaguegothic_regular.ttf");
		obtainingLocationText.setTypeface(leaguegothicRegular);
		
		rotatingAnimation = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5592f);
		
		r = new Rect();
		
		cancelledToast = Toast.makeText(this, "Canceled.", Toast.LENGTH_SHORT);
		
		phoneImg.setOnLongClickListener(this); 
		phoneImg.setOnTouchListener(this);
		
		// make instances of LocationRequest and LocationClient
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		mLocationClient = new LocationClient(this, this, this);
		
		sendLocation = false;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		getContacts();
		if (smsContacts.size() == 0 && callContact == null) {
			textUp.setText(Html.fromHtml(noContacts), TextView.BufferType.SPANNABLE);
		}
		else {
			textUp.setText(Html.fromHtml(startAlert), TextView.BufferType.SPANNABLE);
			if (sendLocation) {
				shownLocationSuccessToast = false;
				if (servicesConnected()) {
					mLocationClient.connect();
				} else {
					sendLocation = false;
					String msg = "Google Play Services is not available. Messages will be sent without your location.";
					Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	@Override
	public void onStop() {
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		mLocationClient.disconnect();
		super.onStop();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		//---create the BroadcastReceiver when the SMS is sent---
		if (listenForSmsStatus) {
			return; 
		}
		
		smsSentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				String name = intent.getExtras().getString("contactName");
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					updateStatusDialog(name, SmsStatus.SENT);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_NO_SERVICE:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					updateStatusDialog(name, SmsStatus.FAILED_TO_SEND);
					break;
			    }
			}
		};
		
		//---create the BroadcastReceiver when the SMS is delivered---
		smsDeliveredReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				String name = intent.getExtras().getString("contactName");
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					updateStatusDialog(name, SmsStatus.DELIVERED);
					break;				
				case Activity.RESULT_CANCELED:
					updateStatusDialog(name, SmsStatus.FAILED_TO_DELIVER);
					break;
				}
			}
		};
		
		//---register the two BroadcastReceivers---
		registerReceiver(smsSentReceiver, new IntentFilter(SENT));
		registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		rotatingAnimation.cancel();
		
		if (listenForSmsStatus) {
			return;
		}
		
		//--unregister the two BroadcastReceivers--
		unregisterReceiver(smsSentReceiver);
		unregisterReceiver(smsDeliveredReceiver);
	}
	
	@Override
	public void onConnected(Bundle dataBundle) {
		obtainingLocationText.setVisibility(View.VISIBLE);
		obtainingLocationProgressBar.setVisibility(View.VISIBLE);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}
	
	@Override
	public void onDisconnected() {
		obtainingLocationText.setVisibility(View.GONE);
		obtainingLocationProgressBar.setVisibility(View.GONE);
		
		String errorMessage = "Diconnected from Google Play Services. Messages will be sent without your location.";
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		obtainingLocationText.setVisibility(View.GONE);
		obtainingLocationProgressBar.setVisibility(View.GONE);
		
		String errorMessage = "Failed to connect to Google Play Services. Messages will be sent without your location.";
		Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();	
	}
	
	@Override
	public void onLocationChanged(Location location) {
		obtainingLocationText.setVisibility(View.GONE);
		obtainingLocationProgressBar.setVisibility(View.GONE);
		
		if (!shownLocationSuccessToast) {
			Toast.makeText(this, "Successfully listening to your location.", Toast.LENGTH_SHORT).show();
			shownLocationSuccessToast = true;
		}
	}
	
	
	@Override
	public boolean onLongClick(View v) {
		if (smsContacts.size() == 0 && callContact == null) {
			showNoContactsDialog();
		} else {
			cancelledToast.cancel();
			
			rotatingAnimation.setRepeatCount(Animation.INFINITE);
			rotatingAnimation.setDuration(1500);
			phoneImg.startAnimation(rotatingAnimation);
			
			textUp.setText(Html.fromHtml(releaseToAlert), TextView.BufferType.SPANNABLE);
			textDown.setText(Html.fromHtml(dragToCancel), TextView.BufferType.SPANNABLE);
			textDown.setVisibility(View.VISIBLE);
			
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			
			fingerPressing = true;
			alert = true;
		}
		return true;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		v.getHitRect(r);
		final float x = event.getX() + r.left;
		final float y = event.getY() + r.top;
		
		if (event.getAction() == MotionEvent.ACTION_UP && fingerPressing) {
			stopAnimationAndSetUpperText(startAlert);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			
			fingerPressing = false;
			if (!alert) {
				cancelledToast.show();
			} else {
				if (callContact != null) {
					callContact();
					listenForSmsStatus = true;
				} 
				if (smsContacts.size() != 0) {
					if (sendLocation && !shownLocationSuccessToast) {
						String msg = "Location is not yet obtained. Messages will be sent without location.";
						Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
					}
					sendSMSTexts();
					showSMSStatusDialog();
				}
			}
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE && fingerPressing) {
			if (!r.contains((int) x, (int) y)) {
				stopAnimationAndSetUpperText(cancelAlert);
				alert = false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_people) {
			Intent intent = new Intent(this, ContactsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.
				isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {
			return false;
		}
	}

	private void updateStatusDialog(String name, SmsStatus status) {
		smsContactStatus.put(name,  status); 
		smsStatusAdapter = new SmsStatusAdapter(MainActivity.this,
				android.R.layout.simple_list_item_1, new ArrayList<String>(smsContactStatus.keySet()),
				smsContactStatus);
		lv.setAdapter(smsStatusAdapter);
	}
	
	private void stopAnimationAndSetUpperText(String text) {
		rotatingAnimation.setRepeatCount(0);
		textUp.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
		textDown.setVisibility(View.INVISIBLE);
	}
	
	private void sendSMSTexts() {
		StringBuffer locationMsg = new StringBuffer();
		
		if (shownLocationSuccessToast) {
			Location location = mLocationClient.getLastLocation();
			locationMsg.append(" @ http://maps.google.com/?q=");
			locationMsg.append(location.getLatitude());
			locationMsg.append(",");
			locationMsg.append(location.getLongitude());
		}
		
		smsContactStatus = new HashMap<String, SmsStatus>();
		for (Contact contact : smsContacts) {
			if (contact.isSendLocation()) {
				sendSMS(contact.getName(), contact.getFormatedNumber(), contact.getSMSContent() + locationMsg.toString());
			} else {
				sendSMS(contact.getName(), contact.getFormatedNumber(), contact.getSMSContent());
			}
			smsContactStatus.put(contact.getName(), SmsStatus.SENDING);
		}
	}
	
	private void sendSMS(String name, String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		
		Intent sentIntent = new Intent(SENT);
		sentIntent.putExtra("contactName", name);
		sentPI = PendingIntent.getBroadcast(this, Integer.parseInt(phoneNumber), sentIntent, PendingIntent.FLAG_ONE_SHOT);
		
		Intent deliveredIntent = new Intent(DELIVERED);
		deliveredIntent.putExtra("contactName", name);
		deliveredPI = PendingIntent.getBroadcast(this, Integer.parseInt(phoneNumber), 
				deliveredIntent, PendingIntent.FLAG_ONE_SHOT);

		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	
	private void callContact() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNo = "tel:" + callContact.getFormatedNumber();
		callIntent.setData(Uri.parse(phoneNo));
		startActivity(callIntent);
	}
	
	private void setUpActionBar() {
		String title = "<font color='red'>Fear</font><font color='green'> Alert</font>";
		Spanned titleReal = Html.fromHtml(title);
		
		SpannableString s = new SpannableString(titleReal);
		s.setSpan(new TypefaceSpan(this, "flexdisplay.ttf"), 0, s.length(), 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(s);
	}
	
	private void getContacts() {
		smsContacts = new ArrayList<Contact>();
		callContact = null;
		sendLocation = false;
		HashMap<String, String> map = new HashMap<String, String>();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			
		Map<String, ?> contacts = sharedPref.getAll();
		for (Map.Entry<String, ?> entry : contacts.entrySet()) {
			String json = sharedPref.getString(entry.getKey(), null);
			if (json != null) {
				map.put(entry.getKey(), json);
				Contact contact = new Gson().fromJson(json, Contact.class);
				if (contact.isCall()) {
					callContact = contact;
				} 
				if (contact.isSMS()) {
					smsContacts.add(contact);
					
					if (contact.isSendLocation()) {
						sendLocation = true;
					}
				}
			}
		}
	}
	
	private void showSMSStatusDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		View convertView = (View) inflater.inflate(R.layout.sms_list, null);
		alertDialog.setView(convertView);
		alertDialog.setTitle("SMS status");
		lv = (ListView) convertView.findViewById(R.id.sms_list_view);
		smsStatusAdapter = new SmsStatusAdapter(MainActivity.this, 
				android.R.layout.simple_list_item_1, new ArrayList<String>(smsContactStatus.keySet()), 
				smsContactStatus);
		lv.setAdapter(smsStatusAdapter);
		alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listenForSmsStatus = false;
			}
		});
		alertDialog.show();
	}
	
	private void showNoContactsDialog() {
    	new AlertDialog.Builder(this)
		.setMessage("You have not chosen any contacts to alert.")
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
    			// Do nothing.
			}
		})
		.show();
	}
}
