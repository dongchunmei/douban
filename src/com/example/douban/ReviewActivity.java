package com.example.douban;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.douban.entity.Book;
import com.example.douban.entity.Review;
import com.example.douban.util.ConvertUtil;
import com.example.douban.util.NetUtil;
import com.example.douban.R;
import com.google.gdata.data.douban.ReviewFeed;
import com.google.gdata.util.ServiceException;

public class ReviewActivity extends BaseListActivity {
	private static final String ORDERBY_TIME = "time";// 按评分排序
	private static final String ORDERBY_SCORE = "score";// 按评分排序
	private String orderby;// 排序
	private boolean isOrderbyTime;
	private List<Review> reviews = new ArrayList<Review>();
	private ReviewListAdapter listAdapter;
	private int index = 1;
	private int count = 6; // 每次获取数目
	private int total; // 最大条目数
	private boolean isFilling = false; // 判断是否正在获取数据
	private Book book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review);

		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Button btnRuleScore = (Button) findViewById(R.id.btnRuleScore);
		isOrderbyTime = false;
		btnRuleScore.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					System.out.println("score clicked");
					if (!isOrderbyTime)
						return;
					isOrderbyTime = false;
					orderby = ORDERBY_SCORE;
					reviews.clear();
					index = 1;
					getListView().setSelection(0);
					fillDataBySubject(book);
				}
			}
		});

		findViewById(R.id.btnRuleTime).setOnFocusChangeListener(
				new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						if (hasFocus) {
							System.out.println("time clicked");
							if (isOrderbyTime)
								return;
							isOrderbyTime = true;
							orderby = ORDERBY_TIME;
							reviews.clear();
							index = 1;
							getListView().setSelection(0);
							fillDataBySubject(book);
						}
					}
				});

		findViewById(R.id.btn_book_new_review).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (NetUtil.isAuthed) {
							Intent intent = null;
							intent = new Intent(ReviewActivity.this,
									ReviewEditActivity.class);
							intent.putExtra("book", book);
							startActivity(intent);
						} else {
							doAuth(true);
						}
					}
				});

		Bundle extras = getIntent().getExtras();
		TextView titleView = (TextView) findViewById(R.id.myTitle);
		book = extras != null ? (Book) extras.getSerializable("book") : null;
		String title = "《" + book.getTitle() + "》的评论";
		titleView.setText(title);
		fillDataBySubject(book);
		getListView().setOnScrollListener(new OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						loadRemnantListItem(book);
					}
				}
			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == DOAUTH && resultCode == AuthActivity.SUCCESS) {
			Intent intent = new Intent(ReviewActivity.this,
					ReviewEditActivity.class);
			intent.putExtra("book", book);
			startActivity(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 加载更多评论
	private void loadRemnantListItem(Book book) {
		if (isFilling) {
			return;
		}
		index = index + count;
		if (index > total) {
			return;
		}
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		LayoutParams lp = (LayoutParams) loading.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		loading.setLayoutParams(lp);

		fillDataBySubject(book);
	}

	AsyncTask<Book, Void, ReviewFeed> task;

	// 获取评论数据
	private void fillDataBySubject(final Book book) {
		if (task != null)
			task.cancel(true);
		task = new AsyncTask<Book, Void, ReviewFeed>() {

			@Override
			protected ReviewFeed doInBackground(Book... args) {
				ReviewFeed feed = null;
				Book book1 = args[0];
				try {
					feed = NetUtil.doubanService.getBookReviews(book1.getId(),
							index, count, orderby);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return feed;
			}

			@Override
			protected void onPostExecute(ReviewFeed feed) {
				super.onPostExecute(feed);
				closeProgressBar();
				if (feed != null) {
					total = feed.getTotalResults();
					reviews.addAll(ConvertUtil.ConvertReviews(feed, book));
					if (listAdapter == null) {
						listAdapter = new ReviewListAdapter(
								ReviewActivity.this, getListView(), reviews);
						setListAdapter(listAdapter);
					} else {
						listAdapter.notifyDataSetChanged();
					}

					if (reviews.size() == 0) {
						Toast.makeText(ReviewActivity.this, "没有找到相关评论！",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(ReviewActivity.this, "数据加载失败！",
							Toast.LENGTH_SHORT).show();
				}
				isFilling = false;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				isFilling = true;
			}

		}.execute(book);

	}

	// 选中事件
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, ReviewViewActivity.class);
		Review review = reviews.get(position);
		i.putExtra("review", review);
		startActivity(i);
	}

	public class ReviewListAdapter extends BaseAdapter {

		private List<Review> reviews;
		private LayoutInflater mInflater;

		public ReviewListAdapter(Context context, ListView listView,
				List<Review> reviews) {
			this.reviews = reviews;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return reviews.size();
		}

		public Object getItem(int i) {
			return reviews.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(int i, View view, ViewGroup vg) {
			if (view == null) {
				view = mInflater.inflate(R.layout.review_item, null);
			}

			Review review = reviews.get(i);

			TextView txtTitle = (TextView) view.findViewById(R.id.review_title);
			TextView txtUpdated = (TextView) view
					.findViewById(R.id.review_updated);
			txtUpdated.setText(review.getUpdated());
			TextView txtSummary = (TextView) view
					.findViewById(R.id.review_summary);
			RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
			TextView txtAuthorName = (TextView) view
					.findViewById(R.id.author_name);

			txtTitle.setText(review.getTitle());
			String summary = review.getSummary();
			summary = summary.replaceAll("\\\n", "");
			summary = summary.replaceAll("\\\t", "");
			summary = summary.replaceAll(" ", "");
			txtSummary.setText(summary);
			ratingBar.setRating(review.getRating());
			txtAuthorName.setText("评论人:" + review.getAuthorName());
			return view;
		}
	}

}
