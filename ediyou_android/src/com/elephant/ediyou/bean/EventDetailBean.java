package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

/**
 * 活动详情页数据
 * 
 * @author syghh
 * 
 */
public class EventDetailBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long promoter;// 发起人id
	private String title;// 活动名称
	private String content;// 活动描述
	private String startTime;// 开始时间
	private String endTime;// 结束时间
	private int limitCount;// 人数限额
	private int currentCount;// 目前已参加人数
	private int likeCount;// 希望有多少人参加
	private int cost;// 人均费用
	private long kangarooId;// 袋鼠id
	private String avatarUrl;// 袋鼠头像
	private String nickName;// 袋鼠昵称
	private long userId;// 用户id
	private String age;// 年龄
	private String badge;// 徽章
	private int level;// 等级
	private String gender; // 性别
	private String createdTime; // 创建时间
	private String coverUrl;// 封面图
	private int imgCount;// 活动的图片数量
	
	private String currentTime;

	private String typeName;// 活动类型

	public EventDetailBean(JSONObject obj) throws JSONException {
		if (obj.has("promoter")) {
			this.promoter = obj.getLong("promoter");
		}
		if (obj.has("title")) {
			this.title = obj.getString("title");
		}

		if (obj.has("content")) {
			this.content = obj.getString("content");
		}

		if (obj.has("startTime")) {
			this.startTime = obj.getString("startTime");
		}

		if (obj.has("endTime")) {
			this.endTime = obj.getString("endTime");
		}

		if (obj.has("limitCount")) {
			this.limitCount = obj.getInt("limitCount");
		}
		if (obj.has("currentCount")) {
			this.currentCount = obj.getInt("currentCount");
		}
		if (obj.has("likeCount")) {
			this.likeCount = obj.getInt("likeCount");
		}
		if (obj.has("cost")) {
			this.cost = obj.getInt("cost");
		}
		if (obj.has("kangarooId")) {
			this.kangarooId = obj.getLong("kangarooId");
		}

		if (obj.has("nickName")) {
			this.nickName = obj.getString("nickName");
		}

		if (obj.has("userId")) {
			this.userId = obj.getLong("userId");
		}

		if (obj.has("gender")) {
			this.gender = obj.getString("gender");
		}
		if (obj.has("age")) {
			this.age = obj.getString("age");
		}
		if (obj.has("badge")) {
			this.badge = obj.getString("badge");
		}
		if (obj.has("level")) {
			this.level = obj.getInt("level");
		}

		if (obj.has("createdTime")) {
			this.createdTime = obj.getString("createdTime");
		}
		if (obj.has("coverUrl")) {
			this.coverUrl = obj.getString("coverUrl");
		}
		if (obj.has("avatarUrl")) {
			this.avatarUrl = obj.getString("avatarUrl");
		}

		if (obj.has("imgCount")) {
			this.imgCount = obj.getInt("imgCount");
		}

		if (obj.has("typeName")) {
			this.typeName = obj.getString("typeName");
		}
		if(obj.has("currentTime")){
			this.currentTime = obj.getString("currentTime");
		}
	}

	public EventDetailBean() {
		super();
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public int getImgCount() {
		return imgCount;
	}

	public void setImgCount(int imgCount) {
		this.imgCount = imgCount;
	}

	public long getPromoter() {
		return promoter;
	}

	public void setPromoter(long promoter) {
		this.promoter = promoter;
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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	public int getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public long getKangarooId() {
		return kangarooId;
	}

	public void setKangarooId(long kangarooId) {
		this.kangarooId = kangarooId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}
	
}