package com.example.douban;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.example.douban.entity.Book;
import com.example.douban.entity.BookListAdapter;
import com.example.douban.util.ConvertUtil;
import com.example.douban.util.NetUtil;
import com.example.douban.R;
import com.google.gdata.data.douban.SubjectFeed;

public class DoubanActivity extends BaseActivity {
	private List<Book> books = new ArrayList<Book>();
	private EditText searchText;
	private int bookIndex = 1;
	private int count = 6; // 每次获取数目
	private boolean isFilling = false; // 判断是否正在获取数据
	protected BookListAdapter bookListAdapter;

	private int bookTotal; // 最大条目数
	private InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_douban);

		initView();

		initDouban();
	}

	private void initView() {
		searchText = (EditText) this.findViewById(R.id.search_text);
		searchText.setHint(R.string.book_search_hint);
		ImageButton searchButton = (ImageButton) this
				.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				hideKeyboaard();
				if (checkNetWorkStatus())
					doSearch();
				else {
					Toast toast = Toast.makeText(getApplicationContext(), "程序未联网！",
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});

		ListView listView = (ListView) this.findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(DoubanActivity.this,
						BookViewActivity.class);
				Book book = books.get(position);
				i.putExtra("book", book);
				startActivity(i);
			}
		});

		listView.setOnScrollListener(new OnScrollListener() {

			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						loadRemnantListItem();
					}
				}
			}
		});

	}
	
	public void hideKeyboaard() {
		if(imm == null)
			imm = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				this.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 获取更多条目
	private void loadRemnantListItem() {
		if (isFilling) {
			return;
		}
		bookIndex = bookIndex + count;
		if (bookIndex > bookTotal) {
			return;
		}

		RelativeLayout loading = (RelativeLayout) this
				.findViewById(R.id.loading);
		LayoutParams lp = (LayoutParams) loading.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		loading.setLayoutParams(lp);

		fillData();
	}

	private boolean checkNetWorkStatus() {
		boolean netSataus = false;

		ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cwjManager.getActiveNetworkInfo();
		if (info != null) {
			netSataus = info.isAvailable();
		}
		return netSataus;
	}

	private void doSearch() {
		String searchTitle = searchText.getText().toString();
		if ("".equals(searchTitle.trim())) {
			return;
		}
		bookIndex = 1;
		bookListAdapter = null;
		books.clear();
		fillData();
	}

	private AsyncTask<String, Void, SubjectFeed> task;

	private void fillData() {
		if (task != null)
			task.cancel(true);
		task = new AsyncTask<String, Void, SubjectFeed>() {

			@Override
			protected SubjectFeed doInBackground(String... args) {

				String title = searchText.getText().toString();
				SubjectFeed feed = null;
				try {
					feed = NetUtil.doubanService.findBook(title, "", bookIndex,
							count);
					bookTotal = feed.getTotalResults();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return feed;
			}

			@Override
			protected void onPostExecute(SubjectFeed result) {
				super.onPostExecute(result);
				closeProgressBar();
				if (result != null) {
					ListView listView = (ListView) DoubanActivity.this
							.findViewById(R.id.list);
					books.addAll(ConvertUtil.ConvertSubjects(result));
					if (bookListAdapter == null) {
						bookListAdapter = new BookListAdapter(
								DoubanActivity.this, listView, books);
						listView.setAdapter(bookListAdapter);
					} else {
						bookListAdapter.notifyDataSetChanged();
					}

				}
				isFilling = false;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				isFilling = true;
				showProgressBar();
			}

		}.execute("");
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doExit();
			return true;
		}
		return true;
	}

}
