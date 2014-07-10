package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

/**
 * 城市
 * @author syghh
 *
 */
public class CitysBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private int provinceId;
	private int state;
	
	public CitysBean(JSONObject obj) throws JSONException {
		if (obj.has("id")) {
			this.id = obj.getInt("id");
		}
		if (obj.has("name")) {
			this.name = obj.getString("name");
		}
		if (obj.has("provinceId")) {
			this.provinceId = obj.getInt("provinceId");
		}
		if (obj.has("state")) {
			this.state = obj.getInt("state");
		}
		
	}

	public static List<CitysBean> constractList(JSONArray jsonArray)
			throws SystemException {
		try {
			List<CitysBean> list = new ArrayList<CitysBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject citysJson = jsonArray.getJSONObject(i);
				list.add(new CitysBean(citysJson));
			}
			return list;
		} catch (JSONException je) {
			throw new SystemException(je.getMessage());
		}
	}

	public CitysBean() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	
	
}