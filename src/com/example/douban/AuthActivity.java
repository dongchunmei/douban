package com.example.douban;

import java.io.IOException;
import java.util.ArrayList;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.douban.util.ConfigData;
import com.example.douban.util.NetUtil;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

public class AuthActivity extends BaseActivity {
	public static final int SUCCESS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);

		TextView myTitle = (TextView) this.findViewById(R.id.myTitle);
		myTitle.setText("µÇÂ¼ÊÚÈ¨");
		((ImageButton) this.findViewById(R.id.back_button))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AuthActivity.this.finish();
					}
				});
		
		Uri uri = this.getIntent().getData();
		if(uri != null)
			doAuth(uri);
	}

	void doAuth(final Uri uri) {
		new AsyncTask<String, Void, String[]>() {

			@Override
			protected String[] doInBackground(String... args) {
				String request_token = uri.getQueryParameter("oauth_token");
				return saveAccessToken(request_token, NetUtil.requestTokenSecret);
			}

			@Override
			protected void onPostExecute(String[] result) {
				super.onPostExecute(result);
				if (result != null) {
					Toast toast = null;
					toast = Toast.makeText(AuthActivity.this, "µÇÂ¼³É¹¦",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					NetUtil.isAuthed = true;

					setResult(SUCCESS);
					finish();
				} else {
					doAgain();
					NetUtil.isAuthed = false;
				}
			}

		}.execute("");
	}

	private void doAgain() {
		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		Button btn = new Button(getApplicationContext());
		btn.setText("µÇÂ¼Ê§°Ü,ÇëÖØÊÔ");
		btn.setLayoutParams(params);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				doAuth(false);
			}
		});
		RelativeLayout layout = (RelativeLayout) AuthActivity.this
				.findViewById(R.id.content);
		layout.addView(btn);
	}

	String[] saveAccessToken(String requestToken, String requestTokenSecret) {
		String[] result = null;

		NetUtil.doubanService.setRequestToken(requestToken);
		NetUtil.doubanService.setRequestTokenSecret(requestTokenSecret);

		try {
			ArrayList<String> list = NetUtil.doubanService.getAccessToken();
			if (list == null) {
				return null;
			}
			result = new String[3];
			result[0] = list.get(0);
			result[1] = list.get(1);

			NetUtil.doubanService.setAccessToken(result[0], result[1]);
			UserEntry user = NetUtil.doubanService.getAuthorizedUser();
			result[2] = user.getUid();
			System.out.println(result[2]);
			sharedata.edit().putString(ConfigData.ACCESSTOKEN, result[0])
					.putString(ConfigData.TOKENSECRET, result[1])
					.putString(ConfigData.UID, result[2]).commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return result;
	}

}
