/**
 * 
 */
package com.elephant.ediyou.bean;

import java.io.Serializable;

/**
 * 预约(实体)
 * @author Arvin
 *
 */
public class AppointmentBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8165734190757194976L;
	private Long userID;//自己的id
	private Long rooID;//袋鼠的id
	private String orderTime;//预约时间；
	private String servicerequire;//服务内容
	private String address;//预约地点
	/**
	 * @return the userID
	 */
	public Long getUserID() {
		return userID;
	}
	/**
	 * @param userID the userID to set
	 */
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	/**
	 * @return the rooID
	 */
	public Long getRooID() {
		return rooID;
	}
	/**
	 * @param rooID the rooID to set
	 */
	public void setRooID(Long rooID) {
		this.rooID = rooID;
	}

	
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	/**
	 * @return the servicerequire
	 */
	public String getServicerequire() {
		return servicerequire;
	}
	/**
	 * @param servicerequire the servicerequire to set
	 */
	public void setServicerequire(String servicerequire) {
		this.servicerequire = servicerequire;
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	
}
