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
 * 活动的图片
 * 
 * @author syghh
 * 
 */
public class EventPhotoBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4666058500587563034L;
	private long PhotoId;// 图片的id
	private long activityId;// 活动id
	private String thumbUrl;// 微缩图
	private int width;
	private int height;
	private String photoUrl;// 原图
	private int likeCount;// 收藏数量
	private int forwardCount;// 分享数
	private String createdTime;// 创建时间
	private boolean isDel;// 是否为删除状态

	public EventPhotoBean(JSONObject obj) throws JSONException {

		if (obj.has("id")) {
			this.PhotoId = obj.getLong("id");
		}

		if (obj.has("activityId")) {
			this.activityId = obj.getLong("activityId");
		}
		if (obj.has("thumbUrl")) {
			this.thumbUrl = obj.getString("thumbUrl");
		}
		if (obj.has("photoUrl")) {
			this.photoUrl = obj.getString("photoUrl");
		}
		if (obj.has("likeCount")) {
			try {
				this.likeCount = obj.getInt("likeCount");
			} catch (Exception e) {
				this.likeCount = 0;
			}
		}

		if (obj.has("forwardCount")) {
			try {
				this.forwardCount = obj.getInt("forwardCount");
			} catch (Exception e) {
				this.forwardCount = 0;
			}
		}
		if (obj.has("createdTime")) {
			this.createdTime = obj.getString("createdTime");
		}

		if (obj.has("width")) {
			this.width = obj.getInt("width");
		}

		if (obj.has("height")) {
			this.height = obj.getInt("height");
		}

	}

	public static ArrayList<EventPhotoBean> constantListBean(JSONArray arr, ArrayList<EventPhotoBean> beans) throws JSONException {
		if (arr != null) {
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventPhotoBean(obj));
			}
		}
		return beans;
	}
	
	public static List<EventPhotoBean> constantListBean(JSONArray arr) throws JSONException {
		List<EventPhotoBean> beans = null;
		if (arr != null) {
			beans = new ArrayList<EventPhotoBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventPhotoBean(obj));
			}
		}
		return beans;
	}

	public EventPhotoBean() {
		super();
	}

	public boolean isDel() {
		return isDel;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setDel(boolean isDel) {
		this.isDel = isDel;
	}

	public long getPhotoId() {
		return PhotoId;
	}

	public void setPhotoId(long photoId) {
		PhotoId = photoId;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getForwardCount() {
		return forwardCount;
	}

	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

}
