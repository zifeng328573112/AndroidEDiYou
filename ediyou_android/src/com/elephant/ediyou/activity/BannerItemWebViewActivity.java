package com.elephant.ediyou.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * 内嵌浏览器，Banner的type为跳转网页时
 * @author syghh
 *
 */
public class BannerItemWebViewActivity extends Activity implements IBaseActivity, OnClickListener {
	private WebView 		webView;
	private String 			bannerItemUrl;
	private String 			name;
	private View 			progress;
	
	private Button 			btnLeft, btnRight;
	private ImageView 		ivTitle;
	private TextView 		tvTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.banner_item_webview);
		bannerItemUrl 	= getIntent().getStringExtra("url");
		bannerItemUrl 	= "http://www.hao123.com";	//测试数据
		name 			= getIntent().getStringExtra("name");
		findView();
		fillData();
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

	@Override
	public void findView() {
		btnLeft 	= (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight 	= (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle 	= (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setSingleLine();
		btnLeft.setOnClickListener(this);
		progress 	= this.findViewById(R.id.progress);
		webView 	= (WebView) this.findViewById(R.id.wvProduct);
	}

	@Override
	public void fillData() {
		if(name.length() > 5){
			name = name.substring(0, 5) + "...";
		}
		tvTitle.setText(name);
		tvTitle.setMaxWidth(220);
		tvTitle.setSingleLine();
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.requestFocus();//使WebView内的输入框等获得焦点
		webView.loadUrl(bannerItemUrl);
		webView.setWebViewClient(new WebViewClient() {
			// 点击网页里面的链接还是在当前的webView内部跳转，不跳转外部浏览器
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) { 
				view.loadUrl(url);
				return true;
			}
			//可以让webView处理https请求
			@Override
			public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
				handler.proceed();
			};
			
			public void onLoadResource(WebView view, String url) {
				Log.i("ProductWebViewActivity", "url: " + url);
			};
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progress.setVisibility(View.GONE);
			}
		});	
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (webView.canGoBack()) {
				webView.goBack(); // goBack()表示返回webView的上一页面，而不直接关闭WebView
				return true;
			}else {
				finish();
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		default:
			break;
		}
	}
}
