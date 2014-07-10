package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonalPhotoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5735436981039298279L;
	
	private boolean isDel;
	private long photoId;
	private String thumbUrl ;
	private String photoUrl ;
	
	public PersonalPhotoBean() {
	}
	
	public PersonalPhotoBean(JSONObject obj) throws JSONException{
		if(obj.has("id")){
			this.photoId = obj.getInt("id");
		}
		if(obj.has("thumbUrl")){
			this.thumbUrl = obj.getString("thumbUrl");
		}
		if(obj.has("photoUrl")){
			this.photoUrl = obj.getString("photoUrl");
		}
	}
	
	public static ArrayList<PersonalPhotoBean> constractListBean(JSONArray arr,ArrayList<PersonalPhotoBean> beans)
			throws JSONException {
		if (arr != null) {
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new PersonalPhotoBean(obj));
			}
		}
		return beans;
	}
	
	public boolean isDel() {
		return isDel;
	}

	public void setDel(boolean isDel) {
		this.isDel = isDel;
	}

	public long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(long photoId) {
		this.photoId = photoId;
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
	
	
}
