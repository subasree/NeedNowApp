package com.google.cloud.backend.neednow.sample.guestbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.cloud.backend.neednow.R;

public class MyMapActivity extends FragmentActivity implements
		OnMarkerClickListener, OnMapClickListener, OnMapLongClickListener {
	Circle myCircle;
	private static final double EARTH_RADIUS = 6378100.0;
	private int offset;
	Button mBtnFind;
	GoogleMap mMap;
	EditText etPlace;
	String username;
	String flag, item_name, need_item, have_item;
	public SQLiteDatabase db;
	SupportMapFragment mapFragment;
	int a;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_bkp);

		// Getting reference to the find button
		mBtnFind = (Button) findViewById(R.id.btn_show);

		// Getting reference to the SupportMapFragment
		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		// Getting reference to the Google Map
		mMap = mapFragment.getMap();

		// Getting reference to EditText
		etPlace = (EditText) findViewById(R.id.et_place);

		// Setting click event listener for the find button

		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		mMap.setOnMapClickListener(this);

		mMap.setOnMapLongClickListener(this);

		String item_name = "Mug";

		ArrayList<String> list = new ArrayList<String>();
		list.add("600025");
		list.add("600036");
		list.add("600017");

		// String location[]={"chennai"};
		String mainurl[] = new String[100];
		String addressvalue[] = new String[100];
		String andsymbol[] = new String[100];
		String sensorValue[] = new String[100];
		String url[] = new String[100];
		String url1[] = new String[100];

		int listSize = list.size();
		String location[] = new String[listSize];
		for (int i = 0; i < listSize; i++) {
			location[i] = list.get(i);

			mainurl[i] = "https://maps.googleapis.com/maps/api/geocode/json?";
			addressvalue[i] = "address=";
			andsymbol[i] = "&";
			sensorValue[i] = "sensor=false";

			url[i] = mainurl[i] + addressvalue[i] + location[i] + andsymbol[i]
					+ sensorValue[i];

			Log.d("url is-->" + location[i], "");
			DownloadTask downloadTask = new DownloadTask();

			// Start downloading the geocoding places
			downloadTask.execute(url[i]);

		}

	}

	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;

	}

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {

			// Instantiating ParserTask which parses the json data from
			// Geocoding webservice
			// in a non-ui thread
			ParserTask parserTask = new ParserTask();

			// Start parsing the places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}

	}

	/** A class to parse the Geocoding Places in non-ui thread */
	class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			GeocodeJSONParser parser = new GeocodeJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a an ArrayList */
				places = parser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			// Clears all the existing markers
			// mMap.clear();

			String short_name3 = null;
			String short_name1 = null;
			String short_name2 = null;
			/*
			 * String
			 * have_location_area=loginDataBaseAdapter.getAreaAdd(username);
			 * 
			 * String
			 * have_location_city=loginDataBaseAdapter.getCityAdd(username);
			 * 
			 * String have_location_p=loginDataBaseAdapter.getzipcode(username);
			 */

			String have_location_p = "600017";

			for (int i = 0; i < list.size(); i++) {

				// Creating a marker
				MarkerOptions markerOptions = new MarkerOptions();

				// Getting a place from the places list
				HashMap<String, String> hmPlace = list.get(i);

				// Getting latitude of the place
				double lat = Double.parseDouble(hmPlace.get("lat"));

				// Getting longitude of the place
				double lng = Double.parseDouble(hmPlace.get("lng"));

				// Getting name
				LatLng latLng = new LatLng(lat, lng);

				String name = hmPlace.get("formatted_address");

				// String long_name = hmPlace.get("formatted_address");

				String arr[] = name.split(",");
				int p = arr.length;
				System.out.println("length is" + p);

				if (p == 1) {
					short_name1 = arr[0].replaceAll("\\D+", "");
				} else if (p == 2) {
					short_name1 = arr[0].replaceAll("\\D+", "");
					short_name2 = arr[1].replaceAll("\\D+", "");

				} else if (p >= 3) {
					short_name1 = arr[0].replaceAll("\\D+", "");
					short_name2 = arr[1].replaceAll("\\D+", "");
					short_name3 = arr[2].replaceAll("\\D+", "");

				}

				// Toast.makeText(getApplicationContext(),
				// "Long name Place-->"+long_name+"Long name City"+long_name1,
				// Toast.LENGTH_SHORT).show();

				// Setting the position for the marker

				markerOptions.position(latLng);

				// Setting the title for the marker
				markerOptions.title(name);

				if (have_location_p.equalsIgnoreCase(short_name1)
						|| have_location_p.equalsIgnoreCase(short_name2)
						|| have_location_p.equalsIgnoreCase(short_name3)) {
					LatLng MELBOURNE = new LatLng(lat, lng);
					// MarkerOptions options = new MarkerOptions();
					// options.position(getCoords(lat, lng));
					// options.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(lat,lng)));

					// mMap.addMarker(options);

					mMap.addMarker(new MarkerOptions()
							.position(MELBOURNE)
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
					CircleOptions circleOptions = new CircleOptions()
							.center(MELBOURNE) // set center
							.radius(10000) // set radius in meters
							.fillColor(0x40ff0000) // semi-transparent
							.strokeColor(Color.BLUE).strokeWidth(5);

					myCircle = mMap.addCircle(circleOptions);
					a = 0;

				} else {
					a = 1;
					// Placing a marker on the touched position
					mMap.addMarker(markerOptions);
					mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

						@Override
						public boolean onMarkerClick(Marker arg0) {
							// if marker source is clicked
							if (a == 1) {

								showAlertDialog(arg0);
							}

							return true;

						}

					});
				}

				if (have_location_p.equalsIgnoreCase(short_name1)
						|| have_location_p.equalsIgnoreCase(short_name2)
						|| have_location_p.equalsIgnoreCase(short_name3)) {

					CameraUpdate center = CameraUpdateFactory
							.newLatLng(new LatLng(lat, lng));
					CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
					mMap.moveCamera(center);
					mMap.animateCamera(zoom);

					// icon
					// Bitmap icon =
					// BitmapFactory.decodeResource(mapFragment.getResources(),
					// R.drawable);

					// circle radius - 200 meters

					// create empty bitmap
					// Bitmap b = Bitmap.createBitmap(radius * 2, radius * 2,
					// Config.ARGB_8888);
					// Canvas c = new Canvas(b);
					// if zoom too small

					// draw icon

					// markerOptions.title(name);
				}

				// Locate the first location

			}
		}
	}

	@SuppressWarnings("deprecation")
	void showAlertDialog(Marker arg0) {

		AlertDialog alertDialog = new AlertDialog.Builder(MyMapActivity.this)
				.create();

		alertDialog.setMessage("Do U Want To contact need person?");

		alertDialog.setButton2("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				// arg0.setTitle("Geetha,7299932769");
				Toast.makeText(getApplicationContext(), "Geetha,7299932769",
						Toast.LENGTH_SHORT).show();

			}
		});

		alertDialog.setButton3("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Toast.makeText(getApplicationContext(), "Thank You",
						Toast.LENGTH_SHORT).show();
			}
		});
		alertDialog.show();
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

	}

}
