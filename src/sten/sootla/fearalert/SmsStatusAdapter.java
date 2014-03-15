package sten.sootla.fearalert;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SmsStatusAdapter extends ArrayAdapter<String> {
	
	private final Context context;
	private final int layout;
	private ArrayList<String> contactNames;
	private HashMap<String, SmsStatus> smsContactStatus;
	
	public SmsStatusAdapter(Context context, int layout, 
			ArrayList<String> contactNames, HashMap<String, SmsStatus> smsContactStatus) {
		super(context, layout, contactNames);
		this.context = context;
		this.layout = layout;
		this.contactNames = contactNames;
		this.smsContactStatus = smsContactStatus;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(layout, parent, false);
		
		TextView tv = (TextView) rowView.findViewById(android.R.id.text1);
		
		String name = contactNames.get(position);
		SmsStatus status = smsContactStatus.get(name);
		
		tv.setText(name + " - " + status.toString());
		
		return rowView;
	}
	
}
