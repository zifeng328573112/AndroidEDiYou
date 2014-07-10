/**
 * 
 */
package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Arvin
 * 
 */
public class RooSearchBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2397288336301636200L;
	private long userId;
	private Integer kangarooId;
	private Integer level;
	private String title;
	private String birthday;
	private String age;
	private String gender;
	private String avatarUrl;

	public RooSearchBean(JSONObject obj) throws JSONException {
		if (obj.has("kangarooId")) {
			this.kangarooId = obj.getInt("kangarooId");
		}

		if (obj.has("userId")) {
			this.userId = obj.getLong("userId");
		}
		if (obj.has("level")) {
			this.level = obj.getInt("level");
		}
		if (obj.has("title")) {
			this.title = obj.getString("title");
		}
		if (obj.has("birthday")) {
			this.birthday = obj.getString("birthday");
		}
		if (obj.has("age")) {
			this.age = obj.getString("age");
		}
		if (obj.has("gender")) {
			this.gender = obj.getString("gender");
		}
		if (obj.has("avatarUrl")) {
			this.avatarUrl = obj.getString("avatarUrl");
		}
	}

	public static List<RooSearchBean> constractList(JSONArray arr) throws JSONException {
		List<RooSearchBean> beans = null;
		if (arr != null) {
			beans = new ArrayList<RooSearchBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new RooSearchBean(obj));
			}
		}
		return beans;
	}

	/**
	 * @return the kangarooId
	 */
	public Integer getKangarooId() {
		return kangarooId;
	}

	/**
	 * @param id
	 *            the kangarooId to set
	 */
	public void setKangarooId(Integer kangarooId) {
		this.kangarooId = kangarooId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @param id
	 *            the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param id
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the birthday
	 */
	public String getBirthday() {
		return birthday;
	}

	/**
	 * @param id
	 *            the birthday to set
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the age
	 */
	public String getAge() {
		return age;
	}

	/**
	 * @param id
	 *            the age to set
	 */
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param id
	 *            the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the avatarUrl
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}

	/**
	 * @param id
	 *            the avatarUrl to set
	 */
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
