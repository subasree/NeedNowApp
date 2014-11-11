package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.cloud.backend.neednow.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
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

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title); // title
		TextView artist = (TextView) vi.findViewById(R.id.artist); // artist
																	// name
		TextView duration = (TextView) vi.findViewById(R.id.duration); //
		TextView post_ts = (TextView) vi.findViewById(R.id.post_ts);
		// duration
		// ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);
		// // thumb
		// image

		HashMap<String, String> song = new HashMap<String, String>();
		if (getCount() > 0) {
			song = data.get(position);

			// Setting all values in listview
			title.setText(song.get("title"));
			String item_desc = song.get("desc");
			if (!item_desc.equals("") && !item_desc.equals("Desc: "))
				artist.setText(item_desc);
			duration.setText(song.get("category"));
			post_ts.setText(song.get("post_ts"));

		}
		return vi;
	}
}
