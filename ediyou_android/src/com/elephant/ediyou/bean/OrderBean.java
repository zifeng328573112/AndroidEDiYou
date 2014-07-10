package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.Constants;
import com.elephant.ediyou.SystemException;

/**
 * 省份
 * 
 * @author syghh
 * 
 */
public class OrderBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private int orderNumber;// 订单号
	private String createdTime;// 创建时间
	private int state;// 订单状态:0-未支付1-已支付2-袋鼠已确认3-袋鼠未确认4-袋鼠拒绝订单
	private int unitPrice;// 单价
	private int serviceDays;// 服务天数
	private int totalPrice;// 总价
	private String headUrl;
	private String name;// 服务对象名字；
	private long wasId;// 对方的userId
	private String gender;
	private int age;
	private int badge;// 徽章
	private int level;// 等级
	private String city;// 地区
	private String orderTime;
	private String orderPlace;
	private String orderServerInfo;
	private String qrcode;// 二维码；
	private int isOnline;// 0线上，1线下；
	private long kangarooId;// 若用户为袋鼠，袋鼠id
	private long userId;// 用户的userId
	private long promoter;// 发起人id
	private int isKangaroo;// 对方是否为袋鼠
	private int wasIsKangaroo;//服务对象是否为袋鼠
	
	private String insuranceNo;//保险单号；
	private String insuranceType;//保险种类；
	private String insuranceCompany;//保险公司；
	private String insuranceTime;//投保时间

	public OrderBean(JSONObject obj) throws JSONException {
		
		if (obj.has("wasIsKangaroo")) {
			this.wasIsKangaroo =obj.getInt("wasIsKangaroo");
		}
		if (obj.has("orderNumber")) {
			this.orderNumber = obj.getInt("orderNumber");
		}
		if (obj.has("createdTime")) {
			this.createdTime = obj.getString("createdTime");
		}
		if (obj.has("state")) {
			this.state = obj.getInt("state");
		}
		if (obj.has("wasId")) {
			this.wasId = obj.getLong("wasId");
		}
		if (obj.has("unitPrice")) {
			this.unitPrice = obj.getInt("unitPrice");
		}
		if (obj.has("serviceDays")) {
			this.serviceDays = obj.getInt("serviceDays");
		}
		if (obj.has("totalPrice")) {
			this.totalPrice = obj.getInt("totalPrice");
		}
		if (obj.has("avatarUrl")) {
			this.headUrl = obj.getString("avatarUrl").replace("\\", "");
		}
		if (obj.has("nickname")) {
			this.name = obj.getString("nickname");
		}
		if (obj.has("gender")) {
			this.gender = obj.getString("gender");
		}
		if (obj.has("age")) {
			this.age = obj.getInt("age");
		}
		if (obj.has("title")) {
			this.badge = obj.getInt("title");
		}
		if (obj.has("level")) {
			this.level = obj.getInt("level");
		}
		if (obj.has("cityName")) {
			this.city = obj.getString("cityName");
		}
		if (obj.has("reservationTime")) {
			this.orderTime = obj.getString("reservationTime");
		}
		if (obj.has("address")) {
			this.orderPlace = obj.getString("address");
		}
		if (obj.has("serviceRequire")) {
			this.orderServerInfo = obj.getString("serviceRequire");
		}
		if (obj.has("qrcode")) {
			this.qrcode = obj.getString("qrcode");
		}
		if (obj.has("isOnline")) {
			this.isOnline = obj.getInt("isOnline");
		}

		if (obj.has("kangarooId")) {
			this.kangarooId = obj.getLong("kangarooId");
		}

		if (obj.has("userId")) {
			this.userId = obj.getLong("userId");
		}

		if (obj.has("promoter")) {
			this.promoter = obj.getLong("promoter");
		}

		if (obj.getLong("kangarooId") != 0) {
			this.isKangaroo = Constants.ROO;
		} else {
			this.isKangaroo = Constants.KOALA;
		}
		
		if(obj.has("insuranceNo")){
			this.insuranceNo = obj.getString("insuranceNo");
		}
		if(obj.has("type")){
			this.insuranceType = obj.getString("type");
		}
		if(obj.has("company")){
			this.insuranceCompany = obj.getString("company");
		}
		if(obj.has("inTime")){
			this.insuranceTime = obj.getString("inTime");
		}
	}

	public OrderBean() {
		super();
	}

	public int getWasIsKangaroo() {
		return wasIsKangaroo;
	}

	public void setWasIsKangaroo(int wasIsKangaroo) {
		this.wasIsKangaroo = wasIsKangaroo;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getServiceDays() {
		return serviceDays;
	}

	public void setServiceDays(int serviceDays) {
		this.serviceDays = serviceDays;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public long getWasId() {
		return wasId;
	}

	public void setWasId(long wasId) {
		this.wasId = wasId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getBadge() {
		return badge;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public String getOrderPlace() {
		return orderPlace;
	}

	public void setOrderPlace(String orderPlace) {
		this.orderPlace = orderPlace;
	}

	public String getOrderServerInfo() {
		return orderServerInfo;
	}

	public void setOrderServerInfo(String orderServerInfo) {
		this.orderServerInfo = orderServerInfo;
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	public int getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(int isOnline) {
		this.isOnline = isOnline;
	}

	public long getKangarooId() {
		return kangarooId;
	}

	public void setKangarooId(long kangarooId) {
		this.kangarooId = kangarooId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getPromoter() {
		return promoter;
	}

	public void setPromoter(long promoter) {
		this.promoter = promoter;
	}

	public int getIsKangaroo() {
		return isKangaroo;
	}

	public void setIsKangaroo(int isKangaroo) {
		this.isKangaroo = isKangaroo;
	}

	public String getInsuranceNo() {
		return insuranceNo;
	}

	public void setInsuranceNo(String insuranceNo) {
		this.insuranceNo = insuranceNo;
	}

	public String getInsuranceType() {
		return insuranceType;
	}

	public void setInsuranceType(String insuranceType) {
		this.insuranceType = insuranceType;
	}

	public String getInsuranceCompany() {
		return insuranceCompany;
	}

	public void setInsuranceCompany(String insuranceCompany) {
		this.insuranceCompany = insuranceCompany;
	}

	public String getInsuranceTime() {
		return insuranceTime;
	}

	public void setInsuranceTime(String insuranceTime) {
		this.insuranceTime = insuranceTime;
	}
	
}