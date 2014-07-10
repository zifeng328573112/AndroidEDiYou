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
public class LetterListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7003978646361075720L;
	private Integer id;
	private Integer letterCount;
	private Integer friendId;
	private String nickName;
	private String avaterurl;
	private String lastLetter;
	private String sendTime;
	private int status;//消息是否已读,0未读1已读；
	private int isRoo ;//是否是袋鼠；
	
	public LetterListBean(JSONObject obj) throws JSONException{
		if(obj.has("id")){
			this.id = obj.getInt("id");
		}
		if(obj.has("letterCount")){
			this.letterCount = obj.getInt("letterCount");
		}
		if(obj.has("friendId")){
			this.friendId = obj.getInt("friendId");
		}
		if(obj.has("nickname")){
			this.nickName = obj.getString("nickname");
		}
		if(obj.has("avatarUrl")){
			this.avaterurl = obj.getString("avatarUrl");
		}
		if(obj.has("lastLetter")){
			this.lastLetter = obj.getString("lastLetter");
		}
		if(obj.has("sendTime")){
			this.sendTime = obj.getString("sendTime");
		}
		if(obj.has("status")){
			this.status = obj.getInt("status");
		}
		if(obj.has("isKangaroo")){
			this.isRoo = obj.getInt("isKangaroo");
		}
	}
	
	public static List<LetterListBean> constantsLetterListBean(JSONArray arr) throws JSONException{
		List<LetterListBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<LetterListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new LetterListBean(obj));
			}
		}
		return beans;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the letterCount
	 */
	public Integer getLetterCount() {
		return letterCount;
	}
	/**
	 * @param letterCount the letterCount to set
	 */
	public void setLetterCount(Integer letterCount) {
		this.letterCount = letterCount;
	}
	/**
	 * @return the friendId
	 */
	public Integer getFriendId() {
		return friendId;
	}
	/**
	 * @param friendId the friendId to set
	 */
	public void setFriendId(Integer friendId) {
		this.friendId = friendId;
	}
	/**
	 * @return the nickName
	 */
	public String getNickName() {
		return nickName;
	}
	/**
	 * @param nickName the nickName to set
	 */
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	/**
	 * @return the avaterurl
	 */
	public String getAvaterurl() {
		return avaterurl;
	}
	/**
	 * @param avaterurl the avaterurl to set
	 */
	public void setAvaterurl(String avaterurl) {
		this.avaterurl = avaterurl;
	}
	/**
	 * @return the lastLetter
	 */
	public String getLastLetter() {
		return lastLetter;
	}
	/**
	 * @param lastLetter the lastLetter to set
	 */
	public void setLastLetter(String lastLetter) {
		this.lastLetter = lastLetter;
	}
	/**
	 * @return the sendTime
	 */
	public String getSendTime() {
		return sendTime;
	}
	/**
	 * @param sendTime the sendTime to set
	 */
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIsRoo() {
		return isRoo;
	}

	public void setIsRoo(int isRoo) {
		this.isRoo = isRoo;
	}

	
}
