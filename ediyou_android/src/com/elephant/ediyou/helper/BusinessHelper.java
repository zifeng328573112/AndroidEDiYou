package com.elephant.ediyou.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.util.JsonReader;

import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.internet.HttpClient;
import com.elephant.ediyou.internet.PostParameter;
import com.elephant.ediyou.util.StringUtil;

/**
 * 网络访问操作
 * 
 * @author Aizhimin 说明： 1、一些网络操作方法 2、访问系统业务方法，转换成json数据对象，或者业务对象。
 */
public class BusinessHelper {
	/**
	 * 网络访问路径
	 */
	private static final String BASE_URL = "http://api.ediyou.cn/api/v1/";

	// private static final String BASE_URL =
	// "http://192.168.1.115:8080/ediyou/api/v1/";

	HttpClient httpClient = new HttpClient();

	/**
	 * 注册一
	 * 
	 * @param loginName
	 * @param password
	 * @return
	 * @throws SystemException
	 */
	public JSONObject register(String loginName, String password) throws SystemException {
		return httpClient.post(
				BASE_URL + "regist.json", 
				new PostParameter[] { 
						new PostParameter("loginName", loginName), 
						new PostParameter("plainPassword", password) }).asJSONObject();
	}

	/**
	 * 考拉注册协议
	 * 
	 * @return
	 * @throws SystemException
	 */
	public JSONObject koalaProtocol() throws SystemException {
		return httpClient.post(BASE_URL + "koala/xieyi.json").asJSONObject();
	}

	/**
	 * 袋鼠注册协议
	 * 
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooProtocol() throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/xieyi.json").asJSONObject();
	}

	/**
	 * 更新用户信息（含头像）---注册二
	 * 
	 * @param nickname
	 * @param birthday
	 * @param gender
	 * @param avatarUrl
	 *            头像文件（file类型）
	 * @param userId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileUpload(String nickname, String birthday, String gender, File file, long userId, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("nickname", nickname));
		params.add(new PostParameter("birthday", birthday));
		params.add(new PostParameter("gender", gender));
		params.add(new PostParameter("id", userId));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}
		return httpClient.multPartURL("file", BASE_URL + "profile/upload.json",
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();
	}

	/**
	 * 更新用户信息（不含头像）---注册二
	 * 
	 * @param nickname
	 *            昵称
	 * @param birthday
	 *            生日
	 * @param gender
	 *            性别
	 * @param userId
	 *            当前用户id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileUpdate(String nickname, String birthday, String gender, long userId, String accessToken) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/update.json",
				new PostParameter[] {
						new PostParameter("nickname", nickname), 
						new PostParameter("birthday", birthday), 
						new PostParameter("gender", gender), 
						new PostParameter("id", userId),
						new PostParameter("access_token", accessToken) }).asJSONObject();
	}

	/**
	 * 登录接口
	 * 
	 * @param loginName
	 * @param password
	 * @return
	 * @throws SystemException
	 */
	public JSONObject login(String loginName, String password) throws SystemException {
		return httpClient.post(BASE_URL + "login.json", 
				new PostParameter[] { 
				new PostParameter("loginName", loginName), 
				new PostParameter("plainPassword", password) }).asJSONObject();
	}

	/**
	 * 第三方登陆
	 * 
	 * @param tType
	 *            第三方供应商0-新浪微博1-腾讯微博
	 * @param tId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject thirdLogin(int tType, String tId) throws SystemException {
		return httpClient.post(BASE_URL + "thirdLogin.json", 
				new PostParameter[] { 
				new PostParameter("tType", tType), 
				new PostParameter("tId", tId) }).asJSONObject();
	}

	/**
	 * 通过id和access_token获取用户信息
	 * 
	 * @param uid
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getUsersInfo(long uid, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/show.json", 
				new PostParameter[] { 
				new PostParameter("uid", uid), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 获取我的订单和消息中心的数目；
	 * 
	 * @param token
	 * @param userId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getMsgNum(String token, long userId) throws SystemException {
		return httpClient.post(BASE_URL + "message/listNotiCount.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 拉黑
	 * 
	 * @param token
	 * @param userId
	 *            用户ID
	 * @param blackId
	 *            被拉黑人的ID
	 * @return
	 * @throws SystemException
	 */
	public JSONObject black(String token, long userId, long blackId) throws SystemException {
		return httpClient.post(BASE_URL + "profile/black.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("access_token", token), 
				new PostParameter("blackId", blackId) }).asJSONObject();
	}

	/**
	 * 更新背景图片
	 * 
	 * @param userId
	 * @param file
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateBg(long userId, File file) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		return httpClient.multPartURL("file", BASE_URL + "profile/upBg.json", 
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();
	}

	/**
	 * 获取个人介绍；
	 * 
	 * @param userId
	 * @param file
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getIntroduction(long userId, String token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/intro.json", 
				new PostParameter[] { 
				new PostParameter("access_token", token), 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 更新个人介绍；
	 * 
	 * @param id
	 * @param intro
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateIntro(long id, String intro, String token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/upIntro.json", 
				new PostParameter[] { 
				new PostParameter("id", id), 
				new PostParameter("intro", intro), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 获取个人信息；
	 * 
	 * @param userId
	 * @param file
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getInfo(long userId) throws SystemException {
		return httpClient.post(BASE_URL + "profile/info.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 更新个人信息；
	 * 
	 * @param userId
	 * @param token
	 * @param height
	 * @param weight
	 * @param hobby
	 * @param nationality
	 * @param constellation
	 * @param animalyear
	 * @param faith
	 * @param provinceId
	 * @param cityId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateInfo(long userId, String token, int height, int weight, String hobby, String nationality, String constellation, String animalyear, String faith, int provinceId,
			int cityId, String birthday, String nation, String nickname, String telephone) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/updateDetail.json",
				new PostParameter[] { 
				new PostParameter("access_token", token), 
				new PostParameter("userId", userId), 
				new PostParameter("height", height), 
				new PostParameter("weight", weight),
				new PostParameter("hobby", hobby), 
				new PostParameter("nationality", nationality), 
				new PostParameter("constellation", constellation),
				new PostParameter("animalyear", animalyear), 
				new PostParameter("faith", faith), 
				new PostParameter("provinceId", provinceId), 
				new PostParameter("cityId", cityId),
				new PostParameter("birthday", birthday), 
				new PostParameter("nation", nation), 
				new PostParameter("nickname", nickname), 
				new PostParameter("telephone", telephone) }).asJSONObject();
	}

	/**
	 * 获取袋鼠认证信息；
	 * 
	 * @param userId
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getAuthInfo(long userId, String token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/authInfo.json", 
				new PostParameter[] { 
				new PostParameter("access_token", token), 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 获取袋鼠所有认证信息；
	 * 
	 * @param userId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getRooAuthInfo(long userId) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/getAuthThrough.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 更新袋鼠服务信息
	 * 
	 * @param rooId
	 * @param token
	 * @param service
	 * @param language
	 * @param price
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateRooService(long rooId, String token, String service, String language, double price) throws SystemException {
		return httpClient.post(
				BASE_URL + "/profile/updateKangaroo.json",
				new PostParameter[] { 
				new PostParameter("userId", rooId), 
				new PostParameter("service", service), 
				new PostParameter("access_token", token), 
				new PostParameter("language", language),
				new PostParameter("price", price) }).asJSONObject();
	}

	/**
	 * 更新基本信息；
	 * 
	 * @param id
	 * @param height
	 * @param weight
	 * @param hobby
	 * @param telephone
	 * @param city
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateBasicInfo(long id, int height, int weight, String hobby, String telephone, int city, int pId, String token) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/updateKangarooBasic.json",
				new PostParameter[] { 
				new PostParameter("id", id), 
				new PostParameter("height", height), 
				new PostParameter("weight", weight), 
				new PostParameter("hobby", hobby),
				new PostParameter("telephone", telephone), 
				new PostParameter("city", city), 
				new PostParameter("pId", pId), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 更新详细信息
	 * 
	 * @param id
	 * @param nationality
	 * @param birthday
	 * @param constellation
	 * @param nation
	 * @param animalyear
	 * @param faith
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateDetailInfo(long id, String nationality, String birthday, String constellation, String nation, String animalyear, String faith, String token) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/updateKangarooDetail.json",
				new PostParameter[] { 
				new PostParameter("userId", id), 
				new PostParameter("nationality", nationality),
				new PostParameter("birthday", birthday), 
				new PostParameter("nation", nation),
				new PostParameter("constellation", constellation), 
				new PostParameter("animalyear", animalyear), 
				new PostParameter("faith", faith), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 获取袋鼠或者考拉的个人照片
	 * 
	 * @param userId
	 *            袋鼠或者考拉的userId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getPersonalPhoto(long userId, int pageNo, int pageSize) throws SystemException {
		return httpClient.post(BASE_URL + "koala/koalaPhoto.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("pageNo", pageNo), 
				new PostParameter("pageSize", pageSize) }).asJSONObject();
	}

	/**
	 * 删除个人照片；
	 * 
	 * @param photoId
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject delPersonalPhoto(long photoId, String token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/delPhotoById.json", 
				new PostParameter[] { 
				new PostParameter("id", photoId), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 上传个人照片
	 * 
	 * @param userId
	 * @param file
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject uploadPersonalPhoto(long userId, File file, String token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("access_token", token));
		return httpClient.multPartURL("file", BASE_URL + "profile/upPhoto.json", 
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();
	}

	/**
	 * 获取袋鼠更改最大佣金；
	 * 
	 * @param level
	 * @param token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getMaxCommission(String level, String token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/selMaxMoney.json", 
				new PostParameter[] { 
				new PostParameter("level", level), 
				new PostParameter("access_token", token) }).asJSONObject();
	}

	/**
	 * 通过用户id获取袋鼠id
	 * 
	 * @param uid
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileKangarooId(long userId, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/kangarooId.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 
	 * 获取考拉服务信息；
	 * 
	 * @param rooId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getKoalaInfo(long rooId) throws SystemException {
		return httpClient.post(BASE_URL + "koala/koala.json", 
				new PostParameter[] { 
				new PostParameter("id", rooId) }).asJSONObject();
	}

	/**
	 * 首页袋鼠列表
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @param city
	 *            城市Id
	 * @param pId
	 *            省份Id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooList(int pageNo, int pageSize, Integer city, Integer pId) throws SystemException {
		if (city == null && pId != null) {
			return httpClient.post(BASE_URL + "kangroo/kangarooList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize), 
					new PostParameter("pId", pId) }).asJSONObject();
		} else if (city != null && pId == null) {
			return httpClient.post(BASE_URL + "kangroo/kangarooList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize), 
					new PostParameter("city", city) }).asJSONObject();
		} else if (city == null && pId == null) {
			return httpClient.post(BASE_URL + "kangroo/kangarooList.json", 
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize) }).asJSONObject();
		} else {
			return httpClient.post(BASE_URL + "kangroo/kangarooList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize), 
					new PostParameter("city", city), 
					new PostParameter("pId", pId) }).asJSONObject();
		}

	}

	/**
	 * 获取location数据（热门城市、省、市）
	 * 
	 * @return
	 * @throws SystemException
	 */
	public JSONObject addressShow() throws SystemException {
		return httpClient.post(BASE_URL + "address/show.json").asJSONObject();
	}

	/**
	 * 获取袋鼠服务信息；
	 * 
	 * @param rooId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getRooServiceInfo(long rooId) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/kangroo.json", 
				new PostParameter[] { 
				new PostParameter("id", rooId) }).asJSONObject();
	}

	/**
	 * 获取考拉信息(New)；
	 * 
	 * @param rooId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getKoalaInfoNew(long userId) throws SystemException {
		return httpClient.post(BASE_URL + "koala/koalaInfo.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 获取袋鼠服务信息(New)；
	 * 
	 * @param rooId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getRooInfo(long rooId) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/kangrooNew.json", 
				new PostParameter[] { 
				new PostParameter("userId", rooId) }).asJSONObject();
	}

	/**
	 * 获取袋鼠日程安排；
	 * 
	 * @param yearMonth
	 *            日期格式为2012-11
	 * @param rooId
	 *            袋鼠ID
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getRooSchedule(String yearMonth, long rooId) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/schedule.json", 
				new PostParameter[] { 
				new PostParameter("yearMonth", yearMonth), 
				new PostParameter("kangarooId", rooId) }).asJSONObject();
	}

	/**
	 * 日程更新接口
	 * 
	 * @param rooId
	 * @param date
	 * @param dateType
	 *            1.表示忙碌2.表示雇佣
	 * @return
	 * @throws SystemException
	 */
	public JSONObject setRooSchedule(long rooId, String date, String dayType) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/setSchedule.json",
				new PostParameter[] { 
				new PostParameter("kangarooId", rooId), 
				new PostParameter("date", date), 
				new PostParameter("dayType", dayType) }).asJSONObject();
	}

	/**
	 * 取消忙碌
	 * 
	 * @param rooId
	 * @param date
	 * @return
	 * @throws SystemException
	 */
	public JSONObject delRooSchedule(long rooId, String date) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/delSchedule.json", 
				new PostParameter[] { 
				new PostParameter("kangarooId", rooId), 
				new PostParameter("date", date) }).asJSONObject();
	}

	/**
	 * 当订单结束后，双方评价的接口。通过isKangaroo判断是被预约方或预约方。
	 * 
	 * @param kangarooId
	 *            被预约方id
	 * @param promoter
	 *            预约方id
	 * @param level
	 *            星级
	 * @param orderNumber
	 *            订单号
	 * @param content
	 *            评论内容
	 * @param access_token
	 * @param isKangaroo
	 *            ：用于判断是被预约方或预约方
	 * @return
	 * @throws SystemException
	 */
	public JSONObject orderCommentInsert(long kangarooId, long promoter, int level, int orderNumber, String content, String access_token, boolean firstComment) throws SystemException {
		if (firstComment == false) {
			return httpClient.post(
					BASE_URL + "profile/kangarooComments.json",
					new PostParameter[] { 
					new PostParameter("kangarooId", kangarooId), 
					new PostParameter("promoter", promoter), 
					new PostParameter("kLevel", level),
					new PostParameter("orderNumber", orderNumber), 
					new PostParameter("kContent", content), 
					new PostParameter("access_token", access_token) }).asJSONObject();
		} else {
			return httpClient.post(
					BASE_URL + "profile/koalaComments.json",
					new PostParameter[] { 
					new PostParameter("kangarooId", kangarooId), 
					new PostParameter("promoter", promoter), 
					new PostParameter("pLevel", level),
					new PostParameter("orderNumber", orderNumber), 
					new PostParameter("pContent", content), 
					new PostParameter("access_token", access_token) }).asJSONObject();

		}

	}

	/**
	 * 考拉生成订单接口
	 * 
	 * @param userId
	 *            自己的id
	 * @param rooId
	 *            袋鼠的id
	 * @param reservationTime
	 *            预约时间 如:2012-12-12,2012-12-16,2012-12-20
	 * @param serviceRequire
	 *            服务内容
	 * @param address
	 *            预约地点
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileCreateOrder(long userId, long rooId, String serviceRequire, String address, String reservationTime, String access_token) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/insertRes.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("kangarooId", rooId), 
				new PostParameter("reservationTime", reservationTime),
				new PostParameter("serviceRequire", serviceRequire), 
				new PostParameter("address", address), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 获取订单详情（已有订单的情况下）
	 * 
	 * @param orderNumber
	 * @param userId
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileOrderDetail(int orderNumber, long userId, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/orderDetail.json",
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("userId", userId), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 更改订单状态
	 * 
	 * @param orderNumber
	 * @param state
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateOrderState(String orderNumber, int state, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/updateOrderState.json",
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("state", state), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 袋鼠确认订单；
	 * 
	 * @param orderNumber
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooConfirmOrder(int orderNumber, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/kangarooConfirm.json", 
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 免费券支付接口；
	 * 
	 * @param userId
	 * @param orderNumber
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject payUseCoupon(long userId, int orderNumber, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/payCash.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 账户支付接口；
	 * 
	 * @param userId
	 * @param orderNumber
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject payUseAccount(long userId, int orderNumber, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/pay.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 袋鼠确认预约接口
	 * 
	 * @param orderNum
	 *            订单号
	 * @param state
	 *            修改状态
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileUpdateOrderState(String orderNum, int state, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "profile/updateOrderState.json",
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNum), 
				new PostParameter("state", state), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 我的订单表接口
	 * 
	 * @param orderNum
	 *            订单号
	 * @param state
	 *            修改状态
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileMyOrders(long userID, int pageNo, int pageSize, String access_token, Integer state, Integer state2, Integer state3, Integer state4, Integer state5) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", userID));
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		params.add(new PostParameter("access_token", access_token));
		params.add(new PostParameter("state", state));
		if (state2 != null) {
			params.add(new PostParameter("state2", state2));
		}
		if (state3 != null) {
			params.add(new PostParameter("state3", state3));
		}
		if (state4 != null) {
			params.add(new PostParameter("state4", state4));
		}
		if (state5 != null) {
			params.add(new PostParameter("state5", state5));
		}
		return httpClient.post(BASE_URL + "profile/myorders.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();

	}

	/**
	 * 我收到的订单表接口
	 * 
	 * @param rooId
	 *            袋鼠id
	 * @param pageNo
	 * @param pageSize
	 * @param access_token
	 * @param state
	 * @param state2
	 * @param state3
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileReorders(long rooId, int pageNo, int pageSize, String access_token, Integer state, Integer state2, Integer state3, Integer state4, Integer state5) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", rooId));
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		params.add(new PostParameter("access_token", access_token));
		params.add(new PostParameter("state", state));
		if (state2 != null) {
			params.add(new PostParameter("state2", state2));
		}
		if (state3 != null) {
			params.add(new PostParameter("state3", state3));
		}
		if (state4 != null) {
			params.add(new PostParameter("state4", state4));
		}
		if (state5 != null) {
			params.add(new PostParameter("state5", state5));
		}
		return httpClient.post(BASE_URL + "profile/reorders.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 当用户选择线下支付时调用。
	 * 
	 * @param orderNumber
	 * @param accessToken
	 * @return
	 * @throws SystemException
	 */
	public JSONObject profileUpdateOrderOnline(String orderNumber, String accessToken) throws SystemException {
		return httpClient.post(BASE_URL + "profile/updateOrderOnline.json",
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("access_token", accessToken) }).asJSONObject();
	}

	/**
	 * 获取未读系统通知
	 * 
	 * @param userID
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject unreadlistNotify(int userID, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "message/listNoti.json", 
				new PostParameter[] { 
				new PostParameter("userId", userID), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 获取私信会话列表
	 * 
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject listConv(long userId, int pageIndex, int pageSize, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("pageIndex", pageIndex));
		params.add(new PostParameter("pageSize", pageSize));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}

		return httpClient.post(BASE_URL + "message/listConv.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 系统消息
	 * 
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject listNoti(long userId, int pageNo, int pageSize, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}

		/*
		 * return httpClient.post(BASE_URL + "message/listNoti.json",
		 * params.toArray(new PostParameter[params.size()])).asJSONObject();
		 */
		return httpClient.post(BASE_URL + "message/allListNoti.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 黑名单
	 * 
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject blackList(long userId, int pageNo, int pageSize, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}

		return httpClient.post(BASE_URL + "profile/blackList.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 黑名单删除
	 * 
	 * @param userId
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject blackListDel(long id, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", id));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}

		return httpClient.post(BASE_URL + "profile/delBlack.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 获取某个会话的私信列表
	 * 
	 * @param userID
	 * @param friendId
	 * @param pageIndex
	 * @param pageSize
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject listLetter(long userID, long friendId, String dateline, String access_token) throws SystemException {
		return httpClient.post(
				BASE_URL + "message/listLetter.json",
				new PostParameter[] { 
				new PostParameter("userId", userID), 
				new PostParameter("friendId", friendId), 
				new PostParameter("dateline", dateline),
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 判断用户是否袋鼠；
	 * 
	 * @param id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject isRoo(long id) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/isKangaroo.json", 
				new PostParameter[] { 
				new PostParameter("id", id) }).asJSONObject();
	}

	// access_token true string
	// userId true long 用户id
	// friendId true long 好友id
	// dateline true string

	/**
	 * 发送私信
	 * 
	 * @param content
	 * @param sender
	 *            发送者
	 * @param receiver
	 *            接受者
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject sendLetter(String content, long sender, long receiver, String access_token) throws SystemException {
		return httpClient.post(
				BASE_URL + "message/sendLetter.json",
				new PostParameter[] { 
				new PostParameter("content", content), 
				new PostParameter("sender", sender), 
				new PostParameter("receiver", receiver),
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 接受私信接口
	 * 
	 * @param content
	 * @param sender
	 * @param receiver
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject receiveLetter(long sender, long receiver, String access_token) throws SystemException {
		return httpClient.post(BASE_URL + "message/receiveLetter.json",
				new PostParameter[] { 
				new PostParameter("sender", sender), 
				new PostParameter("receiver", receiver), 
				new PostParameter("access_token", access_token) }).asJSONObject();
	}

	/**
	 * 获取活动类型
	 * 
	 * @return
	 * @throws SystemException
	 */
	public JSONObject activityFindAllType() throws SystemException {
		return httpClient.post(BASE_URL + "activity/findAllType.json").asJSONObject();
	}

	/**
	 * 新建活动
	 * 
	 * @param userId
	 * @param title
	 * @param content
	 * @param startTime
	 * @param endTime
	 * @param limitCount
	 * @param type
	 * @param file
	 * @param cost
	 * @return
	 * @throws SystemException
	 */
	public JSONObject insertActivity(long userId, String title, String content, String startTime, String endTime, int limitCount, int type, File file, double cost, long kangarooId)
			throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("promoter", userId));
		params.add(new PostParameter("title", title));
		params.add(new PostParameter("content", content));
		params.add(new PostParameter("startTime", startTime));
		params.add(new PostParameter("endTime", endTime));
		params.add(new PostParameter("limitCount", limitCount));
		params.add(new PostParameter("type", type));
		params.add(new PostParameter("cost", cost));
		params.add(new PostParameter("kangarooId", kangarooId));

		return httpClient.multPartURL("file", BASE_URL + "activity/insertActivity.json", 
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();
	}

	/**
	 * 获取活动详情
	 * 
	 * @param activityId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject loadEventDetail(long activityId) throws SystemException {
		return httpClient.post(BASE_URL + "activity/activityDetail.json", 
				new PostParameter[] { 
				new PostParameter("activityId", activityId) }).asJSONObject();
	}

	/**
	 * 参加活动
	 * 
	 * @param activityId
	 * @param userId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject joinActivity(long activityId, long userId) throws SystemException {
		return httpClient.post(BASE_URL + "activity/joinActivity.json", 
				new PostParameter[] { 
				new PostParameter("activityId", activityId), 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 发送评论
	 * 
	 * @param activityId
	 * @param userId
	 * @param content
	 * @return
	 * @throws SystemException
	 */
	public JSONObject insertComments(long activityId, long userId, String content) throws SystemException {
		return httpClient.post(BASE_URL + "activity/insertComments.json",
				new PostParameter[] { 
				new PostParameter("activityId", activityId), 
				new PostParameter("userId", userId), 
				new PostParameter("content", content) }).asJSONObject();
	}

	/**
	 * 收藏
	 * 
	 * @param activityId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject upLikeCount(long activityId, long userId) throws SystemException {
		return httpClient.post(BASE_URL + "activity/upLikeCount.json", 
				new PostParameter[] { 
				new PostParameter("activityId", activityId), 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 获取当前活动图片
	 * 
	 * @param activityId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws SystemException
	 */
	public JSONObject activityPhoto(long activityId, int pageNo, int pageSize) throws SystemException {
		return httpClient.post(BASE_URL + "activity/activityPhoto.json",
				new PostParameter[] { 
				new PostParameter("activityId", activityId), 
				new PostParameter("pageNo", pageNo), 
				new PostParameter("pageSize", pageSize) }).asJSONObject();
	}

	/**
	 * 上传活动图片
	 * 
	 * @param activityId
	 * @param file
	 * @return
	 * @throws SystemException
	 */
	public JSONObject insertActivityPhoto(long activityId, File file) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("activityId", activityId));
		return httpClient.multPartURL("file", BASE_URL + "activity/insertActivityPhoto.json", 
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();

	}

	/**
	 * 删除活动照片
	 * 
	 * @param id
	 *            当前照片id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject delActivityPhoto(long id) throws SystemException {
		return httpClient.post(BASE_URL + "activity/delPhoto.json", 
				new PostParameter[] { 
				new PostParameter("id", id) }).asJSONObject();

	}

	/**
	 * 赞当前的活动照片
	 * 
	 * @param id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject upActivityPhotoLikeCount(long id, long userId) throws SystemException {
		return httpClient.post(BASE_URL + "activity/upPhotoLikeCount.json", 
				new PostParameter[] { 
				new PostParameter("id", id), 
				new PostParameter("userId", userId) }).asJSONObject();

	}

	/**
	 * 活动首页活动列表请求
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * 
	 * @param city
	 *            城市Id
	 * @param pId
	 *            省Id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject eventList(int pageNo, int pageSize, Integer city, Integer pId) throws SystemException {
		if (city == null && pId != null) {
			return httpClient.post(BASE_URL + "activity/activityList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize), 
					new PostParameter("pId", pId) }).asJSONObject();
		} else if (city != null && pId == null) {
			return httpClient.post(BASE_URL + "activity/activityList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize), 
					new PostParameter("city", city) }).asJSONObject();
		} else if (city == null && pId == null) {
			return httpClient.post(BASE_URL + "activity/activityList.json", 
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize) }).asJSONObject();
		} else {
			return httpClient.post(BASE_URL + "activity/activityList.json",
					new PostParameter[] { 
					new PostParameter("pageNo", pageNo), 
					new PostParameter("pageSize", pageSize),
					new PostParameter("city", city),
					new PostParameter("pId", pId) }).asJSONObject();
		}

	}

	/**
	 * 活动首页活动推荐页
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @return
	 * @throws SystemException
	 */
	public JSONObject eventRecommList() throws SystemException {
		return httpClient.post(BASE_URL + "activity/selRecomAct.json").asJSONObject();
	}

	/**
	 * 活动搜索请求
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @return
	 * @throws SystemException
	 */
	public JSONObject eventSearch(int pageNo, int pageSize, long type, String title, String startTime, int start, int end) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		params.add(new PostParameter("type", type));
		if (!StringUtil.isBlank(title)) {
			params.add(new PostParameter("title", title));
		}
		if (!StringUtil.isBlank(startTime)) {
			params.add(new PostParameter("startTime", startTime));
		}
		params.add(new PostParameter("start", start));
		params.add(new PostParameter("end", end));
		return httpClient.post(BASE_URL + "activity/searchActivity.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 袋鼠推荐列表
	 * 
	 * @param kangarooId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooRecommendList(long kangarooId, int pageNo, int pageSize) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/kangarooRecom.json",
				new PostParameter[] { 
				new PostParameter("kangarooId", kangarooId), 
				new PostParameter("pageNo", pageNo), 
				new PostParameter("pageSize", pageSize) }).asJSONObject();
	}

	/**
	 * 袋鼠推荐的详情
	 * 
	 * @param recomId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooRecommendDetail(long recomId, int pageNo, int pageSize) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/kangarooRecomDetail.json",
				new PostParameter[] { 
				new PostParameter("recomId", recomId), 
				new PostParameter("pageNo", pageNo), 
				new PostParameter("pageSize", pageSize) }).asJSONObject();
	}

	/**
	 * 新建推荐
	 * 
	 * @param kangarooId
	 * @param title
	 * @param content
	 * @param coverFile
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooInsertRecom(long kangarooId, String title, String content, File coverFile) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("kangarooId", kangarooId));
		params.add(new PostParameter("title", title));
		params.add(new PostParameter("content", content));
		return httpClient.multPartURL("file", BASE_URL + "kangroo/insertRecom.json", 
				params.toArray(new PostParameter[params.size()]), coverFile).asJSONObject();

	}

	/**
	 * 上传推荐图片
	 * 
	 * @param recomId
	 * @param coverFile
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooUpLoadRecomPic(long recomId, File coverFile) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("recomId", recomId));
		return httpClient.multPartURL("file", BASE_URL + "kangroo/upLoad.json", 
				params.toArray(new PostParameter[params.size()]), coverFile).asJSONObject();

	}

	/**
	 * 删除袋鼠推荐的图片
	 * 
	 * @param photoid
	 * @return
	 * @throws SystemException
	 */
	public JSONObject delKangarooRecommendPhoto(long photoid) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/delKangarooPhoto.json", 
				new PostParameter[] { 
				new PostParameter("id", photoid) }).asJSONObject();
	}

	/**
	 * 修改袋鼠推荐详情(不含主题图)
	 * 
	 * @param recomId
	 * @param title
	 * @param content
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooUpRecommend(long recomId, String title, String content) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", recomId));
		params.add(new PostParameter("title", title));
		params.add(new PostParameter("content", content));
		return httpClient.post(BASE_URL + "kangroo/upRecommend.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 修改袋鼠推荐详情的主题图
	 * 
	 * @param recomId
	 * @param coverFile
	 * @return
	 * @throws SystemException
	 */
	public JSONObject kangarooUpRecommendPhoto(long recomId, File coverFile) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", recomId));
		return httpClient.multPartURL("file", BASE_URL + "kangroo/upRecommendPhoto.json", 
				params.toArray(new PostParameter[params.size()]), coverFile).asJSONObject();
	}

	/**
	 * 袋鼠当前活动搜索请求
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooCurrEvent(int pageNo, int pageSize, long userId) throws SystemException {
		return httpClient.post(BASE_URL + "kangroo/currentActivity.json",
				new PostParameter[] { new PostParameter("pageNo", pageNo), 
				new PostParameter("pageSize", pageSize), 
				new PostParameter("userId", userId) }).asJSONObject();
	}

	/**
	 * 袋鼠搜索
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooSearch(int pageNo, int pageSize, int city, String title, int count, int level, String birthday, String gender) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		if (0 != city) {
			params.add(new PostParameter("city", city));
		}
		if (!StringUtil.isBlank(title)) {
			params.add(new PostParameter("title", title));
		}
		if (0 != count) {
			params.add(new PostParameter("count", count));
		}
		if (0 != level) {
			params.add(new PostParameter("level", level));
		}
		if (!StringUtil.isBlank(birthday)) {
			params.add(new PostParameter("birthday", birthday));
		}
		if (!StringUtil.isBlank(gender)) {
			params.add(new PostParameter("gender", gender));
		}

		return httpClient.post(BASE_URL + "kangroo/selectKangaroo.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 考拉当前活动搜索请求
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @return
	 * @throws SystemException
	 */
	public JSONObject currHadJoinEvent(int pageNo, int pageSize, long userId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		params.add(new PostParameter("userId", userId));
		return httpClient.post(BASE_URL + "profile/joinActivity.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 账户信息
	 * 
	 * @param pageNo
	 *            分页页码
	 * @param pageSize
	 *            每页数量
	 * @param userId
	 *            用户ID
	 * @param state
	 *            0-收入1-支出
	 * @return
	 * @throws SystemException
	 */
	public JSONObject accountInfo(int pageNo, int pageSize, long userId, int state, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("state", state));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}
		return httpClient.post(BASE_URL + "profile/account.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 袋鼠搜索
	 * 
	 * @param access_token
	 *            网络句柄
	 * @param userId
	 *            用户ID
	 * @param code
	 *            代金券系列号
	 * @return
	 * @throws SystemException
	 */
	public JSONObject vouchersPrepaid(String access_token, long userId, long code) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();

		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("code", code));

		return httpClient.post(BASE_URL + "profile/vouchers.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 袋鼠服务认证
	 * 
	 * @param userId
	 *            用户ID
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooAuth(long userId, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		if (!StringUtil.isBlank(access_token)) {
			params.add(new PostParameter("access_token", access_token));
		}

		return httpClient.post(BASE_URL + "profile/kangarooAuth.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 袋鼠认证验证
	 * 
	 * @param userId
	 *            用户ID
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooAuthCheck(long userId, String code, String realyName, File file, long authcardId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		if (!StringUtil.isBlank(code)) {
			params.add(new PostParameter("code", code));
		}
		if (!StringUtil.isBlank(realyName)) {
			params.add(new PostParameter("realyName", realyName));
		}
		params.add(new PostParameter("authcardId", authcardId));
		if (null == file) {
			return httpClient.post(BASE_URL + "kangroo/insertAuth.json", 
					params.toArray(new PostParameter[params.size()])).asJSONObject();
		} else {
			return httpClient.multPartURL("file", BASE_URL + "kangroo/insertAuthFile.json", 
					params.toArray(new PostParameter[params.size()]), file).asJSONObject();
		}
	}

	/**
	 * 认证失败后再次认证；
	 * 
	 * @param userId
	 * @param code
	 * @param file
	 * @param authcardId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooAuthAgain(long userId, String code, File file, long authcardId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		if (!StringUtil.isBlank(code)) {
			params.add(new PostParameter("code", code));
		}
		params.add(new PostParameter("authcardId", authcardId));
		return httpClient.multPartURL("file", BASE_URL + "profile/kangarooKangarooAuth.json", 
				params.toArray(new PostParameter[params.size()]), file).asJSONObject();
	}

	/**
	 * 获取当前用户（袋鼠）的服务信息（新的接口）
	 * 
	 * @param userId
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getRooServiceInfoNew(long userId, String access_token) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("access_token", access_token));
		return httpClient.post(BASE_URL + "profile/kangarooService.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 设置相册中某个图片为用户的头像
	 * 
	 * @param userId
	 * @param access_token
	 * @param avatarUrl
	 * @return
	 * @throws SystemException
	 */
	public JSONObject setAvatarPhoto(long userId, String access_token, String avatarUrl) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("access_token", access_token));
		params.add(new PostParameter("avatarUrl", avatarUrl));
		return httpClient.post(BASE_URL + "profile/updateAvatarUrl.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 获取当前用户所拥有的评论数据，包含预约评论（订单）和活动评论，通过type值来判断分别获取
	 * 
	 * @param userId
	 * @param access_token
	 * @param pageNo
	 * @param pageSize
	 * @param type
	 *            :type为0，获取预约评论（订单）；type为1，获取活动评论。
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getCommentList(long userId, int pageNo, int pageSize, int type) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("pageNo", pageNo));
		params.add(new PostParameter("pageSize", pageSize));
		if (type == 0) {// 获取订单评论
			return httpClient.post(BASE_URL + "profile/orderComments.json", 
					params.toArray(new PostParameter[params.size()])).asJSONObject();
		} else if (type == 1) {// 获取活动评论
			return httpClient.post(BASE_URL + "profile/activityComments.json",
					params.toArray(new PostParameter[params.size()])).asJSONObject();
		} else {
			return null;
		}
	}

	/**
	 * 更新活动图片转发数
	 * 
	 * @param photoId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateAcPhotoForwordCount(long photoId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("id", photoId));
		return httpClient.post(BASE_URL + "activity/photoForword.json",
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 更新活动转发数
	 * 
	 * @param activityId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateAcForwordCount(long activityId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("activityId", activityId));
		return httpClient.post(BASE_URL + "activity/activityForword.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 考拉评价活动
	 * 
	 * @param userId
	 * @param activityId
	 * @param uContent
	 * @param uLevel
	 * @return
	 * @throws SystemException
	 */
	public JSONObject koalaCommentInEvent(long userId, long activityId, String uContent, int uLevel, long wasId, long notiId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("activityId", activityId));
		params.add(new PostParameter("uContent", uContent));
		params.add(new PostParameter("uLevel", uLevel));
		params.add(new PostParameter("promoter", wasId));
		params.add(new PostParameter("notiId", notiId));
		return httpClient.post(BASE_URL + "activity/insertEvaByKoala.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 袋鼠评价活动
	 * 
	 * @param userId
	 * @param activityId
	 * @param pContent
	 * @param pLevel
	 * @return
	 * @throws SystemException
	 */
	public JSONObject rooCommentInEvent(long userId, long activityId, String pContent, int pLevel, long promoter, long notiId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("activityId", activityId));
		params.add(new PostParameter("pContent", pContent));
		params.add(new PostParameter("pLevel", pLevel));
		params.add(new PostParameter("promoter", promoter));
		params.add(new PostParameter("notiId", notiId));
		return httpClient.post(BASE_URL + "activity/insertEvaByKangaroo.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 举报订单
	 * 
	 * @param userId
	 * @param contents
	 * @param informerId
	 * @param access_token
	 * @return
	 * @throws SystemException
	 */
	public JSONObject reportOrder(long userId, String contents, long informerId, String access_token, String orderNum) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("contents", contents));
		params.add(new PostParameter("informerId", informerId));
		params.add(new PostParameter("access_token", access_token));
		params.add(new PostParameter("orderNum", orderNum));

		return httpClient.post(BASE_URL + "profile/report.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 获取首页Banner数据
	 * 
	 * @param cityId
	 * @param provinceId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getBannerList(Integer cityId, Integer provinceId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		if (cityId != null) {
			params.add(new PostParameter("cityId", cityId));
		}
		if (provinceId != null) {
			params.add(new PostParameter("provinceId", provinceId));
		}
		return httpClient.post(BASE_URL + "banner/bannerList.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 
	 * 找回密码（邮箱）
	 * 
	 * @param email
	 * @return
	 * @throws SystemException
	 */
	public JSONObject sendUpdatePassEmail(String email) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("email", email));
		return httpClient.post(BASE_URL + "profile/sendUpdatePassEmail.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 获取所有的爱好标签
	 * 
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getAllHobbyList() throws SystemException {
		return httpClient.post(BASE_URL + "profile/allHobbyList.json").asJSONObject();
	}

	/**
	 * 获取当前用户的爱好标签
	 * 
	 * @param userId
	 * @return
	 * @throws SystemException
	 */
	public JSONObject getUserHobbyList(Long userId) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		return httpClient.post(BASE_URL + "profile/getUserHobbyList.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 用户反馈
	 * 
	 * @param userId
	 * @param access_token
	 * @param content
	 * @param telephone
	 * @return
	 * @throws SystemException
	 */
	public JSONObject upLoadUsersFeedBack(Long userId, String content, String access_token, String telephone) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("userId", userId));
		params.add(new PostParameter("content", content));
		params.add(new PostParameter("telephone", telephone));
		params.add(new PostParameter("access_token", access_token));
		return httpClient.post(BASE_URL + "profile/insertBug.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 手机号码注册账号获取验证码
	 * 
	 * @param phoneNum
	 * @return
	 * @throws SystemException
	 */
	public JSONObject registerGetCode(String phoneNum) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("phoneNum", phoneNum));
		return httpClient.post(BASE_URL + "getCode.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 验证验证码是否一致
	 * 
	 * @param phoneNum
	 * @param code
	 * @return
	 * @throws SystemException
	 */
	public JSONObject registerCheckCode(String phoneNum, String code) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("phoneNum", phoneNum));
		params.add(new PostParameter("code", code));
		return httpClient.post(BASE_URL + "checkCode.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 账户提现
	 * 
	 * @param userId
	 * @param touchMoney
	 * @return
	 * @throws SystemException
	 */
	public JSONObject withdraw(long userId, double touchMoney) throws SystemException {
		return httpClient.post(BASE_URL + "profile/saveAccountDetail.json", 
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("touchMoney", touchMoney) }).asJSONObject();
	}

	/**
	 * 修改订单已读状态
	 * 
	 * @param userId
	 * @param orderNumber
	 * @param type
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateOrderTag(long userId, String orderNumber, Integer type) throws SystemException {
		return httpClient.post(BASE_URL + "profile/updateOrderTag.json",
				new PostParameter[] { 
				new PostParameter("userId", userId), 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("type", type) }).asJSONObject();
	}

	/**
	 * 获取回调页
	 * 
	 * @return
	 */
	public String getNotifyUrl() {
		return BASE_URL + "profile/paySuccess.json";
	}

	/**
	 * 支付成功后回调页
	 * 
	 * @return
	 */
	public JSONObject getPayForSuccess(String orderNum, String accessToken) throws SystemException {
		return httpClient.post(BASE_URL + "profile/paySuccess.json", 
				new PostParameter[] { 
				new PostParameter("orderNum", orderNum), 
				new PostParameter("accessToken", accessToken) }).asJSONObject();
	}

	/**
	 * 提交保险信息
	 * 
	 * @param orderNumber
	 * @param realyName
	 * @param card
	 * @param email
	 * @param phone
	 * @return
	 * @throws SystemException
	 */
	public JSONObject uploadInsuranceInfo(String orderNumber, String realName, String card, String email, String phone, String gender) throws SystemException {
		return httpClient.post(
				BASE_URL + "profile/insurance.json",
				new PostParameter[] { 
				new PostParameter("orderNumber", orderNumber), 
				new PostParameter("realName", realName), 
				new PostParameter("card", card), 
				new PostParameter("email", email),
				new PostParameter("phone", phone), 
				new PostParameter("sex", gender) }).asJSONObject();
	}

	/**
	 * 设置消息已读
	 * 
	 * @param id
	 * @return
	 * @throws SystemException
	 */
	public JSONObject updateNotificationStatus(long id) throws SystemException {
		return httpClient.get(BASE_URL + "message/updateNotificationStatus.json", 
				new PostParameter[] { 
				new PostParameter("id", id) }).asJSONObject();
	}

	/**
	 * 找回密码获取验证码
	 * 
	 * @param phoneNum
	 * @return
	 * @throws SystemException
	 */
	public JSONObject passwordGetCode(String phoneNum) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("phoneNum", phoneNum));
		return httpClient.post(BASE_URL + "profile/getCode.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 找回密码验证验证码是否一致
	 * 
	 * @param phoneNum
	 * @param code
	 * @return
	 * @throws SystemException
	 */
	public JSONObject passwordCheckCode(String phoneNum, String code) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("phoneNum", phoneNum));
		params.add(new PostParameter("code", code));
		return httpClient.post(BASE_URL + "profile/checkCode.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 重置密码
	 * 
	 * @param phoneNum
	 * @param password
	 * @return
	 * @throws SystemException
	 */
	public JSONObject resetPassword(String phoneNum, String password) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("phone", phoneNum));
		params.add(new PostParameter("pass", password));
		return httpClient.post(BASE_URL + "profile/updatePassWord.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}

	/**
	 * 检查版本更新
	 * 
	 * @param clientType
	 * @param versCode
	 * @return
	 * @throws SystemException
	 */
	public JSONObject checkVersion(String channelType, String clientType, int versCode, String versionName) throws SystemException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("clientType", clientType));
		params.add(new PostParameter("versCode", versCode));
		params.add(new PostParameter("versionName", versionName));
		params.add(new PostParameter("channelType", channelType));
		return httpClient.get(BASE_URL + "version/version.json", 
				params.toArray(new PostParameter[params.size()])).asJSONObject();
	}
}
