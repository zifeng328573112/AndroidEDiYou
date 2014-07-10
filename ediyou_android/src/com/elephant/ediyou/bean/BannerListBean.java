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

import android.text.TextUtils;

/**
 * 首页Banner
 * 
 * @author Yuan
 * 
 */
public class BannerListBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -759751590376677730L;
	private Integer type;// 0：活动；1：weburl
	private String url;
	private Long activityId;
	private String picUrlApp;
	private String titleName;

	public BannerListBean(JSONObject obj) throws JSONException {
		if (obj.has("type")) {
			this.type = obj.getInt("type");
		}

		if (obj.has("url")) {
			this.url = obj.getString("url");
		}

		if (obj.has("activityId")) {
			String activityIdL = obj.getString("activityId");
			if (!TextUtils.isEmpty(activityIdL)) {
				this.activityId = Long.valueOf(activityIdL);
			}
		}
		if (obj.has("picUrlApp")) {
			this.picUrlApp = obj.getString("picUrlApp");
		}

		if (obj.has("titleName")) {
			this.titleName = obj.getString("titleName");
		}
	}

	public static ArrayList<BannerListBean> constractList(JSONArray arr) throws JSONException {
		ArrayList<BannerListBean> beans = null;
		if (arr != null) {
			beans = new ArrayList<BannerListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new BannerListBean(obj));
			}
		}
		return beans;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPicUrlApp() {
		return picUrlApp;
	}

	public void setPicUrlApp(String picUrlApp) {
		this.picUrlApp = picUrlApp;
	}

	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	public Long getActivityId() {
		return activityId;
	}

}
