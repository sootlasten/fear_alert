package sten.sootla.fearalert;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sten.sootla.fearalert.R;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public abstract class BaseContactActivity extends ActionBarActivity {

	public AutoCompleteTextView name;
	public EditText number, smsContent;
	public CheckBox call, sms, sendLocation;
	public TextView locationText;
	public SharedPreferences.Editor editor;
	public ArrayList<String> phoneNoList;
	LinkedHashMap<String, String> nameAndPhone, nameAndText;
	ArrayList<String> nameList;
	LinkedHashMap<String, String> textAndPhone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_contact);
				
		name = (AutoCompleteTextView) findViewById(R.id.name);
		number = (EditText) findViewById(R.id.number);
		call = (CheckBox) findViewById(R.id.call);
		sms = (CheckBox) findViewById(R.id.sms);
		smsContent = (EditText) findViewById(R.id.sms_content);
		sendLocation = (CheckBox) findViewById(R.id.send_location);
		locationText = (TextView) findViewById(R.id.location_text);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = sharedPref.edit();
		
		nameAndPhone = new LinkedHashMap<String, String>();
		
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(
						ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
							Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
									null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{id}, null);
							while (pCur.moveToNext()) {
								String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								nameAndPhone.put(name, phoneNo);
							} 
							pCur.close();					
				}
			}
		}
		
		ArrayList<String> suggestionListTexts = new ArrayList<String>();
		textAndPhone = new LinkedHashMap<String, String>();
		nameAndText = new LinkedHashMap<String, String>();
		nameList = new ArrayList<String>();
		
		for (String name : nameAndPhone.keySet()) {
			String sugText = name + " (" + nameAndPhone.get(name) + ")";
			
			nameList.add(name);
			suggestionListTexts.add(sugText);
			textAndPhone.put(sugText, nameAndPhone.get(name));
			nameAndText.put(name, sugText);
		}
		
        AddContactAdapter adapter = new AddContactAdapter(this,
                R.layout.simple_dropdown_item_1line, nameList, nameAndText, "fonts/leaguegothic_regular.ttf");
        name.setAdapter(adapter);
        
        name.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
            	TextView suggestionTv = (TextView) v;
                String phoneNumber = textAndPhone.get(suggestionTv.getText());
                number.setText(phoneNumber);
            }
        });
        
        sendLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					locationText.setVisibility(View.VISIBLE);
				} else {
					locationText.setVisibility(View.GONE);
				}
			}
		});
	    
		setTypeface("fonts/leaguegothic_regular.ttf");
	}
	
	public View getCustomActionBarView(int actionBarViewResource) {
		final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(actionBarViewResource, null);
	}
	
	public void setActionBarDisplayOptions(ActionBar actionBar) {
		actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
	}
	
	public void shareContact(Contact contact) {
		String jsonContact = new Gson().toJson(contact);
		editor.putString(contact.getName(), jsonContact);
		editor.commit();
	}
	
	public VerifyConst verifyInput() {
		// Name field empty
		if (name.getText().length() == 0) {
			return VerifyConst.EMPTY_NAME;
		} 
		// Number field empty
		if (number.getText().length() == 0) {
			return VerifyConst.EMPTY_NUMBER;
		}
		// Number incorrectly formatted
		try {
			String numberWoSpaces = number.getText().toString().replaceAll("\\s+","");
			Integer.parseInt(numberWoSpaces);
		} catch (NumberFormatException ex) {
			return VerifyConst.BADLY_FORMATTED_NUMBER;
		}
		// SMS checked, but SMS field empty
		if (sms.isChecked() && smsContent.getText().length() == 0) {
			return VerifyConst.NO_SMS_CONTENT;
		}
		
		ArrayList<Contact> contacts = getAllContacts();
		// Name already taken by another contact
		for (Contact contact : contacts) {
			if (contact.getName().equals(name.getText().toString())) {
				return VerifyConst.NAME_TAKEN;
			}
		}
		// Phone number already taken by another contact
		for (Contact contact : contacts) {
			if (contact.getNumber().equals(number.getText().toString())) {
				return VerifyConst.NUMBER_TAKEN;
			}
		}
		// There is already another call being made
		for (Contact contact : contacts) {
			if (contact.isCall() == call.isChecked() && call.isChecked()) {
				return VerifyConst.CALL_TAKEN;
			}
		}
		return VerifyConst.OK;
	}
	
	public void showVerificationDialog(Context context, final VerifyConst problem) {
		new AlertDialog.Builder(context)
		.setMessage(problem.toString())
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (problem) {
				case EMPTY_NAME:
					name.requestFocus();
					break;
				case EMPTY_NUMBER:
					number.requestFocus();
					break;
				case BADLY_FORMATTED_NUMBER:
					number.setText("");
					number.requestFocus();
					break;
				case NO_SMS_CONTENT:
					smsContent.requestFocus();
					break;
				case NAME_TAKEN:
					name.setText("");
					name.requestFocus();
					break;
				case NUMBER_TAKEN:
					number.setText("");
					number.requestFocus();
					break;
				case CALL_TAKEN:
					call.setChecked(false);
					call.requestFocus();
					break;
				}
			}
		}).show();
	}

	public Contact makeContact() {
		String inputName = name.getText().toString();
		String inputNumber = number.getText().toString();
		boolean inputCall = call.isChecked();
		boolean inputSMS = sms.isChecked();
		String inputSMSContent = smsContent.getText().toString();
		boolean inputSendLocation = sendLocation.isChecked();
		
		return new Contact(inputName, inputNumber, inputCall, inputSMS, inputSMSContent, inputSendLocation);
	}
	
	public void setTypeface(String path) {
		Typeface typeface = Typeface.createFromAsset(getAssets(), path);
		name.setTypeface(typeface);
		number.setTypeface(typeface);
		call.setTypeface(typeface);
		sms.setTypeface(typeface);
		smsContent.setTypeface(typeface);
		sendLocation.setTypeface(typeface);
		locationText.setTypeface(typeface);
	}
	
	public ArrayList<Contact> getAllContacts() {
		ArrayList<Contact> contactList = new ArrayList<Contact>();
		HashMap<String, String> map = new HashMap<String, String>();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Map<String, ?> contacts = sharedPref.getAll();
		for (Map.Entry<String, ?> entry : contacts.entrySet()) {
			String json = sharedPref.getString(entry.getKey(), null);
			if (json != null) {
				map.put(entry.getKey(), json);
				Contact contact = new Gson().fromJson(json, Contact.class);
				contactList.add(contact);
			}
		}

		return contactList;
	}
}
