package sten.sootla.fearalert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import sten.sootla.fearalert.R;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

public class ContactsActivity extends ActionBarActivity implements View.OnClickListener {
	public final static String EXTRA_MESSAGE = "com.example.fearcaller.MESSAGE";
	private ArrayList<Contact> contactList;
	private HashMap<String, String> map;
	RelativeLayout rLayout;
	ImageView noContactsImage;
	TextView defaultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		
		String title = "<font color='yellow'>Contacts</font>";
		Spanned titleReal = Html.fromHtml(title);
		
		SpannableString s = new SpannableString(titleReal);
		s.setSpan(new TypefaceSpan(this, "flexdisplay.ttf"), 0, s.length(), 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		actionBar.setTitle(s);
		
		rLayout = (RelativeLayout) findViewById(R.id.no_contacts_layout);
		noContactsImage = (ImageView) findViewById(R.id.no_contacts_img); 
		defaultText = (TextView) findViewById(R.id.default_text);
		
		noContactsImage.setOnClickListener(this);
		defaultText.setOnClickListener(this);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/leaguegothic_regular.ttf");
		defaultText.setTypeface(typeface);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, AddContact.class);
		startActivity(intent);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		contactList = new ArrayList<Contact>();
		map = new HashMap<String, String>();
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
			
		if (contactList.size() == 0) {
			rLayout.setVisibility(View.VISIBLE);
		} else {
			rLayout.setVisibility(View.GONE);
			final ListView listview = (ListView) findViewById(R.id.listview);
			
			Collections.sort(contactList);
			
			MySimpleAdapter adapter = new MySimpleAdapter(this, contactList, "fonts/leaguegothic_regular.ttf");
			listview.setAdapter(adapter);
			
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					final Contact item = (Contact) parent.getItemAtPosition(position);
					Intent intent = new Intent(getBaseContext(), EditContact.class);
					intent.putExtra(EXTRA_MESSAGE, map.get(item.toString()));
					startActivity(intent);
				}
			});
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacts, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_user_add) {
			Intent intent = new Intent(this, AddContact.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
