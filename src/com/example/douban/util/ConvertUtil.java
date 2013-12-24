package com.example.douban.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.douban.entity.Book;
import com.example.douban.entity.Review;
import com.google.gdata.data.Person;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.ReviewEntry;
import com.google.gdata.data.douban.ReviewFeed;
import com.google.gdata.data.douban.Subject;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.data.douban.SubjectFeed;
import com.google.gdata.data.douban.UserEntry;

public class ConvertUtil {
	private static ArrayList<String> names;
	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",Locale.US);

	static {
		names = new ArrayList<String>();
		names.add("authors");
		names.add("pubdate");
		names.add("publisher");
		names.add("price");
		names.add("pages");
		names.add("binding");
	}

	/**
	 * 获取用户的评论列表
	 */
	public static List<Review> ConvertReviews(ReviewFeed feed, Book book,
			UserEntry ue) {
		List<Review> reviews = ConvertReviews(feed, null);
		for (Review review : reviews) {
			review.setAuthorId(ue.getUid());
			review.setAuthorName(ue.getTitle().getPlainText());
			review.setAuthorImageUrl(ue.getLink("icon", null).getHref());
		}
		return reviews;
	}

	/**
	 * 获取图书列表
	 * 
	 * @param subjectFeed
	 * @return
	 */
	public static List<Book> ConvertSubjects(SubjectFeed subjectFeed) {
		List<Book> books = new ArrayList<Book>();

		for (SubjectEntry entry : subjectFeed.getEntries()) {
			Book book = new Book();
			book.setTitle(entry.getTitle().getPlainText());
			book.setDescription(getDescription(entry));
			book.setUrl(entry.getId());
			book.setImgUrl(entry.getLink("image", null).getHref());
			book.setRating(entry.getRating().getAverage() / 2);
			books.add(book);
		}
		return books;
	}

	/**
	 * 转换图书条目
	 * 
	 * @param entry
	 * @return
	 */
	public static Book convertOneSubject(SubjectEntry entry) {
		Book book = new Book();
		if (entry.getTitle() != null) {
			book.setTitle(entry.getTitle().getPlainText());
		}
		book.setDescription(getDescription(entry));
		book.setUrl(entry.getId());
		if (entry.getLink("image", null) != null) {
			book.setImgUrl(entry.getLink("image", null).getHref());
		}
		if (entry.getRating() != null) {
			book.setRating(entry.getRating().getAverage() / 2);
		}
		if (entry.getSummary() != null) {
			book.setSummary(entry.getSummary().getPlainText());
		} else {
			book.setSummary("");
		}
		book.setTags(entry.getTags());
		book.setAuthorIntro(getAuthorInfo(entry));
		return book;
	}

	/**
	 * 获取评论列表
	 */
	public static List<Review> ConvertReviews(ReviewFeed feed, Book book) {
		List<Review> reviews = new ArrayList<Review>();
		for (ReviewEntry entry : feed.getEntries()) {
			Review review = new Review();

			if (entry.getId() != null) {
				review.setUrl(entry.getId());
			}

			if (entry.getTitle() != null) {
				review.setTitle(entry.getTitle().getPlainText());
			}
			if (entry.getSummary() != null) {
				review.setSummary(entry.getSummary().getPlainText());
			}

			if (entry.getRating() != null) {
				review.setRating(entry.getRating().getValue());
			}

			if (entry.getUpdated() != null) {
				review.setUpdated(df.format(new Date(entry.getUpdated()
						.getValue())));
			}

			List<Person> authors = entry.getAuthors();
			if (authors != null && authors.size() > 0) {
				Person author = entry.getAuthors().get(0);
				review.setAuthorName(author.getName());
				review.setAuthorId(author.getUri());
			}

			// 如果不存在subject，则获取
			if (book == null) {
				book = new Book();
				com.google.gdata.data.douban.Subject googleSubject = entry
						.getSubjectEntry();
				book.setTitle(googleSubject.getTitle().getPlainText());
				book.setDescription(getDescription(googleSubject));
				book.setUrl(googleSubject.getId());
			}
			review.setSubject(book);
			reviews.add(review);
		}
		return reviews;
	}

	// 组装图书描述信息
	private static String getDescription(Subject subject) {
		String description = "";
		List<Attribute> attributes = subject.getAttributes();
		String authors = "";
		for (Person author : subject.getAuthors()) {
			authors += "," + author.getName();
		}
		if (authors.length() > 0) {
			authors = authors.substring(1);
		}

		Map<String, String> map = new HashMap<String, String>();

		for (Attribute attribute : attributes) {
			if (names.contains(attribute.getName())) {
				map.put(attribute.getName(), attribute.getContent());
			}
		}
		map.put("authors", authors);
		for (String name : names) {
			if (map.get(name) != null) {
				if ("price".equals(name)) {
					description += "\n" + map.get(name) + "元";
				} else if ("pages".equals(name)) {
					description += "\n" + map.get(name) + "页";
				} else {
					description += "\n" + map.get(name);
				}
			}
		}
		if (description.length() > 0) {
			description = description.substring(1);
		}

		return description;
	}

	/**
	 * 获取图书描述信息
	 * 
	 * @param entry
	 * @return
	 */
	private static String getDescription(SubjectEntry entry) {
		String description = "";
		List<Attribute> attributes = entry.getAttributes();
		String authors = "";
		for (Person author : entry.getAuthors()) {
			authors += "," + author.getName();
		}
		if (authors.length() > 0) {
			authors = authors.substring(1);
		}

		Map<String, String> map = new HashMap<String, String>();

		for (Attribute attribute : attributes) {
			if (names.contains(attribute.getName())) {
				map.put(attribute.getName(), attribute.getContent());
			}
		}
		map.put("authors", authors);
		for (String name : names) {
			if (map.get(name) != null) {
				if ("price".equals(name)) {
					description += "\n" + map.get(name) + "元";
				} else if ("pages".equals(name)) {
					description += "\n" + map.get(name) + "页";
				} else {
					description += "\n" + map.get(name);
				}
			}
		}
		if (description.length() > 0) {
			description = description.substring(1);
		}

		return description;
	}

	private static String getAuthorInfo(SubjectEntry entry) {
		String authorInfo = "";
		for (Attribute attribute : entry.getAttributes()) {
			if ("author-intro".equals(attribute.getName())) {
				authorInfo = attribute.getContent();
			}
		}
		return authorInfo;
	}
}
