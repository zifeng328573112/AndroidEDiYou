package com.elephant.ediyou.sec.code;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.LoginActivity;
import com.elephant.ediyou.activity.RooOrderDetailActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.sec.code.camera.CameraManager;
import com.elephant.ediyou.sec.code.decoding.CaptureActivityHandler;
import com.elephant.ediyou.sec.code.decoding.InactivityTimer;
import com.elephant.ediyou.sec.code.view.ViewfinderView;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

public class CaptureActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private static final String TAG = "CaptureActivity";

	private ProgressDialog pd;
	private int orderNumber;
	private Intent intent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zxing);
		// 初始化 CameraManager
		CameraManager.init(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		txtResult = (TextView) findViewById(R.id.txtResult);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		intent = getIntent();
		if (intent != null) {
			orderNumber = getIntent().getIntExtra("orderNumber", 0);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = "ISO-8859-1";

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
		txtResult.setText(obj.getBarcodeFormat().toString() + ":"
				+ obj.getText());
		// 以下至Intent之前的代码，解决二维码扫描返回数据乱码的问题
		String resultStr = obj.getText();
		if (resultStr.startsWith("{")) {
			resultStr = resultStr.substring(1, resultStr.length() - 1);
		}
		// System.out.println("### resultcode="+resultStr);
		// Intent intent = new Intent();
		// intent.putExtra("scanResult", resultStr);
		//
		// setResult(200, intent);
		// finish();
		String UTF_Str = "";
		String GB_Str = "";
		boolean is_cN = false;
		try {
			UTF_Str = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
			is_cN = StringUtil.isChineseCharacter(UTF_Str);
			// 防止有人特意使用乱码来生成二维码来判断的情况
			boolean b = StringUtil.isSpecialCharacter(resultStr);
			if (b) {
				is_cN = true;
			}
			if (!is_cN) {
				GB_Str = new String(resultStr.getBytes("ISO-8859-1"), "GB2312");
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int qrCodeOrderNumber = -1;
		if (is_cN) {
			txtResult.setText(UTF_Str);
			String jsonStr;
			if (UTF_Str.contains(",\"USERID")) {

				jsonStr = UTF_Str.substring(14, UTF_Str.indexOf(",\"USERID"));
			} else
				jsonStr = UTF_Str.substring(14, UTF_Str.indexOf(";USERID"));
			qrCodeOrderNumber = Integer.parseInt(jsonStr);
		} else {
			txtResult.setText(GB_Str);
			String jsonStr;
			if (GB_Str.contains(",\"USERID")) {

				jsonStr = GB_Str.substring(14, GB_Str.indexOf(",\"USERID"));
			} else
				jsonStr = GB_Str.substring(14, GB_Str.indexOf(";USERID"));
			qrCodeOrderNumber = Integer.parseInt(jsonStr);
			/*try {
				JSONObject json = new JSONObject(GB_Str);
				qrCodeOrderNumber = json.getInt("OID");
			} catch (JSONException e) {
			}*/
		}

		if (orderNumber == qrCodeOrderNumber) {
			if (NetUtil.checkNet(this)) {
				String access_token = SharedPrefUtil.getUserBean(this)
						.getAccessToken();
				new UpdateOrderStateTask(qrCodeOrderNumber,
						Constants.KOALA_NOT_CONFIRM, access_token).execute();
			} else {
				Toast.makeText(this, "无法连接到网络，请检查网络后，重新扫描二维码",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	/**
	 * 修改订单状态；
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateOrderStateTask extends AsyncTask<Void, Void, JSONObject> {

		private int state;
		private int orderNumber;
		private String access_token;

		public UpdateOrderStateTask(int orderNumber, int state,
				String access_token) {
			this.orderNumber = orderNumber;
			this.state = state;
			this.access_token = access_token;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(CaptureActivity.this);
				pd.setMessage("正在提交到服务器...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateOrderState(orderNumber + "",
						state, access_token);
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
						Toast.makeText(CaptureActivity.this,
								"您已确认与袋鼠见面，祝您旅途愉快！", Toast.LENGTH_LONG).show();
						setResult(RESULT_OK);
						finish();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(CaptureActivity.this, R.string.time_out,
								Toast.LENGTH_LONG).show();
						startActivity(new Intent(CaptureActivity.this,
								LoginActivity.class));
					} else {
						Toast.makeText(
								CaptureActivity.this,
								result.getString("error") + "，请重新扫描或者直接联系E地游客服",
								Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(CaptureActivity.this,
						"服务器请求失败，请重新扫描或者直接联系E地游客服", Toast.LENGTH_SHORT).show();
			}
		}
	}
}