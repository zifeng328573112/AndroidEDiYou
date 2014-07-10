package com.elephant.ediyou.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.HobbyBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 选择爱好界面
 * 
 * @author syghh
 * 
 */
public class ChoiseHobbyActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button 					btnLeft;
	private Button 					btnRight;
	private TextView 				tvTitle;

	private DataBaseAdapter 		dba;

	private ProgressDialog 			pd;

	private GridView 				gvHasChoisedHobby;// 已选
	private GridView 				gvAllHobby;// 全部

	private List<HobbyBean> 		allHobbyBeansList = new ArrayList<HobbyBean>();// 所有爱好标签
	private List<HobbyBean> 		userHobbyBeansList = new ArrayList<HobbyBean>();// 用户当前的爱好

	private UserHobbyAdapter 		userHobbyAdapter = new UserHobbyAdapter();
	private AllHobbyAdapter 		allHobbyAdapter = new AllHobbyAdapter();

	private String 					hobbyStr;// 返回个人中心传递数据

	private boolean 				isEdit = false;
	private long 					userId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choise_hobby);
		dba = ((CommonApplication) getApplicationContext()).getDbAdapter();
		findView();
		fillData();
		// 获取所有爱好标签
		long userId = SharedPrefUtil.getUserBean(this).getUserId();
		if (NetUtil.checkNet(ChoiseHobbyActivity.this)) {
			new GetUserHobbyListTask(userId).execute();
		} else {
			Toast.makeText(ChoiseHobbyActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft 			= (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight 			= (Button) findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle 			= (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("编辑标签");

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		gvHasChoisedHobby 	= (GridView) this.findViewById(R.id.gvHasChoisedHobby);
		gvAllHobby 			= (GridView) this.findViewById(R.id.gvAllHobby);

	}

	@Override
	public void fillData() {
		allHobbyBeansList = dba.findAllHobbys();
		gvAllHobby.setAdapter(allHobbyAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:
			dba.bantchUserHobbys(userHobbyBeansList);
			finish();
			break;
		}
	}

	/**
	 * 用户的爱好标签
	 * 
	 * @author syghh
	 * 
	 */
	private class UserHobbyAdapter extends BaseAdapter {

		public void add(HobbyBean userHobbyBean) {
			userHobbyBeansList.add(userHobbyBean);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return userHobbyBeansList.size();
		}

		@Override
		public Object getItem(int position) {
			return userHobbyBeansList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(int position) {
			userHobbyBeansList.remove(position);
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HobbyBean userHobbyBean 	= userHobbyBeansList.get(position);
			final int deletePosition 	= position;
			ViewHolder holder 			= null;
			if (convertView == null) {
				holder 					= new ViewHolder();
				convertView 			= getLayoutInflater().inflate(R.layout.choice_hobby_user_item, null);
				holder.tvHobby 			= (TextView) convertView.findViewById(R.id.tvHobby);
				convertView.setTag(holder);
			} else {
				holder 					= (ViewHolder) convertView.getTag();
			}

			holder.tvHobby.setText(userHobbyBean.getName());

			// 点击删除
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					remove(deletePosition);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			private TextView tvHobby;
		}
	}

	/**
	 * 用户的爱好标签
	 * 
	 * @author syghh
	 * 
	 */
	private class AllHobbyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return allHobbyBeansList.size();
		}

		@Override
		public Object getItem(int position) {
			return allHobbyBeansList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(int position) {
			allHobbyBeansList.remove(position);
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final HobbyBean hobbyBean = allHobbyBeansList.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.choice_hobby_all_item, null);
				holder.tvAllHobby = (TextView) convertView.findViewById(R.id.tvAllHobby);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvAllHobby.setText(hobbyBean.getName());

			// 点击添加到user的Hobby
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (userHobbyBeansList.size() < 6 && userHobbyBeansList.size() > 0) {
						if (!userHobbyBeansList.contains(hobbyBean)) {
							userHobbyAdapter.add(hobbyBean);
						} else {
							Toast.makeText(ChoiseHobbyActivity.this, "请选择不同的爱好标签", Toast.LENGTH_SHORT).show();
						}
					} else if (userHobbyBeansList.size() >= 6) {
						Toast.makeText(ChoiseHobbyActivity.this, "最多可以选择6个爱好标签哦！", Toast.LENGTH_SHORT).show();
					} else if (userHobbyBeansList.size() == 0) {
						userHobbyAdapter.add(hobbyBean);
					}
				}
			});
			return convertView;
		}

		private class ViewHolder {
			private TextView tvAllHobby;
		}
	}

	/**
	 * 获取用户的爱好标签
	 * 
	 * @author syghh
	 * 
	 */
	class GetUserHobbyListTask extends AsyncTask<Void, Void, JSONObject> {
		private long userId;

		public GetUserHobbyListTask(long userId) {
			super();
			this.userId = userId;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				obj = new BusinessHelper().getUserHobbyList(userId);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return obj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						JSONObject data = result.getJSONObject("data");
						userHobbyBeansList = HobbyBean.constractList(data.getJSONArray("userHobby"));
						if (userHobbyBeansList != null && userHobbyBeansList.size() > 0) {
							gvHasChoisedHobby.setAdapter(userHobbyAdapter);
							// 将用户爱好插如数据库
							dba.bantchUserHobbys(userHobbyBeansList);
						}
					} else {
						gvHasChoisedHobby.setAdapter(userHobbyAdapter);
						Toast.makeText(ChoiseHobbyActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(ChoiseHobbyActivity.this, "数据错误", Toast.LENGTH_SHORT).show();
				} catch (SystemException e) {
					Toast.makeText(ChoiseHobbyActivity.this, "数据错误", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(ChoiseHobbyActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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
