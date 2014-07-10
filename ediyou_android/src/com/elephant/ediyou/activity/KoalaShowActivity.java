package com.elephant.ediyou.activity;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 考拉个人展示页
 * 
 * @author ISP
 * 
 */
public class KoalaShowActivity extends Activity implements IBaseActivity,
		OnClickListener {

	// title
	private Button btnLeft, btnRight;
	private TextView tvTitle;

	private ImageView ivPortrait;
	private TextView tvName;
	private ImageView ivGender;
	private TextView tvAge;
	private TextView tvLevel;
	private TextView tvStar;
	private TextView tvPersonalIntroduce;

	private View viewPersonalInfo;

	private TextView tvCommentCount;
	private RatingBar ratingbar;
	private TextView tvCommentContent, tvCommentName, tvCommentTime;
	private View viewComment;
	private View layoutComment;// 进入评论界面；

	private Button btnEvent, btnPhoto;

	private ProgressDialog pd;

	private long userId;
	private boolean isSelf = false;
	private String name;
	private Drawable photoDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_show);
		if (getIntent() != null) {
			userId = getIntent().getLongExtra("uid", 0);
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
		btnRight.setVisibility(View.INVISIBLE);
		btnLeft.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		tvName = (TextView) findViewById(R.id.tvName);
		ivGender = (ImageView) findViewById(R.id.ivGender);
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvLevel = (TextView) findViewById(R.id.tvLevel);
		tvStar = (TextView) findViewById(R.id.tvStar);

		tvPersonalIntroduce = (TextView) findViewById(R.id.tvPersonalIntroduce);

		viewPersonalInfo = findViewById(R.id.viewPersonalInfo);
		viewPersonalInfo.setOnClickListener(this);

		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ratingbar = (RatingBar) findViewById(R.id.ratingbar);
		tvCommentContent = (TextView) findViewById(R.id.tvCommentContent);
		tvCommentName = (TextView) findViewById(R.id.tvCommentName);
		tvCommentTime = (TextView) findViewById(R.id.tvCommentTime);
		viewComment = findViewById(R.id.viewComment);
		layoutComment = findViewById(R.id.layoutComment);
		layoutComment.setOnClickListener(this);

		btnEvent = (Button) findViewById(R.id.btnEvent);
		btnPhoto = (Button) findViewById(R.id.btnPhoto);
		btnEvent.setOnClickListener(this);
		btnPhoto.setOnClickListener(this);

		tvLevel.setOnClickListener(this);
		tvStar.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("考拉详情");
		if (NetUtil.checkNet(this)) {
			new GetKoalaTask(userId, this).execute();
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
		case R.id.viewPersonalInfo:
			Intent intent = new Intent(this, SelfBaseInfoEditActivity.class);
			intent.putExtra(Constants.EXTRA_USER_ID, userId);
			startActivity(intent);
			break;
		case R.id.layoutComment:
			startActivity(new Intent(this, CommentShowActivity.class).putExtra(
					"userId", userId));
			break;
		case R.id.btnEvent:
			Intent eventIntent = new Intent(this,
					EventListHadJoinActivity.class);
			eventIntent.putExtra(Constants.EXTRA_USER_ID, userId);
			eventIntent.putExtra(Constants.EXTRA_NAME, name);
			startActivity(eventIntent);
			break;
		case R.id.btnPhoto:
			Intent photoIntent = new Intent(this,
					SelfPhotosAndAvatarActivity.class);
			photoIntent.putExtra(Constants.EXTRA_USER_ID, userId);
			photoIntent.putExtra(Constants.EXTRA_NAME, name);
			photoIntent.putExtra("rooOrKoala", "koala");
			startActivity(photoIntent);
			break;
		case R.id.ivPortrait:

			LayoutInflater inflater = LayoutInflater
					.from(KoalaShowActivity.this);
			View imgEntryView = inflater.inflate(R.layout.dialog_photo, null); // 加载自定义的布局文件
			final Dialog dialog = new Dialog(this, R.style.dialog);
			ImageView img = (ImageView) imgEntryView
					.findViewById(R.id.largeImage);
			if (photoDrawable != null) {
				img.setImageDrawable(photoDrawable);
			} else {
				img.setImageResource(R.drawable.bg_photo_defualt);
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

		case R.id.tvLevel:// 等级介绍
			String levelContent = "这个是当前袋鼠的等级，等级越高，经验越丰富哦";
			notiIntroduce(levelContent);
			break;
		case R.id.tvStar:// 星级介绍
			String starContent = "这个是平均分,当前用户被评价后的得分的平均值";
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
				KoalaShowActivity.this).create();
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

	/**
	 * 获取考拉信息;
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class GetKoalaTask extends AsyncTask<Void, Void, JSONObject> {

		private long id;
		private Context context;

		public GetKoalaTask(long id, Context context) {
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
				rooInfo = new BusinessHelper().getKoalaInfoNew(id);
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
						JSONObject dataJson = result.getJSONObject("data");
						name = dataJson.getString("nickname");
						tvName.setText(name);
						String avatarUrl = dataJson.getString("avatarUrl");
						if (avatarUrl != null && avatarUrl.trim().length() != 0) {
							ivPortrait.setTag(avatarUrl);
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
						} else
							ivPortrait
									.setImageResource(R.drawable.bg_photo_defualt);
						if ("f".equals(dataJson.getString("gender"))) {
							ivGender.setImageResource(R.drawable.ic_fale);
						} else {
							ivGender.setImageResource(R.drawable.ic_male);
						}
						tvAge.setText(dataJson.getString("age") + "岁");
						tvLevel.setText("Lv" + dataJson.getString("level"));
						tvStar.setText(dataJson.getString("currentExper"));

						tvPersonalIntroduce.setText(dataJson.getString("intro")
								.replace("<BR>", "\n").replace("<br>", "\n"));
						int commentsCount = dataJson.getInt("commentsCount");
						if (commentsCount > 0) {
							JSONObject commentObj = dataJson
									.getJSONObject("commentsVO");
							tvCommentCount
									.setText("评论 (" + commentsCount + ")");
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
}
