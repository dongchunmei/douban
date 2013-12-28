package com.example.douban;

import com.example.douban.util.ConfigData;
import com.example.douban.util.NetUtil;
import com.example.douban.R;
import com.google.gdata.client.douban.DoubanService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseListActivity extends ListActivity {
	protected int DOAUTH = 5;
	protected SharedPreferences sharedata;

	protected void initDouban() {
		String accessToken = sharedata.getString(ConfigData.ACCESSTOKEN, null);
		String tokenSecret = sharedata.getString(ConfigData.TOKENSECRET, null);
		if(NetUtil.doubanService == null)
			NetUtil.doubanService = new DoubanService("DoubanYP",
					NetUtil.apiKey, NetUtil.secret);
		if (accessToken != null && tokenSecret != null) {
			NetUtil.doubanService.setAccessToken(accessToken, tokenSecret);
			NetUtil.isAuthed = true;
		} else {
			NetUtil.isAuthed = false;
		}
	}

	protected void doAuth(boolean showDialog) {
		if(showDialog){
		new AlertDialog.Builder(BaseListActivity.this).setTitle("提示")
				.setMessage("用户未登录或授权已过期，请先登录！")
				.setPositiveButton("登录", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						loadUrl();
					}
				}).show();
		}
		else{
			loadUrl();
		}
	}
	
	void loadUrl() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				String url = NetUtil.doubanService
						.getAuthorizationUrl(NetUtil.callback);
				NetUtil.requestTokenSecret = NetUtil.doubanService.getRequestTokenSecret();
				return url;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(result));
				startActivityForResult(intent, DOAUTH);
			}

		}.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sharedata == null)
			sharedata = getSharedPreferences(ConfigData.TAG, 0);
		initDouban();
	}

	public void showProgressBar() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		loading.setLayoutAnimation(controller);
	}

	public void closeProgressBar() {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);

		loading.setLayoutAnimation(controller);

		loading.setVisibility(View.INVISIBLE);
	}

	public void showProgressBar(String title) {
		TextView loading = (TextView) findViewById(R.id.txt_loading);
		loading.setText(title);
		showProgressBar();
	}

}
