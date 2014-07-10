package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 活动类型
 * @author syghh
 *
 */
public class EventTypesBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2914586055872670263L;
	private int id;
	private String title;
	
	public EventTypesBean(JSONObject obj) throws JSONException{
		if(obj.has("id")){
			this.id = obj.getInt("id");
		}
		
		if(obj.has("title")){
			this.title = obj.getString("title");
		}
	}

	public static List<EventTypesBean> constractList(JSONArray arr) throws JSONException{
		List<EventTypesBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<EventTypesBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new EventTypesBean(obj));
			}
		}
		return beans;
	}

	public EventTypesBean(){
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	

}
