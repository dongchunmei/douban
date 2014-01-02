package com.example.douban;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.douban.entity.Book;
import com.example.douban.entity.Review;
import com.example.douban.util.NetUtil;
import com.example.douban.R;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.douban.ReviewEntry;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.util.ServiceException;

public class ReviewEditActivity extends BaseActivity {
	private Review review;
	private EditText reviewContent;
	private RatingBar reviewRatingbar;
	private EditText reviewTitle;
	private Book book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_edit);

		this.reviewTitle = (EditText) findViewById(R.id.EdtReviewTitle);
		this.reviewContent = (EditText) findViewById(R.id.EdtReviewContent);
		this.reviewRatingbar = (RatingBar) findViewById(R.id.ratingbar);
		Bundle extras = getIntent().getExtras();
		book = extras != null ? (Book) extras.getSerializable("book") : null;
		review = extras != null ? (Review) extras.getSerializable("review")
				: null;
		String title = "评论《" + this.book.getTitle() + "》";
		((TextView) findViewById(R.id.myTitle)).setText(title);
		initView();
		setData();
		
		DOAUTH = 0;
	}

	private void initView() {
		Button btnSave = (Button) findViewById(R.id.btnSave);
		// 保存
		btnSave.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				doPost();
			}

		});
		// 取消
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				finish();
			}

		});
		// 返回
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				goBack();
			}

		});

	}

	private void setData() {
		if (this.review != null) {
			reviewTitle.setText(review.getTitle());
			reviewContent.setText(review.getContent());
			reviewRatingbar.setRating(review.getRating());
		}
	}

	private void doPost() {
		String title = this.reviewTitle.getText().toString().trim();
		String content = this.reviewContent.getText().toString().trim();
		int rating = (int) this.reviewRatingbar.getRating();
		if ("".equals(title)) {
			Toast.makeText(this, "评论标题不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (rating == 0) {
			Toast.makeText(this, "评分不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		if ("".equals(content)) {
			Toast.makeText(this, "评论内容不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (content.length() < 50) {
			Toast.makeText(this, "评论内容不能小于50个字符！", Toast.LENGTH_SHORT).show();
			return;
		}

		new AsyncTask<Void, Void, Boolean>() {

			private boolean goAuth;
			
			@Override
			protected void onPostExecute(Boolean result) {
				if(goAuth){
					closeDialog();
					doAuth(true);
					return;
				}
				if (result) {
					closeDialog();
					String message = "";
					if (review == null) {
						message = "评论新增成功！";
					} else {
						message = "评论修改成功！";
					}
					Toast.makeText(ReviewEditActivity.this, message,
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					String message = "";
					if (review == null) {
						message = "评论新增失败！";
					} else {
						message = "评论修改失败！";
					}
					Toast.makeText(ReviewEditActivity.this, message,
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			protected void onPreExecute() {
				goAuth = false;
				showDialog();
			}

			@Override
			protected Boolean doInBackground(Void... arg0) {
				String title = reviewTitle.getText().toString().trim();
				String content = reviewContent.getText().toString().trim();
				int ratingValue = (int) reviewRatingbar.getRating();
				Rating rating = new Rating();
				rating.setValue(ratingValue);
				try {
					// 更新
					if (review != null) {
						ReviewEntry reviewEntry = new ReviewEntry();
						reviewEntry.setId(review.getId());
						NetUtil.doubanService.updateReview(reviewEntry,
								new PlainTextConstruct(title),
								new PlainTextConstruct(content), rating);
						return true;
					}
					// 新增
					else {
						SubjectEntry subjectEntry = new SubjectEntry();
						subjectEntry.setId(book.getId());
						NetUtil.doubanService.createReview(subjectEntry,
								new PlainTextConstruct(title),
								new PlainTextConstruct(content), rating);
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					String cause = e.getMessage();
					if(cause.equals("Unauthorized"))
						goAuth = true;
					return false;
				}
			}

		}.execute();
	}

	protected void goBack() {
		String title = this.reviewTitle.getText().toString().trim();
		String content = this.reviewContent.getText().toString().trim();
		if (("".equals(title)) && ("".equals(content))) {
			finish();
			return;
		}

		if (this.review != null) {
			String reviewTitle = this.review.getTitle();
			String reviewContent = this.review.getContent();
			if (title.equals(reviewTitle) && content.equals(reviewContent)) {
				finish();
				return;
			}
		}
		new AlertDialog.Builder(ReviewEditActivity.this)
				.setTitle("提示")
				.setMessage("数据未保存，确定要退出吗？")
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		goBack();
	}
	
	

}
