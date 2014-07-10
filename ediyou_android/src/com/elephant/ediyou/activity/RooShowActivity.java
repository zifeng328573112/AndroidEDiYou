package com.elephant.ediyou.activity;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠个人展示页
 * 
 * @author ISP
 * 
 */
public class RooShowActivity extends Activity implements IBaseActivity,
		OnClickListener {

	// title
	private Button btnLeft, btnRight;
	private TextView tvTitle;

	private ImageView ivPortrait;
	private TextView tvName;
	private ImageView ivGender;
	private TextView tvAge;
	private TextView tvLevel;// 显示等级，点击显示说明
	private TextView tvStar;// 显示评分星级平均值，点击显示说明
	private TextView tvPersonalIntroduce;
	private TextView tvPrice, tvServiceContent, tvLanguage;

	private View viewPersonalInfo, viewAuth;

	private LinearLayout llCTCA;// 证书显示模块，点击显示说明
	private TextView tvGuider, tvMandarin, tvDriver, tvHealth, tvEmergency;
	private TextView tvCommentCount;
	private RatingBar ratingbar;
	private TextView tvCommentContent, tvCommentName, tvCommentTime;
	private View viewComment;
	private View layoutComment;// 进入评论界面；

	private View viewButton;
	private Button btnEvent, btnPhoto, btnOrder;
	private ImageView ivRooBig;

	private ProgressDialog pd;

	private long rooId;// 袋鼠的ID；
	private long userId;
	private boolean isSelf = false;
	private String from = null;
	private String name;
	private String avatarUrl;

	private Drawable photoDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_show);
		if (getIntent() != null) {
			userId = getIntent().getLongExtra("uid", 0);
			rooId = getIntent().getLongExtra("rooId", 0);
			from = getIntent().getStringExtra("from");
		}
		if (from == null) {
			from = "";
		}
		// 当from不为私信界面时，isSelf才可为true
		if (userId == SharedPrefUtil.getUserBean(this).getUserId()
				&& !from.equals("personalLetter")) {
			isSelf = true;
		}
		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_connect_btn_selector);
		// btnRight.setText("联系");
		btnRight.setGravity(Gravity.CENTER);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		tvName = (TextView) findViewById(R.id.tvName);
		ivGender = (ImageView) findViewById(R.id.ivGender);
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvLevel = (TextView) findViewById(R.id.tvLevel);
		tvStar = (TextView) findViewById(R.id.tvStar);
		llCTCA = (LinearLayout) findViewById(R.id.llCTCA);
		tvGuider = (TextView) findViewById(R.id.tvGuider);
		tvMandarin = (TextView) findViewById(R.id.tvMandarin);
		tvDriver = (TextView) findViewById(R.id.tvDriver);
		tvHealth = (TextView) findViewById(R.id.tvHealth);
		tvEmergency = (TextView) findViewById(R.id.tvEmergency);

		tvPersonalIntroduce = (TextView) findViewById(R.id.tvPersonalIntroduce);
		tvPrice = (TextView) findViewById(R.id.tvPrice);
		tvServiceContent = (TextView) findViewById(R.id.tvServiceContent);
		tvLanguage = (TextView) findViewById(R.id.tvLanguage);

		viewPersonalInfo = findViewById(R.id.viewPersonalInfo);
		viewPersonalInfo.setOnClickListener(this);
		viewAuth = findViewById(R.id.viewAuth);
		viewAuth.setOnClickListener(this);

		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ratingbar = (RatingBar) findViewById(R.id.ratingbar);
		tvCommentContent = (TextView) findViewById(R.id.tvCommentContent);
		tvCommentName = (TextView) findViewById(R.id.tvCommentName);
		tvCommentTime = (TextView) findViewById(R.id.tvCommentTime);
		viewComment = findViewById(R.id.viewComment);
		layoutComment = findViewById(R.id.layoutComment);
		layoutComment.setOnClickListener(this);

		viewButton = findViewById(R.id.viewButton);
		ivRooBig = (ImageView) findViewById(R.id.ivRooBig);
		btnEvent = (Button) findViewById(R.id.btnEvent);
		btnPhoto = (Button) findViewById(R.id.btnPhoto);
		btnOrder = (Button) findViewById(R.id.btnOrder);
		btnEvent.setOnClickListener(this);
		btnPhoto.setOnClickListener(this);
		btnOrder.setOnClickListener(this);

		llCTCA.setOnClickListener(this);
		tvLevel.setOnClickListener(this);
		tvStar.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("袋鼠详情");
		if (isSelf) {
			btnRight.setVisibility(View.INVISIBLE);
			viewButton.setVisibility(View.GONE);
			ivRooBig.setVisibility(View.GONE);
		}
		// 当自私信界面跳转而来时，取消袋鼠展示界面右上角联系按钮的显示
		if (from.equals("personalLetter")) {
			btnRight.setVisibility(View.INVISIBLE);
		}
		if (NetUtil.checkNet(this)) {
			new GetRooServiceTask(userId, this).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			this.finish();
			break;
		case R.id.btnRight:
			if (SharedPrefUtil.getUserBean(this).getAccessToken() != null) {
				Intent letterIntent = new Intent(this,
						PersonalLetterActivity.class);
				letterIntent.putExtra(Constants.EXTRA_USER_ID, userId);
				letterIntent.putExtra(Constants.EXTRA_NAME, name);
				letterIntent.putExtra(Constants.EXTRA_AVATAR, avatarUrl);
				letterIntent.putExtra("isRoo", 1);
				startActivity(letterIntent);
			} else {
				Toast.makeText(this, R.string.no_login, Toast.LENGTH_LONG)
						.show();
				startActivity(new Intent(this, LoginActivity.class).putExtra(
						"back", "back"));
			}
			break;
		case R.id.viewPersonalInfo:
			Intent intent = new Intent(this, SelfBaseInfoEditActivity.class);
			intent.putExtra("userId", userId);
			startActivity(intent);
			break;
		case R.id.viewAuth:
			startActivity(new Intent(this, RooCTCAActivity.class).putExtra(
					"userId", userId));
			break;
		case R.id.layoutComment:
			startActivity(new Intent(this, CommentShowActivity.class).putExtra(
					"userId", userId));
			break;
		case R.id.btnEvent:
			Intent eventIntent = new Intent(this,
					EventListHadCreateActivity.class);
			eventIntent.putExtra(Constants.EXTRA_USER_ID, userId);
			eventIntent.putExtra(Constants.EXTRA_NAME, name);
			startActivity(eventIntent);
			break;
		case R.id.btnPhoto:
			Intent photoIntent = new Intent(this,
					SelfPhotosAndAvatarActivity.class);
			photoIntent.putExtra(Constants.EXTRA_USER_ID, userId);
			photoIntent.putExtra(Constants.EXTRA_NAME, name);
			photoIntent.putExtra("rooOrKoala", "roo");
			startActivity(photoIntent);
			break;
		case R.id.btnOrder:
			if (isSelf) {
				Toast.makeText(this, "不能预约自己哦", Toast.LENGTH_SHORT).show();
				btnOrder.setClickable(false);
				return;
			} else {
				if (SharedPrefUtil.getUserBean(this).getAccessToken() != null) {
					Intent orderIntent = new Intent(this,
							RooScheduleActivity.class);
					orderIntent.putExtra("isSelf", isSelf);
					orderIntent.putExtra("rooId", rooId);
					startActivity(orderIntent);
				} else {
					Toast.makeText(this, R.string.no_login, Toast.LENGTH_LONG)
							.show();
					startActivity(new Intent(this, LoginActivity.class)
							.putExtra("back", "back"));
				}
			}
			break;

		case R.id.ivPortrait:
			LayoutInflater inflater = LayoutInflater.from(RooShowActivity.this);
			View imgEntryView = inflater.inflate(R.layout.dialog_photo, null); // 加载自定义的布局文件
			final Dialog dialog = new Dialog(this, R.style.dialog);
			ImageView img = (ImageView) imgEntryView
					.findViewById(R.id.largeImage);
			if (photoDrawable != null) {
				img.setImageDrawable(photoDrawable);
			} else {
				img.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}

			dialog.setContentView(imgEntryView); // 自定义dialog
			dialog.show();

			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = display.getWidth(); // 设置宽度
			lp.height = display.getHeight();
			dialog.getWindow().setAttributes(lp);

			// 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
			imgEntryView.setOnClickListener(new OnClickListener() {
				public void onClick(View paramView) {
					dialog.cancel();
				}
			});
			break;
		case R.id.llCTCA:// 证书介绍
			String CTCAContent = "这个是当前用户的所有非必要的证书，但是很实用";
			notiIntroduce(CTCAContent);
			break;
		case R.id.tvLevel:// 等级介绍
			String levelContent = "这个是当前袋鼠的等级，等级越高，经验越丰富哦";
			notiIntroduce(levelContent);
			break;
		case R.id.tvStar:// 星级介绍
			String starContent = "这个是平均分";
			notiIntroduce(starContent);
			break;
		default:
			break;
		}
	}

	/**
	 * 显示说明
	 * 
	 * @param content
	 */
	private void notiIntroduce(String intrContent) {
		final AlertDialog dialogExit = new AlertDialog.Builder(
				RooShowActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);

		TextView tvDialogMsg = (TextView) dialogWindow
				.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText(intrContent);
		Button btnDialogLeft = (Button) dialogWindow
				.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("哦！明白了");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
			}
		});

		Button btnDialogRight = (Button) dialogWindow
				.findViewById(R.id.btnDialogRight);
		btnDialogRight.setVisibility(View.GONE);
	}

	private Integer health = 0;// 健康证;
	private Integer guider = 2;// 导游证；
	private Integer mandarin = 9;// 普通话；
	private Integer emergency = 10;// 急救;
	private Integer driver = 11;// 驾照；

	/**
	 * 获取袋鼠信息;
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class GetRooServiceTask extends AsyncTask<Void, Void, JSONObject> {

		private long id;
		private Context context;

		public GetRooServiceTask(long id, Context context) {
			super();
			this.id = id;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(context);
				pd.setMessage("正在获取信息...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject rooInfo = null;
			try {
				rooInfo = new BusinessHelper().getRooInfo(id);
			} catch (Exception e) {
				MobclickAgent.reportError(context,
						StringUtil.getExceptionInfo(e));
			}
			return rooInfo;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					if (Constants.SUCCESS == result.getInt("status")) {
						HashMap<Integer, String> authMap = new HashMap<Integer, String>();
						JSONObject dataJson = result.getJSONObject("data");
						rooId = dataJson.getLong("kangarooId");
						avatarUrl = dataJson.getString("avatarUrl");
						ivPortrait.setTag(avatarUrl);
						if (avatarUrl != null && avatarUrl.trim().length()!=0) {

							Drawable cacheDrawable = AsyncImageLoader
									.getInstance().loadDrawable(avatarUrl,
											new ImageCallback() {
												@Override
												public void imageLoaded(
														Drawable imageDrawable,
														String imageUrl) {
													ImageView ivImage = (ImageView) ivPortrait
															.findViewWithTag(imageUrl);
													if (ivImage != null) {
														if (imageDrawable != null) {
															ivImage.setImageDrawable(imageDrawable);
															photoDrawable = imageDrawable;
														}
													}
												}
											});
							if (cacheDrawable != null) {
								ivPortrait.setImageDrawable(cacheDrawable);
								photoDrawable = cacheDrawable;
							}
						}else
							ivPortrait
							.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						name = dataJson.getString("nickName");
						if (!StringUtil.isBlank(name)) {
							tvName.setText(name);
						} else {
							name = "";
						}
						if ("f".equals(dataJson.getString("gender"))) {
							ivGender.setImageResource(R.drawable.ic_fale);
						} else {
							ivGender.setImageResource(R.drawable.ic_male);
						}
						tvAge.setText(dataJson.getString("age") + "岁");
						tvLevel.setText("Lv"
								+ dataJson.getString("kangarooLevel"));
						tvStar.setText(dataJson.getString("currentExper"));

						JSONArray dataArray = dataJson.getJSONArray("authId");
						for (int i = 0; i < dataArray.length(); i++) {
							Integer auth = (Integer) dataArray.get(i);
							authMap.put(auth, auth + "");
						}
						if (authMap.containsKey(guider)) {
							tvGuider.setVisibility(View.VISIBLE);
						}
						if (authMap.containsKey(mandarin)) {
							tvMandarin.setVisibility(View.VISIBLE);
						}
						if (authMap.containsKey(driver)) {
							tvDriver.setVisibility(View.VISIBLE);
						}
						if (authMap.containsKey(health)) {
							tvHealth.setVisibility(View.VISIBLE);
						}
						if (authMap.containsKey(emergency)) {
							tvEmergency.setVisibility(View.VISIBLE);
						}
						tvPersonalIntroduce.setText(dataJson.getString("intro")
								.replace("<BR>", "\n").replace("<br>", "\n"));
						tvPrice.setText(dataJson.getString("price") + "元/每天");
						tvServiceContent.setText(dataJson.getString("service"));
						tvLanguage.setText(dataJson.getString("language"));
						int commentsCount = dataJson.getInt("commentsCount");
						if (commentsCount > 0) {
							tvCommentCount
									.setText("评论 (" + commentsCount + ")");
							JSONObject commentObj = dataJson
									.getJSONObject("commentsVO");
							int level = 0;
							try {
								level = Integer.parseInt(commentObj
										.getString("level"));
							} catch (Exception e) {
							}
							ratingbar.setRating(level);
							tvCommentContent.setText(commentObj
									.getString("content"));
							tvCommentName.setText(commentObj
									.getString("nickname"));
							tvCommentTime.setText(commentObj
									.getString("createdTime"));
						} else {
							tvCommentCount.setText("评论 (0)");
							viewComment.setVisibility(View.GONE);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(context, "服务器请求失败", Toast.LENGTH_LONG).show();
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
