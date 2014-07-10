package com.elephant.ediyou.activity.impl;
/**
 * @author Aizhimin
 *	基础activity  规范activity的写法
 *	1、提供findView方法
 *	2、提供fillData方法
 */
public interface IBaseActivity {
	/**
	 * 获取或初始化组件
	 */
	public void findView();
	/**
	 * 填充组件数据
	 */
	public void fillData();
}
