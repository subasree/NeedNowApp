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

public class MatchNeedHave extends CloudBackendActivity {

	private String need_post_id;
	private String need_item_name, categ_id;
	private List<String> have_post_id;
	private List<CloudEntity> have_posts;
	private LinkedHashMap<String, Double> lhm;
	private CloudEntity need_entity;
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
		setContentView(R.layout.matchneedhave);
		need_post_id = getIntent().getExtras().getString("need_post_id");
		need_item_name = getIntent().getExtras().getString("item_name_txt");
		categ_id = getIntent().getExtras().getString("categ_id");
		subscribe_btn = (Button) findViewById(R.id.btnSelect);
		tview = (TextView) findViewById(R.id.page_text);
		need_entity = null;
		have_post_id = new ArrayList<String>();
		String[] params = { need_post_id };
		new setNeedEntityAsyncTask(MatchNeedHave.this).execute(params);
		// setNeedEntity();
		retrieveHavePosts();
	}

	private class setNeedEntityAsyncTask extends
			AsyncTask<String, Void, CloudEntity> {
		Context context;

		public setNeedEntityAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			// preexecution stmt

		}

		protected CloudEntity doInBackground(String... params) {
			CloudEntity response = null;
			try {
				String need_id = params[0];
				response = getCloudBackend().get("Need", need_id);
			} catch (Exception e) {
				Log.d("Could not Post Have", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(CloudEntity ce) {
			need_entity = ce;
			Toast.makeText(getBaseContext(),
					"Need entity id " + need_entity.getId(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void retrieveHavePosts() {

		CloudCallbackHandler<List<CloudEntity>> post_handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				if (results.size() > 0) {
					have_posts = results;
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
						have_post_id = new ArrayList<String>();
						if (lhm.get(first_key) == 1.0) { // atleast one
															// perfect match
							for (int i = 0; i < mapKeys.size(); i++) {
								String key = mapKeys.get(i).toString();
								double score = lhm.get(key);
								if (score == 1.0) {
									have_post_id.add(key); // added 100% matched
															// have posts
								} else {
									indx = i;
									break;
								}
							}
							if (indx == 0) {
								new updateNeedAsyncTask(MatchNeedHave.this)
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

						updateUI(entities);
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
		CloudQuery cq = new CloudQuery("Have");
		cq.setFilter(Filter.eq("categid", categ_id));
		getCloudBackend().list(cq, post_handler);
	}

	private void handleEndpointException(Exception e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}

	private class updateNeedAsyncTask extends AsyncTask<String, Void, Need> {
		Context context;
		private ProgressDialog pd;

		public updateNeedAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Subscribing to the Items...");
			pd.show();

		}

		protected Need doInBackground(String... params) {
			Need n = null;
			try {
				if (need_entity != null) {
					ArrayList<String> new_list = new ArrayList<String>();
					Object obj = need_entity.get("have_idlist");
					if (obj instanceof ArrayList) {
						new_list = (ArrayList) obj;
					}

					for (int i = 0; i < have_post_id.size(); i++) {
						new_list.add(have_post_id.get(i));
					}
					// fetching the existing list of have id and adding new ids
					// and updating the entry
					need_entity.put("have_idlist", new_list);
					getCloudBackend().update(need_entity);
				}
				// response = insertHaveDB(selected_categ, item_name_txt, locn);
				// getCloudBackend().insert(newHave.asEntity());

			} catch (Exception e) {
				Log.d("Could not Post Have", e.getMessage(), e);
			}
			return n;
		}

		protected void onPostExecute(Need n) {
			// Clear the progress dialog and the fields
			pd.dismiss();

			Toast.makeText(getBaseContext(), "Susbscribed succesfully ",
					Toast.LENGTH_SHORT).show();

			// update have entity
			new updateHaveAsyncTask(MatchNeedHave.this).execute(new String[0]);

			// load NeedList Activity
			/*
			 * Intent intent = new Intent(MatchNeedHave.this, TabView.class);
			 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(intent); finish();
			 */
		}
	}

	private class updateHaveAsyncTask extends
			AsyncTask<String, Void, ArrayList<Have>> {
		Context context;
		private ProgressDialog pd;

		public updateHaveAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Notifying Have...");
			pd.show();

		}

		protected ArrayList<Have> doInBackground(String... params) {
			ArrayList<Have> have_list = null;
			try {
				for (int i = 0; i < have_post_id.size(); i++) {
					String have_key = have_post_id.get(i);
					ArrayList<String> new_list = new ArrayList<String>();
					CloudEntity ce = getEntity(have_key);
					Object obj = ce.get("need_idlist");
					if (obj instanceof ArrayList) {
						new_list = (ArrayList) obj;
					}
					new_list.add(need_post_id);
					// fetching the existing list of have id and adding new ids
					// and updating the entry
					ce.put("need_idlist", new_list);
					getCloudBackend().update(ce);

				}
			} catch (Exception e) {
				Log.d("Could not Update Have", e.getMessage(), e);
			}
			return have_list;
		}

		protected void onPostExecute(ArrayList<Have> have_list) {
			// Clear the progress dialog and the fields
			pd.dismiss();

			Toast.makeText(getBaseContext(), "Notified succesfully ",
					Toast.LENGTH_SHORT).show();

			Intent intent = new Intent(MatchNeedHave.this, TabView.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();

		}
	}

	public void loadNextActivity() {

		Intent intent = new Intent(MatchNeedHave.this, TabView.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void updateUI(ArrayList<CloudEntity> entities) {
		/*
		 * TextView tv = (TextView) findViewById(R.id.label1); String text = "";
		 * for (int i = 0; i < entities.size(); i++) { text +=
		 * entities.get(i).get("itemname") + "\n"; } tv.setText(text);
		 */

		Toast.makeText(getBaseContext(), "size= " + entities.size(),
				Toast.LENGTH_SHORT).show();

		if (entities != null && entities.size() > 0) {

			subscribe_btn.setVisibility(View.VISIBLE);
			tview.setVisibility(View.VISIBLE);

			ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < entities.size(); i++) {

				HashMap<String, String> map = new HashMap<String, String>();
				CloudEntity current_entity = entities.get(i);
				map.put(KEY_ID, current_entity.getId());
				map.put(KEY_TITLE, current_entity.get("itemname").toString());
				map.put(KEY_DESC, "Desc: " + current_entity.get("desc"));
				itemList.add(map);
			}
			list = (ListView) findViewById(R.id.list1);

			// Getting adapter by passing xml data ArrayList
			adapter = new CustomAdapter(this, itemList);
			list.setAdapter(adapter);

			subscribe_btn = (Button) findViewById(R.id.btnSelect);
			subscribe_btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), "clicked button ",
							Toast.LENGTH_SHORT).show();
					SparseBooleanArray checkedstates = adapter.mCheckStates;
					for (int i = 0; i < checkedstates.size(); i++) {
						if (checkedstates.valueAt(i)) {
							have_post_id.add(adapter.getKey(i));
							Toast.makeText(getBaseContext(),
									"clicked " + adapter.getKey(i),
									Toast.LENGTH_SHORT).show();

							// update the need

						}
					}
					new updateNeedAsyncTask(MatchNeedHave.this)
							.execute(new String[0]);
					/*
					 * (ArrayList<String> keys = new ArrayList<String>(); for
					 * (int i = 0; i < list_id.length; i++) { int pos =
					 * Integer.parseInt(list_id[i]);
					 * keys.add(adapter.getKey(pos)); }
					 */
				}
			});

			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					long item = list.getItemIdAtPosition(position);
					View v = adapter.getView(position, view, parent);
					TextView title = (TextView) v.findViewById(R.id.title);
					final CheckBox cbox = (CheckBox) v
							.findViewById(R.id.checkBox1);

					cbox.setChecked(!cbox.isChecked());
					adapter.mCheckStates.put(position, cbox.isChecked());
					/*
					 * cbox.setOnClickListener(new View.OnClickListener() {
					 * 
					 * @Override public void onClick(View v) {
					 * cbox.setChecked(true); Toast.makeText(getBaseContext(),
					 * "Checked box", Toast.LENGTH_SHORT).show(); } });
					 */
					// cbox.setChecked(!cbox.isChecked());
					final String title_text = title.getText().toString();
					Toast.makeText(getBaseContext(), "clicked " + title_text,
							Toast.LENGTH_SHORT).show();

				}
			});

		} else
			loadNextActivity();
		// default activity
	}

	public void populateMatchedPosts() {

		if (have_posts.size() > 0) {
			ArrayList<String> item_posts = new ArrayList<String>();
			ArrayList<String> post_id = new ArrayList<String>();
			for (int i = 0; i < have_posts.size(); i++) {
				CloudEntity ce = have_posts.get(i);
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

			scores.add(TextSimilarity.compareStrings(need_item_name,
					item_posts.get(i)));
		}

		for (int i = 0; i < scores.size(); i++) {
			Log.d(item_posts.get(i), "" + scores.get(i));
		}

		HashMap<String, Double> hm = new HashMap<String, Double>();
		for (int i = 0; i < post_id.size(); i++) {
			double score = scores.get(i);
			if (score > 0.5)
				hm.put(post_id.get(i), score);
			Log.d("values ", post_id.get(i));
		}

		// remove 0.0 matched posts from have_posts
		for (int i = 0; i < have_posts.size(); i++) {
			String current_id = have_posts.get(i).getId();
			if (!hm.containsKey(current_id))
				have_posts.remove(i);
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
		have_posts = temp_entities;

	}

	public CloudEntity getEntity(String key) {

		for (int i = 0; i < have_posts.size(); i++) {
			CloudEntity curr_entity = have_posts.get(i);
			String curr_key = curr_entity.getId();
			if (curr_key.equals(key))
				return curr_entity;
		}
		return null;
	}

}
