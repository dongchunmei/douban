package com.example.douban.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.douban.entity.Book;
import com.example.douban.entity.Review;
import com.google.gdata.client.douban.DoubanService;

public class NetUtil {
	private static final String reviewUrl = "http://www.douban.com/review/";
	// APIKEY
	public static final String apiKey = "042cbf0c503dee720c863d24f0b9cb56";
	// secret
	public static final String secret = "4b08025f5bc7f1a2";
	public static final String callback = "app://dongchunmei";

	public static String requestTokenSecret;

	// 图片加载管理器
	public static AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
	public static DoubanService doubanService;
	public static boolean isAuthed;

	/**
	 * 获取豆瓣最受欢迎评论
	 * 
	 * @param start
	 * @return
	 */
	public static List<Review> getBestReviews(int start) {
		int max = 50;
		List<Review> reviews = new ArrayList<Review>();
		// 最多50个
		if (start >= max) {
			return reviews;
		}
		try {
			URL uri = new URL("http://book.douban.com/review/best/?start="
					+ start);
			HttpURLConnection httpConn = (HttpURLConnection) uri
					.openConnection();
			httpConn.setDoInput(true);
			httpConn.connect();
			InputStream is = httpConn.getInputStream();
			Source source = new Source(is);

			Element divContent = source.getElementById("content");
			for (Element item : divContent
					.getAllElementsByClass("tlst clearfix")) {
				String reviewTitle = item.getFirstElement("a")
						.getTextExtractor().toString();
				String reviewUrl = item.getFirstElementByClass("j a_unfolder")
						.getAttributeValue("href");

				Element subjectElement = item.getFirstElementByClass("ilst")
						.getContent().getFirstElement();
				String subjectUrl = subjectElement.getAttributeValue("href");
				String subjectTitle = subjectElement.getAttributeValue("title");

				Element authorElement = item.getFirstElementByClass("starb")
						.getContent().getFirstElement();
				String authorUrl = authorElement.getAttributeValue("href");
				String authorName = authorElement.getTextExtractor().toString();
				String summary = item.getAllElements("div").get(1)
						.getTextExtractor().toString();

				float rating = 0;
				for (int i = 1; i <= 5; i++) {
					String cssClass = "stars" + i + " stars";
					if (item.getAllElementsByClass(cssClass).size() > 0) {
						rating = i;
						break;
					}
				}

				Review review = new Review();

				// 处理作者URL
				authorUrl = authorUrl.replaceFirst("book", "api");
				authorUrl = authorUrl.substring(0, authorUrl.length() - 1);
				review.setAuthorId(authorUrl);
				review.setAuthorName(authorName);

				// 处理评论URL
				reviewUrl = reviewUrl.replaceFirst("book", "api");
				reviewUrl = reviewUrl.substring(0, reviewUrl.length() - 1);
				review.setUrl(reviewUrl);

				review.setTitle(reviewTitle);
				review.setSummary(summary);
				review.setRating(rating);
				review.setSelf(false);// 全部作为别人的评论，不可编辑

				Book book = new Book();
				subjectUrl = subjectUrl.replaceFirst("book", "api");
				subjectUrl = subjectUrl.substring(0, subjectUrl.length() - 1);
				book.setUrl(subjectUrl);
				book.setTitle(subjectTitle);
				review.setSubject(book);
				reviews.add(review);
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return reviews;

	}

	// 获取评论全文
	public static Review getReviewContentAndComments(Review review)
			throws IOException {
		HttpGet request = new HttpGet(reviewUrl + review.getId() + "/");
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			Source source = new Source(response.getEntity().getContent());
			Element contentDivElement = source.getElementById("content");
			for (Element e : contentDivElement.getAllElements("img")) {
				if ("pil".equals(e.getAttributeValue("class"))) {
					review.setAuthorImageUrl(e.getAttributeValue("src"));
					review.setAuthorImage(getNetImage(review
							.getAuthorImageUrl()));
					break;
				}
			}

			for (Element e : contentDivElement.getAllElements("span")) {
				if ("v:description".equals(e.getAttributeValue("property"))) {
					String content = e.getContent().toString();
					review.setContent(content);
					break;
				}
			}

			Element commentsDiv = source.getElementById("comments");
			if (commentsDiv != null) {
				String comments = commentsDiv.getContent().toString();
				review.setComments(comments);
			}
		}
		return review;
	}

	// 获取网络上的图片
	public static Bitmap getNetImage(String url) throws IOException {
		try {
			URL imageUri = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) imageUri
					.openConnection();
			httpConn.setDoInput(true);
			httpConn.connect();
			InputStream is = httpConn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			Bitmap bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("获取验证码图片失败！");
		}
	}

}
