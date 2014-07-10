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
public class AccountListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7767141774539343170L;
	private int isKangaroo;
	private String touchTime;
	private String touchMoney;
	private String tname;

	public AccountListBean(JSONObject obj) throws JSONException{
		if(obj.has("isKangaroo")){
			this.isKangaroo = obj.getInt("isKangaroo");
		}
		if(obj.has("touchTime")){
			this.touchTime = obj.getString("touchTime");
		}
		if(obj.has("touchMoney")){
			this.touchMoney = obj.getString("touchMoney");
		}
		if(obj.has("tname")){
			this.tname = obj.getString("tname");
		}
	}

	public static List<AccountListBean> constractList(JSONArray arr) throws JSONException{
		List<AccountListBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<AccountListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new AccountListBean(obj));
			}
		}
		return beans;
	}
	
	/**
	 * @return get the isKangaroo
	 */
	public Integer getIsKangaroo() {
		return isKangaroo;
	}
	/**
	 * @param the isKangaroo to set
	 */
	public void setIsKangaroo(Integer isKangaroo) {
		this.isKangaroo = isKangaroo;
	}
	
	/**
	 * @return get the touchTime
	 */
	public String getTouchTime() {
		return touchTime;
	}
	/**
	 * @param the touchTime to set
	 */
	public void setTouchTime(String touchTime) {
		this.touchTime = touchTime;
	}
	
	/**
	 * @return get the touchMoney
	 */
	public String getTouchMoney() {
		return touchMoney;
	}
	/**
	 * @param the touchMoney to set
	 */
	public void setTouchMoney(String touchMoney) {
		this.touchMoney = touchMoney;
	}
	
	/**
	 * @return get the tname
	 */
	public String getTname() {
		return tname;
	}
	/**
	 * @param the tname to set
	 */
	public void setTname(String tname) {
		this.tname = tname;
	}
}
