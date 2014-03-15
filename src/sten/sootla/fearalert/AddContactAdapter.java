package sten.sootla.fearalert;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import sten.sootla.fearalert.R;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddContactAdapter extends ArrayAdapter<String> {
	
	private final Context context;
	private final int layout;
	private final Typeface tf;
	private LinkedHashMap<String, String> nameAndText;
	
	public AddContactAdapter(Context context, int layout, ArrayList<String> nameList,
			LinkedHashMap<String, String> nameAndText, String FONT) {
		super(context, layout, nameList);
		this.context = context;
		this.layout = layout;
		this.nameAndText = nameAndText;
		tf = Typeface.createFromAsset(context.getAssets(), FONT);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(layout, parent, false);
		
		TextView suggestion = (TextView) rowView.findViewById(R.id.suggestion);
		suggestion.setText(nameAndText.get(getItem(position)));
		suggestion.setTypeface(tf);
		
		return rowView;
	}
}
