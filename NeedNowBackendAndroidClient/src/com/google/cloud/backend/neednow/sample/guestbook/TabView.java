package com.google.cloud.backend.neednow.sample.guestbook;

import com.google.cloud.backend.neednow.R;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressLint("NewApi")
public class TabView extends TabActivity {
	// TabSpec Names
	private static final String INBOX_SPEC = "NeedList";
	private static final String OUTBOX_SPEC = "HaveList";
	private static final String PROFILE_SPEC = "History";
	public static final String PREFS_NAME = "LoginPrefs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.main);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		actionBar.setSplitBackgroundDrawable(new ColorDrawable(Color.BLACK));

		TabHost tabHost = getTabHost();

		// Inbox Tab
		TabSpec inboxSpec = tabHost.newTabSpec(INBOX_SPEC);
		// Tab Icon
		inboxSpec.setIndicator(INBOX_SPEC,
				getResources().getDrawable(R.drawable.icon_inbox));

		// Intent inboxIntent;
		Intent mIntent = new Intent(TabView.this, NeedListActivity.class);
		Bundle mBundle;

		// inboxIntent = getIntent();
		mBundle = new Bundle();
		mBundle.putString("flag", "need");
		mIntent.putExtras(mBundle);
		// = new Intent(this, CustomizedListView.class);
		// Tab Content
		inboxSpec.setContent(mIntent);

		// Outbox Tab
		TabSpec outboxSpec = tabHost.newTabSpec(OUTBOX_SPEC);
		outboxSpec.setIndicator(OUTBOX_SPEC,
				getResources().getDrawable(R.drawable.icon_outbox));
		// Intent outboxIntent = getIntent();
		Bundle b = new Bundle();
		Intent intent = new Intent(TabView.this, HaveListActivity.class);
		b.putString("flag", "have");
		intent.putExtras(b);
		// outboxSpec.setContent(outboxIntent);
		outboxSpec.setContent(intent);

		// Profile Tab
		TabSpec profileSpec = tabHost.newTabSpec(PROFILE_SPEC);
		profileSpec.setIndicator(PROFILE_SPEC,
				getResources().getDrawable(R.drawable.icon_profile));
		Intent profileIntent = new Intent(this, NeedListActivity.class);
		profileSpec.setContent(profileIntent);

		// Adding all TabSpec to TabHost
		tabHost.addTab(inboxSpec); // Adding Inbox tab
		tabHost.addTab(outboxSpec); // Adding Outbox tab
		tabHost.addTab(profileSpec); // Adding Profile tab

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		Intent intent;

		switch (item.getItemId()) {

		case R.id.action_home:
			intent = new Intent(TabView.this, HomeActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_account:
			return true;
		case R.id.action_list:
			intent = new Intent(TabView.this, TabView.class);
			startActivity(intent);
			return true;
		case R.id.action_refresh:
			return true;
		case R.id.action_help:
			// help action
			return true;
		case R.id.action_check_updates:
			// check for updates action
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}