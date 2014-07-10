package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.EventTypesBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 创建活动；
 * @author ISP
 *
 */
public class EventNewActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private RelativeLayout rlChoiceStartTime;// 点击选择开始时间
	private TextView tvEventStartTime;// 设置选择开始时间
	private RelativeLayout rlChoiceEndTime;// 点击选择时长
	private TextView tvEventEndTime;// 设置选择结束时间
	private TextView tvEventStartEndTimeShow;// 显示选择的起止时间
	private String startTimeStr;
	private String endTimeStr;

	private TextView tvEventTotal;// 人数上限
	private TextView tvEventJoinedCount;// 此次活动人数限制
	private SeekBar skPeopleNum;// 选择条
	private RadioGroup rgEventState;// 类型组
	private RadioButton rbTravleEvent;// 旅游类型
	private RadioButton rbNightEvent;// 夜店类型
	private RadioButton rbBuysEvent;// 购物类型
	private RadioButton rbFoodsEvent;// 美食类型
	private EditText edtEventPrice;// 人均费用
	private EditText edtEventTheme;// 活动主题词
	private EditText edtEventContent;// 活动内容描述
	private LinearLayout llWarnAddNewThemePhoto;// 点击上传主题图
	private ImageView ivNewThemePhoto;// 设置主题图

	private List<EventTypesBean> eventTypesBeans;// 活动类型
	private long activityId;

	private int insetType;
	private final static int ONLY_INSERT = 0;
	private final static int INSERT_AND_ADD = 1;

	private long userId;
	private long kangarooId;
	private String title;// 主题词
	private String content;// 说明
//	private String startTime;// 开始时间（到毫秒）
//	private String endTime;// 结束时间（到毫秒）
	private int limitCount;// 人数总数
	private int eventType = -1;// 活动类型
	private double cost;// 活动人均费用

	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File themePhotofile;// 主题图
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_new);
		userId = (long) SharedPrefUtil.getUserBean(this).getUserId();
		kangarooId = SharedPrefUtil.getRooId(this);
		// userId = 1;// 测试
		// kangarooId = 1;// 测试
		// 获取活动类型
		if (NetUtil.checkNet(this)) {
			new ActivityFindAllTypeTask().execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		createPhotoDir();
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("新建活动");

		rlChoiceStartTime = (RelativeLayout) this.findViewById(R.id.rlChoiceStartTime);
		tvEventStartTime = (TextView) this.findViewById(R.id.tvEventStartTime);
		rlChoiceEndTime = (RelativeLayout) this.findViewById(R.id.rlChoiceEndTime);
		tvEventEndTime = (TextView) this.findViewById(R.id.tvEventEndTime);
		tvEventStartEndTimeShow = (TextView) this.findViewById(R.id.tvEventStartEndTimeShow);

		tvEventTotal = (TextView) this.findViewById(R.id.tvEventTotal);
		tvEventJoinedCount = (TextView) this.findViewById(R.id.tvEventJoinedCount);
		skPeopleNum = (SeekBar) this.findViewById(R.id.skPeopleNum);
		rgEventState = (RadioGroup) this.findViewById(R.id.rgEventState);
		rbTravleEvent = (RadioButton) this.findViewById(R.id.rbTravleEvent);
		rbNightEvent = (RadioButton) this.findViewById(R.id.rbNightEvent);
		rbBuysEvent = (RadioButton) this.findViewById(R.id.rbBuysEvent);
		rbFoodsEvent = (RadioButton) this.findViewById(R.id.rbFoodsEvent);
		edtEventPrice = (EditText) this.findViewById(R.id.edtEventPrice);
		edtEventTheme = (EditText) this.findViewById(R.id.edtEventTheme);
		edtEventContent = (EditText) this.findViewById(R.id.edtEventContent);
		llWarnAddNewThemePhoto = (LinearLayout) this.findViewById(R.id.llWarnAddNewThemePhoto);
		ivNewThemePhoto = (ImageView) this.findViewById(R.id.ivNewThemePhoto);

		rlChoiceStartTime.setOnClickListener(this);
		rlChoiceEndTime.setOnClickListener(this);
		llWarnAddNewThemePhoto.setOnClickListener(this);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		//限制字数
		StringUtil.limitEditTextLength(edtEventTheme, 64, this);
		StringUtil.limitEditTextLength(edtEventContent, 600, this);
		
		skPeopleNum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// tvEventJoinedCount.setText("当前" +
				// String.valueOf(skPeopleNum.getProgress()) + "人");

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tvEventJoinedCount.setText("当前" + String.valueOf(skPeopleNum.getProgress()) + "人");

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:
			title = edtEventTheme.getText().toString().trim();
			content = edtEventContent.getText().toString().trim();
			limitCount = skPeopleNum.getProgress();
			eventType = (Integer) ((RadioButton) findViewById(rgEventState.getCheckedRadioButtonId())).getTag();
			List<Integer> types = new ArrayList<Integer>();
			for (int i = 0; i < eventTypesBeans.size(); i++) {
				types.add(eventTypesBeans.get(i).getId());
			}
			if (StringUtil.isBlank(title) || StringUtil.isBlank(content) || StringUtil.isBlank(edtEventPrice.getText().toString().trim())) {
				Toast.makeText(EventNewActivity.this, "请您填写完整的活动信息", Toast.LENGTH_LONG).show();
			} else if (StringUtil.isBlank(startTimeStr) || StringUtil.isBlank(endTimeStr)) {
				Toast.makeText(EventNewActivity.this, "请您选择活动时间信息", Toast.LENGTH_LONG).show();
			} else if (limitCount == 0) {
				Toast.makeText(EventNewActivity.this, "活动人数不能为零哦", Toast.LENGTH_LONG).show();
			} else if (!types.contains(eventType)) {
				Toast.makeText(EventNewActivity.this, "请点选活动的类型", Toast.LENGTH_LONG).show();
			} else if( themePhotofile == null){
				Toast.makeText(EventNewActivity.this, "请添加活动主题图", Toast.LENGTH_LONG).show();
			}else {
				cost = Double.parseDouble(edtEventPrice.getText().toString().trim());
				LayoutInflater inflater = EventNewActivity.this.getLayoutInflater();
				View view = inflater.inflate(R.layout.dialog_common_layout, null);
				TextView tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
				Button btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
				Button btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);
				btnDialogLeft.setText("立即创建新活动");
				btnDialogRight.setText("创建并添加照片");
				tvDialogMsg.setText("请您选择下一步的操作");
				final Dialog dialog = new Dialog(EventNewActivity.this, R.style.dialog);
				dialog.setContentView(view);
				dialog.show();
				// 立即创建新活动
				btnDialogLeft.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						insetType = ONLY_INSERT;
						if (NetUtil.checkNet(EventNewActivity.this)) {
							new InsertActivityTask(userId, title, content, startTimeStr, endTimeStr, limitCount, eventType, themePhotofile, cost,
									kangarooId).execute();
						} else {
							Toast.makeText(EventNewActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();
					}
				});
				// 创建并添加照片
				btnDialogRight.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						insetType = INSERT_AND_ADD;
						if (NetUtil.checkNet(EventNewActivity.this)) {
							new InsertActivityTask(userId, title, content, startTimeStr, endTimeStr, limitCount, eventType, themePhotofile, cost,
									kangarooId).execute();
						} else {
							Toast.makeText(EventNewActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();
					}
				});
			}
			break;
		case R.id.rlChoiceStartTime:// 时间选择器（开始时间）
			Intent startIntent = new Intent(this, RooChooseEventTimeActivity.class);
			startIntent.putExtra("rooId", kangarooId);
			startIntent.putExtra("isChooseEndTime", false);
			startActivityForResult(startIntent, Constants.REQUEST_CHOOSE_START_TIME);
			break;
		case R.id.rlChoiceEndTime:// 时间选择器（时长）
			if (StringUtil.isBlank(tvEventStartTime.getText().toString().trim())) {
				Toast.makeText(this, "您还没有选择开始时间哦！", Toast.LENGTH_SHORT).show();
			} else {
				Intent endIntent = new Intent(this, RooChooseEventTimeActivity.class);
				endIntent.putExtra("rooId", kangarooId);
				endIntent.putExtra("isChooseEndTime", true);
				startActivityForResult(endIntent, Constants.REQUEST_CHOOSE_END_TIME);
			}
			break;
		case R.id.llWarnAddNewThemePhoto:
			createUploadIconDialog();
			break;
		default:
			break;
		}
	}

	/**
	 * 创建头像存储目录
	 */
	private void createPhotoDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/" + Constants.APP_DIR_NAME + "/");
			if (!PHOTO_DIR.exists()) {
				// 创建照片的存储目录
				PHOTO_DIR.mkdirs();
			}
		} else {
			Toast.makeText(this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 上传头像
	 * 
	 * @param v
	 */
	private void createUploadIconDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("选择主题图");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(EventNewActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					intent.putExtra("crop", "true");
					intent.putExtra("aspectX", 2);
					intent.putExtra("aspectY", 1);
					intent.putExtra("outputX", 400);
					intent.putExtra("outputY", 200);
					intent.putExtra("return-data", true);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					break;
				case 2:

					break;
				}
			}
		});
		ab.show();
	}

	/**
	 * 拍照获取图片
	 * 
	 */
	protected void doTakePhoto() {
		try {
			mCurrentPhotoFile = new File(PHOTO_DIR, ImageUtil.getPhotoFileName());// 给新照的照片文件命名
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "拍照出错", Toast.LENGTH_LONG).show();
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:// 相册
				Bitmap cameraBitmap = data.getParcelableExtra("data");
				if (cameraBitmap == null) {
					Uri dataUri = data.getData();
					Intent intent = getCropImageIntent(dataUri);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				}
				llWarnAddNewThemePhoto.setVisibility(View.GONE);
				ivNewThemePhoto.setVisibility(View.VISIBLE);
				ivNewThemePhoto.setImageBitmap(cameraBitmap);
	
				try {
					// 保存缩略图
					FileOutputStream out = null;
					File file = new File(PHOTO_DIR, ImageUtil.getPhotoFileName());
					if (file != null && file.exists()) {
						file.delete();
					}
					themePhotofile = new File(PHOTO_DIR, ImageUtil.getPhotoFileName());
					out = new FileOutputStream(themePhotofile, false);
	
					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(EventNewActivity.this, StringUtil.getExceptionInfo(e));
				}
	
				break;
			case CAMERA_WITH_DATA:// 拍照
				doCropPhoto(mCurrentPhotoFile);
				break;
			case Constants.REQUEST_CHOOSE_START_TIME:
				startTimeStr = data.getStringExtra("startTime");
				tvEventStartTime.setText(startTimeStr);
				break;
			case Constants.REQUEST_CHOOSE_END_TIME:
				endTimeStr = data.getStringExtra("endTime");
				tvEventEndTime.setText(endTimeStr);
				break;
			}
		}
	}

	/**
	 * 调用图片剪辑程序去剪裁图片
	 * 
	 * @param f
	 */
	protected void doCropPhoto(File f) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			MobclickAgent.reportError(EventNewActivity.this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 2);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 400);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		return intent;
	}

	/**
	 * 获取活动的所有类型id
	 * 
	 * @author syghh
	 * 
	 */
	private class ActivityFindAllTypeTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject commission = null;
			try {
				commission = new BusinessHelper().activityFindAllType();
			} catch (SystemException e) {
			}
			return commission;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						JSONArray eventTypesJA = result.getJSONArray("data");
						eventTypesBeans = EventTypesBean.constractList(eventTypesJA);
						rbTravleEvent.setText(eventTypesBeans.get(0).getTitle());
						rbNightEvent.setText(eventTypesBeans.get(1).getTitle());
						rbBuysEvent.setText(eventTypesBeans.get(2).getTitle());
						rbFoodsEvent.setText(eventTypesBeans.get(3).getTitle());

						rbTravleEvent.setTag(eventTypesBeans.get(0).getId());
						rbNightEvent.setTag(eventTypesBeans.get(1).getId());
						rbBuysEvent.setTag(eventTypesBeans.get(2).getId());
						rbFoodsEvent.setTag(eventTypesBeans.get(3).getId());
					} else {
						Toast.makeText(EventNewActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(EventNewActivity.this, "获取失败", Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(EventNewActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 新建活动
	 * 
	 * @author syghh
	 * 
	 */
	class InsertActivityTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private long kangarooId;
		private String title;
		private String content;
		private String startTime;
		private String endTime;
		private int limitCount;
		private int type;
		private File file;
		private double cost;

		public InsertActivityTask(long userId, String title, String content, String startTime, String endTime, int limitCount, int type, File file,
				double cost, long kangarooId) {
			this.userId = userId;
			this.title = title;
			this.content = content;
			this.startTime = startTime;
			this.endTime = endTime;
			this.limitCount = limitCount;
			this.type = type;
			this.file = file;
			this.cost = cost;
			this.kangarooId = kangarooId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventNewActivity.this);
				pd.setMessage("正在创建新活动...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().insertActivity(userId, title, content, startTime, endTime, limitCount, type, file, cost, kangarooId);
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
						activityId = result.getLong("data");
						if (insetType == ONLY_INSERT) {
							// 只创建
							Intent intent = new Intent(EventNewActivity.this, EventDetailActivity.class);
							intent.putExtra("activityId", activityId);
							startActivity(intent);
							finish();
						} else if (insetType == INSERT_AND_ADD) {
							// 创建并跳转到图片添加界面
							Intent intent = new Intent(EventNewActivity.this, EventPhotoEditActivity.class);
							intent.putExtra("activityId", activityId);
							intent.putExtra("from", "new");
							startActivity(intent);
							finish();
						}
						Toast.makeText(EventNewActivity.this, "创建成功", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(EventNewActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventNewActivity.this, "创建失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventNewActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

}
