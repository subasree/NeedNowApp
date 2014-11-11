package com.google.cloud.backend.neednow.sample.guestbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import SimilarityMatching.TextSimilarity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.cloud.backend.neednow.R;
import com.google.cloud.backend.neednow.core.CloudBackendActivity;
import com.google.cloud.backend.neednow.core.CloudCallbackHandler;
import com.google.cloud.backend.neednow.core.CloudEntity;
import com.google.cloud.backend.neednow.core.CloudQuery;
import com.google.cloud.backend.neednow.core.CloudQuery.Scope;
import com.google.cloud.backend.neednow.core.Filter;

public class MatchHaveNeed extends CloudBackendActivity {
	private String have_post_id;
	private String have_item_name;
	private String categ_id;

	private List<String> need_post_id;
	private List<CloudEntity> need_posts;
	private LinkedHashMap<String, Double> lhm;
	private CloudEntity have_entity;
	static final String KEY_TITLE = "title";
	static final String KEY_DESC = "desc";
	static final String KEY_ID = "id";
	ListView list;
	Button subscribe_btn;
	CustomAdapter adapter;
	TextView tview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matchhaveneed);
		have_post_id = getIntent().getExtras().getString("have_post_id");
		have_item_name = getIntent().getExtras().getString("item_name_txt");
		categ_id = getIntent().getExtras().getString("categ_id");

		subscribe_btn = (Button) findViewById(R.id.btnSelect);
		tview = (TextView) findViewById(R.id.page_text);
		have_entity = null;
		need_post_id = new ArrayList<String>();
		String[] params = { have_post_id };
		new setHaveEntityAsyncTask(MatchHaveNeed.this).execute(params);
		// setNeedEntity();
		retrieveNeedPosts();
	}

	private class setHaveEntityAsyncTask extends
			AsyncTask<String, Void, CloudEntity> {
		Context context;

		public setHaveEntityAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			// preexecution stmt

		}

		protected CloudEntity doInBackground(String... params) {
			CloudEntity response = null;
			try {
				String have_id = params[0];
				response = getCloudBackend().get("Have", have_id);
			} catch (Exception e) {
				Log.d("Could not Assign Have", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(CloudEntity ce) {
			have_entity = ce;
			Toast.makeText(getBaseContext(),
					"Have entity id " + have_entity.getId(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void retrieveNeedPosts() {

		CloudCallbackHandler<List<CloudEntity>> post_handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				if (results.size() > 0) {
					need_posts = results;
					Toast.makeText(getBaseContext(),
							"Posts retrieved succesfully", Toast.LENGTH_SHORT)
							.show();

					// step 1 just populate lhm and have_post_id
					populateMatchedPosts();
					// check if atleast a 100% match is found
					if (lhm.size() > 0) {

						List mapKeys = new ArrayList(lhm.keySet());
						String first_key = mapKeys.get(0).toString();
						int indx = 0;
						need_post_id = new ArrayList<String>();
						if (lhm.get(first_key) == 1.0) { // atleast one
															// perfect match
							for (int i = 0; i < mapKeys.size(); i++) {
								String key = mapKeys.get(i).toString();
								double score = lhm.get(key);
								if (score == 1.0) {
									need_post_id.add(key); // added 100% matched
															// have posts
								} else {
									indx = i;
									break;
								}
							}
							if (indx == 0) {
								new updateHaveAsyncTask(MatchHaveNeed.this)
										.execute(new String[0]);

							}
						}
						ArrayList<CloudEntity> entities = new ArrayList<CloudEntity>();
						for (int i = indx; i < mapKeys.size(); i++) {
							CloudEntity ce = getEntity(mapKeys.get(i)
									.toString());
							if (ce != null)
								entities.add(ce);
						}

						//updateUI(entities);
						// update Need to add the list of have
						// post_ids

						// show posts from indx
					} else
						loadNextActivity();
				} else
					// no matching have found
					loadNextActivity();

			}

			@Override
			public void onError(IOException exception) {
				handleEndpointException(exception);
			}
		};

		getCloudBackend().clearAllSubscription();
		// cloud server data
		// getCloudBackend().listByKind("Have", null, null, 50,
		// Scope.PAST,post_handler);
		CloudQuery cq = new CloudQuery("Need");
		cq.setFilter(Filter.eq("categid", categ_id));
		getCloudBackend().list(cq, post_handler);
	}

	private void handleEndpointException(Exception e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}

	public void populateMatchedPosts() {

		if (need_posts.size() > 0) {
			ArrayList<String> item_posts = new ArrayList<String>();
			ArrayList<String> post_id = new ArrayList<String>();
			for (int i = 0; i < need_posts.size(); i++) {
				CloudEntity ce = need_posts.get(i);
				item_posts.add(ce.get("itemname").toString());
				post_id.add(ce.getId());

			}
			populateSimilarity(item_posts, post_id);

		} else
			loadNextActivity();
		// populatematchedpsosts
	}

	public void populateSimilarity(ArrayList<String> item_posts,
			ArrayList<String> post_id) {
		ArrayList<Double> scores = new ArrayList<Double>();
		for (int i = 0; i < item_posts.size(); i++) {

			scores.add(TextSimilarity.compareStrings(have_item_name,
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

		// remove 0.0 matched posts from have_posts
		for (int i = 0; i < need_posts.size(); i++) {
			String current_id = need_posts.get(i).getId();
			if (!hm.containsKey(current_id))
				need_posts.remove(i);
		}
		lhm = TextSimilarity.sortHashMapByValuesD(hm);

		ArrayList<CloudEntity> temp_entities = new ArrayList<CloudEntity>();
		List mapKeys = new ArrayList(lhm.keySet());
		for (int i = 0; i < mapKeys.size(); i++) {
			CloudEntity ce = getEntity(mapKeys.get(i).toString());
			if (ce != null) {
				temp_entities.add(ce);
			}
		}
		need_posts = temp_entities;

	}

	private class updateHaveAsyncTask extends AsyncTask<String, Void, Have> {
		Context context;
		private ProgressDialog pd;

		public updateHaveAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Subscribing to the Items...");
			pd.show();

		}

		protected Have doInBackground(String... params) {
			Have n = null;
			try {
				if (have_entity != null) {
					ArrayList<String> new_list = new ArrayList<String>();
					Object obj = have_entity.get("need_idlist");
					if (obj instanceof ArrayList) {
						new_list = (ArrayList) obj;
					}

					for (int i = 0; i < need_post_id.size(); i++) {
						new_list.add(need_post_id.get(i));
					}
					// fetching the existing list of have id and adding new ids
					// and updating the entry
					have_entity.put("need_idlist", new_list);
					getCloudBackend().update(have_entity);
				}
				// response = insertHaveDB(selected_categ, item_name_txt, locn);
				// getCloudBackend().insert(newHave.asEntity());

			} catch (Exception e) {
				Log.d("Could not Post Have", e.getMessage(), e);
			}
			return n;
		}

		protected void onPostExecute(Have n) {
			// Clear the progress dialog and the fields
			pd.dismiss();

			Toast.makeText(getBaseContext(), "Susbscribed succesfully ",
					Toast.LENGTH_SHORT).show();

			// update have entity
			//new updateNeedAsyncTask(MatchHaveNeed.this).execute(new String[0]);

			// load NeedList Activity
			/*
			 * Intent intent = new Intent(MatchNeedHave.this, TabView.class);
			 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(intent); finish();
			 */
			// load until the date is fine.
			// updateNeedAsync activity to call the match need if matched (100%)
			// else send the remaining 25% [0.5 confidence score]

			/*
			 * load next activity only when match not found or auto-match is the
			 * only entry case1 only one match - load next activity case2
			 * multiple 100% matches - load next activity case3
			 */
		}
	}

	public void loadNextActivity() {

		//activity to load 
		Intent intent = new Intent(MatchHaveNeed.this, TabView.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public CloudEntity getEntity(String key) {

		for (int i = 0; i < need_posts.size(); i++) {
			CloudEntity curr_entity = need_posts.get(i);
			String curr_key = curr_entity.getId();
			if (curr_key.equals(key))
				return curr_entity;
		}
		return null;
	}

}
