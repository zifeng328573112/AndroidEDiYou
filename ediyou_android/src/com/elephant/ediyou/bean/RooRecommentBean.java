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
 * 袋鼠推荐
 * 
 * @author syghh
 * 
 */
public class RooRecommentBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3022379272024303812L;
	// 推荐列表数据
	private long RecommentId;// 图片的id
	private long kangarooId;// 袋鼠id
	private String title;// 推荐地
	private String content;// 描述内容
	private String coverUrl;// 图片url
	private String createdTime;// 创建时间

	// 推荐详情中图片的数据
	private long photoId;// 图片id
	private String description;// 推荐介绍
	private String thumbUrl;// 微缩图
	private String photoUrl;// 高清图
	private String likeCount;// 收藏统计

	private boolean isDelState;// 在删除状态

	public RooRecommentBean(JSONObject obj) throws JSONException {
		if (obj.has("kangarooId")) {
			if (obj.has("id")) {
				this.RecommentId = obj.getLong("id");
			}
			if (obj.has("kangarooId")) {
				this.kangarooId = obj.getLong("kangarooId");
			}
			if (obj.has("title")) {
				this.title = obj.getString("title");
			}
			if (obj.has("content")) {
				this.content = obj.getString("content");
			}
			if (obj.has("coverUrl")) {
				this.coverUrl = obj.getString("coverUrl");
			}
			if (obj.has("createdTime")) {
				this.createdTime = obj.getString("createdTime");
			}
		}

		if (obj.has("description")) {

			if (obj.has("id")) {
				this.photoId = obj.getLong("id");
			}
			if (obj.has("description")) {
				this.description = obj.getString("description");
			}
			if (obj.has("thumbUrl")) {
				this.thumbUrl = obj.getString("thumbUrl");
			}
			if (obj.has("photoUrl")) {
				this.photoUrl = obj.getString("photoUrl");
			}
			if (obj.has("likeCount")) {
				this.likeCount = obj.getString("likeCount");
			}

		}

	}

	public static ArrayList<RooRecommentBean> constantAddListBean(JSONArray arr,
			ArrayList<RooRecommentBean> beans) throws JSONException {
		if (arr != null) {
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new RooRecommentBean(obj));
			}
		}
		return beans;
	}

	public static ArrayList<RooRecommentBean> constantListBean(JSONArray arr)
			throws JSONException {
		ArrayList<RooRecommentBean> beans = null;
		if (arr != null) {
			beans = new ArrayList<RooRecommentBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new RooRecommentBean(obj));
			}
		}
		return beans;
	}

	public RooRecommentBean() {
		super();
	}

	public long getRecommentId() {
		return RecommentId;
	}

	public void setRecommentId(long recommentId) {
		RecommentId = recommentId;
	}

	public long getKangarooId() {
		return kangarooId;
	}

	public void setKangarooId(long kangarooId) {
		this.kangarooId = kangarooId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(long photoId) {
		this.photoId = photoId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(String likeCount) {
		this.likeCount = likeCount;
	}

	public boolean isDelState() {
		return isDelState;
	}

	public void setDelState(boolean isDelState) {
		this.isDelState = isDelState;
	}

}
