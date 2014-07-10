package com.elephant.ediyou.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.LetterBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.service.PullService;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.PullToRefreshListView;
import com.elephant.ediyou.view.PullToRefreshListView.OnRefreshListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 私信页面
 * 
 * @author syghh
 * 
 */
public class PersonalLetterActivity extends Activity implements IBaseActivity, OnClickListener {
	// title
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	// Task progress

	private PullToRefreshListView lvPersonalLetter;// 聊天的listView
	// private ImageView ivCamera;//取得照片
	private ImageView ivEmoticon; // 选择表情
	private EditText edtLetter; // 编辑私信
	private Button btnSend; // 发送私信
	// 表情
	private ScrollView scrollViewFace;
	private LinearLayout vFace01;
	private LinearLayout vFace02;
	private Map<String, Integer> faceMap = new HashMap<String, Integer>();
	private int[] faceRes = new int[] { R.drawable.ic_face_001, R.drawable.ic_face_002, R.drawable.ic_face_003, R.drawable.ic_face_004, R.drawable.ic_face_005, R.drawable.ic_face_006,
			R.drawable.ic_face_007, R.drawable.ic_face_008, R.drawable.ic_face_009, R.drawable.ic_face_010, R.drawable.ic_face_011, R.drawable.ic_face_012, R.drawable.ic_face_013,
			R.drawable.ic_face_014, R.drawable.ic_face_015, R.drawable.ic_face_016 };
	int haveFacePic = -1;// 一段对话中是否含有表情图片；是，0；否，-1。

	private List<String> sendLetterStrList;
	private UserBean userBean;
	private int userId;
	private LetterAdapter letterAdapter;

	private Handler iLetterHandler;
	private TimerTask letterTimerTask;
	private Timer letterTimer;

	private List<LetterBean> letterBeans;

	private long friendId;
	private String friendName;
	private String friendAvatar;
	private String dateLine = "";

	private boolean isLoaded = false;
	private boolean isSend = false;

	private ProgressDialog pd;

	private final static String TAG = "PersonalLetterActivity";

	private final static int HANDLER_DATA = 11;
	private boolean friendIsRoo = false;
	private long friendRooId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_letter_layout);
		for (int i = 0; i < faceRes.length; i++) {
			String j;
			int k = i + 1;
			if (k < 10) {
				j = "00" + k;
			} else if (k < 100) {
				j = "0" + k;
			} else {
				j = "" + k;
			}
			String key = "[edu" + j + "]";
			faceMap.put(key, faceRes[i]);
		}
		userBean = SharedPrefUtil.getUserBean(this);
		if (userBean != null) {
			userId = userBean.getUserId();
			if (userId == 0) {
				relogin();
			} else {
			}
		}

		Intent data = getIntent();
		friendId = data.getLongExtra(Constants.EXTRA_USER_ID, 0);
		friendName = data.getStringExtra(Constants.EXTRA_NAME);
		friendAvatar = data.getStringExtra(Constants.EXTRA_AVATAR);
		friendIsRoo = data.getIntExtra("isRoo", 0) == Constants.ROO ? true : false;
		Log.i(TAG, "friendId:" + friendId);

		findView();
		fillData();

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("TA的资料");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setText(friendName);
		tvTitle.setVisibility(View.VISIBLE);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		lvPersonalLetter = (PullToRefreshListView) this.findViewById(R.id.lvPersonalLetter);
		ivEmoticon = (ImageView) this.findViewById(R.id.ivEmoticon);
		edtLetter = (EditText) this.findViewById(R.id.edtLetter);
		btnSend = (Button) this.findViewById(R.id.btnSend);

		ivEmoticon.setOnClickListener(this);
		btnSend.setOnClickListener(this);

		scrollViewFace = (ScrollView) this.findViewById(R.id.scroll_view_face);
		vFace01 = (LinearLayout) this.findViewById(R.id.view_face01);
		vFace02 = (LinearLayout) this.findViewById(R.id.view_face02);

	}

	@Override
	public void fillData() {
		fillFacePic();

		PullService.isCurrActivity = true;
		pd = new ProgressDialog(this);
		pd.setMessage("消息发送中...");
		letterBeans = new ArrayList<LetterBean>();
		letterAdapter = new LetterAdapter(this, letterBeans);
		lvPersonalLetter.setAdapter(letterAdapter);
		lvPersonalLetter.setonRefreshListener(onRefreshListener);

		initNotifyHandler();

		if (NetUtil.checkNet(this)) {
			new JudgeIsRooTask(friendId).execute();
			new ListLetter().execute();
		} else {
			Toast.makeText(PersonalLetterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 加载表情图片
	 */
	private void fillFacePic() {
		for (int i = 0; i < faceRes.length; i++) {
			View emotionV = getLayoutInflater().inflate(R.layout.personal_letter_face_item, null);
			ImageView ivEmotion = (ImageView) emotionV.findViewById(R.id.ivFacePic);
			ivEmotion.setImageResource(faceRes[i]);
			emotionV.setOnClickListener(facePicOnClickListener);
			emotionV.setTag(i + 1);
			if (i < faceRes.length / 2) {
				vFace01.addView(emotionV);
			} else if (i > (faceRes.length / 2) - 1) {
				vFace02.addView(emotionV);
			}
		}
	}

	/**
	 * 表情图片的点击事件
	 */
	OnClickListener facePicOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String j;
			int insertFacePicNum = (Integer) v.getTag();
			if (insertFacePicNum < 10) {
				j = "00" + insertFacePicNum;
			} else if (insertFacePicNum < 100) {
				j = "0" + insertFacePicNum;
			} else {
				j = "" + insertFacePicNum;
			}
			String FaceName = "[edu" + j + "]";
			String editStr = edtLetter.getText().toString();
			String neeEditStr = editStr + FaceName;
			edtLetter.setText(neeEditStr);
			edtLetter.setSelection(neeEditStr.length());
		}
	};

	OnRefreshListener onRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			if (NetUtil.checkNet(PersonalLetterActivity.this)) {
				new ListLetter().execute();
			} else {
				Toast.makeText(PersonalLetterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void showPD() {
		if (pd != null) {
			pd.show();
		}
	}

	private void dismisPD() {
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

	private void relogin() {
		Intent loginIntent = new Intent(PersonalLetterActivity.this, LoginActivity.class);
		loginIntent.putExtra("back", "back");
		startActivity(loginIntent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:// 返回
			finish();
			break;
		case R.id.btnRight:// 查看对方信息
			if (friendIsRoo) {// 跳转到袋鼠界面；
				Intent intent = new Intent(PersonalLetterActivity.this, RooShowActivity.class);
				intent.putExtra("uid", friendId);// userId
				intent.putExtra("rooId", friendRooId);
				intent.putExtra("from", "personalLetter");
				startActivity(intent);
			} else {
				Intent intent = new Intent(PersonalLetterActivity.this, KoalaShowActivity.class);
				intent.putExtra("uid", friendId);// userId
				startActivity(intent);
			}

			break;
		case R.id.ivEmoticon:// 添加表情
			if (scrollViewFace.getVisibility() == View.VISIBLE) {
				scrollViewFace.setVisibility(View.GONE);
			} else {
				scrollViewFace.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.btnSend:// 发送
			try {
				String letterStr = edtLetter.getText().toString().trim();
				if (!StringUtil.isBlank(letterStr)) {
					if (!isSend) {
						new SendLetter(letterStr).execute();
					} else {
						Toast.makeText(this, "消息正在发送中...", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, "请您输入信息", Toast.LENGTH_LONG).show();
				}

			} catch (Exception e) {
			}

			break;
		default:
			break;
		}

	}

	/**
	 * 私信Adapter
	 * 
	 * @author syghh
	 * 
	 */
	private class LetterAdapter extends BaseAdapter {
		private Context context;
		private List<LetterBean> letterBeans;

		public LetterAdapter(Context context, List<LetterBean> letterBeans) {
			this.context = context;
			this.letterBeans = letterBeans;
		}

		@Override
		public int getCount() {
			return letterBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return letterBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LetterBean bean = letterBeans.get(position);
			final int sender = bean.getSender();
			ViewHolder viewHolder = null;
			View view = null;
			if (sender == userId) {
				view = LayoutInflater.from(context).inflate(R.layout.personal_letter_right_item, null);
			} else {
				view = LayoutInflater.from(context).inflate(R.layout.personal_letter_left_item, null);
			}
			if (view.getTag() == null) {
				viewHolder = new ViewHolder();
				viewHolder.ivUserPhoto = (ImageView) view.findViewById(R.id.ivUserPhoto);
				viewHolder.tvLetter = (TextView) view.findViewById(R.id.tvLetter);
				viewHolder.tvSendTime = (TextView) view.findViewById(R.id.tvSendTime);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			String contentStr = bean.getContent();
			SpannableString spannableString = null;
			if (!StringUtil.isBlank(contentStr)) {
				boolean isHaveFacePic = contentStr.contains("[edu");
				if (isHaveFacePic == true) {
					if (faceMap != null) {
						spannableString = ImageUtil.changeTextToEmotions(faceMap, contentStr, PersonalLetterActivity.this);
					}
				}
			}

			if (spannableString != null) {
				viewHolder.tvLetter.setText(spannableString);
			} else {
				viewHolder.tvLetter.setText(contentStr);
			}

			// viewHolder.tvLetter.setText(bean.getContent());

			viewHolder.tvSendTime.setText(bean.getSendTime());

			String photoUrl = null;
			if (sender == userId) {
				photoUrl = userBean.getAvatarUrl();
			} else {
				photoUrl = friendAvatar;
			}
			if (!StringUtil.isBlank(photoUrl)) {
				viewHolder.ivUserPhoto.setTag(photoUrl);
				Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(photoUrl, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
						ImageView ivPhoto = (ImageView) lvPersonalLetter.findViewWithTag(imageUrl);
						if (ivPhoto != null) {
							if (imageDrawable != null) {
								ivPhoto.setImageDrawable(imageDrawable);
								LetterAdapter.this.notifyDataSetChanged();
							} else {
								if (sender == userId) {
									ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
								} else
									ivPhoto.setImageResource(R.drawable.bg_photo_defualt);
							}
						}
					}
				});
				if (cacheDrawable != null) {
					viewHolder.ivUserPhoto.setImageDrawable(cacheDrawable);
				} else {
					if (sender == userId) {
						if (userBean.getIsKangaroo() == 1) {
							viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						} else
							viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
					} else {
						if (friendIsRoo) {
							viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						} else
							viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
					}
				}
			} else {
				if (sender == userId) {
					if (userBean.getIsKangaroo() == 1) {

						viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
					} else

						viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
				} else {
					if (friendIsRoo) {

						viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
					} else
						viewHolder.ivUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
				}
			}
			return view;
		}

		class ViewHolder {
			ImageView ivUserPhoto;
			TextView tvLetter;
			TextView tvSendTime;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initNotifyHandler() {
		iLetterHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case HANDLER_DATA:
					List<LetterBean> beans = (List<LetterBean>) msg.obj;
					if (beans != null) {
						letterBeans.addAll(beans);
						sortNotifyListByTime(letterBeans);
						letterAdapter.notifyDataSetChanged();
						lvPersonalLetter.onRefreshComplete();
						if (beans.size() > 0) {
							lvPersonalLetter.setSelection(letterBeans.size() - 1);
						}
					}
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	private void startNotifyTask() {
		if (letterTimerTask == null) {
			letterTimerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						if (NetUtil.checkNet(PersonalLetterActivity.this)) {
							if (!isLoaded) {
								isLoaded = true;
								UserBean userBean = SharedPrefUtil.getUserBean(PersonalLetterActivity.this);
								if (userBean != null) {
									int userID = userBean.getUserId();
									String accessToken = userBean.getAccessToken();
									BusinessHelper businessHelper = new BusinessHelper();
									if (userID != 0 && userID != -1 && !StringUtil.isBlank(accessToken)) {
										JSONObject obj = businessHelper.receiveLetter(friendId, userID, accessToken);
										if (obj != null) {
											if (obj.has("status")) {
												int status = obj.getInt("status");
												if (status == 1) {
													if (obj.has("letters")) {
														JSONArray arr = obj.getJSONArray("letters");
														if (arr != null) {
															ArrayList<LetterBean> beans = (ArrayList<LetterBean>) LetterBean.constantsList(arr);
															if (beans != null) {
																Message msg = new Message();
																msg.what = HANDLER_DATA;
																msg.obj = beans;
																iLetterHandler.sendMessage(msg);
															}
														}
													}
												}
											}
										} else {
										}
									}
								}
								isLoaded = false;
							}
						}
					} catch (Exception e) {
					}
				}
			};
			letterTimer = new Timer();
			letterTimer.schedule(letterTimerTask, 0, 2 * 1000);
		}
	}

	private void stopNotifyTimer() {
		if (letterTimer != null) {
			letterTimer.cancel();
			letterTimer = null;
		}
		if (letterTimerTask != null) {
			letterTimerTask = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (NetUtil.checkNet(this)) {
			startNotifyTask();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopNotifyTimer();
		PullService.isCurrActivity = false;
	}

	private class ListLetter extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				obj = new BusinessHelper().listLetter(userId, friendId, dateLine, userBean.getAccessToken());
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
			return obj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				if (result.has("status")) {
					try {
						int status = result.getInt("status");
						if (status == Constants.SUCCESS) {
							if (result.has("letters")) {
								JSONArray arr = result.getJSONArray("letters");
								if (arr != null) {
									List<LetterBean> beans = LetterBean.constantsList(arr);
									if (beans != null) {
										if (StringUtil.isBlank(dateLine)) {
											letterBeans.clear();
										}

										int beansSize = beans.size();
										if (beansSize > 0) {
											dateLine = beans.get(0).getSendTime();
										}

										// 按时间排序
										letterBeans.addAll(beans);
										sortNotifyListByTime(letterBeans);
										letterAdapter.notifyDataSetChanged();
										lvPersonalLetter.onRefreshComplete();
										lvPersonalLetter.setSelection(letterBeans.size() - 1);
										((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(PersonalLetterActivity.this.getCurrentFocus().getWindowToken(),
												InputMethodManager.HIDE_NOT_ALWAYS);
									}
								}
							}
						} else {
						}
					} catch (Exception e) {
						Log.i(TAG, e.getMessage());
					}
				}
			}
		}
	}

	public void sortNotifyListByTime(List<LetterBean> list) {
		Collections.sort(list, new Comparator<LetterBean>() {

			@Override
			public int compare(LetterBean obj1, LetterBean obj2) {
				String time1 = obj1.getSendTime();
				String time2 = obj2.getSendTime();
				if (time1 != null && !"".equals(time1) && time2 != null && !"".equals(time2)) {
					return compareTime(time1, time2);
				} else {
					return 0;
				}
			}
		});
	}

	public int compareTime(String firTimeS, String secTimeS) {
		try {
			SimpleDateFormat chineseSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
			chineseSdf.setTimeZone(timeZone);
			Date firDate = chineseSdf.parse(firTimeS);
			Date secDate = chineseSdf.parse(secTimeS);
			return firDate.compareTo(secDate);
		} catch (ParseException e) {
		}
		return 0;
	}

	/**
	 * 发送私信
	 * 
	 * @author Arvin
	 * 
	 */
	private class SendLetter extends AsyncTask<Void, Void, JSONObject> {

		private String content;

		public SendLetter(String content) {
			this.content = content;
		}

		@Override
		protected void onPreExecute() {
			isSend = true;
			showPD();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				obj = new BusinessHelper().sendLetter(content, userId, friendId, userBean.getAccessToken());
			} catch (Exception e) {
			}
			return obj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			dismisPD();
			if (result != null) {
				if (result.has("status")) {
					try {
						int status = result.getInt("status");
						if (status == Constants.SUCCESS) {
							Toast.makeText(PersonalLetterActivity.this, "发送成功", Toast.LENGTH_LONG).show();
							edtLetter.setText("");
							((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(PersonalLetterActivity.this.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
							if (result.has("letter")) {
								JSONArray jsonArray = result.getJSONArray("letter");
								JSONObject obj = jsonArray.getJSONObject(0);
								if (obj != null) {
									LetterBean bean = new LetterBean(obj);
									if (bean != null) {
										letterBeans.add(bean);
										letterAdapter.notifyDataSetChanged();
										lvPersonalLetter.onRefreshComplete();
										lvPersonalLetter.setSelection(letterBeans.size() - 1);
									}
								}
							}
						} else if (status == Constants.TOKEN_FAILED) {
							Toast.makeText(PersonalLetterActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
							startActivity(new Intent(PersonalLetterActivity.this, LoginActivity.class).putExtra("back", "back"));
							finish();
						} else {
							Toast.makeText(PersonalLetterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
					}
				}
			}
			isSend = false;
		}

	}

	/**
	 * 判断聊天对方是袋鼠还是考拉
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class JudgeIsRooTask extends AsyncTask<Void, Void, JSONObject> {

		private long id;

		public JudgeIsRooTask(long id) {
			super();
			this.id = id;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				String token = SharedPrefUtil.getUserBean(PersonalLetterActivity.this).getAccessToken();
				return new BusinessHelper().profileKangarooId(id, token);
			} catch (SystemException e) {
				return null;
			}

		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						friendIsRoo = true;
						friendRooId = result.getLong("data");
					} else {
						friendIsRoo = false;
					}
				} catch (JSONException e) {
				}
			}
		}
	}
}
