package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.RooAuthListBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 申请成为袋鼠
 * 
 * @author
 * 
 */
public class RooApplyActivity extends Activity implements IBaseActivity,
		OnClickListener {

	private CommonApplication app;
	private UserBean userBean;
	private long uId;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private ProgressDialog pd;

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private static ListView lvRooAuthList;
	private TextView tvProtocolCheck;

	private AuthListAdapter authListAdapter;
	private AuthListTask authListTask;
	private List<RooAuthListBean> rooAuthListBean;

	// For Dialog show
	private EditText dialogEditText;
	private EditText dialogEtName;
	private ImageView ivIdPhoto;
	private Button btnAddIdPhoto;

	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File photoFile;// 照片文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_apply);
		app = (CommonApplication) getApplication();
		userBean = SharedPrefUtil.getUserBean(this);
		uId = userBean.getUserId();
		findView();
		fillData();
		updateList();
		createPhotoDir();
		app.addActivity(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvProtocolCheck:
			Intent intent = new Intent(this, RegisterProtocolActivity.class);
			intent.putExtra("isKangaroo", Constants.ROO); // 0-考拉， 1-袋鼠
			startActivity(intent);
			break;
		case R.id.btnLeft:
			finish();
			break;
		}
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		tvProtocolCheck = (TextView) findViewById(R.id.tvProtocolCheck);
		lvRooAuthList = (ListView) this.findViewById(R.id.lvRooAuthList);

	}

	@Override
	public void fillData() {
		tvTitle.setText("申请袋鼠");
		tvProtocolCheck.setText(Html.fromHtml("<u><font color=\"#3366FF\">"
				+ "《E地游袋鼠协议》" + "</u>"));
		tvProtocolCheck.setOnClickListener(this);
		btnLeft.setOnClickListener(this);
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

	/**
	 * 创建照片存储目录
	 */
	private void createPhotoDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory()
					+ "/" + Constants.APP_DIR_NAME + "/");
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
		ab.setTitle("选择照片");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" },
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:// 相机拍摄
							if (Environment.getExternalStorageState().equals(
									Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
								doTakePhoto();// 用户点击了从照相机获取
							} else {
								Toast.makeText(RooApplyActivity.this,
										"请检查SD卡是否正常", Toast.LENGTH_SHORT)
										.show();
							}
							break;
						case 1:// 手机相册
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							intent.putExtra("return-data", true);
							startActivityForResult(intent,
									PHOTO_PICKED_WITH_DATA);
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
			mCurrentPhotoFile = new File(PHOTO_DIR,
					ImageUtil.getPhotoFileName());// 给新照的照片文件命名
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
			case PHOTO_PICKED_WITH_DATA:
				Bitmap cameraBitmap = data.getParcelableExtra("data");
				if (cameraBitmap == null) {
					Uri dataUri = data.getData();
					Intent intent = getCropImageIntent(dataUri,
							PHOTO_PICKED_WITH_DATA);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					return;
				}
				ivIdPhoto.setImageBitmap(cameraBitmap);
				try {
					// 保存缩略图
					FileOutputStream out = null;
					photoFile = new File(PHOTO_DIR,
							ImageUtil.getPhotoFileName());
					if (photoFile != null && photoFile.exists()) {
						photoFile.delete();
					}
					out = new FileOutputStream(photoFile, false);

					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100,
							out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(RooApplyActivity.this,
							StringUtil.getExceptionInfo(e));
				}

				break;
			case CAMERA_WITH_DATA:// 拍照
				doCropPhoto(mCurrentPhotoFile);
				break;

			default:
				break;
			}

		}
	};

	/**
	 * 调用图片剪辑程序去剪裁图片
	 * 
	 * @param f
	 */
	protected void doCropPhoto(File f) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = getCropImageIntent(Uri.fromFile(f),
					CAMERA_WITH_DATA);
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			MobclickAgent.reportError(this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 调用图片剪辑程序
	 */
	public Intent getCropImageIntent(Uri photoUri, int type) {
		String img_path = null;
		Drawable drawable;
		int width = 0;
		int height = 0;
		if (type == PHOTO_PICKED_WITH_DATA) {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = managedQuery(photoUri, proj, null, null,
					null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			img_path = actualimagecursor.getString(actual_image_column_index);
			// File file = new File(img_path);
			drawable = Drawable.createFromPath(img_path);
			width = drawable.getIntrinsicWidth();
			height = drawable.getIntrinsicHeight();
		} else if (type == CAMERA_WITH_DATA) {
			img_path = photoUri.getPath();
			drawable = Drawable.createFromPath(img_path);
			width = drawable.getIntrinsicWidth();
			height = drawable.getIntrinsicHeight();
		}

		int aspectX;
		int aspectY;
		int outputX;
		int outputY;
		// 防止原图像素过大造成无法截图
		if (width > 1000 || height > 1000) {
			aspectX = width / 50;
			aspectY = height / 50;
			outputX = width / 10;
			outputY = height / 10;
		} else {
			aspectX = width / 10;
			aspectY = height / 10;
			outputX = width / 2;
			outputY = height / 2;
		}

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("return-data", true);
		return intent;
	}

	/**
	 * 首页活动列表
	 * 
	 * @author syghh
	 * 
	 */
	private static class AuthListAdapter extends BaseAdapter {
		private Context mContext;
		private List<RooAuthListBean> beansList = null;
		private Boolean isNull;
		private Handler diagHandle;

		public AuthListAdapter(Context context, Handler diaHdl) {
			this.mContext = context;
			this.diagHandle = diaHdl;
		}

		public void setData(List<RooAuthListBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.beansList = eventBeans;
		}

		public void add(List<RooAuthListBean> eventBeans) {
			this.beansList.clear();
			this.beansList.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (beansList != null)
				beansList.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			int len = beansList.size();
			return beansList.size();
		}

		@Override
		public Object getItem(int position) {
			return beansList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.roo_auth_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			this.fillData(position, viewHolder);

			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.viewAuth = convertView.findViewById(R.id.viewAuth);
			viewHolder.tvFail = (TextView) convertView
					.findViewById(R.id.tvFail);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.tvTitle);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tvTime);
			viewHolder.tvPass = (TextView) convertView
					.findViewById(R.id.tvPass);
			viewHolder.btnGotToAuth = (Button) convertView
					.findViewById(R.id.btnGotToAuth);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder) {
			String tmpStr = null;
			RooAuthListBean bean = beansList.get(position);
			viewHolder.tvTitle.setText(bean.getAuthTitle());
			tmpStr = bean.getAuthTime().toString();
			if (!StringUtil.isBlank(tmpStr)) {
				viewHolder.tvTime.setText(bean.getAuthTime().substring(0, 9));
			}
			switch (bean.getState()) {
			case -1:// 获取列表
			case 0: // 未认证
				viewHolder.viewAuth.setVisibility(View.GONE);
				viewHolder.btnGotToAuth.setVisibility(View.VISIBLE);
				viewHolder.tvFail.setVisibility(View.GONE);
				// 0,'健康证验证'),(1,'身份证验证'),(2,'导游证验证'),(4,'手机验证'),(5,'邮箱验证'),(6,'支付宝验证'),(7,'银行卡验证');
				break;
			case 1: // 审核通过
				viewHolder.viewAuth.setVisibility(View.VISIBLE);
				viewHolder.btnGotToAuth.setVisibility(View.GONE);
				viewHolder.tvFail.setVisibility(View.GONE);

				viewHolder.tvPass.setText("通过认证");
				viewHolder.tvTime.setText(bean.getAuthTime());
				break;
			case 2: // 审核未通过，显示未通过原因
				viewHolder.viewAuth.setVisibility(View.GONE);
				viewHolder.btnGotToAuth.setVisibility(View.VISIBLE);
				viewHolder.tvFail.setVisibility(View.VISIBLE);

				viewHolder.btnGotToAuth.setText("重新认证");
				break;
			case 3: // 审核中...
				viewHolder.viewAuth.setVisibility(View.VISIBLE);
				viewHolder.btnGotToAuth.setVisibility(View.GONE);
				viewHolder.tvFail.setVisibility(View.GONE);

				viewHolder.tvPass.setText("审核中");
				viewHolder.tvTime.setText(bean.getAuthTime());
			}
			viewHolder.btnGotToAuth.setTag(String.format("%d", bean.getaId()));
			viewHolder.btnGotToAuth.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle bd = new Bundle();
					bd.putInt("auth_type",
							Integer.parseInt((String) v.getTag()));
					Message msg = new Message();
					msg.setData(bd);
					diagHandle.sendMessage(msg);
				}
			});
			if (TextUtils.isEmpty(bean.getAuthTime())) {
				viewHolder.tvTime.setVisibility(View.GONE);
			} else {
				viewHolder.tvTime.setVisibility(View.VISIBLE);
			}
		}

		private class ViewHolder {
			private View viewAuth;
			private TextView tvFail;
			private TextView tvTitle;
			private TextView tvTime;
			private TextView tvPass;
			private Button btnGotToAuth;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance()
					.loadDrawable(img, new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							ImageView image = null;
							image = (ImageView) lvRooAuthList
									.findViewWithTag(imageUrl);
							if (image != null) {
								if (imageDrawable != null) {
									image.setImageDrawable(imageDrawable);
								} else {
									image.setImageResource(R.drawable.bg_photo_defualt);
								}
							}
						}
					});
			if (cacheDrawable != null) {
				imageView.setImageDrawable(cacheDrawable);
			} else {
				imageView.setImageResource(R.drawable.bg_photo_defualt);
			}
		}
	}

	/**
	 * 认证列表异步任务
	 * 
	 * @author syghh
	 * 
	 */
	class AuthListTask extends AsyncTask<Void, Void, JSONObject> {
		public AuthListTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooApplyActivity.this);
			}
			pd.setMessage("正在加载认证信息，请稍后...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().rooAuth(uId,
						userBean.getAccessToken());
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
						JSONArray beanListJson = result.getJSONArray("data");
						rooAuthListBean = RooAuthListBean
								.constractList(beanListJson);
						if (rooAuthListBean.get(0).getState() == 1
								&& rooAuthListBean.get(1).getState() == 1
								&& rooAuthListBean.get(2).getState() == 1) {
							userBean.setIsKangaroo(1);
							SharedPrefUtil.setUserBean(RooApplyActivity.this,
									userBean);
						}
						if (authListAdapter == null
								|| authListAdapter.getCount() == 0) {
							authListAdapter = new AuthListAdapter(
									RooApplyActivity.this, dialogHdl);
							authListAdapter.setData(rooAuthListBean);
							lvRooAuthList.setAdapter(authListAdapter);
							pageNo = 1;
						} else {
							authListAdapter.add(rooAuthListBean);
						}
						pageNo = pageNo + 1;

						Toast.makeText(RooApplyActivity.this, "加载成功",
								Toast.LENGTH_LONG).show();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(RooApplyActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooApplyActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooApplyActivity.this,
								result.getString("error"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooApplyActivity.this, "加载失败",
							Toast.LENGTH_LONG).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(RooApplyActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
			cancel(true);
		}
	}

	protected void updateList() {
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				authListTask = new AuthListTask();
				authListTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG)
					.show();
		}
	}

	public Handler dialogHdl = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (false == LIST_RECORD_TASK_RUNING) {
				showDialog(msg.getData().getInt("auth_type"));
			}
		}
	};

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch ((byte) id) {
		case 0: // (0,'健康证验证')
			dialog = authCheckEditDialogShow("健康证验证", "请输入健康证号码:", "发送", id);
			break;
		case 1: // (1,'身价证验证')
			dialog = authCheckEditDialogShow("身份证验证", "请输入身份证号码:", "发送", id);
			break;
		case 2: // (2,'导游证验证')
			dialog = authCheckEditDialogShow("导游证验证", "请输入导游证号码:", "发送", id);
			break;
		case 3: // (3,'')
			break;
		case 4: // (4,'手机验证')
			dialog = authCheckEditDialogShow("手机验证", "请输入手机号码:", "发送", id);
			break;
		case 5: // (5,'邮箱验证')
			dialog = authCheckEditDialogShow("电子邮箱验证", "请输入电子邮箱地址:", "发送", id);
			break;
		case 6: // (6,'支付宝验证')
			dialog = authCheckEditDialogShow("绑定支付宝验证", "请输入支付宝帐号:", "发送", id);
			break;
		case 7: // (7,'银行卡验证')
			dialog = authCheckEditDialogShow("银行卡验证", "请输入银行卡号码:", "发送", id);
			break;
		}

		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		removeDialog(id);
	}

	public Handler dialogSendHdl = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (false == LIST_RECORD_TASK_RUNING) {
				switch ((byte) (msg.getData().getInt("authcardId"))) {
				case 0: // (0,'健康证验证')
				case 1: // (1,'身份证验证')
				case 2: // (2,'导游证验证')
					new AuthCheckTask(msg.getData().getString("code"), msg
							.getData().getString("name"), photoFile, msg
							.getData().getInt("authcardId")).execute();
					break;
				case 3: // (3,'')
					break;
				case 4: // (4,'手机验证')
					sendCheckMessageToServer();
					break;
				case 5: // (5,'邮箱验证')
					// sendCheckMail("xxx@gmail.com",
					// msg.getData().getString("code"));
					new AuthCheckTask(msg.getData().getString("code"), null,
							null, msg.getData().getInt("authcardId")).execute();

					break;
				case 6: // (6,'支付宝验证')
					new AuthCheckTask(msg.getData().getString("code"), null,
							null, msg.getData().getInt("authcardId")).execute();
					break;
				case 7: // (7,'银行卡验证')
					new AuthCheckTask(msg.getData().getString("code"), null,
							null, msg.getData().getInt("authcardId")).execute();
					break;
				}

			}
		}
	};

	private Dialog authCheckEditDialogShow(String title, String message,
			String PosButtonText, final int authType) {
		final AlertDialog dlg = new AlertDialog.Builder(RooApplyActivity.this)
				.setTitle(title).setMessage(message).create(); // 设置内容
		// .setView(dialogEditText)
		dlg.setButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 点击"取消"按钮之后退出程序
				dlg.cancel();
			}
		});
		dlg.setButton2("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				Bundle bd = new Bundle();
				String tmpStr = dialogEditText.getText().toString();
				if (!TextUtils.isEmpty(tmpStr)&& authType != 1&& authType != 6) {
					if (!StringUtil.isEmail(tmpStr)) {
						Toast.makeText(RooApplyActivity.this, "邮箱格式不正确",
								Toast.LENGTH_SHORT).show();
						return;
					}
					bd.putString("code", tmpStr);
				}
				/*else {
					Toast.makeText(RooApplyActivity.this, "请输入认证信息",
							Toast.LENGTH_SHORT).show();
					return;
				}*/

				if (dialogEtName != null && authType == 1) {
					String nameStr = dialogEtName.getText().toString();
					if (!TextUtils.isEmpty(nameStr)) {
						bd.putString("name", nameStr);
					} else {
						Toast.makeText(RooApplyActivity.this, "请输入姓名",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}

				bd.putInt("authcardId", authType);
				Message msg = new Message();
				msg.setData(bd);
				dialogSendHdl.sendMessage(msg);
				dlg.cancel();
			}
		});// 创建
		switch ((byte) authType) {
		case 0: // (0,'健康证验证')
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		case 1: // (1,'身份证验证')
			View view = this.getLayoutInflater()
					.inflate(R.layout.id_auth, null);
			dialogEditText = (EditText) view.findViewById(R.id.etIdAuth);
			StringUtil.limitEditTextLength(dialogEditText, 18, this);
			dialogEtName = (EditText) view.findViewById(R.id.etNameAuth);
			ivIdPhoto = (ImageView) view.findViewById(R.id.ivIdPhoto);
			btnAddIdPhoto = (Button) view.findViewById(R.id.btnAddIdPhoto);
			btnAddIdPhoto.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					createUploadIconDialog();
				}
			});
			dlg.setView(view);
			break;
		case 2: // (2,'导游证验证')
			// For Dialog show
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		case 3: // (3,'')
			break;
		case 4: // (4,'手机验证')
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		case 5: // (5,'邮箱验证')
			// For Dialog show
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		case 6: // (6,'支付宝验证')
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		case 7: // (7,'银行卡验证')
			dialogEditText = new EditText(this);
			dlg.setView(dialogEditText);
			break;
		}

		return dlg;
	}

	/**
	 * 认证验证
	 * 
	 * @author syghh
	 * 
	 */
	private class AuthCheckTask extends AsyncTask<Void, Void, JSONObject> {

		private String codeString;
		private File file;
		private long authcardId;
		private String realyName;

		public AuthCheckTask(String codeIn, String realyName, File fl,
				long authcardIdIn) {
			LIST_RECORD_TASK_RUNING = true;
			codeString = codeIn;
			file = fl;
			authcardId = authcardIdIn;
			this.realyName = realyName;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooApplyActivity.this);
			}
			pd.setMessage("正在发送认证信息，请稍后...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().rooAuthCheck(uId, codeString,
						realyName, file, authcardId);
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
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						Toast.makeText(RooApplyActivity.this, "发送成功",
								Toast.LENGTH_LONG).show();
						updateList();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(RooApplyActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooApplyActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooApplyActivity.this,
								result.getString("error"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooApplyActivity.this, "加载失败",
							Toast.LENGTH_LONG).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(RooApplyActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
			}

			cancel(true);
		}
	}

	private void sendCheckMessageToServer() {
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(
				RooApplyActivity.this, 0, new Intent(), 0);
		smsManager.sendTextMessage("13911112222", null, "E地游手机验证测试短信",
				sentIntent, null);
	}

	// private void sendCheckMail(String mailAddr, String content) {
	// String subject = "E地游邮箱验证";
	//
	// // 创建Intent
	// Intent emailIntent = new Intent(
	// android.content.Intent.ACTION_SEND);
	// //设置内容类型
	// emailIntent.setType("plain/text");
	// //设置额外信息
	// emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] {
	// mailAddr });
	// emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,subject);
	// emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
	// //启动Activity
	// startActivity(Intent.createChooser(emailIntent, "发送邮件..."));
	// }

	/********************************** 获取照片、相机，并截图 ***********************************/

}
