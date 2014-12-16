package sten.sootla.fearalert;

import sten.sootla.fearalert.R;
import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

public class EditContact extends BaseContactActivity {

	Contact contact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        final View customActionBarView = getCustomActionBarView(
        		R.layout.actionbar_custom_view_done);
        
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	saveContact();
                    }
                });

        final ActionBar actionBar = getSupportActionBar();
        setActionBarDisplayOptions(actionBar);
        actionBar.setCustomView(customActionBarView);
        
        populateFields();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cancel, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
            	finish();
            	return true;
            case R.id.delete:
            	new AlertDialog.Builder(this)
            		.setMessage("This contact will be deleted.")
            		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) {
                			editor.remove(contact.getName());
                			editor.commit();
                			finish();
            			}
            		})
            		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) {
            				// do nothing
            			}
            		})
            		.show();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onBackPressed() {
		saveContact();
	}

	public void saveContact() {
		editor.remove(contact.getName());
		editor.commit();
		if (verifyInput() != VerifyConst.OK) {
			showVerificationDialog(this, verifyInput());
			shareContact(contact);
			
			return;
		}
		shareContact(makeContact());
		if (contactInfChanged()) {
			Toast.makeText(this, "Contact saved.", Toast.LENGTH_SHORT).show();
		}
		finish();
	}
	
	public void populateFields() {
		Intent intent = getIntent();
		String extra = intent.getStringExtra(ContactsActivity.EXTRA_MESSAGE);
		
		contact = new Gson().fromJson(extra, Contact.class);
		
		name.setText(contact.getName());
		number.setText(contact.getNumber());
		call.setChecked(contact.isCall());
		sms.setChecked(contact.isSMS());
		smsContent.setText(contact.getSMSContent());
		sendLocation.setChecked(contact.isSendLocation());
	}
	
	public boolean contactInfChanged() {
		if (contact.getName().equals(name.getText().toString()) &&
				contact.getNumber().equals(number.getText().toString()) &&
				contact.isCall() == call.isChecked() &&
				contact.isSMS() == sms.isChecked() &&
				contact.getSMSContent().equals(smsContent.getText().toString())) {
			return false;
		} else {
			return true;
		}
	}
}