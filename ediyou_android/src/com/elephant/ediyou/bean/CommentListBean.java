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
 * 评论（当前用户的评论，无论袋鼠考拉，包含预约和活动）
 * @author SongYuan
 *
 */
public class CommentListBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7403460848507851144L;
	private String content;//评论内容
	private String nickname;//昵称
	private String level;//星级
	private String createdTime;//发布时间
	public CommentListBean(JSONObject obj) throws JSONException{
		
		if(obj.has("content")){
			this.content = obj.getString("content");
		}
		
		if(obj.has("nickname")){
			this.nickname = obj.getString("nickname");
		}
		
		if(obj.has("level")){
			this.level = obj.getString("level");
		}
		
		if(obj.has("createdTime")){
			this.createdTime = obj.getString("createdTime");
		}
		
	}
	
	public static List<CommentListBean> constantsLetterListBean(JSONArray arr) throws JSONException{
		List<CommentListBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<CommentListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new CommentListBean(obj));
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	
	
}
