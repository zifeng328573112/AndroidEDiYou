package com.elephant.ediyou.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elephant.ediyou.SystemException;

/**
 * 订单列表item对象
 * @author syghh
 *
 */
public class OrderListBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private long userId;//发起人id
	private int state;//当前的详细状态
	private String loginName;//服务对象的昵称
	private String createdTime;//创建时间
	private String reservationTime;//预约时间
	private String orderNumber;//订单编号
	private String address;//地点
	private int isOnline;//0为在线支付，1为线下支付
	private boolean isReaded;//是否已读 true已读，false未读
	
	public OrderListBean(JSONObject obj) throws JSONException {
		if(obj.has("userId")){
			this.userId = obj.getInt("userId");
		}
		if (obj.has("state")) {
			this.state = obj.getInt("state");
		}
		if (obj.has("loginName")) {
			this.loginName = obj.getString("loginName");
		}
		if (obj.has("createdTime")) {
			this.createdTime = obj.getString("createdTime");
		}
		if (obj.has("reservationTime")) {
			this.reservationTime = obj.getString("reservationTime");
		}
		if (obj.has("orderNumber")) {
			this.orderNumber = obj.getString("orderNumber");
		}
		if (obj.has("address")) {
			this.address = obj.getString("address");
		}
		if(obj.has("isOnline")){
			this.isOnline = obj.getInt("isOnline");
		}
		/*if(obj.has("status")){
			this.isReaded = obj.getInt("status") == 1 ? true:false;
		}*/
		if(obj.has("tag")){
			this.isReaded = obj.getInt("tag") == 1 ? true:false;
		}
		
	}

	public static ArrayList<OrderListBean> constractList(JSONArray jsonArray)
			throws SystemException {
		try {
			ArrayList<OrderListBean> list = new ArrayList<OrderListBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject orderListJson = jsonArray.getJSONObject(i);
				list.add(new OrderListBean(orderListJson));
			}
			return list;
		} catch (JSONException je) {
			throw new SystemException(je.getMessage());
		}
	}

	public OrderListBean() {
		super();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(String reservationTime) {
		this.reservationTime = reservationTime;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(int isOnline) {
		this.isOnline = isOnline;
	}

	public boolean isReaded() {
		return isReaded;
	}

	public void setReaded(boolean isReaded) {
		this.isReaded = isReaded;
	}

	
	
}