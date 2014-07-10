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
public class EventListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 62448549411324030L;
	private Long id;
	private int promoter;
	private String title;
	private String content;
	private String startTime;
	private String endTime;
	private int limitCount;
	private int currentCount;
	private int likeCount;
	private Double cost;
	private String type;
	private String createdTime;
	private String coverUrl;
	private int forwardCount;
	
	public EventListBean() {
	}
	
	public EventListBean(JSONObject obj) throws JSONException{
		if(obj.has("id")){
			this.id = obj.getLong("id");
		}
		if(obj.has("promoter")){
			this.promoter = obj.getInt("promoter");
		}
		if(obj.has("title")){
			this.title = obj.getString("title");
		}
		if(obj.has("content")){
			this.content = obj.getString("content");
		}
		if(obj.has("startTime")){
			this.startTime = obj.getString("startTime");
		}
		if(obj.has("endTime")){
			this.endTime = obj.getString("endTime");
		}
		if(obj.has("limitCount")){
			this.limitCount = obj.getInt("limitCount");
		}
		if(obj.has("currentCount")){
			this.currentCount = obj.getInt("currentCount");
		}
		if(obj.has("likeCount")){
			this.likeCount = obj.getInt("likeCount");
		}
		if(obj.has("cost")){
			this.cost = obj.getDouble("cost");
		}
		if(obj.has("typeName")){
			this.type = obj.getString("typeName");
		}
		if(obj.has("type")){
			this.type = obj.getString("type");
		}
		if(obj.has("createdTime")){
			this.createdTime = obj.getString("createdTime");
		}
		if(obj.has("coverUrl")){
			this.coverUrl = obj.getString("coverUrl");
		}
		if(obj.has("forwardCount")){
			this.forwardCount = obj.getInt("forwardCount");
		}
	}
	
	public static ArrayList<EventListBean> constractList(JSONArray arr, ArrayList<EventListBean> beans) throws JSONException{
		if(arr!=null){
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventListBean(obj));
			}
		}
		return beans;
	}

	public static List<EventListBean> constractList(JSONArray arr) throws JSONException{
		List<EventListBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<EventListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventListBean(obj));
			}
		}
		return beans;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getPromoter() {
		return promoter;
	}

	public void setPromoter(int promoter) {
		this.promoter = promoter;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param id the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param id the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}
	/**
	 * @param id the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}
	/**
	 * @param id the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * @return the limitCount
	 */
	public Integer getLimitCount() {
		return limitCount;
	}
	/**
	 * @param id the limitCount to set
	 */
	public void setLimitCount(Integer limitCount) {
		this.limitCount = limitCount;
	}
	
	/**
	 * @return the currentCount
	 */
	public Integer getCurrentCount() {
		return currentCount;
	}
	/**
	 * @param id the currentCount to set
	 */
	public void setCurrentCount(Integer currentCount) {
		this.currentCount = currentCount;
	}
	
	/**
	 * @return the likeCount
	 */
	public Integer getLikeCount() {
		return likeCount;
	}
	/**
	 * @param id the likeCount to set
	 */
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	
	/**
	 * @return the cost
	 */
	public Double getCost() {
		return cost;
	}
	
	/**
	 * @param id the cost to set
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param id the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the createdTime
	 */
	public String getCreatedTime() {
		return createdTime;
	}
	
	/**
	 * @param id the createdTime to set
	 */
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	
	/**
	 * @return the coverUrl
	 */
	public String getCoverUrl() {
		return coverUrl;
	}
	
	/**
	 * @param id the coverUrl to set
	 */
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	
	/**
	 * @return the forwardCount
	 */
	public int getForwardCount() {
		return forwardCount;
	}
	
	/**
	 * @param id the forwardCount to set
	 */
	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}
}
