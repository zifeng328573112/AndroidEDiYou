package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

/**
 * 省份
 * @author syghh
 *
 */
public class ProvincesBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	
	public ProvincesBean(JSONObject obj) throws JSONException {
		if (obj.has("id")) {
			this.id = obj.getInt("id");
		}
		if (obj.has("name")) {
			this.name = obj.getString("name");
		}	
	}

	public static List<ProvincesBean> constractList(JSONArray jsonArray)
			throws SystemException {
		try {
			List<ProvincesBean> list = new ArrayList<ProvincesBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject provincesJson = jsonArray.getJSONObject(i);
				list.add(new ProvincesBean(provincesJson));
			}
			return list;
		} catch (JSONException je) {
			throw new SystemException(je.getMessage());
		}
	}

	public ProvincesBean() {
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