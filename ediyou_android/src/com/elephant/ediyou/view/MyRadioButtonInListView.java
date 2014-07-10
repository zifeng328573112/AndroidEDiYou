package com.elephant.ediyou.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.elephant.ediyou.R;
/**
 * 自定义的RadioButton，可用于ListView中。需配合一个RadioButton的布局（含文字、表示选择的ImageView）使用；
 * @author syghh
 *
 */
public class MyRadioButtonInListView extends LinearLayout {
	private Context context;
	private ImageView imageView;
	private TextView textView;

	private int index = 0;
	private int id = 0;// 判断是否选中

	private RadioButton tempRadioButton;// 模版用于保存上次点击的对象

	private int state[] = { R.drawable.ic_check_nor, R.drawable.ic_check_sel };

	/***
	 * 改变图片
	 */
	public void chageImage() {
		index++;
		id = index % 2;// 获取图片id
		imageView.setImageResource(state[id]);
	}
	/**
	 * 取消所有勾选
	 */
	public void cancleImage() {
		imageView.setImageResource(R.drawable.ic_check_nor);
	}
	
	/***
	 * 设置文本
	 * 
	 * @param text
	 */
	public void setText(String text) {
		textView.setText(text);
	}

	public String getText() {
		return /*id == 0 ? "" : */textView.getText().toString();

	}

	public MyRadioButtonInListView(Context context) {
		this(context, null);

	}

	public MyRadioButtonInListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.choice_hot_location_item, this, true);
//		imageView = (ImageView) findViewById(R.id.ivSelectHotLoction);
		textView = (TextView) findViewById(R.id.tvHotLocation);
	}
}
