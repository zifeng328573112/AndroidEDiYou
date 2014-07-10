package com.elephant.ediyou;


/**
 * 常量类
 * @author Aizhimin
 * 说明：	1、一些应用常量在此定义
 * 		2、常量包括：一些类型的定义，在其他程序中不能够出现1 2 3之类的值。
 */
public class Constants {
	/**
	 * 应用文件存放目录
	 */
	public static final String APP_DIR_NAME = "etravel";
	public static final String CAMERA_DIR_NAME = "camera";
	public static final int CLIENT_TYPE = 1;
	public static final String CHANNEL_TYPE = "yingyonghui";
	/**
	 * 微博绑定类型，点击账号绑定和新浪微博
	 */
	public static final String EXTRA_BIND_FROM="extra_bind_from";
	public static final String BIND_WEIBO="bind_weibo";//微博
	public static final String BIND_RENREN="bind_renren";//绑定人人
	
	/**
	 * 微博绑定的request code
	 */
	public static final int REQUEST_CODE_BIND_QQ = 10;
	public static final int REQUEST_CODE_BIND_WEIBO = 11;
	public static final int REQUEST_CODE_BIND_RENREN = 12;
	
	/**
	 * 新浪微博配置
	 */
	public static final String WEIBO_CONSUMER_KEY = "2858374558";// 替换为开发者的appkey，例如"1646212960";
	public static final String WEIBO_CONSUMER_SECRET = "25c96172721fc71ee187bd222044a7ad";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";
	public static final String WEIBO_REDIRECT_URL = "http://www.ediyou.cn";//微博应用回调地址
	public static final String WEIBO_USER_UID = "1291843462";
	
	/**
	 * 腾讯微博配置
	 */
	public static final String TENCENT_APP_ID = "801293627";//app id
	public static final String TENCENT_APP_KEY = "f88beeeb4daddbb41732fbd178ceb245";//app key
	public static final String TENCENT_REDIRECT_URL = "http://www.ediyou.cn";
	
	/**
	 * QQ互联配置
	 */
	public static final String QQ_APP_ID = "100368530";//app id
	public static final String QQ_APP_KEY = "34c64078dac2118b2e78b08512d10f7a";//app key
	public static final String QQ_APP_SCOPE = "get_user_info,get_user_profile,add_share,add_topic,list_album,upload_pic,add_album";
	public static final String QQ_APP_TARGET ="_self";

	
	/**
	 * 人人网
	 */
	public static final String RENREN_APP_ID = "211176";//app id
	public static final String RENREN_API_KEY = "979175fc39c14a8eba6ea78f6e876c01";//api key
	public static final String RENREN_SECRET_KEY = "a113d3aa3cde431eb499f6fc37ff1e30";//secret key
	/**
	 * 微信
	 */
	public static final String WEIXIN_APP_ID = "wx86c3082e40c74a20";//app id
	public static final String WEIXIN_APP_KEY = "daf7cf859bb1618811e034c1cb188528";// api key
	/**
	 * 分享到社交平台的参数
	 */
	public static final String EXTRA_SHARE_TYPE = "extra_share_type";//平台类型
	public static final String EXTRA_RES_TYPE = "extra_res_type";//分享内容的类型：
	public static final String EXTRA_SHARE_CONTENT = "extra_share_content";//分享文字内容
	public static final String EXTRA_IMAGE_URL = "extra_image_url";//图片路径
	/**
	 * 分享到社交平台
	 */
	public static final int SHARE_TO_WEIBO = 1;
	public static final int SHARE_TO_QQ = 2;
	public static final int SHARE_TO_RENREN = 3;
	public static final int SHARE_TO_WEIXIN = 4;
	public static final int SHARE_TO_WEIXIN_FRS = 5;
	
	/**
	 * 分享内容的类型
	 */
	public static final int SHARE_RES_CONTENT = 1;//文字微博
	public static final int SHARE_RES_IMAGE = 2;//图片微博
	/**
	 * 袋鼠和考拉
	 */
	public static final int KOALA = 0;//考拉
	public static final int ROO = 1;//袋鼠
	
	/**
	 * 网络通讯状态码,成功
	 */
	public static final int SUCCESS = 1;
	/**
	 * 网络通讯状态码,失败
	 */
	public static final int FAILED = -1;
	/**
	 * 网络通讯状态码,token失效
	 */
	public static final int TOKEN_FAILED = -1;
	
	public static final String ORDER_ROO = "order_roo";
	public static final String EXTRA_DATA = "data";
	/**
	 * Activity请求码
	 */
	public static final int REQUEST_LOCATION = 1;
	
	
	
	/**
	 * 订单状态
	 */
	public static final int ROO_REFUSE = 0;  //袋鼠拒绝
	public static final int ROO_NOT_CONFIRM = 1;  //袋鼠未确认
	public static final int KOALA_NOT_PAY = 2;  //未支付
	public static final int ONLINE_PAY = 3;  //进行中、线上支付
	public static final int OFFLINE_PAY = 4;  //进行中、线下支付
	public static final int KOALA_NOT_CONFIRM = 5;  //考拉未确认
	public static final int KOALA_NOT_COMMENT = 6;  //考拉未评价
	public static final int ROO_NOT_COMMENT = 7;  //袋鼠未评价
	public static final int ORDER_DONE = 8;  //完成
	public static final int ORDER_INVALID = 9;//订单过期（失效）

	
	/**
	 * 消息通知类型
	 *
	 */	
	public static final int NOTI_RESERVATION = 1;//预约通知
	public static final int NOTI_ORDER = 2;//订单状态更改通知
	public static final int NOTI_LETTER = 3;//私信通知
	public static final int NOTI_SYSTEM=21;//系统通知

	public static final int NOTI_ACTIVITY = 6; //参加活动
	public static final int NOTI_ACTIVITY_SEND = 7;//考拉评论活动通知（袋鼠参加活动视为考拉）
	public static final int NOTI_ACTIVITY_RECEIVER = 8;//活动发起人给参加者评论
	
	public static final int NOTI_IDCARD_ACCPET=11;//身份证认证通过
	public static final int NOTI_IDCARD_REFUSED=12;//身份证认证拒绝
	public static final int NOTI_TEL_ACCPET=13;//手机号认证通过
	public static final int NOTI_TEL_REFUSED=14;//手机号认证拒绝
	public static final int NOTI_EMAIL_ACCPET=15;//邮箱认证通过
	public static final int NOTI_EMAIL_REFUSED=16;//邮箱认证拒绝
	public static final int NOTI_ALIPAY_ACCPET=17;//支付宝认证通过
	public static final int NOTI_ALIPAY_REFUSED=18;//支付宝认证拒绝
	public static final int NOTI_BANK_ACCPET=19;//银行卡认证通过
	public static final int NOTI_BANK_REFUSED=20;//银行卡认证拒绝

	/**
	 * notifyId
	 */
	public static final int NOTIFY_RESERVATION_ID = 30001;
	public static final int NOTIFY_ORDER_ID = 30002;
	public static final int NOTIFY_LETTER_ID = 30003;
	public static final int NOTIFY_SYS_ID = 30021;
	public static final int NOTIFY_ACTIVITY_ID = 30006;
	
	
	
	public static final String EXTRA_NAME = "name";
	public static final String EXTRA_USER_ID = "userId";
	public static final String EXTRA_AVATAR = "avatar";
	
	/**
	 * 瀑布流所需的字段
	 */
	public final static int COLUMN_COUNT = 2; // 显示列数
	public final static int PICTURE_COUNT_PER_LOAD = 30; // 每次加载30张图片
	public final static int PICTURE_TOTAL_COUNT = 10000;   //允许加载的最多图片数
	public final static int HANDLER_WHAT = 1;
	public final static int MESSAGE_DELAY = 200;
	
	/**
	 * 袋鼠搜索后把结果传递给结果显示ACTIVTY
	 */
	public final static String KEY_ROO_SEARCH_CITY = "key_search_city";
	public final static String KEY_ROO_SEARCH_FREE_STATE = "key_free_state";
	public final static String KEY_ROO_SEARCH_SEX = "key_sex";
	public final static String KEY_ROO_SEARCH_AGE_SECTION = "key_age_section";
	public final static String KEY_ROO_SEARCH_GOO_GRADE = "key_goo_grade";
	public final static String KEY_ROO_SEARCH_SERVICE_GRADE = "key_service_city";
	
	/**
	 * 选择切换城市时调用的来源ACTIVITY
	 */
	public final static String KEY_SEL_CITY = "select_city";
	public final static String KEY_SEL_CITY_FROM_HOME = "from_home_activity";
	public final static String KEY_SEL_CITY_FROM_EVENT = "from_event_activity";
	
	/**
	 * 活动搜索后把结果传递给结果显示ACTIVTY
	 */
	public final static String KEY_SEARCH_EVENT_TITLE = "key_event_title";
	public final static String KEY_SEARCH_EVENT_TYPE = "key_event_type";
	public final static String KEY_SEARCH_EVENT_STARTTIME = "key_event_start_time";
	public final static String KEY_SEARCH_EVENT_COSTSTART = "key_event_cost_start";
	public final static String KEY_SEARCH_EVENT_COSTEND = "key_event_cost_end";
	
	/**
	 * 消息中心的消息类型
	 */
	public final static int MSG_CENTER_TYPE_LETTER = 1;
	public final static int MSG_CENTER_TYPE_SYS = 2;
	public final static int MSG_CENTER_TYPE_BLACK = 3;
	/**
	 * activity request_code
	 */
	public final static int REQUEST_PAY_SUCCESS = 1;//跳转到付款界面(付款到第三方)；
	public final static int REQUEST_SCAN_QRCODE = 2;//扫描二维码；
	
	public final static int REQUEST_CHOOSE_START_TIME = 3;//选择活动开始时间；
	public final static int REQUEST_CHOOSE_END_TIME = 4;//选择活动结束时间；
	/**
	 * 首页Banner类型
	 */
	public final static int BANNER_TYPE_EVENT = 0;//活动
	public final static int BANNER_TYPE_WEB = 1;//web内嵌浏览器
	
	public final static int MY_ORDER = 1;//发起的订单
	public final static int RECEIVE_ORDER = 2;//收到的订单
	
}
