package com.example.douban;

import com.example.douban.util.ConfigData;
import com.example.douban.util.NetUtil;
import com.example.douban.R;
import com.google.gdata.client.douban.DoubanService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseActivity extends Activity {
	protected int DOAUTH = 5;

	protected ProgressDialog pd;
	protected SharedPreferences sharedata;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sharedata = getSharedPreferences(ConfigData.TAG, 0);
	}

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
		new AlertDialog.Builder(BaseActivity.this).setTitle("提示")
				.setMessage("用户未登录或授权已过期，请先登录！")
				.setPositiveButton("登录", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
//						Intent intent = new Intent(BaseActivity.this,
//								AuthActivity.class);
//						String url = NetUtil.doubanService
//								.getAuthorizationUrl(NetUtil.callback);
//						NetUtil.requestTokenSecret = NetUtil.doubanService.getRequestTokenSecret();
//						Intent intent = new Intent(Intent.ACTION_VIEW);
//						intent.setData(Uri.parse(url));
//						startActivityForResult(intent, DOAUTH);
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
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		if (sharedata == null)
			sharedata = getSharedPreferences(ConfigData.TAG, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeDialog();
	}

	// 退出
	protected void doExit() {
		new AlertDialog.Builder(BaseActivity.this)
				.setTitle("提示")
				.setMessage("确定要退出我的豆瓣吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						finish();
					}
				})
				.setNeutralButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}

				}).show();

	}

	// 加载对话框
	public void showDialog() {
		if (pd == null)
			pd = new ProgressDialog(BaseActivity.this);
		pd.setTitle("信息");
		pd.setMessage("加载数据中...");
		pd.show();
	}

	public void closeDialog() {
		if (pd != null)
			pd.dismiss();
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
