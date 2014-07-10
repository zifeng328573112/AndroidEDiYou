package com.elephant.ediyou.bean;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.elephant.ediyou.util.StringUtil;

/**
 * 用户
 * 
 * @author syghh
 * 
 */
public class UserBean implements Serializable {
	private static final long serialVersionUID = -4874408683032407038L;
	private int id;// 后台数据id（未取）
	private int userId;// 用户id
	private String loginName;// 用户名 电子邮箱
	private String nickname;// 昵称
	private String salt;//
	private String gender;// n 男 f 女
	private String birthday;// 生日
	private String province;// 省份
	private String city;// 城市
	private String avatarUrl;// 头像
	private String intro;// 签名
	private int isKangaroo;// 0为考拉，1为袋鼠

	private String accessToken;
	private long createdTime;
	private long expiresIn;
	private String refreshToken;

	public UserBean() {
	}

	public UserBean(JSONObject userJson) throws JSONException {
		if (userJson.has("loginName")) {
			this.userId = userJson.getInt("id");
			this.loginName = userJson.getString("loginName");
			this.nickname = userJson.getString("nickname");

			if (!StringUtil.isBlank(userJson.getString("salt"))) {
				this.salt = userJson.getString("salt");
			}
			if (!StringUtil.isBlank(userJson.getString("birthday"))) {
				this.birthday = userJson.getString("birthday");
			}
			if (!StringUtil.isBlank(userJson.getString("gender"))) {
				this.gender = userJson.getString("gender");
			}
			if (!StringUtil.isBlank(userJson.getString("province"))) {
				this.province = userJson.getString("province");
			}
			if (!StringUtil.isBlank(userJson.getString("city"))) {
				this.city = userJson.getString("city");
			}
			if (!StringUtil.isBlank(userJson.getString("avatarUrl"))) {
				this.avatarUrl = userJson.getString("avatarUrl");
			}
			if (!StringUtil.isBlank(userJson.getString("intro"))) {
				this.intro = userJson.getString("intro");
			}

			this.isKangaroo = userJson.getInt("isKangaroo");
		}

		if (userJson.has("user") && !TextUtils.isEmpty(userJson.getString("user"))) {
			JSONObject user = userJson.getJSONObject("user");
			this.loginName = user.getString("loginName");
			if (user.has("nickname")) {
				if (!StringUtil.isBlank(user.getString("nickname"))) {
					this.nickname = user.getString("nickname");
				}
			}
			if(user.has("salt")){
				if (!StringUtil.isBlank(user.getString("salt"))) {
					this.salt = user.getString("salt");
				}
			}
			if(user.has("birthday")){
				if (!StringUtil.isBlank(user.getString("birthday"))) {
					this.birthday = user.getString("birthday");
				}
			}
			if(user.has("gender")){
				if (!StringUtil.isBlank(user.getString("gender"))) {
					this.gender = user.getString("gender");
				}
			}
			
			if (user.has("avatarUrl")) {
				if (!StringUtil.isBlank(user.getString("avatarUrl"))) {
					this.avatarUrl = user.getString("avatarUrl");
				}
			}
			this.isKangaroo = user.getInt("isKangaroo");
		}

		if (userJson.has("access_token")) {
			
			this.userId = userJson.getInt("user_id");
			this.accessToken = userJson.getString("access_token");
			this.expiresIn = userJson.getLong("expires_in");
			this.refreshToken = userJson.getString("refresh_token");
			this.createdTime = userJson.getLong("created_time");
		}
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public int getIsKangaroo() {
		return isKangaroo;
	}

	public void setIsKangaroo(int isKangaroo) {
		this.isKangaroo = isKangaroo;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
