package com.elephant.ediyou.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.elephant.ediyou.bean.UserBean;

/**
 * SharedPreferences工具类
 * 
 * @author Aizhimin 说明：SharedPreferences的操作工具类，需要缓存到SharedPreferences中的数据在此设置。
 */
public class SharedPrefUtil {

	public static final String IS_FIRST_LOGIN = "is_first_login";// 第一次进入

	public static final String SINA_UID = "sina_uid";// 新浪微博唯一id
	public static final String WEIBO_ACCESS_TOKEN = "weibo_access_token";// 新浪微博令牌
	public static final String WEIBO_EXPIRES_IN = "weibo_expires_in";// 新浪微博令牌时间
	public static final String WEIBO_ACCESS_CURR_TIME = "weibo_sccess_curr_time";// 新浪微博授权时间

	public static final String QQ_ACCESS_TOKEN = "qq_access_token";// 新浪微博令牌
	public static final String QQ_EXPIRES_IN = "qq_expires_in";// 新浪微博令牌时间
	public static final String QQ_OPENID = "qq_openid";
	public static final String QQ_ACCESS_CURR_TIME = "qq_sccess_curr_time";// 新浪微博授权时间

	/**
	 * qq互联信息
	 */
	public static final String QQ_CONNECT_ACCESS_TOKEN = "qq_connect_access_token";// 新浪微博令牌
	public static final String QQ_CONNECT_EXPIRES_IN = "qq_connect_expires_in";// 新浪微博令牌时间
	public static final String QQ_CONNECT_OPENID = "qq_connect_openid";
	public static final String QQ_CONNECT_ACCESS_CURR_TIME = "qq__connectsccess_curr_time";// 新浪微博授权时间

	public static final String ACCESS_TOKEN = "access_token";
	public static final String CREATEDTIME = "createdTime";
	public static final String EXPIRES_IN = "expires_in";
	public static final String REFRESH_TOKEN = "refresh_token";

	public static final String USER_ID = "user_id";
	public static final String LOGINNAME = "loginName";
	public static final String NICKNAME = "nickname";
	public static final String SALT = "salt";
	public static final String GENDER = "gender";
	public static final String BIRTHDAY = "birthday";
	public static final String PROVINCE = "province";
	public static final String CITY = "city";
	public static final String AVATARURL = "avatarUrl";
	public static final String INTRO = "intro";
	public static final String ISKANGAROO = "isKangaroo";
	public static final String ROOID = "rooId";

	public static final String CHOICEDLOCATION = "choicedlocation";// 已选择的地点

	/**
	 * 缓存用户信息
	 * 
	 * @param context
	 * @param userBean
	 */
	public static void setUserBean(Context context, UserBean userBean) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		if (!StringUtil.isBlank(userBean.getAccessToken()))
			e.putString(ACCESS_TOKEN, userBean.getAccessToken());
		if (userBean.getCreatedTime() > 0)
			e.putLong(CREATEDTIME, userBean.getCreatedTime());
		if (userBean.getExpiresIn() > 0)
			e.putLong(EXPIRES_IN, userBean.getExpiresIn());
		if (!StringUtil.isBlank(userBean.getRefreshToken()))
			e.putString(REFRESH_TOKEN, userBean.getRefreshToken());

		if (userBean.getUserId() >= 0)
			e.putInt(USER_ID, userBean.getUserId());
		if (!StringUtil.isBlank(userBean.getLoginName()))
			e.putString(LOGINNAME, userBean.getLoginName());
		if (!StringUtil.isBlank(userBean.getNickname()))
			e.putString(NICKNAME, userBean.getNickname());
		if (!StringUtil.isBlank(userBean.getSalt()))
			e.putString(SALT, userBean.getSalt());
		if (!StringUtil.isBlank(userBean.getGender()))
			e.putString(GENDER, userBean.getGender());
		if (!StringUtil.isBlank(userBean.getBirthday()))
			e.putString(BIRTHDAY, userBean.getBirthday());
		if (!StringUtil.isBlank(userBean.getProvince()))
			e.putString(PROVINCE, userBean.getProvince());
		if (!StringUtil.isBlank(userBean.getCity()))
			e.putString(CITY, userBean.getCity());
		if (!StringUtil.isBlank(userBean.getAvatarUrl()))
			e.putString(AVATARURL, userBean.getAvatarUrl());
		if (!StringUtil.isBlank(userBean.getIntro()))
			e.putString(INTRO, userBean.getIntro());
		if (userBean.getIsKangaroo() >= 0)
			e.putInt(ISKANGAROO, userBean.getIsKangaroo());
		e.commit();
	}

	/**
	 * 获得用户信息
	 * 
	 * @param context
	 * @return
	 */
	public static UserBean getUserBean(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		UserBean userBean = new UserBean();
		userBean.setAccessToken(sp.getString(ACCESS_TOKEN, null));
		userBean.setCreatedTime(sp.getLong(CREATEDTIME, 0));
		userBean.setExpiresIn(sp.getLong(EXPIRES_IN, 0));
		userBean.setRefreshToken(sp.getString(REFRESH_TOKEN, null));

		userBean.setUserId(sp.getInt(USER_ID, 0));
		userBean.setLoginName(sp.getString(LOGINNAME, null));
		userBean.setNickname(sp.getString(NICKNAME, null));
		userBean.setSalt(sp.getString(SALT, null));
		userBean.setGender(sp.getString(GENDER, null));
		userBean.setBirthday(sp.getString(BIRTHDAY, null));
		userBean.setProvince(sp.getString(PROVINCE, null));
		userBean.setCity(sp.getString(CITY, null));
		userBean.setAvatarUrl(sp.getString(AVATARURL, null));
		userBean.setIntro(sp.getString(INTRO, null));
		userBean.setIsKangaroo(sp.getInt(ISKANGAROO, 0));
		return userBean;
	}

	public static void clearUserBean(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().remove(ACCESS_TOKEN).remove(CREATEDTIME).remove(EXPIRES_IN).remove(REFRESH_TOKEN).remove(USER_ID).remove(LOGINNAME).remove(NICKNAME).remove(SALT).remove(GENDER).remove(BIRTHDAY)
				.remove(PROVINCE).remove(CITY).remove(AVATARURL).remove(INTRO).remove(ISKANGAROO).commit();
	}

	/**
	 * RooId
	 * @param context
	 * @param location
	 */
	public static void setRooId(Context context, long RooId) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		if (RooId >= 0)
			e.putLong(ROOID, RooId);
		e.commit();
	}

	/**
	 * RooId
	 * @param context
	 * @return
	 */
	public static long getRooId(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		long rooid = sp.getLong(ROOID, 0);
		return rooid;
	}

	/**
	 * 缓存用户选择的地点
	 * @param context
	 * @param location
	 */
	public static void setChoicedLocation(Context context, String location) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		if (!StringUtil.isBlank(location))
			e.putString(CHOICEDLOCATION, location);
		e.commit();
	}

	/**
	 * 获取用户选择的地点
	 * @param context
	 * @return
	 */
	public static String getChoicedLocation(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String choicedLocation = sp.getString(CHOICEDLOCATION, null);
		return choicedLocation;
	}

	/**
	 * 检查token是否有效
	 * @return
	 */
	public static boolean checkToken(Context context) {
		try {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
			String token = sp.getString(ACCESS_TOKEN, null);
			if (token == null) {
				return false;
			} else {
				// long date = DateUtil.stringToDate("yyyy-MM-dd HH:mm:ss",
				// sp.getString(CREATEDTIME, null)).getTime();
				long date = sp.getLong(CREATEDTIME, 0);
				long expiresIn = sp.getLong(EXPIRES_IN, 0);
				if ((date + expiresIn - 24 * 60 * 3600) < System.currentTimeMillis()) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 退出登录
	 * @param context
	 */
	public static void exitLogin(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.remove(USER_ID).remove(ACCESS_TOKEN).remove(EXPIRES_IN).remove(CREATEDTIME).remove(REFRESH_TOKEN).remove(LOGINNAME).remove(NICKNAME).remove(SALT).remove(GENDER).remove(BIRTHDAY)
				.remove(PROVINCE).remove(CITY).remove(AVATARURL).remove(INTRO).remove(ISKANGAROO).commit();
		e.clear();
	}

	/**
	 * 判断是否是第一次进入应用
	 * @param context
	 * @return
	 */
	public static boolean isFistLogin(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean(IS_FIRST_LOGIN, true);
	}

	/**
	 * 如果已经进入应用，则设置第一次登录为false
	 * @param context
	 */
	public static void setFistLogined(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putBoolean(IS_FIRST_LOGIN, false);
		e.commit();
	}

	// -----------------------------新浪微博验证信息-----------------
	/**
	 * 设置微博绑定信息
	 * @param context
	 * @param access_token
	 * @param expires_in
	 */
	public static void setWeiboInfo(Context context, String sina_uid, String access_token, String expires_in, String access_curr_time) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putString(SINA_UID, sina_uid);
		e.putString(WEIBO_ACCESS_TOKEN, access_token);
		e.putString(WEIBO_EXPIRES_IN, expires_in);
		e.putString(WEIBO_ACCESS_CURR_TIME, access_curr_time);
		e.commit();
	}

	/**
	 * 清除微博绑定
	 * @param context
	 * @return
	 */
	public static void clearWeiboBind(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().remove(WEIBO_ACCESS_TOKEN).remove(WEIBO_EXPIRES_IN).commit();
	}

	public static String getWeiboAccessToken(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(WEIBO_ACCESS_TOKEN, null);
	}

	public static String getWeiboExpiresIn(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(WEIBO_EXPIRES_IN, null);
	}

	public static String getWeiboAccessCurrTime(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(WEIBO_ACCESS_CURR_TIME, null);
	}

	public static String getWeiboUid(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(SINA_UID, null);
	}

	/**
	 * 检测新浪微博是否绑定
	 */
	public static boolean checkWeiboBind(Context context) {
		String WeiboAccessToken = getWeiboAccessToken(context);
		String WeiboExpiresIn = getWeiboExpiresIn(context);
		String weiboAccessCurrTime = getWeiboAccessCurrTime(context);
		if (WeiboAccessToken == null || WeiboExpiresIn == null || weiboAccessCurrTime == null) {
			return false;
		} else {
			long currTime = System.currentTimeMillis();
			long accessCurrTime = Long.parseLong(weiboAccessCurrTime);
			long expiresIn = Long.parseLong(WeiboExpiresIn);
			if ((currTime - accessCurrTime) / 1000 > expiresIn) {
				return false;
			} else {
				return true;
			}
		}
	}

	// -----------------------------腾讯微博验证信息-----------------
	/**
	 * 设置腾讯微博信息
	 * 
	 * @param context
	 * @param access_token
	 * @param expires_in
	 * @param access_curr_time
	 */
	public static void setQQInfo(Context context, String access_token, String expires_in, String openid, String access_curr_time) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putString(QQ_ACCESS_TOKEN, access_token);
		e.putString(QQ_EXPIRES_IN, expires_in);
		e.putString(QQ_OPENID, openid);
		e.putString(QQ_ACCESS_CURR_TIME, access_curr_time);
		e.commit();
	}

	public static String getQQAccessToken(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(QQ_ACCESS_TOKEN, null);
	}

	public static String getQQExpiresIn(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(QQ_EXPIRES_IN, null);
	}

	public static String getQQOpenid(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(QQ_OPENID, null);
	}

	public static String getQQAccessCurrTime(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(QQ_ACCESS_CURR_TIME, null);
	}

	public static void clearQQBind(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().remove(QQ_ACCESS_TOKEN).remove(QQ_EXPIRES_IN).remove(QQ_OPENID).remove(QQ_ACCESS_CURR_TIME).commit();
	}

	/**
	 * 检查腾讯微博是否绑定
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkQQBind(Context context) {
		String qqAccessToken = getQQAccessToken(context);
		String qqExpiresIn = getQQExpiresIn(context);
		String qqAccessCurrTime = getQQAccessCurrTime(context);
		if (qqAccessToken == null || qqExpiresIn == null || qqAccessCurrTime == null) {
			return false;
		} else {
			long currTime = System.currentTimeMillis();
			long accessCurrTime = Long.parseLong(qqAccessCurrTime);
			long expiresIn = Long.parseLong(qqExpiresIn);
			if ((currTime - accessCurrTime) / 1000 > expiresIn) {
				return false;
			} else {
				return true;
			}
		}
	}

	// -----------------------------QQ互联验证信息-----------------
	public static void setQQConnectInfo(Context context, String access_token, String expires_in, String openid, String access_curr_time) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putString(QQ_CONNECT_ACCESS_TOKEN, access_token);
		e.putString(QQ_CONNECT_EXPIRES_IN, expires_in);
		e.putString(QQ_CONNECT_OPENID, openid);
		e.putString(QQ_CONNECT_ACCESS_CURR_TIME, access_curr_time);
		e.commit();
	}

	public static String getQQConnectOpenid(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(QQ_CONNECT_OPENID, null);
	}

	/**
	 * 获得检测间隔
	 * @param con
	 * @return
	 */
	public static final String CHECK_UPDATE_TIME_KEY = "check_update_time_key";// 轮询时间

	public static long getUpdateInterval(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getLong(CHECK_UPDATE_TIME_KEY, 2 * 60 * 1000);
	}

	/**
	 * 设置是否接收系统通知；
	 * @param context
	 * @param notificationReceiveable
	 */
	public static void setNotificationSetting(Context context, boolean notificationReceiveable) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putBoolean("notificationReceiveable", notificationReceiveable);
		e.commit();
	}

	public static boolean getNotificationSetting(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean("notificationReceiveable", true);
	}

	/**
	 * 设置是否启动定位服务
	 * @param context
	 * @param locationAble
	 */
	public static void setLocationSetting(Context context, boolean locationAble) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.putBoolean("locationAble", locationAble);
		e.commit();
	}

	public static boolean getLocationSetting(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean("locationAble", true);
	}

	/**
	 * 清除定位和通知的配置；
	 */
	public static void clearNotiLocation(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = sp.edit();
		e.remove("notificationReceiveable").remove("locationAble").commit();
	}
}
