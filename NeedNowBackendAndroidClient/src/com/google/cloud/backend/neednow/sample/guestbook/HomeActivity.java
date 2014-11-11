package com.google.cloud.backend.neednow.sample.guestbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.gms.internal.dd;
import com.google.cloud.backend.neednow.core.CloudQuery.Order;
import com.google.cloud.backend.neednow.R;
import com.google.cloud.backend.neednow.core.CloudBackendActivity;
import com.google.cloud.backend.neednow.core.CloudCallbackHandler;
import com.google.cloud.backend.neednow.core.CloudEntity;
import com.google.cloud.backend.neednow.core.CloudQuery;
import com.google.cloud.backend.neednow.core.Filter;
import com.google.cloud.backend.neednow.core.CloudQuery.Scope;
import com.google.cloud.backend.neednow.core.Filter.Op;
import com.google.cloud.backend.neednow.sample.guestbook.IntroductionActivity;

@SuppressLint("NewApi")
public class HomeActivity extends CloudBackendActivity implements
		OnItemSelectedListener {
	// private TextView t;
	// private Button signin_btn;
	private static final int INTRO_ACTIVITY_REQUEST_CODE = 1;
	public static final String GUESTBOOK_SHARED_PREFS = "GUESTBOOK_SHARED_PREFS";
	public static final String PROFILE_SHARED_PREFS = "PROFILE_SHARED_PREFS";
	public static final String SHOW_INTRO_PREFS_KEY = "SHOW_INTRO_PREFS_KEY";
	public static final String SHOW_PROFILE_SEETINGS_PREFS_KEY = "SHOW_PROFILE_SEETINGS_PREFS_KEY";
	private boolean showIntro = true;
	private Profile mSelf;
	private Spinner spinner1;
	private Button find_btn, need_btn, have_btn;
	private EditText item_name;
	private List<CloudEntity> categ;
	private Bundle mBundle;
	private Intent mIntent;
	List<String> category_list;
	List<String> categories;
	CategoryList app_state;
	String categId;
	CloudEntity myProfile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.home);
		app_state = ((CategoryList) this.getApplication());
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		actionBar.setSplitBackgroundDrawable(new ColorDrawable(Color.BLACK));
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		find_btn = (Button) findViewById(R.id.btnSearch);
		need_btn = (Button) findViewById(R.id.btnNeed);
		have_btn = (Button) findViewById(R.id.btnHave);
		item_name = (EditText) findViewById(R.id.item_txt);
		categ = new LinkedList<CloudEntity>();
		categId = "";
		item_name.setText("");
		/*
		 * t = (TextView) findViewById(R.id.msgtxt); signin_btn = (Button)
		 * findViewById(R.id.btnLogin); signin_btn.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // new DoSomethingAsync(this,
		 * endpoint).execute(); displayAccount();
		 * 
		 * } });
		 */

		find_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchPosts();
			}
		});

		need_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				insertNeedPost();
			}
		});

		have_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				insertHavePost();
			}
		});

		checkForPreferences();
		checkProfilePreferences();

		category_list = new ArrayList<String>();

		category_list.add("TextBooks");
		category_list.add("Engineering Equipments");
		category_list.add("Notes");
		category_list.add("Hostel Equipments");
		category_list.add("Other");

		getCategories();
		spinner1 = (Spinner) findViewById(R.id.spinner1);

		// Spinner click listener
		spinner1.setOnItemSelectedListener(this);

		categories = new ArrayList<String>();
		categories.add("Select Category");
		updateSpinner();

	}

	@Override
	protected void onPostCreate() {
		super.onPostCreate();

	}

	/*
	 * public void displayAccount() { if (super.getAccountName() != null)
	 * t.setText(super.getAccountName()); }
	 */

	private void searchPosts() {
		if (!item_name.getText().toString().equals("")) {
			// get all items for the selected category from DB both need and
			// have
			String selected_categ = spinner1.getSelectedItem().toString();
			if (!selected_categ.equals("Select Category")) {
				String item_name_txt = item_name.getText().toString();
				// String[] params = { selected_categ, item_name_txt };
				// new getPostsAsyncTask(HomeActivity.this).execute(params);
				Toast.makeText(getBaseContext(), "Posts going to be retrieved",
						Toast.LENGTH_SHORT).show();

				mIntent = new Intent(HomeActivity.this, ListingPosts.class);
				mBundle = new Bundle();
				mBundle.putString("item_text", item_name_txt);
				mIntent.putExtras(mBundle);
				startActivity(mIntent);
			} else {

			}

			// getPosts();

		} else {
			item_name.setError("Enter an item");
		}
	}

	public void getPosts() {

		CloudCallbackHandler<List<CloudEntity>> post_handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				if (results.size() > 0) {
					ArrayList<String> items = new ArrayList<String>();
					for (int i = 0; i < results.size(); i++) {
						CloudEntity ce = results.get(i);

						items.add(ce.get("itemname").toString());
					}
					// pd.dismiss();
					Toast.makeText(getBaseContext(),
							"Posts retrieved succesfully " + items.get(0),
							Toast.LENGTH_SHORT).show();

				}
			}

			@Override
			public void onError(IOException exception) {
				handleEndpointException(exception);
			}
		};

		getCloudBackend().listByKind("Need", "itemname", null, 50,
				Scope.FUTURE_AND_PAST, post_handler);
	}

	private void insertNeedPost() {
		// Toast.makeText(getApplicationContext(),
		// spinner1.getSelectedItem().toString(),Toast.LENGTH_LONG).show();

		if (spinner1.getSelectedItem() == null
				|| spinner1.getSelectedItem().equals("Select Category")) {

			AlertDialog alertDialog1 = new AlertDialog.Builder(
					HomeActivity.this).create();

			// Setting Dialog Title
			alertDialog1.setTitle("Select Item Category");

			// Setting Dialog Message
			alertDialog1.setMessage("Please select a category");

			// Setting OK Button
			alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

				}
			});

			// Showing Alert Message
			alertDialog1.show();
		}

		final String selected_categ = spinner1.getSelectedItem().toString();
		if (!item_name.getText().toString().equals("")) {
			// alert box
			final String item_name_txt = item_name.getText().toString();
			// selected_categ
			AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
					HomeActivity.this);

			// Setting Dialog Title
			alertDialog2.setTitle("Item Listing Confirmation");

			// Setting Dialog Message
			alertDialog2.setMessage("DO you want to post the need?"
					+ "\n\nItemName:  " + item_name_txt + "\nCategory:   "
					+ selected_categ);
			final EditText input = new EditText(HomeActivity.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			// input.setLayoutParams(lp);
			// alertDialog2.setView(input); // uncomment this line
			// Setting OK Button
			alertDialog2.setCancelable(true);
			alertDialog2.setPositiveButton("Next",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							final Dialog d = new Dialog(HomeActivity.this);

							d.setContentView(R.layout.dialog);
							d.setTitle("Item Location Details");

							// Button dialogButton = (Button)
							// d.findViewById(R.id.btnOK);
							// if button is clicked, close the custom dialog
							TextView text1 = (TextView) d
									.findViewById(R.id.locn_disp);
							text1.setText("Item location!!!");
							TextView text2 = (TextView) d
									.findViewById(R.id.zipcode_disp);
							text2.setText("Item Location: ");

							final EditText locn = (EditText) d
									.findViewById(R.id.locn_txt);
							// locn.setText("600017"); // from db

							CloudEntity profile_entity = app_state.getProfile();
							String default_locn = "";
							if (profile_entity != null) {
								default_locn = profile_entity.get("zipcode")
										.toString();
							}
							locn.setText(default_locn);

							TextView text3 = (TextView) d
									.findViewById(R.id.phone_num);
							String mPhoneNumber = profile_entity.get("phone")
									.toString();
							if (mPhoneNumber.equals("")) {
								TelephonyManager tMgr = (TelephonyManager) getApplicationContext()
										.getSystemService(
												Context.TELEPHONY_SERVICE);
								mPhoneNumber = tMgr.getLine1Number();
							}

							text3.setText("Your Phone num: " + mPhoneNumber);
							final EditText phone_num = (EditText) d
									.findViewById(R.id.phone_no);

							Button okbtn = (Button) d.findViewById(R.id.btnOK);
							Button cancelbtn = (Button) d
									.findViewById(R.id.btnCancel);
							cancelbtn
									.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											d.cancel();
										}
									});
							d.show();
							okbtn.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if (locn.getText().toString().equals("")) {
										locn.setError("Enter valid zipcode");
										return;
									}
									if (phone_num.getText().toString()
											.equals("")) {
										phone_num
												.setError("Enter valid zipcode");
										return;
									}

									d.dismiss();
									setCategId(selected_categ, item_name_txt,
											locn.getText().toString(), "need");
									/*
									 * String[] params = { categId,
									 * item_name_txt, locn.getText().toString()
									 * }; new
									 * AddNeedAsyncTask(HomeActivity.this)
									 * .execute(params); item_name.setText("");
									 */
								}
							});
						}
					});
			alertDialog2.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							//
						}
					});

			alertDialog2.create().show();
			// insert into database
		} else {
			item_name.setError("Enter an item");
		}
	}

	final CloudCallbackHandler<CloudEntity> updtHandler = new CloudCallbackHandler<CloudEntity>() {
		@Override
		public void onComplete(final CloudEntity result) {
			// alert posted need
			// Toast.makeText(getApplicationContext(),
			// "Inserted Need",Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(final IOException exception) {
			handleEndpointException(exception);
		}
	};

	private int setCategId(String selected_categ, final String item_name_txt,
			final String locn, final String type) {

		if (selected_categ != null && !selected_categ.equals("")) {
			CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {

				@Override
				public void onComplete(final List<CloudEntity> results) {
					if (results.size() == 0) {
						// no matching category
						Toast.makeText(getApplicationContext(),
								"catge =" + categId, Toast.LENGTH_LONG).show();

						item_name.setText("");
					} else {
						CloudEntity ce = results.get(0);
						categId = "" + ce.getId();
						// Log.i("category ID=", categId);
						// Toast.makeText(getApplicationContext(),"categ =" +
						// categId, Toast.LENGTH_LONG).show();

						/*
						 * LayoutInflater inflater = getLayoutInflater(); View
						 * toastRoot = inflater.inflate(R.layout.toast, null);
						 * Toast toast = new Toast(getApplicationContext());
						 * toast.setView(toastRoot);
						 * toast.setGravity(Gravity.CENTER_HORIZONTAL |
						 * Gravity.CENTER_VERTICAL, 0, 0);
						 * toast.setDuration(Toast.LENGTH_SHORT); toast.show();
						 */

						String[] params = { categId, item_name_txt, locn };
						if (type.equals("have")) {
							// Toast.makeText(getApplicationContext(),"inside retrieval",
							// Toast.LENGTH_LONG).show();
							new AddHaveAsyncTask(HomeActivity.this)
									.execute(params);
						} else
							new AddNeedAsyncTask(HomeActivity.this)
									.execute(params);
					}
				}
			};

			getCloudBackend().listByProperty("Category", "name", Op.EQ,
					selected_categ, null, 1, Scope.PAST, handler);

		}
		return 1;
	}

	private Need insertNeedDB(String selected_categ,
			final String item_name_txt, final String locn) {

		Need n = null;

		// get the categoryID for the category text from database
		if (selected_categ != null && !selected_categ.equals("")) {

			getCloudBackend().listByProperty("Category", "name", Op.EQ,
					selected_categ, null, 1, Scope.PAST,
					new CloudCallbackHandler<List<CloudEntity>>() {
						@Override
						public void onComplete(List<CloudEntity> results) {
							if (results.size() == 0) {
								// no matching category

							} else {
								CloudEntity ce = results.get(0);
								String categID = "" + ce.getId();
								Log.i("category ID=", categID);
								// create new need and insert into db
								Need newNeed = new Need(item_name_txt, categID,
										"", locn);
								getCloudBackend().insert(newNeed.asEntity(),
										updtHandler);

							}
						}
					});

			return new Need("", "", "", "");
		} else
			return n;
	}

	private void insertHavePost() {
		/*
		 * if (!item_name.getText().toString().equals("")) {
		 * 
		 * } else { item_name.setError("Enter an item"); }
		 */

		// Toast.makeText(getApplicationContext(),
		// spinner1.getSelectedItem().toString(),Toast.LENGTH_LONG).show();

		final String selected_categ = spinner1.getSelectedItem().toString();
		if (!item_name.getText().toString().equals("")) {
			// alert box
			if (spinner1.getSelectedItem() == null
					|| spinner1.getSelectedItem().equals("Select Category")) {

				AlertDialog alertDialog1 = new AlertDialog.Builder(
						HomeActivity.this).create();

				// Setting Dialog Title
				alertDialog1.setTitle("Select Item Category");

				// Setting Dialog Message
				alertDialog1.setMessage("Please select a category");

				// Setting OK Button
				alertDialog1.setButton("OK",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

							}
						});

				// Showing Alert Message
				alertDialog1.show();
			}

			//
			final String item_name_txt = item_name.getText().toString();
			// selected_categ
			AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
					HomeActivity.this);

			// Setting Dialog Title
			alertDialog2.setTitle("Item Listing Confirmation");

			// Setting Dialog Message
			alertDialog2.setMessage("DO you want to post your have?"
					+ "\n\nItemName:  " + item_name_txt + "\nCategory:   "
					+ selected_categ);
			final EditText input = new EditText(HomeActivity.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			// input.setLayoutParams(lp);
			// alertDialog2.setView(input); // uncomment this line
			// Setting OK Button
			alertDialog2.setCancelable(true);
			alertDialog2.setPositiveButton("Next",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							final Dialog d = new Dialog(HomeActivity.this);

							d.setContentView(R.layout.dialog);
							d.setTitle("Item Location Details");

							// Button dialogButton = (Button)
							// d.findViewById(R.id.btnOK);
							// if button is clicked, close the custom dialog
							TextView text1 = (TextView) d
									.findViewById(R.id.locn_disp);
							text1.setText("Confirm item location!!!");
							TextView text2 = (TextView) d
									.findViewById(R.id.zipcode_disp);
							text2.setText("Item Location: ");

							final EditText locn = (EditText) d
									.findViewById(R.id.locn_txt);
							// locn.setText("600017"); // from db
							CloudEntity profile_entity = app_state.getProfile();
							String default_locn = "";
							if (profile_entity != null) {
								default_locn = profile_entity.get("zipcode")
										.toString();
							}
							locn.setText(default_locn);

							Button okbtn = (Button) d.findViewById(R.id.btnOK);
							Button cancelbtn = (Button) d
									.findViewById(R.id.btnCancel);
							cancelbtn
									.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											d.cancel();
										}
									});
							d.show();
							okbtn.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									d.dismiss();

									setCategId(selected_categ, item_name_txt,
											locn.getText().toString(), "have");
									Toast.makeText(getBaseContext(),
											"category " + selected_categ,
											Toast.LENGTH_SHORT).show();
									/*
									 * String[] params = { categId,
									 * item_name_txt, locn.getText().toString()
									 * };
									 * 
									 * new AddHaveAsyncTask(HomeActivity.this)
									 * .execute(params); item_name.setText("");
									 */

								}
							});
						}
					});
			alertDialog2.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							// S
						}
					});

			alertDialog2.create().show();
			// insert into database
		} else {
			item_name.setError("Enter an item");
		}
	}

	private void checkForPreferences() {
		SharedPreferences settings = getSharedPreferences(
				GUESTBOOK_SHARED_PREFS, Context.MODE_PRIVATE);
		boolean showIntro = true;
		if (settings != null) {
			showIntro = settings.getBoolean(SHOW_INTRO_PREFS_KEY, true)
					&& this.showIntro;
		}
		if (showIntro) {
			Intent intent = new Intent(this, IntroductionActivity.class);
			startActivityForResult(intent, INTRO_ACTIVITY_REQUEST_CODE);
		} else {

		}
	}

	private void checkProfilePreferences() {
		SharedPreferences profile_settings = getSharedPreferences(
				PROFILE_SHARED_PREFS, Context.MODE_PRIVATE);
		boolean showProfileSettings = true;
		if (profile_settings != null) {
			showProfileSettings = profile_settings.getBoolean(
					SHOW_PROFILE_SEETINGS_PREFS_KEY, true);
		}
		if (showProfileSettings) {
			// Intent intent = new Intent(this, IntroductionActivity.class);
			// startActivityForResult(intent, INTRO_ACTIVITY_REQUEST_CODE);
			final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
			dlgAlert.setMessage("Update your profile settings?");
			dlgAlert.setTitle("Profile Settings");
			dlgAlert.setPositiveButton("OK", null);
			dlgAlert.setCancelable(true);
			dlgAlert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// open profile intent
							// location zipcode
							// phone number
							// non-editable - gmailid

						}
					});
			dlgAlert.setNegativeButton("Not Now",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// create default profile entity

							createDefaultProfile();
							// after creation of entity update shared prefs

						}
					});
			dlgAlert.create().show();

		} else {
			// load the default profile
			loadProfile();
		}
	}

	final CloudCallbackHandler<List<CloudEntity>> profile_handler = new CloudCallbackHandler<List<CloudEntity>>() {
		@Override
		public void onComplete(final List<CloudEntity> results) {
			// Update mLastLocation only after success so timer will keep
			// trying otherwise
			if (results.size() > 0) {
				CloudEntity ce = results.get(0);
				myProfile = ce;
				app_state.setProfile(myProfile);
			}
		}

		@Override
		public void onError(final IOException exception) {
			handleEndpointException(exception);
		}
	};

	public void loadProfile() {
		CloudQuery cq = new CloudQuery("Profile");
		cq.setFilter(Filter.eq("name", HomeActivity.super.getAccountName()));
		getCloudBackend().list(cq, profile_handler);
	}

	final CloudCallbackHandler<CloudEntity> updateHandler = new CloudCallbackHandler<CloudEntity>() {
		@Override
		public void onComplete(final CloudEntity result) {
			// Update mLastLocation only after success so timer will keep
			// trying otherwise
			mSelf = new Profile(result);
			SharedPreferences.Editor ed = getSharedPreferences(
					PROFILE_SHARED_PREFS, Context.MODE_PRIVATE).edit();// getPreferences(MODE_PRIVATE).edit();
			ed.putBoolean(HomeActivity.SHOW_PROFILE_SEETINGS_PREFS_KEY, false);
			ed.commit();
		}

		@Override
		public void onError(final IOException exception) {
			handleEndpointException(exception);
		}
	};

	private void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}

	public void createDefaultProfile() {
		if (mSelf == null || mSelf.asEntity().getId() == null) {
			// getCloudBackend().clearAllSubscription();
			getCloudBackend().listByProperty("Profile", "name", Op.EQ,
					super.getAccountName(), null, 1, Scope.PAST,
					new CloudCallbackHandler<List<CloudEntity>>() {
						@Override
						public void onComplete(List<CloudEntity> results) {
							if (results.size() == 0) {
								final Profile newProfile = new Profile(
										HomeActivity.super.getAccountName(),
										"", "", false, true);
								getCloudBackend().insert(newProfile.asEntity(),
										updateHandler);
							} else {
								SharedPreferences.Editor ed = getSharedPreferences(
										PROFILE_SHARED_PREFS,
										Context.MODE_PRIVATE).edit();// getPreferences(MODE_PRIVATE).edit();
								ed.putBoolean(
										HomeActivity.SHOW_PROFILE_SEETINGS_PREFS_KEY,
										false);
								ed.commit();
							}
						}
					});
		}

	}

	final CloudCallbackHandler<List<CloudEntity>> updHandler = new CloudCallbackHandler<List<CloudEntity>>() {
		@Override
		public void onComplete(final List<CloudEntity> results) {
			categ = results;
			updateSpinner();
		}

		@Override
		public void onError(final IOException exception) {
			handleEndpointException(exception);
		}
	};

	private void updateSpinner() {
		if (categ != null) {

			// initialize CategoryList class

			app_state.setCategoryList(categ);

			for (int i = 0; i < categ.size(); i++) {
				CloudEntity ce = categ.get(i);
				categories.add(ce.get("name").toString());
			}
		}

		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				R.layout.simple_spinner_item, categories);

		// Drop down layout style - list view with radio button
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attaching data adapter to spinner
		spinner1.setAdapter(dataAdapter);

	}

	private void getCategories() {
		CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				categ = results;
				if (results.size() == 0) {

					List<CloudEntity> ceList = new LinkedList<CloudEntity>();
					for (int i = 0; i < 5; i++) {
						final Category category = new Category(
								category_list.get(i));
						ceList.add(category.asEntity());
					}
					getCloudBackend().insertAll(ceList, updHandler);
					/*
					 * List<CloudEntity> ceList = new LinkedList<CloudEntity>();
					 * for (int i = 0; i < 1; i++) { CloudEntity ce = new
					 * CloudEntity("Category"); ce.put("name",
					 * category_list.get(i)); }
					 * getCloudBackend().insertAll(ceList, updHandler);
					 */
				} else {
					updateSpinner();
				}
			}

			@Override
			public void onError(IOException exception) {
				handleEndpointException(exception);
			}
		};

		getCloudBackend().listByKind("Category", "name", null, 50,
				Scope.FUTURE_AND_PAST, handler);
		getCloudBackend().clearAllSubscription();
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private class AddNeedAsyncTask extends AsyncTask<String, Void, Need> {
		Context context;
		private ProgressDialog pd;
		String need_post_id;
		String item_name_txt;
		CloudEntity need_post;
		String categ_id = categId;

		public AddNeedAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Posting the Need...");
			pd.show();
		}

		protected Need doInBackground(String... params) {
			Need response = null;
			try {
				String categ_id = params[0];
				item_name_txt = params[1];
				String locn = params[2];

				Need newNeed = new Need(item_name_txt, categId, "", locn);
				// response = insertNeedDB(selected_categ, item_name_txt, locn);
				need_post = getCloudBackend().insert(newNeed.asEntity());
				need_post_id = need_post.getId();

				response = newNeed;
			} catch (Exception e) {
				Log.d("Could not Post Need", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(Need n) {
			// Clear the progress dialogand the fields
			pd.dismiss();

			// Display success message to user

			Toast.makeText(getBaseContext(), "Post added succesfully",
					Toast.LENGTH_SHORT).show();
			// matched entities = run the text matching algo
			// 100% match = auto-notify ->needlist tab
			// partial match = show have entities -> select to notify have posts

			Intent nIntent = new Intent(HomeActivity.this, MatchNeedHave.class);
			Bundle nBundle = new Bundle();

			nBundle.putString("need_post_id", need_post_id);
			nBundle.putString("item_name_txt", item_name_txt);
			nBundle.putString("categ_id", categ_id);

			nIntent.putExtras(nBundle);
			startActivity(nIntent);

		}

		protected void onPostExecute(needlist n) {
			// Clear the progress dialog and the fields
			pd.dismiss();
			// editMessage.setText("");
			// editAuthorName.setText("");

			// Display success message to user
			Toast.makeText(getBaseContext(),
					"Post added succesfully " + n.getIdList(),
					Toast.LENGTH_SHORT).show();
			// matched entities = run the text matching algo
			// 100% match = auto-notify -> needlist tab
			// partial match = show have entities -> select to notify have posts

			// findMatchedListings();

		}

	}

	private class getPostsAsyncTask extends AsyncTask<String, Void, Void> {
		Context context;
		private ProgressDialog pd;
		String categ_name;
		String item_name_txt;
		ArrayList<String> items;

		public getPostsAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			items = new ArrayList<String>();
			pd = new ProgressDialog(context);
			pd.setMessage("Retriveing Posts...");
			pd.show();
		}

		protected Void doInBackground(String... params) {
			// List<CloudEntity> response = null;
			try {
				categ_name = params[0];
				item_name_txt = params[1];
				getCloudBackend().listByKind("Need", "itemname", null, 50,
						Scope.FUTURE_AND_PAST, post_handler);

			} catch (Exception e) {
				Log.d("Could not Retrive Posts", e.getMessage(), e);
			}
			// return response;
			return null;
		}

		/*
		 * CloudCallbackHandler<List<CloudEntity>> post_handler = new
		 * CloudCallbackHandler<List<CloudEntity>>() {
		 * 
		 * @Override public void onComplete(List<CloudEntity> results) { if
		 * (results.size() > 0) { for (int i = 0; i < results.size(); i++) {
		 * CloudEntity ce = results.get(i);
		 * items.add(ce.get("itemname").toString()); } } } };
		 */
		public CloudCallbackHandler<List<CloudEntity>> post_handler; // Your
																		// handler

		class LooperThread extends Thread {
			public void run() {
				Looper.prepare();

				post_handler = new CloudCallbackHandler<List<CloudEntity>>() {
					@Override
					public void onComplete(List<CloudEntity> results) {
						if (results.size() > 0) {
							for (int i = 0; i < results.size(); i++) {
								CloudEntity ce = results.get(i);
								items.add(ce.get("itemname").toString());
							}
						}
					}
				};

				Looper.loop();
			}
		}

		protected void onPostExecute() {
			// Clear the progress dialog and the fields
			pd.dismiss();
			// editMessage.setText("");
			// editAuthorName.setText("");

			// Display success message to user
			Toast.makeText(getBaseContext(), "Posts retrieved succesfully",
					Toast.LENGTH_SHORT).show();
			Log.d("items", "" + items);

		}
	}

	private class AddHaveAsyncTask extends AsyncTask<String, Void, Have> {
		Context context;
		private ProgressDialog pd;
		String item_name_txt;
		String categ_id;
		CloudEntity have_post;
		String have_post_id;

		public AddHaveAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setCancelable(false);
			pd.setMessage("Posting the Have...");
			pd.show();
			Toast.makeText(getBaseContext(), "category found " + categId,
					Toast.LENGTH_SHORT).show();
		}

		protected Have doInBackground(String... params) {
			Have response = null;
			try {
				categ_id = params[0];
				item_name_txt = params[1];
				String locn = params[2];

				Have newHave = new Have(item_name_txt, categ_id, "", locn);
				// response = insertHaveDB(selected_categ, item_name_txt, locn);
				have_post = getCloudBackend().insert(newHave.asEntity());
				have_post_id = have_post.getId();
				response = newHave;
			} catch (Exception e) {
				Log.d("Could not Post Have", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(Have n) {
			// Clear the progress dialog and the fields
			pd.dismiss();
			// editMessage.setText("");
			// editAuthorName.setText("");

			// Display success message to user
			Toast.makeText(getBaseContext(), "Post added succesfully ",
					Toast.LENGTH_SHORT).show();
			/*
			 * Intent nIntent = new Intent(HomeActivity.this,
			 * MatchHaveNeed.class); Bundle nBundle = new Bundle();
			 * 
			 * nBundle.putString("have_post_id", have_post_id);
			 * nBundle.putString("item_name_txt", item_name_txt);
			 * nBundle.putString("categ_id", categ_id);
			 * 
			 * nIntent.putExtras(nBundle); startActivity(nIntent);
			 */

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {

		case R.id.action_home:
			intent = new Intent(HomeActivity.this, HomeActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_account:
			return true;
		case R.id.action_list:
			intent = new Intent(HomeActivity.this, TabView.class);
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
