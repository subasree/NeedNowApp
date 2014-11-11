package com.google.cloud.backend.neednow.sample.guestbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import SimilarityMatching.TextSimilarity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.cloud.backend.neednow.R;
import com.google.cloud.backend.neednow.core.CloudBackendActivity;
import com.google.cloud.backend.neednow.core.CloudCallbackHandler;
import com.google.cloud.backend.neednow.core.CloudEntity;
import com.google.cloud.backend.neednow.core.CloudQuery.Scope;

public class ListingPosts extends CloudBackendActivity {

	List<CloudEntity> posts;
	ArrayList<String> item_posts;
	ArrayList<String> post_id;
	ArrayList<Double> scores;
	List mapKeys;
	String item_text;
	static final String KEY_TITLE = "title";
	static final String KEY_DESC = "desc";
	ListView list;
	LazyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		item_posts = new ArrayList<String>();
		scores = new ArrayList<Double>();
		post_id = new ArrayList<String>();
		item_text = getIntent().getExtras().getString("item_text");
		Toast.makeText(getBaseContext(),
				"Bundle retrieved succesfully " + item_text, Toast.LENGTH_SHORT)
				.show();

		getPosts();
	}

	public void populateSimilarity() {

		for (int i = 0; i < item_posts.size(); i++) {

			scores.add(TextSimilarity.compareStrings(item_text,
					item_posts.get(i)));
		}

		for (int i = 0; i < scores.size(); i++) {
			Log.d(item_posts.get(i), "" + scores.get(i));
		}

		HashMap<String, Double> hm = new HashMap<String, Double>();
		for (int i = 0; i < post_id.size(); i++) {
			double score = scores.get(i);
			if (score > 0.0)
				hm.put(post_id.get(i), score);
			Log.d("values ", post_id.get(i));
		}

		LinkedHashMap<String, Double> lhm = TextSimilarity
				.sortHashMapByValuesD(hm);
		mapKeys = new ArrayList(lhm.keySet());
		for (int i = 0; i < mapKeys.size(); i++) {
			String key = mapKeys.get(i).toString();
			Log.d("values ", key + " " + item_posts.get(post_id.indexOf(key))
					+ " " + lhm.get(key).toString());
		}
		updateDisplay();

	}

	public void updateDisplay() {
		if (mapKeys != null && mapKeys.size() > 0) {
			ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < mapKeys.size(); i++) {
				String key = mapKeys.get(i).toString();
				int indx = post_id.indexOf(key);
				HashMap<String, String> map = new HashMap<String, String>();
				CloudEntity current_entity = posts.get(indx);

				map.put(KEY_TITLE, current_entity.get("itemname").toString());
				map.put(KEY_DESC, "Desc: " + current_entity.get("desc"));
				itemList.add(map);
			}
			list = (ListView) findViewById(R.id.list);

			// Getting adapter by passing xml data ArrayList
			adapter = new LazyAdapter(this, itemList);
			list.setAdapter(adapter);
		}
	}

	public void getPosts() {

		CloudCallbackHandler<List<CloudEntity>> post_handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				if (results.size() > 0) {
					posts = results;
					for (int i = 0; i < results.size(); i++) {
						CloudEntity ce = results.get(i);

						item_posts.add(ce.get("itemname").toString());
						post_id.add(ce.getId());

					}
					if (item_posts.size() > 0 && post_id.size() > 0) {
						// trigger algorithm

						populateSimilarity();

					}
					Toast.makeText(getBaseContext(),
							"Posts retrieved succesfully", Toast.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void onError(IOException exception) {
				handleEndpointException(exception);
			}
		};
		
		
		getCloudBackend().clearAllSubscription();
		getCloudBackend().listByKind("Need", null, null, 50, Scope.PAST,
				post_handler);
	}

	private void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}
}
