package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.cloud.backend.neednow.R;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	public SparseBooleanArray mCheckStates;

	public CustomAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
		mCheckStates = new SparseBooleanArray(data.size());
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public String getKey(int position) {
		return data.get(position).get("id");
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_new_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title); // title
		TextView artist = (TextView) vi.findViewById(R.id.artist); // artist
																	// name
		final CheckBox cb = (CheckBox) vi.findViewById(R.id.checkBox1);
		cb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(activity.getApplicationContext(), "clicked ",
						Toast.LENGTH_SHORT).show();
				if (cb.isChecked())

					mCheckStates.put(position, true);
				else
					mCheckStates.put(position, false);
			}
		});

		// TextView duration = (TextView) vi.findViewById(R.id.duration); //
		// duration
		// ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);
		// // thumb
		// image

		HashMap<String, String> song = new HashMap<String, String>();
		if (getCount() > 0) {
			song = data.get(position);

			// Setting all values in listview
			title.setText(song.get("title"));
			artist.setText(song.get("desc"));

			// duration.setText(song.get("timestamp"));

		}
		return vi;
	}
}
