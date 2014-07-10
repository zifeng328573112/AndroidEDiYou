package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.view.CalendarGridView;
import com.umeng.analytics.MobclickAgent;

/**
 * 自定义日历，活动开始日期和结束日期的选择；
 * 
 * @author zhoujun
 * 
 */
public class RooChooseEventTimeActivity extends Activity implements IBaseActivity, OnClickListener {

	// 标题；
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private View viewHire;
	private TextView tvPrompt;// 友情提示；
	// 日历控件
	private LinearLayout layoutCalendar;
	private TextView tvMonth;
	private ImageView ivPreMonth, ivNextMonth;
	private static boolean isSelf = true;// 是否是自己；
	private long rooId;
	private Context mContext = RooChooseEventTimeActivity.this;
	private ViewFlipper viewFlipper;
	private static final int TIME_FREE = 0;// 时间空闲
	private static final int TIME_BUSY = 1;// 时间繁忙
	private static final int TIME_HIRED = 2;// 时间已经被雇佣；
	private static final int TIME_HIRING = 3;// 时间将要雇佣；

	// private List<String> busyTimeList = new ArrayList<String>();
	// private List<String> orderTimeList = new ArrayList<String>();
	// 动画
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	AnimationListener animationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// 当动画完成后调用
			CreateGirdView();
		}
	};

	private GridView title_gView;
	private GridView gView1;// 上一个月
	private GridView gView2;// 当前月
	private GridView gView3;// 下一个月

	boolean bIsSelection = false;// 是否是选择事件发生
	private Calendar calStartDate = Calendar.getInstance();// 当前显示的日历
	private Calendar calSelected = Calendar.getInstance(); // 选择的日历
	private Calendar calToday = Calendar.getInstance(); // 今日
	private CalendarGridViewAdapter gAdapter2;
	private CalendarGridViewAdapter gAdapter1;
	private CalendarGridViewAdapter gAdapter3;

	private static final int mainLayoutID = 88; // 设置主布局ID
	private static final int caltitleLayoutID = 66; // title布局ID
	private static final int calLayoutID = 55; // 日历布局ID

	private int iMonthViewCurrentMonth = 0; // 当前视图月
	private int iMonthViewCurrentYear = 0; // 当前视图年
	private int iFirstDayOfWeek = Calendar.MONDAY;
	private int calendarWidth = 0;
	private int calendarHeight = 0;
	private static final String TAG = "RooScheduleActivity";

	private boolean isChooseEndTime;
	private CommonApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_schedule);
		if (getIntent() != null) {
			// isSelf = getIntent().getBooleanExtra("isSelf", false);
			isChooseEndTime = getIntent().getBooleanExtra("isChooseEndTime", false);
			rooId = getIntent().getLongExtra("rooId", 0);
		}
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		findView();
		fillData();
		UpdateStartDateForMonth();
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		viewHire = findViewById(R.id.viewHire);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		// 日历；
		tvMonth = (TextView) findViewById(R.id.tvMonth);
		ivPreMonth = (ImageView) findViewById(R.id.ivPreMonth);
		ivNextMonth = (ImageView) findViewById(R.id.ivNextMonth);
		layoutCalendar = (LinearLayout) findViewById(R.id.layoutCalendar);

		btnLeft.setOnClickListener(this);
		ivPreMonth.setOnClickListener(this);
		ivNextMonth.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		if (!isSelf) {
			tvTitle.setText(R.string.roo_order);
			btnRight.setText("下一步");
		} else {
			tvTitle.setText("日程安排");
			viewHire.setVisibility(View.VISIBLE);
			btnRight.setText("完成");
		}
		if (isChooseEndTime) {
			tvTitle.setText("选择活动结束日期");
		} else {
			tvTitle.setText("选择活动开始日期");
		}
		btnRight.setVisibility(View.INVISIBLE);
		tvPrompt.setText(R.string.event_prompt);

		if (NetUtil.checkNet(this)) {
			new GetScheduleTask(Calendar.getInstance().getTime(), rooId, RooChooseEventTimeActivity.this).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}

		RelativeLayout mainLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params_main = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mainLayout.setLayoutParams(params_main);
		mainLayout.setId(mainLayoutID);
		mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		layoutCalendar.addView(mainLayout);

		viewFlipper = new ViewFlipper(this);
		viewFlipper.setId(calLayoutID);

		calStartDate = getCalendarStartDate();
		// 生成日历头部，星期；
		setTitleGirdView();
		RelativeLayout.LayoutParams params_cal_title = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		params_cal_title.topMargin = 8;
		mainLayout.addView(title_gView, params_cal_title);

		CreateGirdView();

		RelativeLayout.LayoutParams params_cal = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		params_cal.addRule(RelativeLayout.BELOW, caltitleLayoutID);

		mainLayout.addView(viewFlipper, params_cal);
		// 加载动画；
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

		slideLeftIn.setAnimationListener(animationListener);
		// slideLeftOut.setAnimationListener(animationListener);
		slideRightIn.setAnimationListener(animationListener);
		// slideRightOut.setAnimationListener(animationListener);
	}

	private Calendar getCalendarStartDate() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		return calStartDate;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			timeMap.clear();
			break;
		case R.id.btnRight:
			break;
		case R.id.ivPreMonth:
			viewFlipper.setInAnimation(slideRightIn);
			viewFlipper.setOutAnimation(slideRightOut);
			viewFlipper.showPrevious();
			setPrevViewItem();
			if (NetUtil.checkNet(this)) {
				new GetScheduleTask(calStartDate.getTime(), rooId, RooChooseEventTimeActivity.this).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ivNextMonth:
			viewFlipper.setInAnimation(slideLeftIn);
			viewFlipper.setOutAnimation(slideLeftOut);
			viewFlipper.showNext();
			setNextViewItem();
			if (NetUtil.checkNet(this)) {
				new GetScheduleTask(calStartDate.getTime(), rooId, RooChooseEventTimeActivity.this).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 获取时间忙碌雇佣
	 * @author ISP
	 *
	 */
	private class GetScheduleTask extends AsyncTask<Void, Void, JSONObject> {

		private Date date;
		private long id;
		private Context context;

		public GetScheduleTask(Date date, long id, Context context) {
			super();
			this.date = date;
			this.id = id;
			this.context = context;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject scheduleInfo = null;
			try {
				scheduleInfo = new BusinessHelper().getRooSchedule(DateUtil.dateToString("yyyy-MM", date), id);
				Log.d(TAG, "日程安排信息：" + scheduleInfo);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return scheduleInfo;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (Constants.SUCCESS == result.getInt("status")) {
						JSONArray dataArray = result.getJSONArray("data");
						for (int i = 0; i < dataArray.length(); i++) {
							JSONObject obj = dataArray.getJSONObject(i);
							String date = obj.getString("yearMonth") + "-" + obj.getString("day");
							int dayType = obj.getInt("dayType");
							if (dayType == TIME_BUSY) {
								timeMap.put(date, TIME_BUSY);
							} else if (dayType == TIME_HIRED) {
								timeMap.put(date, TIME_HIRED);
							}
						}
						if (timeMap.size() > 0) {
							gAdapter2.notifyDataSetChanged();
						}
					}
				} catch (JSONException e) {
				}
			}
		}
	}

	private void setTitleGirdView() {

		title_gView = setGirdView();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		title_gView.setLayoutParams(params);
		title_gView.setVerticalSpacing(0);// 垂直间隔
		title_gView.setHorizontalSpacing(0);// 水平间隔
		TitleGridAdapter titleAdapter = new TitleGridAdapter(this);
		title_gView.setAdapter(titleAdapter);// 设置菜单Adapter
		title_gView.setId(caltitleLayoutID);
	}

	private void CreateGirdView() {

		Calendar tempSelected1 = Calendar.getInstance(); // 临时
		Calendar tempSelected2 = Calendar.getInstance(); // 临时
		Calendar tempSelected3 = Calendar.getInstance(); // 临时
		tempSelected1.setTime(calStartDate.getTime());
		tempSelected2.setTime(calStartDate.getTime());
		tempSelected3.setTime(calStartDate.getTime());

		gView1 = new CalendarGridView(mContext);
		tempSelected1.add(Calendar.MONTH, -1);
		gAdapter1 = new CalendarGridViewAdapter(this, tempSelected1);
		gView1.setAdapter(gAdapter1);// 设置菜单Adapter
		gView1.setId(calLayoutID);

		gView2 = new CalendarGridView(mContext);
		gAdapter2 = new CalendarGridViewAdapter(this, tempSelected2);
		gView2.setAdapter(gAdapter2);// 设置菜单Adapter
		gView2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ImageView ivOrder = (ImageView) gView2.findViewWithTag(arg2 + 600);
				TextView tvDay = (TextView) gView2.findViewWithTag(arg2 + 500);

				Date myDate = (Date) gAdapter2.getItem(arg2);
				String myDateStr = DateUtil.dateToString("yyyy-MM-dd", myDate);
				if (myDate.before(calToday.getTime())) {
					Toast.makeText(RooChooseEventTimeActivity.this, "请不要选择今天或者今天之前的日期", Toast.LENGTH_SHORT).show();
					return;
				} else if (DateUtil.isNextMonth(myDate, iMonthViewCurrentYear, iMonthViewCurrentMonth) == 1) {// 下一个月；
					viewFlipper.setInAnimation(slideLeftIn);
					viewFlipper.setOutAnimation(slideLeftOut);
					viewFlipper.showNext();
					setNextViewItem();
					if (NetUtil.checkNet(RooChooseEventTimeActivity.this)) {
						new GetScheduleTask(calStartDate.getTime(), rooId, RooChooseEventTimeActivity.this).execute();
					} else {
						Toast.makeText(RooChooseEventTimeActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT)
								.show();
					}
					return;
				} else if (DateUtil.isNextMonth(myDate, iMonthViewCurrentYear, iMonthViewCurrentMonth) == -1) {// 上个月；
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
					viewFlipper.showPrevious();
					setPrevViewItem();
					if (NetUtil.checkNet(RooChooseEventTimeActivity.this)) {
						new GetScheduleTask(calStartDate.getTime(), rooId, RooChooseEventTimeActivity.this).execute();
					} else {
						Toast.makeText(RooChooseEventTimeActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT)
								.show();
					}
					return;
				} else {
					if(null == timeMap.get(myDateStr)){
						Intent intent = getIntent();
						if(isChooseEndTime){
							String startTime = app.getEventStartTime();
							if(!DateUtil.isAfterStartTime(myDateStr, startTime)){
								Toast.makeText(RooChooseEventTimeActivity.this, "请不要选择结束日期在开始日期之前", Toast.LENGTH_SHORT).show();
								return;
							}
							if(DateUtil.isMoreThanThirtyDays(startTime, myDateStr)){
								Toast.makeText(RooChooseEventTimeActivity.this, "活动时间请不要超过30天", Toast.LENGTH_SHORT).show();
								return;
							}
							if(DateUtil.isContainBusyTime(startTime, myDateStr, timeMap)){
								Toast.makeText(RooChooseEventTimeActivity.this, "您选择的日期中包含忙碌时间或者雇佣时间", Toast.LENGTH_SHORT).show();
								return;
							}
							intent.putExtra("endTime", myDateStr);
						}else{
							intent.putExtra("startTime", myDateStr);
							app.setEventStartTime(myDateStr);
						}
						setResult(RESULT_OK, intent);
						finish();
					}else{
						if (TIME_HIRED == timeMap.get(myDateStr) ) {
							Toast.makeText(RooChooseEventTimeActivity.this, "请不要选择被雇佣忙碌时间", Toast.LENGTH_SHORT).show();
							return;
						} else {
							Toast.makeText(RooChooseEventTimeActivity.this, "请不要选择忙碌时间", Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}
			}
		});
		gView2.setId(calLayoutID);

		gView3 = new CalendarGridView(mContext);
		tempSelected3.add(Calendar.MONTH, 1);
		gAdapter3 = new CalendarGridViewAdapter(this, tempSelected3);
		gView3.setAdapter(gAdapter3);// 设置菜单Adapter
		gView3.setId(calLayoutID);

		if (viewFlipper.getChildCount() != 0) {
			viewFlipper.removeAllViews();
		}

		viewFlipper.addView(gView2);
		viewFlipper.addView(gView3);
		viewFlipper.addView(gView1);

		String s = DateUtil.dateToString("yyyy年 MM月", calStartDate.getTime());

		tvMonth.setText(s);
	}

	private GridView setGirdView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		GridView gridView = new GridView(this);
		gridView.setLayoutParams(params);
		gridView.setNumColumns(7);// 设置每行列数
		gridView.setGravity(Gravity.CENTER_VERTICAL);// 位置居中
		gridView.setVerticalSpacing(1);// 垂直间隔
		gridView.setHorizontalSpacing(1);// 水平间隔

		// 设置显示参数
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int i = display.getWidth() / 7;
		int j = display.getWidth() - (i * 7);
		int x = j / 2;
		gridView.setPadding(x, 0, 0, 0);// 居中

		return gridView;
	}

	// 上一个月
	private void setPrevViewItem() {
		iMonthViewCurrentMonth--;// 当前选择月--
		// 如果当前月为负数的话显示上一年
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1); // 设置日为当月1日
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth); // 设置月
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear); // 设置年

		String s = DateUtil.dateToString("yyyy年 MM月", calStartDate.getTime());
		tvMonth.setText(s);

	}

	// 下一个月
	private void setNextViewItem() {
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);

		String s = DateUtil.dateToString("yyyy年 MM月", calStartDate.getTime());
		tvMonth.setText(s);
	}

	/**
	 * 星期的title适配器；
	 * 
	 * @author Zhoujun
	 * 
	 */
	public class TitleGridAdapter extends BaseAdapter {
		// 将titles存入数组
		String[] titles = new String[] { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };

		private Activity activity;

		// construct
		public TitleGridAdapter(Activity a) {
			activity = a;
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Object getItem(int position) {
			return titles[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 设置外观
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout iv = new LinearLayout(activity);
			TextView txtDay = new TextView(activity);
			txtDay.setFocusable(false);
			txtDay.setBackgroundColor(Color.TRANSPARENT);
			iv.setOrientation(1);
			txtDay.setTextColor(getResources().getColor(R.color.calendar_day_font));
			txtDay.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			txtDay.setTextColor(getResources().getColor(R.color.calendar_week_font));

			txtDay.setText(titles[position]);

			iv.addView(txtDay, lp);

			return iv;
		}
	}

	/**
	 * 根据改变的日期更新日历,填充日历控件用
	 */
	private void UpdateStartDateForMonth() {
		// 设置成当月第一天
		calStartDate.set(Calendar.DATE, 1);
		// 得到当前日历显示的月
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		// 得到当前日历显示的年
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);

		String s = DateUtil.dateToString("yyyy年MM 月", calStartDate.getTime());
		tvMonth.setText(s);

		// 星期一是2 星期天是1 填充剩余天数
		int iDay = 0;
		int iFirstDayOfWeek = Calendar.MONDAY;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);

	}

	// private static List<Date> hireList = new ArrayList<Date>();
	private HashMap<String, Integer> timeMap = new HashMap<String, Integer>();

	/**
	 * 日历适配器；
	 * 
	 * @author Zhoujun
	 * 
	 */
	public class CalendarGridViewAdapter extends BaseAdapter {

		private Calendar calStartDate = Calendar.getInstance();// 当前显示的日历

		private Calendar calToday = Calendar.getInstance(); // 今日
		private int iMonthViewCurrentMonth = 0; // 当前视图月
		private int iMonthViewCurrentYear = 0;// 当前视图年；

		ArrayList<java.util.Date> titles;

		private void UpdateStartDateForMonth() {
			calStartDate.set(Calendar.DATE, 1); // 设置成当月第一天
			iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);// 得到当前日历显示的月
			iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);// 得到当前日历显示的年
			// 星期一是2 星期天是1 填充剩余天数
			int iDay = 0;
			int iFirstDayOfWeek = Calendar.MONDAY;
			int iStartDay = iFirstDayOfWeek;
			if (iStartDay == Calendar.MONDAY) {
				iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
				if (iDay < 0)
					iDay = 6;
			}
			if (iStartDay == Calendar.SUNDAY) {
				iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
				if (iDay < 0)
					iDay = 6;
			}
			calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);

			calStartDate.add(Calendar.DAY_OF_MONTH, -1);// 周日第一位

		}

		private ArrayList<java.util.Date> getDates() {

			UpdateStartDateForMonth();

			ArrayList<java.util.Date> alArrayList = new ArrayList<java.util.Date>();
			// 遍历数组
			for (int i = 1; i <= 42; i++) {
				alArrayList.add(calStartDate.getTime());
				calStartDate.add(Calendar.DAY_OF_MONTH, 1);
			}

			return alArrayList;
		}

		private Activity activity;
		Resources resources;

		// construct
		public CalendarGridViewAdapter(Activity a, Calendar cal) {
			calStartDate = cal;
			activity = a;
			resources = activity.getResources();
			titles = getDates();
		}

		public CalendarGridViewAdapter(Activity a) {
			activity = a;
			resources = activity.getResources();
		}

		@Override
		public int getCount() {
			return titles.size();
		}

		@Override
		public Object getItem(int position) {
			return titles.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Date titleDate = DateUtil.stringToDate("yyyy-MM-dd",
			// tvMonth.getText().toString().replace(" ", "")+"12日");
			// if(titleDate != null){
			// iMonthViewCurrentMonth = titleDate.getMonth();
			// }主要在点击下下个月的日期时，不能跳转到下月的的日历；
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.calendar_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvDay = (TextView) convertView.findViewById(R.id.tvDay);
				viewHolder.ivOrder = (ImageView) convertView.findViewById(R.id.ivOrder);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final Date myDate = (Date) getItem(position);
			final String myDateStr = DateUtil.dateToString("yyyy-MM-dd", myDate);
			Calendar calCalendar = Calendar.getInstance();
			calCalendar.setTime(myDate);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_WEEK);

			int day = myDate.getDate();
			calendarWidth = layoutCalendar.getWidth();
			calendarHeight = layoutCalendar.getHeight();
			int i = (calendarWidth - CalendarGridView.GRIDVIEW_SPACING * 6) / 7;

			if (iMonth == iMonthViewCurrentMonth) {
				viewHolder.tvDay.setTextColor(getResources().getColor(R.color.calendar_day_font));
			} else {
				viewHolder.tvDay.setTextColor(getResources().getColor(R.color.calendar_other_month_day_font));
			}
			viewHolder.tvDay.setWidth(i);
			viewHolder.tvDay.setHeight(i);
			String tempDay = String.valueOf(day);
			if (equalsDate(calToday.getTime(), myDate)) {
				viewHolder.tvDay.setText("今天");
				viewHolder.tvDay.setTextSize(12);
				viewHolder.tvDay.setTextColor(Color.BLACK);
			} else {
				viewHolder.tvDay.setText(tempDay);
			}
			if (timeMap.size() > 0 && timeMap.containsKey(myDateStr)) {
				if (timeMap.get(myDateStr) == TIME_HIRED) {
					if (!isSelf) {
						viewHolder.tvDay.setBackgroundResource(R.drawable.bg_calendar_busy_day);
					} else {
						viewHolder.ivOrder.setVisibility(View.VISIBLE);
						viewHolder.tvDay.setTextColor(Color.BLACK);
					}
				} else if (timeMap.get(myDateStr) == TIME_BUSY) {
					viewHolder.tvDay.setBackgroundResource(R.drawable.bg_calendar_busy_day);
				} else if (timeMap.get(myDateStr) == TIME_HIRING) {
					viewHolder.tvDay.setBackgroundResource(R.drawable.bg_calendar_hire_day);
					viewHolder.tvDay.setTextColor(Color.BLACK);
				} else {
					viewHolder.tvDay.setBackgroundResource(R.drawable.bg_calendar_free_day);
				}

			}
			viewHolder.tvDay.setTag(position + 500);
			viewHolder.ivOrder.setTag(position + 600);

			return convertView;
		}

		class ViewHolder {
			private TextView tvDay;
			private ImageView ivOrder;
		}

		// @Override
		// public void notifyDataSetChanged() {
		// super.notifyDataSetChanged();
		// }

		private boolean equalsDate(Date date1, Date date2) {
			if (date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth()
					&& date1.getDate() == date2.getDate()) {
				return true;
			} else {
				return false;
			}

		}

		// @Override
		// public int getItemViewType(int position) {
		// return titles.get(position);
		// }
		//
		// @Override
		// public int getViewTypeCount() {
		// return super.getViewTypeCount();
		// }
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
