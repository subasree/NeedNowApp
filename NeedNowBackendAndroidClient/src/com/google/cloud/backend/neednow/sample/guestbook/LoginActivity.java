package com.google.cloud.backend.neednow.sample.guestbook;

import java.io.IOException;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.cloud.backend.android.mobilebackend.Mobilebackend;
import com.google.cloud.backend.neednow.R;
import com.google.cloud.backend.neednow.core.CloudBackendAsync;

import com.google.cloud.backend.neednow.core.CloudEndpointUtils;
import com.google.cloud.backend.neednow.core.CloudQuery.Order;
import com.google.cloud.backend.neednow.core.CloudQuery.Scope;
import com.google.cloud.backend.neednow.core.CloudCallbackHandler;
import com.google.cloud.backend.neednow.core.CloudEntity;
import com.google.cloud.backend.neednow.core.Consts;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity  {

	private Button signin_btn,btnData;
	private GoogleAccountCredential mCredential;
	private Mobilebackend endpoint;
	private CloudBackendAsync cloudbackend;
	static final int REQUEST_ACCOUNT_PICKER = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		

		// endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();

		// Create the view
		signin_btn = (Button) findViewById(R.id.btnLogin);
		btnData = (Button) findViewById(R.id.btnData);
		
		signin_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//new DoSomethingAsync(this, endpoint).execute();
				chooseAccount();
		        
			}
		});
		
		btnData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cloudbackend.setCredential(mCredential);
				
				Log.i("Account info:",mCredential.getSelectedAccountName());
				
				CloudEntity ce = new CloudEntity("Profile");
		        ce.put("city", "Chennai");

		        
		        // create a response handler that will receive the result or an error
		        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
		            @Override
		            public void onComplete(final CloudEntity result) {
		            	TextView t = (TextView) findViewById(R.id.msgtxt);
		                t.setText(result.get("city").toString());
		            }

		            
		            @Override
		            public void onError(final IOException exception) {
		                handleEndpointException(exception);
		            }
		        };

		        // execute the insertion with the handler
		        cloudbackend.insert(ce, handler);
			}
		});
		
		chooseAccount();
		
	}

	private void chooseAccount() {
		mCredential = GoogleAccountCredential.usingAudience(this,
				Consts.AUTH_AUDIENCE);
		
		startActivityForResult(mCredential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);

		cloudbackend = new CloudBackendAsync(this.getApplicationContext());
		cloudbackend.setCredential(mCredential);
		
		endpoint = cloudbackend.getMBSEndpoint();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					mCredential.setSelectedAccountName(accountName);
					// User is authorized.
				}
			}
			break;
		}
	}

	
	 private void handleEndpointException(IOException e) {
	        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	     
	    }
	
}
