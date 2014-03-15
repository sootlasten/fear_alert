package sten.sootla.fearalert;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.support.v7.app.ActionBar;

public class AddContact extends BaseContactActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final View customActionBarView = getCustomActionBarView(
				R.layout.actionbar_custom_view_done_cancel);
		
		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (verifyInput() == VerifyConst.OK) {
							shareContact(makeContact());
							Toast.makeText(getBaseContext(), "Contact saved.", Toast.LENGTH_SHORT).show();
							finish();
						} else {
							showVerificationDialog(AddContact.this, verifyInput());
						}
					}
				});
	
		customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		
		final ActionBar actionBar = getSupportActionBar();
		setActionBarDisplayOptions(actionBar);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}
}
