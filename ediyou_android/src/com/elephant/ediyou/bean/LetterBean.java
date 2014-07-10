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
 * 私信实体
 * @author Arvin
 *
 */
public class LetterBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6190204637500449066L;
	private int id;
	private int userId;
	private int friendId;
	private int sender;//发送者
	private int receiver;//接受者
	private String content;//内容
	private int status;//状态
	private String sendTime;//发送时间
	
	public LetterBean(JSONObject obj) throws JSONException{
		if(obj.has("id")){
			this.id = obj.getInt("id");
		}
		if(obj.has("userId")){
			this.userId = obj.getInt("userId");
		}
		if(obj.has("friendId")){
			this.friendId = obj.getInt("friendId");
		}
		if(obj.has("sender")){
			this.sender = obj.getInt("sender");
		}
		if(obj.has("receiver")){
			this.receiver = obj.getInt("receiver");
		}
		if(obj.has("content")){
			this.content = obj.getString("content");
		}
		if(obj.has("status")){
			this.status = obj.getInt("status");
		}
		if(obj.has("sendTime")){
			this.sendTime = obj.getString("sendTime");
		}
	}
	
	public static List<LetterBean> constantsList(JSONArray arr) throws JSONException{
		List<LetterBean> letterBeans = new ArrayList<LetterBean>();
		if(arr!=null){
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject subObj = arr.getJSONObject(i);
				if(subObj!=null){
					letterBeans.add(new LetterBean(subObj));
				}
			}
		}
		return letterBeans;
	}
	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the friendId
	 */
	public int getFriendId() {
		return friendId;
	}
	/**
	 * @param friendId the friendId to set
	 */
	public void setFriendId(int friendId) {
		this.friendId = friendId;
	}
	/**
	 * @return the sender
	 */
	public int getSender() {
		return sender;
	}
	/**
	 * @param sender the sender to set
	 */
	public void setSender(int sender) {
		this.sender = sender;
	}
	/**
	 * @return the receiver
	 */
	public int getReceiver() {
		return receiver;
	}
	/**
	 * @param receiver the receiver to set
	 */
	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
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
	
	
	
}
