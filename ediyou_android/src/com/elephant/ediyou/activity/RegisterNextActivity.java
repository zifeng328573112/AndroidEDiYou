package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 注册页面二（个人信息填写）
 * 
 * @author syghh
 * 
 */
public class RegisterNextActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private String phoneNum;
	private String password;
	private long userId;
	private String accessToken;

	private ImageView ivSetPhoto;
	private EditText edtNickName;
	private TextView tvBirthday;
	private LinearLayout llMale;
	private LinearLayout llFale;
	private TextView tvMale,tvFale;
	
	private String nickNamStr;
	private String birthdayStr;

	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File avatarFile;// 头像文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;

	// date and time
	private int mYear;
	private int mMonth;
	private int mDay;
	static final int DATE_DIALOG_ID = 1;
	private int currentId = -1;

	// 男生or女生
	private boolean genderBoolean = true;// 男生为true
	private String gender = "m";// 男为m，女为f
	private ProgressDialog pd;
	private boolean isThirdLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_next_layout);
		password = getIntent().getStringExtra("password");
		phoneNum = getIntent().getStringExtra("email");
//		userId = (long) getIntent().getIntExtra("userId", 0);
//		accessToken = getIntent().getStringExtra("access_token");
		createPhotoDir();
		findView();
		fillData();
		isThirdLogin = getIntent().getBooleanExtra("thirdLogin", false);
		if (isThirdLogin) {
			UserBean userBean = SharedPrefUtil.getUserBean(this);
			userId = userBean.getUserId();
			accessToken = userBean.getAccessToken();
		}
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
		ivSetPhoto = (ImageView) this.findViewById(R.id.ivSetPhoto);
		edtNickName = (EditText) this.findViewById(R.id.edtNickName);
		tvBirthday = (TextView) this.findViewById(R.id.tvBirthday);
		tvBirthday.setOnClickListener(this);
		llMale = (LinearLayout) this.findViewById(R.id.llMale);
		llFale = (LinearLayout) this.findViewById(R.id.llFale);

		tvMale = (TextView) this.findViewById(R.id.tvMale);
		tvFale = (TextView) this.findViewById(R.id.tvFale);
		
		ivSetPhoto.setOnClickListener(this);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		llMale.setOnClickListener(this);
		llFale.setOnClickListener(this);

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public void fillData() {
		tvTitle.setText("注册");

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 提交
			nickNamStr = edtNickName.getText().toString().trim();
			birthdayStr = tvBirthday.getText().toString().trim();
			if (StringUtil.isBlank(nickNamStr) || StringUtil.isBlank(birthdayStr)) {
				if (StringUtil.isBlank(nickNamStr)) {
					Toast.makeText(this, "请填写您的昵称", Toast.LENGTH_SHORT).show();
				}
				if (StringUtil.isBlank(birthdayStr)) {
					Toast.makeText(this, "请填写您的生日", Toast.LENGTH_SHORT).show();
				}
			} else if (StringUtil.countStringLength(nickNamStr, 10)) {
				Toast.makeText(this, "昵称长度不能超过10个字哦", Toast.LENGTH_SHORT).show();
			} else {
				if (genderBoolean == true) {
					gender = "m";
				} else {
					gender = "f";
				}
				// 提交注册
				new RegistTask(phoneNum, password).execute();
			}

			break;
		case R.id.ivSetPhoto:// 头像
			createUploadIconDialog();
			break;
		case R.id.tvBirthday:
			new MyDatePickerDialog(RegisterNextActivity.this, new MyDatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth) {
					String monthOfYearStr = String.valueOf(monthOfYear);
					String dayOfMonthStr = String.valueOf(dayOfMonth);
					if (monthOfYearStr.length() == 1) {
						monthOfYearStr = "0" + monthOfYearStr;
					}
					if (dayOfMonthStr.length() == 1) {
						dayOfMonthStr = "0" + dayOfMonthStr;
					}
					String birthdayStr = year + "-" + monthOfYearStr + "-" + dayOfMonthStr;
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date birthdayDate = null;
					try {
						birthdayDate = df.parse(birthdayStr);

					} catch (ParseException e) {
						e.printStackTrace();
					}
					long birthdayL = birthdayDate.getTime();
					Calendar mCalendar = Calendar.getInstance();
					long currentTimeL = mCalendar.getTimeInMillis();
					if (birthdayL > currentTimeL) {
						Toast.makeText(RegisterNextActivity.this, "生日不能为未来哦！", Toast.LENGTH_LONG).show();
					} else {
						tvBirthday.setText(birthdayStr);

					}
				}

			}).show();
			break;
		case R.id.llMale:// 选择男生
			if (genderBoolean == false) {
				genderBoolean = true;
				llMale.setBackgroundResource(R.drawable.bg_gender_sel);
				llFale.setBackgroundResource(R.drawable.bg_gender_nor);

			} else {
				llMale.setBackgroundResource(R.drawable.bg_gender_sel);
				llFale.setBackgroundResource(R.drawable.bg_gender_nor);
			}
			tvMale.setTextColor(Color.rgb(157, 208, 99));
			tvFale.setTextColor(Color.rgb(134, 134, 134));
			break;
		case R.id.llFale:// 选择女生
			if (genderBoolean == true) {
				genderBoolean = false;
				llFale.setBackgroundResource(R.drawable.bg_gender_sel);
				llMale.setBackgroundResource(R.drawable.bg_gender_nor);
			} else {
				llFale.setBackgroundResource(R.drawable.bg_gender_sel);
				llMale.setBackgroundResource(R.drawable.bg_gender_nor);
			}
			tvFale.setTextColor(Color.rgb(157, 208, 99));
			tvMale.setTextColor(Color.rgb(134, 134, 134));
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
		ab.setTitle("选择头像");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(RegisterNextActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					intent.putExtra("crop", "true");
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					intent.putExtra("outputX", 200);
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
				ivSetPhoto.setImageBitmap(cameraBitmap);

				try {
					// 保存缩略图
					FileOutputStream out = null;
					File file = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(userId)));
					if (file != null && file.exists()) {
						file.delete();
					}
					avatarFile = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(userId)));
					out = new FileOutputStream(avatarFile, false);

					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(RegisterNextActivity.this, StringUtil.getExceptionInfo(e));
				}

				break;
			case CAMERA_WITH_DATA:// 拍照
				doCropPhoto(mCurrentPhotoFile);
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
			MobclickAgent.reportError(RegisterNextActivity.this, StringUtil.getExceptionInfo(e));
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
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		return intent;
	}
	
	/**
	 * 注册
	 * 
	 * @author syghh
	 * 
	 */
	class RegistTask extends AsyncTask<Void, Void, JSONObject> {

		private String username;
		private String password;

		public RegistTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RegisterNextActivity.this);
				pd.setMessage("正在注册...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().register(username, password);
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
						JSONObject userJson = result.getJSONObject("user");
						int userId = userJson.getInt("user_id");
						String accessToken = userJson.getString("access_token");
						new profileInfoAddTask(nickNamStr, birthdayStr, gender, avatarFile, userId, accessToken).execute();
//						Toast.makeText(RegisterSetPasswordActivity.this, "注册成功！您已经成为考拉用户", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RegisterNextActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterNextActivity.this, "注册账号失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RegisterNextActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 补充个人信息
	 * 
	 * @author syghh
	 * 
	 */
	class profileInfoAddTask extends AsyncTask<Void, Void, JSONObject> {

		private String nickname;
		private String birthday;
		private String gender;
		private File avatarFile;
		private long userId;
		private String accessToken;

		public profileInfoAddTask(String nickname, String birthday, String gender, File avatarFile, long userId, String accessToken) {
			this.nickname = nickname;
			this.birthday = birthday;
			this.gender = gender;
			this.avatarFile = avatarFile;
			this.userId = userId;
			this.accessToken = accessToken;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (avatarFile != null && avatarFile.exists()) {
					return new BusinessHelper().profileUpload(nickname, birthday, gender, avatarFile, userId, accessToken);
				} else {
					return new BusinessHelper().profileUpdate(nickname, birthday, gender, userId, accessToken);
				}
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
						JSONObject userJson = result.getJSONObject("user");
						UserBean userBean = new UserBean(userJson);
						SharedPrefUtil.setUserBean(RegisterNextActivity.this, userBean);
						if (isThirdLogin) {
							startActivity(new Intent(RegisterNextActivity.this, MainHomeActivityGroup.class));
						} else {
							startActivity(new Intent(RegisterNextActivity.this, LoginActivity.class).putExtra("username", phoneNum));
						}
						finish();
						Toast.makeText(RegisterNextActivity.this, "注册成功！您已经成为考拉用户", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RegisterNextActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterNextActivity.this, "提交失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RegisterNextActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
