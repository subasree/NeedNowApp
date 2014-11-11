package com.google.cloud.backend.neednow.sample.guestbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.cloud.backend.neednow.R;
import com.google.cloud.backend.neednow.core.CloudBackendActivity;
import com.google.cloud.backend.neednow.core.CloudQuery;
import com.google.cloud.backend.neednow.core.Filter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.cloud.backend.neednow.core.CloudEntity;
import com.google.cloud.backend.neednow.core.CloudQuery.Order;

public class NeedListActivity extends CloudBackendActivity {

	private List<CloudEntity> need_list;
	private String account_name;
	static final String KEY_TITLE = "title";
	static final String KEY_DESC = "desc";
	static final String KEY_ID = "id";
	static final String KEY_CATEGORY = "category";
	static final String KEY_POST_TIME = "post_ts";
	ListView list;
	LazyAdapter adapter;
	CategoryList app_state;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.needlist);
		account_name = super.getAccountName();
		app_state = ((CategoryList) this.getApplication());
		new fetchNeedAsyncTask(NeedListActivity.this).execute(new String[0]);
	}

	private class fetchNeedAsyncTask extends
			AsyncTask<String, Void, List<CloudEntity>> {
		Context context;
		private ProgressDialog pd;

		public fetchNeedAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Fetching NeedList...");
			pd.show();

		}

		protected List<CloudEntity> doInBackground(String... params) {
			List<CloudEntity> entities = null;
			try {
				CloudQuery cq = new CloudQuery("Need");
				cq.setFilter(Filter.eq("_createdBy", account_name));
				// cq.setSort("_createdAt", Order.ASC);
				entities = getCloudBackend().list(cq);

			} catch (Exception e) {
				Log.d("Could not Fetch Need", e.getMessage(), e);
			}
			return entities;
		}

		protected void onPostExecute(List<CloudEntity> entities) {

			// Clear the progress dialog and the fields

			Collections.sort(entities, new Comparator<CloudEntity>() {
				public int compare(CloudEntity o1, CloudEntity o2) {
					return o2.getUpdatedAt().compareTo(o1.getUpdatedAt());
				}
			});

			need_list = entities;
			pd.dismiss();

			Toast.makeText(getBaseContext(),
					"Fetched succesfully " + need_list.size(),
					Toast.LENGTH_SHORT).show();

			updateListView();
		}
	}

	public void updateListView() {
		if (need_list != null && need_list.size() > 0) {
			ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < need_list.size(); i++) {

				HashMap<String, String> map = new HashMap<String, String>();
				CloudEntity current_entity = need_list.get(i);
				String key_id = current_entity.get("categid").toString();
				map.put(KEY_ID, key_id);
				map.put(KEY_TITLE, current_entity.get("itemname").toString());
				map.put(KEY_DESC, "Desc: " + current_entity.get("desc"));
				map.put(KEY_CATEGORY,
						"Category: " + app_state.getCategoryName(key_id));
				long time_ts = current_entity.getCreatedAt().getTime();
				CharSequence result_ts = DateUtils.getRelativeTimeSpanString(
						time_ts, System.currentTimeMillis(),
						DateUtils.SECOND_IN_MILLIS);
				map.put(KEY_POST_TIME, "Posted: " + result_ts);

				itemList.add(map);
			}
			list = (ListView) findViewById(R.id.list2);

			// Getting adapter by passing xml data ArrayList
			adapter = new LazyAdapter(this, itemList);
			list.setAdapter(adapter);
		}
	}

}
