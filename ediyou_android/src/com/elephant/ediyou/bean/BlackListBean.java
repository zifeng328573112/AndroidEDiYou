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
public class BlackListBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8552210265790194177L;
	private String kTitle;
	private Long id;
	private String loginName;
	private String nickName;
	private String age;
	private String avatarUrl;
	private String pName;
	private String cityName;
	private Integer isKangaroo;
	private String gender;
	private String cTitle;
	private String kLevel;
	private String cLevel;
	
	public BlackListBean(JSONObject obj) throws JSONException{
		if(obj.has("kTitle")){
			this.kTitle = obj.getString("kTitle");
		}
		if(obj.has("id")){
			this.id = obj.getLong("id");
		}
		if(obj.has("loginName")){
			this.loginName = obj.getString("loginName");
		}
		if(obj.has("nickName")){
			this.nickName = obj.getString("nickName");
		}
		if(obj.has("age")){
			this.age = obj.getString("age");
		}
		if(obj.has("avatarUrl")){
			this.avatarUrl = obj.getString("avatarUrl");
		}
		if(obj.has("pName")){
			this.pName = obj.getString("pName");
		}
		if(obj.has("cityName")){
			this.cityName = obj.getString("cityName");
		}
		if(obj.has("isKangaroo")){
			this.isKangaroo = obj.getInt("isKangaroo");
		}
		if(obj.has("gender")){
			this.gender = obj.getString("gender");
		}
		if(obj.has("cTitle")){
			this.cTitle = obj.getString("cTitle");
		}
		if(obj.has("kLevel")){
			this.kLevel = obj.getString("kLevel");
		}
		if(obj.has("cLevel")){
			this.cLevel = obj.getString("cLevel");
		}
	}

	public static List<BlackListBean> constractList(JSONArray arr) throws JSONException{
		List<BlackListBean> beans = null;
		if(arr!=null){
			beans = new ArrayList<BlackListBean>();
			int length = arr.length();
			for (int i = 0; i < length; i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				beans.add(new BlackListBean(obj));
			}
		}
		return beans;
	}
	
	/**
	 * @return the kTitle
	 */
	public String getKTitle() {
		return kTitle;
	}
	/**
	 * @param kTitle the kTitle to set
	 */
	public void setKTitle(String kTitle) {
		this.kTitle = kTitle;
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
	
	/**
	 * @return the loginName
	 */
	public String getLoginName() {
		return loginName;
	}
	/**
	 * @param loginName the loginName to set
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	/**
	 * @return the nickName
	 */
	public String getNickName() {
		return nickName;
	}
	/**
	 * @param nickName the nickName to set
	 */
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	/**
	 * @return the age
	 */
	public String getAge() {
		return age;
	}
	/**
	 * @param age the age to set
	 */
	public void setAge(String age) {
		this.age = age;
	}
	
	/**
	 * @return the avatarUrl
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}
	/**
	 * @param avatarUrl the avatarUrl to set
	 */
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	
	/**
	 * @return the pName
	 */
	public String getPName() {
		return pName;
	}
	/**
	 * @param pName the pName to set
	 */
	public void setPName(String pName) {
		this.pName = pName;
	}
	
	/**
	 * @return the cityName
	 */
	public String getCityName() {
		return cityName;
	}
	/**
	 * @param cityName the cityName to set
	 */
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	/**
	 * @return the isKangaroo
	 */
	public Integer getIsKangaroo() {
		return isKangaroo;
	}
	/**
	 * @param isKangaroo the isKangaroo to set
	 */
	public void setIsKangaroo(Integer isKangaroo) {
		this.isKangaroo = isKangaroo;
	}
	
	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	/**
	 * @return the cTitle
	 */
	public String getCTitle() {
		return cTitle;
	}
	/**
	 * @param cTitle the cTitle to set
	 */
	public void setCTitle(String cTitle) {
		this.cTitle = cTitle;
	}
	
	/**
	 * @return the kLevel
	 */
	public String getKLevel() {
		return kLevel;
	}
	/**
	 * @param kLevel the kLevel to set
	 */
	public void setKLevel(String kLevel) {
		this.kLevel = kLevel;
	}
	
	/**
	 * @return the cLevel
	 */
	public String getCLevel() {
		return cLevel;
	}
	/**
	 * @param cLevel the cLevel to set
	 */
	public void setCLevel(String cLevel) {
		this.cLevel = cLevel;
	}
	
}
