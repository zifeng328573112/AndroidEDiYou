package com.elephant.ediyou.view;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * 自定义日历GridView
 * 
 * @author Zhoujun
 * 
 */
public class CalendarGridView extends GridView {

	private Context mContext;
	public static final int GRIDVIEW_SPACING = 5;

	public CalendarGridView(Context context) {
		super(context);
		mContext = context;

		setGirdView();
	}

	private void setGirdView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		setLayoutParams(params);
		setNumColumns(7);// 设置每行列数
		setGravity(Gravity.CENTER_VERTICAL);// 位置居中
		setVerticalSpacing(GRIDVIEW_SPACING);// 垂直间隔
		setHorizontalSpacing(GRIDVIEW_SPACING);// 水平间隔
		// 设置背景
		// setBackgroundColor(getResources().getColor(R.color.calendar_background));
		// 设置参数
		WindowManager windowManager = ((Activity) mContext).getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int i = display.getWidth() / 7;
		int j = display.getWidth() - (i * 7);
		int x = j / 2;
		setPadding(x, 0, 0, 0);// 居中
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
