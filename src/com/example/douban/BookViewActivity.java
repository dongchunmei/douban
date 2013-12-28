package com.example.douban;

import java.io.IOException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.douban.entity.Book;
import com.example.douban.util.AsyncImageLoader.ImageCallback;
import com.example.douban.util.ConvertUtil;
import com.example.douban.util.NetUtil;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.util.ServiceException;

public class BookViewActivity extends BaseActivity {
	private TextView txtTitle;
	private TextView txtDescription;
	private TextView txtSummary;
	private ImageView bookImage;
	private RatingBar ratingBar;
	private Button showReview;
	private Book book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_view);

		Bundle extras = getIntent().getExtras();
		book = extras != null ? (Book) extras.getSerializable("book") : null;

		txtTitle = (TextView) findViewById(R.id.book_title);
		txtDescription = (TextView) findViewById(R.id.book_description);
		txtSummary = (TextView) findViewById(R.id.book_summary);
		bookImage = (ImageView) findViewById(R.id.book_img);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);

		TextView txtInfo = (TextView) findViewById(R.id.txtInfo);

		TextView titleText = (TextView) findViewById(R.id.myTitle);
		titleText.setText("《" + book.getTitle() + "》");

		if (book != null) {
			fillData(book.getUrl());
			titleText.setText("《" + book.getTitle() + "》");
			txtInfo.setText(R.string.bookInfo);
		}

		// 回退按钮
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);

		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}

		});

		// 查看评论按钮
		showReview = (Button) findViewById(R.id.btnShowComment1);
		OnClickListener showReviewClicklistener = new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(BookViewActivity.this,
						ReviewActivity.class);
				i.putExtra("book", book);
				startActivity(i);
			}

		};
		showReview.setOnClickListener(showReviewClicklistener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == DOAUTH && resultCode == AuthActivity.SUCCESS) {
			Intent intent = new Intent(BookViewActivity.this,
					ReviewEditActivity.class);
			intent.putExtra("book", book);
			startActivity(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void fillData(String bookId) {

		new AsyncTask<String, Void, SubjectEntry>() {

			@Override
			protected SubjectEntry doInBackground(String... args) {
				String bookId = args[0];
				SubjectEntry entry = null;

				try {
					entry = NetUtil.doubanService.getBook(bookId);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return entry;
			}

			@Override
			protected void onPostExecute(SubjectEntry result) {
				super.onPostExecute(result);
				if (result != null) {
					closeDialog();
					Book book = ConvertUtil.convertOneSubject(result);
					txtTitle.setText(book.getTitle());
					txtDescription.setText(book.getDescription());
					String summary = "\t\t" + book.getSummary();

					if (!"".equals(book.getAuthorIntro())) {
						summary = summary + "\n\n作者简介:" + book.getAuthorIntro();
					}

					if (!"".equals(book.getTagsToString())) {
						summary = summary + "\n\n标签:" + book.getTagsToString();
					}

					txtSummary.setText(summary);

					ratingBar.setRating(book.getRating());
					ratingBar.setVisibility(View.VISIBLE);
					String imageUrl = book.getImgUrl();
					Drawable drawable = NetUtil.asyncImageLoader.loadDrawable(
							imageUrl, new ImageCallback() {
								public void imageLoaded(Drawable imageDrawable,
										String imageUrl) {
									bookImage.setImageDrawable(imageDrawable);
								}
							});
					if (drawable != null) {
						bookImage.setImageDrawable(drawable);
					} else {
						bookImage.setImageResource(R.drawable.book);
					}
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showDialog();
			}

		}.execute(bookId);

	}

}
