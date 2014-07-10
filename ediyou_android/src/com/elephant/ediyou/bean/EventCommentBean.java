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
 * 活动详情页的评论
 * @author syghh
 *
 */
public class EventCommentBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7094731700443083096L;
	private String content;//评论内容
	private String avatarUrl;//头像
	private String nickname;//昵称
	
	public EventCommentBean(JSONObject obj) throws JSONException{
		
		if(obj.has("content")){
			this.content = obj.getString("content");
		}
		if(obj.has("avatarUrl")){
			this.avatarUrl = obj.getString("avatarUrl");
		}
		if(obj.has("nickname")){
			this.nickname = obj.getString("nickname");
		}
		
	}
	
	public static List<EventCommentBean> constantsLetterListBean(JSONArray arr) throws JSONException{
		List<EventCommentBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<EventCommentBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventCommentBean(obj));
			}
		}
		return beans;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	
}
