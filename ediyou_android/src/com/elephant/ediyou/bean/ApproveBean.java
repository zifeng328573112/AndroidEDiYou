package com.elephant.ediyou.bean;

import java.io.Serializable;
/**
 * 袋鼠认证信息；
 * @author Zhoujun
 *
 */
public class ApproveBean implements Serializable {
	
	private static final long serialVersionUID = 75557365566133237L;

	private String title;//认证项目；
	private String approveDate;//认证的日期;
	private int state;//认证状态；
	private int aId;//认证项目Id；
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getApproveDate() {
		return approveDate;
	}
	public void setApproveDate(String approveDate) {
		this.approveDate = approveDate;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getaId() {
		return aId;
	}
	public void setaId(int aId) {
		this.aId = aId;
	}
	
}
