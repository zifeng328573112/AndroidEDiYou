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
public class RooAuthListBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1820294706985616619L;
	private Integer aId;
	private String kId;
	private String userId;
	private String appendixUrl;

	private String authTitle;
	private String authTime;
	private String remark;
	private Integer state;
	private String authcardId;

	public RooAuthListBean(JSONObject obj) throws JSONException {
		if (obj.has("aId")) {
			this.aId = obj.getInt("aId");
		}
		if (obj.has("kId")) {
			this.kId = obj.getString("kId");
		}
		if (obj.has("userId")) {
			this.userId = obj.getString("userId");
		}
		if (obj.has("appendixUrl")) {
			this.appendixUrl = obj.getString("appendixUrl");
		}

		if (obj.has("authTitle")) {
			this.authTitle = obj.getString("authTitle");
		}
		if (obj.has("authTime")) {
			this.authTime = obj.getString("authTime");
		}
		if (obj.has("remark")) {
			this.remark = obj.getString("remark");
		}
		if (obj.has("state")) {
			this.state = obj.getInt("state");
		}
		if (obj.has("authcardId")) {
			this.authcardId = obj.getString("authcardId");
		}
	}

	public static List<RooAuthListBean> constractList(JSONArray arr) throws JSONException {
		List<RooAuthListBean> beans = null;
		if (arr != null) {
			beans = new ArrayList<RooAuthListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new RooAuthListBean(obj));
			}
		}
		return beans;
	}

	/**
	 * @return the authTitle
	 */
	public String getAuthTitle() {
		return authTitle;
	}

	/**
	 * @param the
	 *            authTitle to set
	 */
	public void setAuthTitle(String authTitle) {
		this.authTitle = authTitle;
	}

	/**
	 * @return the authTime
	 */
	public String getAuthTime() {
		return authTime;
	}

	/**
	 * @param the
	 *            authTime to set
	 */
	public void setAuthTime(String authTime) {
		this.authTime = authTime;
	}

	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param the
	 *            remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * @return the state
	 */
	public Integer getState() {
		return state;
	}

	/**
	 * @param the
	 *            state to set
	 */
	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getaId() {
		return aId;
	}

	public void setaId(Integer aId) {
		this.aId = aId;
	}

	public String getAppendixUrl() {
		return appendixUrl;
	}

	public void setAppendixUrl(String appendixUrl) {
		this.appendixUrl = appendixUrl;
	}

	public String getkId() {
		return kId;
	}

	public void setkId(String kId) {
		this.kId = kId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAuthcardId() {
		return authcardId;
	}

	public void setAuthcardId(String authcardId) {
		this.authcardId = authcardId;
	}

}
