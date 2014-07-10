package com.elephant.ediyou.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *	版本升级
 * @author Zhoujun
 * @version 创建时间：2013-3-22 下午4:15:42 
 */
public class VersionBean {

	private String status;
	private  int id;
	private String addTime;
	private String vers;//版本名
	private String url;//更新地址；
	private String content;//更新内容；
	private boolean comp;//是否强制更新；
	private String vtype;
	
	public VersionBean (JSONObject obj) throws JSONException{
		if(obj.has("status")){
			this.status = obj.getString("status");
		}
		if(obj.has("version")){
			JSONObject vObj = obj.getJSONObject("version");
			if(vObj.has("id")){
				this.id = vObj.getInt("id");
			}
			if(vObj.has("addTime")){
				this.addTime = vObj.getString("addTime");
			}
			if(vObj.has("vers")){
				this.vers = vObj.getString("vers");
			}
			if(vObj.has("url")){
				this.url = vObj.getString("url");
			}
			if(vObj.has("content")){
				this.content = vObj.getString("content");
			}
			if(vObj.has("comp")){
				this.comp = vObj.getBoolean("comp");
			}
			if(vObj.has("vtype")){
				this.vtype = vObj.getString("vtype");
			}
		}
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getVers() {
		return vers;
	}

	public void setVers(String vers) {
		this.vers = vers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isComp() {
		return comp;
	}

	public void setComp(boolean comp) {
		this.comp = comp;
	}

	public String getVtype() {
		return vtype;
	}

	public void setVtype(String vtype) {
		this.vtype = vtype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}

