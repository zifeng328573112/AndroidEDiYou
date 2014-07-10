package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

/**
 * 获取淘宝评论
 * @author syghh
 *
 */
public class HomeRooListBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private int uid;//用户uid
	private long kangarooId;//用户的袋鼠id
	private String avatarUrl;
	private String gender;
	private String age;
	private String kTitle;
	private String loginName;
	private String kangarooLevel;
	
	public HomeRooListBean(JSONObject obj) throws JSONException {
		if (obj.has("id")) {
			this.uid = obj.getInt("id");
		}
		
		if (obj.has("kangarooId")) {
			this.kangarooId = obj.getInt("kangarooId");
		}
		
		if (obj.has("avatarUrl")) {
			this.avatarUrl = obj.getString("avatarUrl");
			if(this.avatarUrl!=null){
				if("null".equals(this.avatarUrl)){
					this.avatarUrl = "";
				}
			}
		}
		if (obj.has("gender")) {
			this.gender = obj.getString("gender");
		}
		if (obj.has("age")) {
			this.age = obj.getString("age");
		}
		if (obj.has("kTitle")) {
			this.kTitle = obj.getString("kTitle");
		}
		if (obj.has("kangarooLevel")) {
			this.kangarooLevel = obj.getString("kangarooLevel");
		}
		if (obj.has("loginName")) {
			this.loginName = obj.getString("loginName");
		}
	}

	public static List<HomeRooListBean> constractList(JSONArray jsonArray)
			throws SystemException {
		try {
			List<HomeRooListBean> list = new ArrayList<HomeRooListBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject rooListJson = jsonArray.getJSONObject(i);
				list.add(new HomeRooListBean(rooListJson));
			}
			return list;
		} catch (JSONException je) {
			throw new SystemException(je.getMessage());
		}
	}

	public HomeRooListBean() {
		super();
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	
	

	public long getKangarooId() {
		return kangarooId;
	}

	public void setKangarooId(long kangarooId) {
		this.kangarooId = kangarooId;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getGender() {
		return gender;
	}
	
	

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getkTitle() {
		return kTitle;
	}

	public void setkTitle(String kTitle) {
		this.kTitle = kTitle;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getKangarooLevel() {
		return kangarooLevel;
	}

	public void setKangarooLevel(String kangarooLevel) {
		this.kangarooLevel = kangarooLevel;
	}
	
}