package sten.sootla.fearalert;

import java.util.ArrayList;

import sten.sootla.fearalert.R;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleAdapter extends ArrayAdapter<Contact> {
	private final Context context;
	private final ArrayList<Contact> contacts;
	private final Typeface tf;
	
	public MySimpleAdapter(Context context, ArrayList<Contact> contacts, String FONT) {
		super(context, R.layout.rowlayout, contacts);
		this.context = context;
		this.contacts = contacts;
		tf = Typeface.createFromAsset(context.getAssets(), FONT);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		ImageView imageView2 = (ImageView) rowView.findViewById(R.id.icon2);
		imageView2.setVisibility(View.GONE);
		
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		textView.setText(contacts.get(position).toString());
		textView.setTypeface(tf);
		
		// Change icon to call / message 
		if (contacts.get(position).isCall() &&
				contacts.get(position).isSMS()) {
			imageView.setImageResource(R.drawable.ic_call);
			imageView2.setImageResource(R.drawable.ic_mail);
			imageView2.setVisibility(View.VISIBLE);
		} else if (contacts.get(position).isCall()) {
			imageView.setImageResource(R.drawable.ic_call);
		} else if (contacts.get(position).isSMS()) {
			imageView.setImageResource(R.drawable.ic_mail);
		} else {
			textView.setTextColor(Color.parseColor("#7F7F7F"));
			imageView.setImageResource(R.drawable.nocontacts);
		}
		return rowView;
	}
}
