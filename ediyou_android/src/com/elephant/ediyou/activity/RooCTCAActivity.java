package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.ApproveBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠的认证中心
 * 
 * @author ISP
 * 
 */
public class RooCTCAActivity extends Activity implements IBaseActivity, OnClickListener {

	// title
	private Button btnLeft, btnRight;
	private TextView tvTitle;

	private ListView lvApprove;
	private List<ApproveBean> approveList;
	private ApproveAdapter approveAdapter;
	
	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File photoFile;// 照片文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;
	
	private int clickPosition = -1;
	private long userId = 0;
	private boolean approved = true;// true为查看已通过认证;

	private ProgressDialog pd;
	private CommonApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_ctc);
		userId = getIntent().getLongExtra("userId", 0);
		if(userId == 0){
			userId = SharedPrefUtil.getUserBean(RooCTCAActivity.this).getUserId();
			approved = false;
		}
		findView();
		fillData();
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		createPhotoDir();
	}
	/**
	 * 创建照片存储目录
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			this.finish();
			break;
		default:
			break;
		}
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

		lvApprove = (ListView) findViewById(R.id.lvApprove);
	}

	@Override
	public void fillData() {
		if(approved){
			tvTitle.setText("已通过认证");
		}else{
			tvTitle.setText("认证中心");
		}

		approveList = new ArrayList<ApproveBean>();
		approveAdapter = new ApproveAdapter();
		lvApprove.setAdapter(approveAdapter);
		if (NetUtil.checkNet(this)) {
			new getAuthInfoTask().execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		
	}
	
	/**
	 * 上传照片
	 * 
	 * @param v
	 */
	private void createUploadIconDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("选择照片");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(RooCTCAActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
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
			case PHOTO_PICKED_WITH_DATA:
				Bitmap cameraBitmap = data.getParcelableExtra("data");
				if (cameraBitmap == null) {
					Uri dataUri = data.getData();
					Intent intent = getCropImageIntent(dataUri, PHOTO_PICKED_WITH_DATA);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					return;
				}

				try {
					// 保存缩略图
					FileOutputStream out = null;
					photoFile = new File(PHOTO_DIR, ImageUtil.getPhotoFileName());
					if (photoFile != null && photoFile.exists()) {
						photoFile.delete();
					}
					out = new FileOutputStream(photoFile, false);

					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
				}
				if (NetUtil.checkNet(this)) {
					new AuthCheckTask(photoFile).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
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
			final Intent intent = getCropImageIntent(Uri.fromFile(f), CAMERA_WITH_DATA);
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
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 3);
		intent.putExtra("aspectY", 3);
		intent.putExtra("outputX", 400);
		intent.putExtra("outputY", 400);

		intent.putExtra("return-data", true);
		return intent;
	}

	private static final int NO_APPROVE = -1;//未提交认证；
	private static final int PASS = 1;// 通过认证
	private static final int REFUSE = 2; // 拒绝；
	private static final int APPROVING = 3; // 审核中；

	/**
	 * 通过认证信息适配器；
	 */
	private class ApproveAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return approveList.size();
		}

		@Override
		public Object getItem(int position) {
			return approveList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ApproveBean bean = approveList.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(RooCTCAActivity.this).inflate(R.layout.roo_auth_item, null);
				holder = new ViewHolder();
				holder.viewAuth = convertView.findViewById(R.id.viewAuth);
				holder.tvFail = (TextView) convertView.findViewById(R.id.tvFail);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
				holder.tvPass = (TextView) convertView.findViewById(R.id.tvPass);
				holder.btnGotToAuth = (Button) convertView.findViewById(R.id.btnGotToAuth);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvTitle.setText(bean.getTitle());
			holder.tvTime.setText(bean.getApproveDate());
			
			if (PASS == bean.getState()) {
				holder.viewAuth.setVisibility(View.VISIBLE);
				holder.btnGotToAuth.setVisibility(View.GONE);
				holder.tvFail.setVisibility(View.GONE);
				
				holder.tvPass.setText("通过认证");
				holder.tvTime.setText(bean.getApproveDate());
			} else if (REFUSE == bean.getState()) {
				holder.viewAuth.setVisibility(View.GONE);
				holder.btnGotToAuth.setVisibility(View.VISIBLE);
				holder.tvFail.setVisibility(View.VISIBLE);
				
				holder.btnGotToAuth.setText("重新认证");
			} else if (APPROVING == bean.getState()) {
				holder.viewAuth.setVisibility(View.VISIBLE);
				holder.btnGotToAuth.setVisibility(View.GONE);
				holder.tvFail.setVisibility(View.GONE);
				
				holder.tvPass.setText("审核中");
				holder.tvTime.setText(bean.getApproveDate());
			} else {
				holder.viewAuth.setVisibility(View.GONE);
				holder.btnGotToAuth.setVisibility(View.VISIBLE);
				holder.tvFail.setVisibility(View.GONE);
				
				holder.btnGotToAuth.setText("申请认证");
			}
			if(TextUtils.isEmpty(bean.getApproveDate())){
				holder.tvTime.setVisibility(View.GONE);
			}else{
				holder.tvTime.setVisibility(View.VISIBLE);
			}
			final int tempPosition = position;
			holder.btnGotToAuth.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					clickPosition = tempPosition;
					createUploadIconDialog();
				}
			});
			return convertView;
		}

		class ViewHolder {
			private View viewAuth;
			private TextView tvFail;
			private TextView tvTitle;
			private TextView tvTime;
			private TextView tvPass;
			private Button btnGotToAuth;
		}
	}

	/**
	 * 获取认证一些信息；
	 * @author ISP
	 *
	 */
	private class getAuthInfoTask extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooCTCAActivity.this);
			}
			pd.setMessage("正在获取...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject json = null;
			String accessToken = SharedPrefUtil.getUserBean(RooCTCAActivity.this).getAccessToken();
			try {
				if(approved){
					json = new BusinessHelper().getRooAuthInfo(userId);
				}else{
					json = new BusinessHelper().getAuthInfo(userId, accessToken);
				}
			} catch (Exception e) {
			}
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if(pd != null){
				pd.dismiss();
			}
			if (result != null) {
				try {
					int status = result.getInt("status");
					if(status == Constants.SUCCESS){
						JSONArray array = result.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);
							ApproveBean bean = new ApproveBean();
							bean.setTitle(obj.getString("authTitle"));
							bean.setaId(obj.getInt("aId"));
							bean.setApproveDate(obj.getString("authTime"));
							bean.setState(obj.getInt("state"));
							approveList.add(bean);
						}

						if (approveList.size() > 0) {
							lvApprove.setVisibility(View.VISIBLE);
							approveAdapter.notifyDataSetChanged();
						}
					}else if(status == Constants.TOKEN_FAILED){
						Toast.makeText(RooCTCAActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooCTCAActivity.this, LoginActivity.class).putExtra("back", "back"));
					}else{
						Toast.makeText(RooCTCAActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
				}
			}
		}
	}
	/**
	 * 发送认证信息
	 * @author ISP
	 *
	 */
	private class AuthCheckTask extends AsyncTask<Void, Void, JSONObject>{
		
		private File file;
		
		public AuthCheckTask(File file) {
			super();
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(pd == null){
				pd = new ProgressDialog(RooCTCAActivity.this);
			}
			pd.setMessage("正在发送认证信息...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject json = null;
			try {
				json = new BusinessHelper().rooAuthCheck(userId, "", "",file, approveList.get(clickPosition).getaId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if(pd != null){
				pd.dismiss();
			}
			if(result != null){
				try {
					int status = result.getInt("status");
					if(status == Constants.SUCCESS){
						Toast.makeText(RooCTCAActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
						approveList.get(clickPosition).setState(APPROVING);
						approveAdapter.notifyDataSetChanged();
					}
				} catch (Exception e) {
				}
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
