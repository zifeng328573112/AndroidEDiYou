package com.elephant.ediyou.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.EventCommentBean;
import com.elephant.ediyou.bean.EventDetailBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.util.WeixingHelper;
import com.elephant.ediyou.view.ListViewInScrollView;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.umeng.analytics.MobclickAgent;

/**
 * 活动详情（由活动列表进入的）
 * 
 * @author syghh
 * 
 */
public class EventDetailActivity extends Activity implements IBaseActivity, OnClickListener {
	// title
	private Button 					btnLeft;
	private Button 					btnRight;
	private TextView 				tvTitle;
	// 加载数据的progress
	private ProgressDialog 			pd;

	private LinearLayout 			llTopShow;
	private LinearLayout 			llContast;
	private ImageView 				ivTitlePhoto; // 活动封面图片
	private TextView 				tvCollect; // 收藏
	private TextView 				tvShare; // 分享
	private TextView 				tvMorePhoto; // 更多图片（数量）
	private ImageView 				ivRooHeadPhoto; // 袋鼠头像
	private TextView 				tvRooName; // 袋鼠名字
	private ImageView 				ivRooGender; // 袋鼠性别
	private TextView 				tvRooAge; // 袋鼠年龄
	private TextView 				tvRooLevel; // 袋鼠等级
	private TextView 				tvRooBadge; // 袋鼠徽章
	private Button 					btnContactRoo; // 联系袋鼠（私信）

	private TextView 				tvStartTime; // 活动开始时间
	private TextView 				tvEndTime; // 活动结束时间
	private ProgressBar 			pbPeopleNum; // 参加人数进度条
	private TextView 				tvPeopleNum; // 参加人数比
	private TextView 				tvAveragePrice; // 人均价格
	private TextView 				tvEventType;// 活动类型
	private TextView 				tvEventDescribe; // 活动描述、说明
	private Button 					btnMore;//更多详情；
	private Boolean 				isExpandMore = false;
	
	private TextView 				tvHadJoinNum; // 已参加人数
	private Gallery 				galleryHadJoin; // 已参加的人的头像

	private Button 					btnSendEvnetComment; // 发送评论留言
	private EditText 				edtEvnetComment; // 编辑评论留言

	private ListViewInScrollView 	lvEvenCommentList; // 评论发言列表

	private EventDetailBean 		eventDetailBean;
	private long 					activityId;// 活动id
	private long 					promoterId;// 活动创建人id
	private long 					userId;
	private boolean 				isSelf = false;
	private int 					screenWidth;// 屏幕宽度

	private GalleryAdapter 			galleryAdapter;
	private List<String> 			hadJoinPhotoUrls;
	private List<Long> 				userIdsHadjoins;
	private List<EventCommentBean> 	eventCommentBeans;
	private EventCommentListAdapter eventCommentListAdapter;

	private File 					localFile;

	View 							dialogV;
	Button 							btnShareToWeibo;
	Button 							btnShareToQQ;
	Button 							btnShareToWeixin;
	Button 							btnShareToWeixins;
	Button 							btnCancel;
	Dialog 							shareDialog;

//	public static IWXAPI wxApi;W
	private static final int 		TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private static final int 		THUMB_SIZE = 150;
	private String 					path;
	private boolean 				versionCanShareWeiXin;
	
	private WeixingHelper 			weixingHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		
		weixingHelper=new WeixingHelper(EventDetailActivity.this);
	
		
		int wxSdkVersion = weixingHelper.getApi().getWXAppSupportAPI();
		if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
			versionCanShareWeiXin = true;
		} else {
			versionCanShareWeiXin = false;
		}

		// 获取到活动列表页面点击活动item传过来的活动id
		if (getIntent() != null) {
			activityId = getIntent().getLongExtra("activityId", 0);
			promoterId = getIntent().getLongExtra("promoterId", 0);
		}
		// activityId = 1;// 测试
		if (NetUtil.checkNet(this)) {
			new LoadEventDetailTask(activityId).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		findView();
		((CommonApplication) getApplication()).addActivity(this);
	}
	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("报名");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("活动详情");
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		llTopShow = (LinearLayout) this.findViewById(R.id.llTopShow);
		llContast = (LinearLayout) this.findViewById(R.id.llContast);

		ivTitlePhoto = (ImageView) this.findViewById(R.id.ivTitlePhoto);
		tvCollect = (TextView) this.findViewById(R.id.tvCollect);
		tvShare = (TextView) this.findViewById(R.id.tvShare);
		tvMorePhoto = (TextView) this.findViewById(R.id.tvMorePhoto);
		ivRooHeadPhoto = (ImageView) this.findViewById(R.id.ivRooHeadPhoto);
		tvRooName = (TextView) this.findViewById(R.id.tvRooName);
		ivRooGender = (ImageView) this.findViewById(R.id.ivRooGender);
		tvRooAge = (TextView) this.findViewById(R.id.tvRooAge);
		tvRooLevel = (TextView) this.findViewById(R.id.tvRooLevel);
		tvRooBadge = (TextView) this.findViewById(R.id.tvRooBadge);
		btnContactRoo = (Button) this.findViewById(R.id.btnContactRoo);
		tvStartTime = (TextView) this.findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) this.findViewById(R.id.tvEndTime);
		pbPeopleNum = (ProgressBar) this.findViewById(R.id.pbPeopleNum);
		tvPeopleNum = (TextView) this.findViewById(R.id.tvPeopleNum);
		tvAveragePrice = (TextView) this.findViewById(R.id.tvAveragePrice);
		tvEventType = (TextView) this.findViewById(R.id.tvEventType);
		tvEventDescribe = (TextView) this.findViewById(R.id.tvEventDescribe);
		btnMore = (Button) findViewById(R.id.btnMore);
		btnMore.setOnClickListener(this);
		 
		tvHadJoinNum = (TextView) this.findViewById(R.id.tvHadJoinNum);

		galleryHadJoin = (Gallery) this.findViewById(R.id.galleryHadJoin);
		btnSendEvnetComment = (Button) this.findViewById(R.id.btnSendEvnetComment);
		edtEvnetComment = (EditText) this.findViewById(R.id.edtEvnetComment);
		lvEvenCommentList = (ListViewInScrollView) this.findViewById(R.id.lvEvenCommentList);

		ivTitlePhoto.setOnClickListener(this);
		tvCollect.setOnClickListener(this);
		tvShare.setOnClickListener(this);
		tvMorePhoto.setOnClickListener(this);
		btnContactRoo.setOnClickListener(this);
		btnSendEvnetComment.setOnClickListener(this);

		// dialog
		dialogV = getLayoutInflater().inflate(R.layout.share_dialog, null);
		btnShareToWeibo = (Button) dialogV.findViewById(R.id.btnShareToWeibo);
		btnShareToQQ = (Button) dialogV.findViewById(R.id.btnShareToQQ);
		btnShareToWeixin = (Button) dialogV.findViewById(R.id.btnShareToWeixinFre);
		btnShareToWeixins = (Button) dialogV.findViewById(R.id.btnShareToWeixinFres);
		btnCancel = (Button) dialogV.findViewById(R.id.dialog_cancel);

		btnShareToWeibo.setOnClickListener(this);
		btnShareToQQ.setOnClickListener(this);
		btnShareToWeixin.setOnClickListener(this);
		btnShareToWeixins.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		shareDialog = showShareDialog();
	}

	@Override
	public void fillData() {
		//当数据有问题时，关闭所有监听事件
		if(eventDetailBean == null){
			ivTitlePhoto.setClickable(false);
			tvCollect.setClickable(false);
			tvShare.setClickable(false);
			tvMorePhoto.setClickable(false);
			btnContactRoo.setClickable(false);
			btnSendEvnetComment.setClickable(false);
		}
		//输入字数限制（字符）
		StringUtil.limitEditTextLength(edtEvnetComment, 200, this);
		
		// 当进入的活动页面时袋鼠自己发布的页面，隐藏“报名”键
		if (SharedPrefUtil.getUserBean(this) != null) {
			userId = SharedPrefUtil.getUserBean(this).getUserId();
			if (userId == eventDetailBean.getUserId()) {
				isSelf = true;
				btnRight.setVisibility(View.INVISIBLE);
				btnContactRoo.setVisibility(View.INVISIBLE);
			} else {
				isSelf = false;
				btnRight.setVisibility(View.VISIBLE);
				btnContactRoo.setVisibility(View.VISIBLE);
			}
		}

		String coverUrl = eventDetailBean.getCoverUrl();
		ivTitlePhoto.setTag(coverUrl);
		Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(coverUrl, new ImageCallback() {
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				ImageView ivImageView = (ImageView) llTopShow.findViewWithTag(imageUrl);
				if (ivImageView != null) {
					if (imageDrawable != null) {
						int oldwidth = imageDrawable.getIntrinsicWidth();
						int oldheight = imageDrawable.getIntrinsicHeight();
						LayoutParams lp = ivImageView.getLayoutParams();
						lp.width = screenWidth;
						lp.height = (oldheight * screenWidth) / oldwidth;
						ivImageView.setLayoutParams(lp);
						ivImageView.setImageDrawable(imageDrawable);
					} else {
						ivTitlePhoto.setImageResource(R.drawable.view);
					}
				}
			}
		});
		if (cacheDrawable != null) {
			int oldwidth = cacheDrawable.getIntrinsicWidth();
			int oldheight = cacheDrawable.getIntrinsicHeight();
			LayoutParams lp = ivTitlePhoto.getLayoutParams();
			lp.width = screenWidth;
			lp.height = (oldheight * screenWidth) / oldwidth;
			ivTitlePhoto.setLayoutParams(lp);
			ivTitlePhoto.setImageDrawable(cacheDrawable);
		} else {
			ivTitlePhoto.setImageResource(R.drawable.view);
		}

		tvMorePhoto.setText(String.valueOf(eventDetailBean.getImgCount()));
		if(eventDetailBean.getImgCount()>1){
			Drawable  img_off;
			Resources res = getResources();
			img_off = res.getDrawable(R.drawable.ic_more_photo);
			// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
			img_off.setBounds(0, 0, img_off.getMinimumWidth(), img_off.getMinimumHeight());
			tvMorePhoto.setCompoundDrawables(img_off, null, null, null); //设置左图标
		}
		String rooHeadPhotoUrl = eventDetailBean.getAvatarUrl();
		ivRooHeadPhoto.setTag(rooHeadPhotoUrl);
		Drawable rooHeadCacheDrawable = ImageCacheLoader.getInstance().loadDrawable(rooHeadPhotoUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				ImageView ivPhoto = (ImageView) llContast.findViewWithTag(imageUrl);
				if (ivPhoto != null) {
					if (imageDrawable != null) {
						ivPhoto.setImageDrawable(imageDrawable);
					} else {
						ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
					}
				}
			}
		});
		if (rooHeadCacheDrawable != null) {
			ivRooHeadPhoto.setImageDrawable(rooHeadCacheDrawable);
		} else {
			ivRooHeadPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
		}
		// ivRooHeadPhoto.setLayoutParams(new LayoutParams(60,60));
		ivRooHeadPhoto.setScaleType(ScaleType.FIT_CENTER);
		tvRooName.setText(eventDetailBean.getNickName());
		if (eventDetailBean.getGender().equals("f")) {
			ivRooGender.setImageResource(R.drawable.ic_fale);
		} else if (eventDetailBean.getGender().equals("m")) {
			ivRooGender.setImageResource(R.drawable.ic_male);
		}
		tvRooAge.setText(String.valueOf(eventDetailBean.getAge()) + "岁");
		tvRooLevel.setText("LV" + String.valueOf(eventDetailBean.getLevel()));
		// tvRooBadge.setText(String.valueOf(eventDetailBean.getBadge()));
		tvStartTime.setText(eventDetailBean.getStartTime());
		tvEndTime.setText(eventDetailBean.getEndTime());
		pbPeopleNum.setProgress(eventDetailBean.getCurrentCount());
		tvPeopleNum.setText(eventDetailBean.getCurrentCount() + "/" + eventDetailBean.getLimitCount());
		CharSequence strAveragePrice = Html.fromHtml("<a><font color=\"#E70E1F\">" + "" + eventDetailBean.getCost() + "</a>" + "元");
		tvAveragePrice.setText(strAveragePrice + "/人/次");
		tvEventType.setText(eventDetailBean.getTypeName());
		tvEventDescribe.setText(eventDetailBean.getContent().replace("<BR>", "\n").replace("<br>", "\n"));
		tvEventDescribe.post(new Runnable() {
			
			@Override
			public void run() {
				if(tvEventDescribe.getLineCount() == 1){
					btnMore.setVisibility(View.GONE);
				}
//				if(tvEventDescribe.getLineCount() == 2){
//					String describeStr = tvEventDescribe.getText().toString();
//					String[] array = describeStr.split("\n");
//					int count1 = array[0].length();
//					int count2 = array[1].length();
//					if(count1 >= count2 + 5){
//						btnMore.setVisibility(View.GONE);
//					}
//				}
			}
		});
		CharSequence strHadJoinNum = Html.fromHtml("<a><font color=\"#9DD063\">" + "" + eventDetailBean.getCurrentCount() + "</a>" + "人");
		tvHadJoinNum.setText(strHadJoinNum);

		fillGalleryAdapter();
		fillListAdapter();
	}

	public void fillGalleryAdapter() {
		galleryAdapter = new GalleryAdapter(this, hadJoinPhotoUrls);
		galleryHadJoin.setAdapter(galleryAdapter);
	}

	public void fillListAdapter() {
		eventCommentListAdapter = new EventCommentListAdapter(this, eventCommentBeans);
		lvEvenCommentList.setAdapter(eventCommentListAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 参与活动（报名）
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date endTimeDate = null;
			Date currentDate = null;
			if (eventDetailBean != null) {
				try {
					currentDate = df.parse(eventDetailBean.getCurrentTime());
					endTimeDate = df.parse(eventDetailBean.getEndTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				long endTime = endTimeDate.getTime();
				long curTime = currentDate.getTime();
				if (!SharedPrefUtil.checkToken(this)) {
					startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));
				} else if (userIdsHadjoins.contains(userId)) {
					Toast.makeText(this, "您已参加过这个活动了~~", Toast.LENGTH_LONG).show();
				} else if (curTime >= endTime) {
					Toast.makeText(this, "报名已经结束了，逛逛其它的活动吧~~", Toast.LENGTH_LONG).show();
				} else if (eventDetailBean.getCurrentCount() == eventDetailBean.getLimitCount()) {
					Toast.makeText(this, "活动满员咯，逛逛其它的活动吧~~", Toast.LENGTH_LONG).show();
				} else {
					if (NetUtil.checkNet(this)) {
						new JoinActivityTask(activityId, userId).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			}
			break;
		case R.id.ivTitlePhoto:// 活动图片界面
			Intent intent = new Intent(this, EventDetailPhotoFallActivity.class);
			intent.putExtra("activityId", activityId);
			intent.putExtra("promoterId", promoterId);
			startActivityForResult(intent, 0);
			break;
		case R.id.tvCollect:// 收藏,赞
			if (!SharedPrefUtil.checkToken(this)) {
				startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));// 到登陆
			} else {
				if (NetUtil.checkNet(this)) {
					new UpLikeCountTask(activityId, userId).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}

			break;
		case R.id.tvShare:// 分享
			path = AsyncImageLoader.PHOTO_DIR + "/" + StringUtil.createImageName(eventDetailBean.getCoverUrl());
			localFile = new File(path);
			if (localFile.exists()) {
				shareDialog.show();

			} else {
				// Toast.makeText(this, R.string.photo_loading,
				// Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnShareToWeibo:
			shareDialog.dismiss();
			Intent intent1 = new Intent(this, SnsShareActivity.class);
			intent1.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
			intent1.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
			intent1.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIBO);
			intent1.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
			startActivity(intent1);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcForwordCountTask(activityId).execute();
			}
			break;
		case R.id.btnShareToQQ:
			shareDialog.dismiss();
			Intent intent2 = new Intent(this, SnsShareActivity.class);
			intent2.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
			intent2.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
			intent2.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_QQ);
			intent2.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
			startActivity(intent2);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcForwordCountTask(activityId).execute();
			}
			break;
		case R.id.btnShareToWeixinFre:
			shareDialog.dismiss();
//			Intent intent_weixin = new Intent(this, SnsShareActivity.class);
//			intent_weixin.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
//			intent_weixin.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
//			intent_weixin.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIXIN);
//			intent_weixin.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
//			startActivity(intent_weixin);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcForwordCountTask(activityId).execute();
			}
			if (versionCanShareWeiXin) {
				// 分享到好友
				shareToWeixin("Fre", path, localFile);
			} else {
				Toast.makeText(this, "您当前的微信版本不支持分享，请升级微信客户端", Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.btnShareToWeixinFres:
			shareDialog.dismiss();
//			Intent intent_pengyouquan = new Intent(this, SnsShareActivity.class);
//			intent_pengyouquan.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
//			intent_pengyouquan.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
//			intent_pengyouquan.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIXIN_FRS);
//			intent_pengyouquan.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
//			startActivity(intent_pengyouquan);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcForwordCountTask(activityId).execute();
			}
			if (versionCanShareWeiXin) {
				// 分享到朋友圈
				shareToWeixin("Fres", path, localFile);
			} else {
				Toast.makeText(this, "您当前的微信版本不支持分享，请升级微信客户端", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.dialog_cancel:
			shareDialog.cancel();
			break;
		case R.id.tvMorePhoto:// 活动图片界面
			Intent intent3 = new Intent(this, EventDetailPhotoFallActivity.class);
			intent3.putExtra("activityId", activityId);
			intent3.putExtra("promoterId", promoterId);
			startActivity(intent3);
			break;
		case R.id.btnContactRoo:// 私信联系袋鼠
			if (!SharedPrefUtil.checkToken(this)) {
				startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));// 到登陆
			} else {
				// 到私信
				Long EventRooId = eventDetailBean.getUserId();
				String EventRooNickName = eventDetailBean.getNickName();
				Intent intent4 = new Intent(this, PersonalLetterActivity.class);
				intent4.putExtra(Constants.EXTRA_AVATAR, eventDetailBean.getAvatarUrl());
				intent4.putExtra(Constants.EXTRA_USER_ID, EventRooId);
				intent4.putExtra(Constants.EXTRA_NAME, EventRooNickName);
				startActivity(intent4);
			}
			break;
		case R.id.btnSendEvnetComment:// 发布评论
			String content = edtEvnetComment.getText().toString().trim();
			if (!StringUtil.isBlank(content)) {
				if (!SharedPrefUtil.checkToken(this)) {
					startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));// 到登陆
				} else {
					if (NetUtil.checkNet(this)) {
						new InsertCommentsTask(activityId, userId, content).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(this, "请您输入评论内容~~", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnMore:
			if(isExpandMore){
				tvEventDescribe.setMaxLines(2);
				btnMore.setText("更多详情∨");
				isExpandMore = false;
			}else{
				tvEventDescribe.setMaxLines(100);
				btnMore.setText("收起");
				isExpandMore = true;
			}
			break;

		}
	}

	
	
	/**
	 * 分享到微信，直接调取微信客户端
	 */
	private void shareToWeixin(String type, String path, File localFile) {


	
		weixingHelper.shareWeb(type,"content",path);
		
		
		/*
		if (!localFile.exists()) {
			Toast.makeText(EventDetailActivity.this, "图片文件不存在", Toast.LENGTH_LONG).show();
		}

		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.ediyou.cn";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "来自E地游的本地Party,快来看看吧!";
//		msg.description = "立即点击链接：http://www.ediyou.com";
		
//		Bitmap bmp = BitmapFactory.decodeFile(path);
		Bitmap thumbBmp = ImageUtil.extractThumbNail(path, 150, 300, true);
//		msg.thumbData = ImageUtil.bmpToByteArray(thumbBmp, true, 100);
		msg.setThumbImage(thumbBmp);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		if (type.equals("Fre")) {
			req.scene = SendMessageToWX.Req.WXSceneSession;
		} else if (type.equals("Fres")) {
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		}
		boolean bool = wxApi.sendReq(req);
		System.out.println(bool);

	*/}

	
	
	

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 0:
			finish();
			break;

		default:
			break;
		}

	}

	/**
	 * 分享对话框
	 */
	private Dialog showShareDialog() {
		Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(dialogV);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 40); // 设置宽度
		lp.gravity = Gravity.BOTTOM;
		dialog.getWindow().setAttributes(lp);

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
		return dialog;
	}

	/**
	 * 已参加的人的头像适配器
	 * 
	 * @author syghh
	 * 
	 */
	class GalleryAdapter extends BaseAdapter {
		private Context context;
		private List<String> hadJoinPhotoUrls;

		public GalleryAdapter(Context context, List<String> hadJoinPhotoUrls) {
			this.context = context;
			this.hadJoinPhotoUrls = hadJoinPhotoUrls;
		}

		@Override
		public int getCount() {
			return hadJoinPhotoUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return hadJoinPhotoUrls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView hadJoinPhoto = new ImageView(context);

			String hadJoinPhotoUrl = hadJoinPhotoUrls.get(position);
			hadJoinPhoto.setTag(hadJoinPhotoUrl);
			Drawable hadJoinPhotoCacheDrawable = ImageCacheLoader.getInstance().loadDrawable(hadJoinPhotoUrl, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView ivPhoto = (ImageView) galleryHadJoin.findViewWithTag(imageUrl);
					if (ivPhoto != null) {
						if (imageDrawable != null) {
							ivPhoto.setImageBitmap(ImageUtil.getRoundCornerBitmapWithPic(imageDrawable, 0.15f));
						} else {
							ivPhoto.setImageBitmap(ImageUtil.getRoundedCornerBitmapWithPic(
									ImageUtil.getBitMapByRes(EventDetailActivity.this, R.drawable.bg_photo_defualt), 0.15f));
						}
					}
				}
			});
			if (hadJoinPhotoCacheDrawable != null) {
				hadJoinPhoto.setImageBitmap(ImageUtil.getRoundCornerBitmapWithPic(hadJoinPhotoCacheDrawable, 0.15f));
			} else {
				hadJoinPhoto.setImageBitmap(ImageUtil.getRoundedCornerBitmapWithPic(
						ImageUtil.getBitMapByRes(EventDetailActivity.this, R.drawable.bg_photo_defualt), 0.15f));
			}
			hadJoinPhoto.setLayoutParams(new Gallery.LayoutParams(60, 60));
			hadJoinPhoto.setScaleType(ScaleType.FIT_CENTER);

			return hadJoinPhoto;
		}

	}

	/*
	 * 活动评论适配器
	 */
	class EventCommentListAdapter extends BaseAdapter {
		private Context context;
		private List<EventCommentBean> eventCommentBeans;

		public EventCommentListAdapter(Context context, List<EventCommentBean> eventCommentBeans) {
			this.context = context;
			this.eventCommentBeans = eventCommentBeans;
		}

		@Override
		public int getCount() {
			return eventCommentBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return eventCommentBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.event_detail_comment_item, null);
				viewHolder = new ViewHolder();
				viewHolder.ivCommentHeadImg = (ImageView) convertView.findViewById(R.id.ivCommentHeadImg);
				viewHolder.tvCommentName = (TextView) convertView.findViewById(R.id.tvCommentName);
				viewHolder.tvCommentContent = (TextView) convertView.findViewById(R.id.tvCommentContent);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			String hadCommentPhotoUrl = eventCommentBeans.get(position).getAvatarUrl();

			viewHolder.ivCommentHeadImg.setTag(hadCommentPhotoUrl);
			Drawable hadJoinPhotoCacheDrawable = ImageCacheLoader.getInstance().loadDrawable(hadCommentPhotoUrl, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView ivPhoto = (ImageView) lvEvenCommentList.findViewWithTag(imageUrl);
					if (ivPhoto != null) {
						if (imageDrawable != null) {
							ivPhoto.setImageBitmap(ImageUtil.getRoundCornerBitmapWithPic(imageDrawable, 0.15f));
						} else {
							ivPhoto.setImageBitmap(ImageUtil.getRoundedCornerBitmapWithPic(
									ImageUtil.getBitMapByRes(EventDetailActivity.this, R.drawable.bg_photo_defualt), 0.15f));
						}
					}
				}
			});
			if (hadJoinPhotoCacheDrawable != null) {
				viewHolder.ivCommentHeadImg.setImageBitmap(ImageUtil.getRoundCornerBitmapWithPic(hadJoinPhotoCacheDrawable, 0.15f));
			} else {
				viewHolder.ivCommentHeadImg.setImageBitmap(ImageUtil.getRoundedCornerBitmapWithPic(
						ImageUtil.getBitMapByRes(EventDetailActivity.this, R.drawable.bg_photo_defualt), 0.15f));
			}
			// viewHolder.ivCommentHeadImg.setLayoutParams(new
			// LayoutParams(30,30));
			viewHolder.ivCommentHeadImg.setScaleType(ScaleType.FIT_CENTER);
			viewHolder.tvCommentName.setText(eventCommentBeans.get(position).getNickname());
			viewHolder.tvCommentContent.setText(eventCommentBeans.get(position).getContent());

			return convertView;
		}

		private class ViewHolder {
			ImageView ivCommentHeadImg;
			TextView tvCommentName;
			TextView tvCommentContent;
		}

	}

	/**
	 * 获取活动详情
	 * 
	 * @author syghh
	 * 
	 */
	class LoadEventDetailTask extends AsyncTask<Void, Void, JSONObject> {

		private long activityId;

		public LoadEventDetailTask(long activityId) {
			this.activityId = activityId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventDetailActivity.this);
				pd.setMessage("正在读取...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().loadEventDetail(activityId);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						// 详情主要数据
						eventDetailBean = new EventDetailBean(result.getJSONObject("data"));
						promoterId = eventDetailBean.getPromoter();
						// 评论数据
						eventCommentBeans = EventCommentBean.constantsLetterListBean(result.getJSONObject("data").getJSONArray("userCommentsVOs"));
						// 已参加过的人的头像
						JSONArray avatarUrls = result.getJSONObject("data").getJSONArray("avatarUrls");
						hadJoinPhotoUrls = new ArrayList<String>();
						for (int i = 0; i < avatarUrls.length(); i++) {
							hadJoinPhotoUrls.add((String) avatarUrls.get(i));
						}
						// 已参加过的人的userId
						JSONArray userIdsHadJoin = result.getJSONObject("data").getJSONArray("userIds");
						userIdsHadjoins = new ArrayList<Long>();
						for (int i = 0; i < userIdsHadJoin.length(); i++) {
							long a = userIdsHadJoin.getInt(i);
							userIdsHadjoins.add(a);
						}

						// 加载数据
						fillData();

					} else {
						Toast.makeText(EventDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventDetailActivity.this, "读取失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 参加活动
	 * 
	 * @author syghh
	 * 
	 */
	class JoinActivityTask extends AsyncTask<Void, Void, JSONObject> {

		private long activityId;
		private long userId;

		public JoinActivityTask(long activityId, long userId) {
			this.activityId = activityId;
			this.userId = userId;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().joinActivity(activityId, userId);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						// 更新ui数据
						int NewCurrentCount = result.getJSONObject("data").getInt("currentCount");
						CharSequence strHadJoinNum = Html.fromHtml("<a><font color=\"#9DD063\">" + NewCurrentCount + "</a>" + "人");
						tvHadJoinNum.setText(strHadJoinNum);
						pbPeopleNum.setProgress(NewCurrentCount);
						tvPeopleNum.setText(NewCurrentCount + "/" + eventDetailBean.getLimitCount());
						// 已参加过的人的头像
						JSONArray avatarUrls = result.getJSONObject("data").getJSONArray("avatarUrls");
						hadJoinPhotoUrls = new ArrayList<String>();
						for (int i = 0; i < avatarUrls.length(); i++) {
							hadJoinPhotoUrls.add((String) avatarUrls.get(i));
						}
						fillGalleryAdapter();
						// 已参加过的人的userId
						JSONArray userIdsHadJoin = result.getJSONObject("data").getJSONArray("userIds");
						userIdsHadjoins = new ArrayList<Long>();
						for (int i = 0; i < userIdsHadJoin.length(); i++) {
							long a = userIdsHadJoin.getInt(i);
							userIdsHadjoins.add(a);
						}

						Toast.makeText(EventDetailActivity.this, "报名成功！请等待袋鼠与您联系！", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(EventDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventDetailActivity.this, "报名未成功，请稍后重试", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 发送评论
	 * 
	 * @author syghh
	 * 
	 */
	class InsertCommentsTask extends AsyncTask<Void, Void, JSONObject> {

		private long activityId;
		private long userId;
		private String content;

		public InsertCommentsTask(long activityId, long userId, String content) {
			this.activityId = activityId;
			this.userId = userId;
			this.content = content;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().insertComments(activityId, userId, content);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						// 评论数据
						eventCommentBeans = EventCommentBean.constantsLetterListBean(result.getJSONObject("data").getJSONArray("userCommentsVOs"));
						fillListAdapter();
						edtEvnetComment.setText(null);
						Toast.makeText(EventDetailActivity.this, "发送留言成功", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(EventDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventDetailActivity.this, "发送留言失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 收藏
	 * 
	 * @author syghh
	 * 
	 */
	class UpLikeCountTask extends AsyncTask<Void, Void, JSONObject> {

		private long activityId;
		private long userId;

		public UpLikeCountTask(long activityId, long userId) {
			this.activityId = activityId;
			this.userId = userId;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().upLikeCount(activityId, userId);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						Toast.makeText(EventDetailActivity.this, "赞一个", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(EventDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventDetailActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 更新活动转发（分享到微博）的数量
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateAcForwordCountTask extends AsyncTask<Void, Void, JSONObject> {
		private long activityId;

		public UpdateAcForwordCountTask(long activityId) {
			this.activityId = activityId;

		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateAcForwordCount(activityId);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
					} else {
						Toast.makeText(EventDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(EventDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
}
