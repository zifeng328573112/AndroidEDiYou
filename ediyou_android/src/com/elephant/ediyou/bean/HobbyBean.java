package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

public class HobbyBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7863304887211353165L;
	private int id;
	private String name;

	public HobbyBean(JSONObject obj) throws JSONException {
		if (obj.has("id")) {
			this.id = obj.getInt("id");
		}
		if (obj.has("name")) {
			this.name = obj.getString("name");
		}

	}

	public static List<HobbyBean> constractList(JSONArray jsonArray) throws SystemException {
		try {
			List<HobbyBean> list = new ArrayList<HobbyBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject JsonOb = jsonArray.getJSONObject(i);
				list.add(new HobbyBean(JsonOb));
			}
			return list;
		} catch (JSONException je) {
			throw new SystemException(je.getMessage());
		}
	}

	public HobbyBean() {
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

}