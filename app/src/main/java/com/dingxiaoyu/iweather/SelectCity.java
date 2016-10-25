package com.dingxiaoyu.iweather;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;

import com.dingxiaoyu.iweather.util.Utils;
import com.dingxiaoyu.iweather.web.CaiyunWeather;
import com.umeng.analytics.MobclickAgent;

public class SelectCity extends Activity {

	private static final String TAG = "iWeather";

	private String[] citys;
	private ImageView back;
	private GridView cityList;
	private Intent intent;
	private EditText inputCity;
	private Button search;
	private ProgressDialog dialog;
	private Builder builder;
	private String city;

	private AMapLocationClient locationClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.select_city);

		// 定位
		//initLocation();

		dialog = new ProgressDialog(SelectCity.this);
		dialog.setMessage("正在定位...");
		dialog.setCanceledOnTouchOutside(false);

		citys = getResources().getStringArray(R.array.citys);
		cityList = (GridView) findViewById(R.id.city_list);
		back = (ImageView) findViewById(R.id.back);
		inputCity = (EditText) findViewById(R.id.input_city);
		search = (Button) findViewById(R.id.search);
		back.setOnClickListener(new ButtonListener());
		search.setOnClickListener(new ButtonListener());
		inputCity.addTextChangedListener(new Watcher());
		cityList.setAdapter(new MyAdapter(SelectCity.this));
		cityList.setOnItemClickListener(new ClickListener());
	}

	/**
	 * 初始化定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation(){

		Log.i(TAG, "SelectCity：AMapLocationClient");
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		//设置定位参数
		locationClient.setLocationOption(getDefaultOption());
		// 设置定位监听
		locationClient.setLocationListener(locationListener);
		locationClient.setApiKey("c4543016b5b84f8f46c4da42a1ac1cd7");

		//启动定位
		locationClient.startLocation();
	}

	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption(){
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		//mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		//mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
		mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		return mOption;
	}

	class MyAdapter extends BaseAdapter {

		private Context mContext;

		private MyAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return citys.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.city_item, null);
				holder = new ViewHolder();
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.city.setText(citys[position]);
			return convertView;
		}

	}

	class ViewHolder {
		TextView city;
	}

	class ClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			city = citys[arg2];
			if ("自动定位".equals(city)) {
				if (Utils.checkNetwork(SelectCity.this) == false) {
					Toast.makeText(SelectCity.this, "网络异常,请检查网络设置",
							Toast.LENGTH_SHORT).show();
					return;
				}
				dialog.show();
				requestLocation();
			} else {
				intent = new Intent();
				intent.putExtra("city", city);
				dialog.dismiss();
				SelectCity.this.setResult(1, intent);
				SelectCity.this.finish();
			}
		}

	}

	/**
	 * 监听编辑框内容，输入内容，显示搜索按键
	 */
	class Watcher implements TextWatcher {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (inputCity.getText().toString().length() == 0) {
				search.setVisibility(View.GONE);
			} else {
				search.setVisibility(View.VISIBLE);
			}
		}
	}

	class ButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back:
				back();
				break;
			case R.id.search:
				city = inputCity.getText().toString();
				intent = new Intent();
				intent.putExtra("city", city);
				SelectCity.this.setResult(1, intent);
				SelectCity.this.finish();
				break;
			default:
				break;
			}
		}

	}

	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			Log.i(TAG, "SelectCity：locationListener");
			if (null != location) {
				//解析定位结果
				if(null == location){
					return;
				}
				StringBuffer sb = new StringBuffer();
				//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if(location.getErrorCode() == 0){
					sb.append("定位成功" + "\n");
					sb.append("定位类型: " + location.getLocationType() + "\n");
					sb.append("经    度    : " + location.getLongitude() + "\n");
					sb.append("纬    度    : " + location.getLatitude() + "\n");
					sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
					sb.append("提供者    : " + location.getProvider() + "\n");

					if (location.getProvider().equalsIgnoreCase(
							android.location.LocationManager.GPS_PROVIDER)) {
						// 以下信息只有提供者是GPS时才会有
						sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
						sb.append("角    度    : " + location.getBearing() + "\n");
						// 获取当前提供定位服务的卫星个数
						sb.append("星    数    : "
								+ location.getSatellites() + "\n");
					} else {
						// 提供者是GPS时是没有以下信息的
						sb.append("国    家    : " + location.getCountry() + "\n");
						sb.append("省            : " + location.getProvince() + "\n");
						sb.append("市            : " + location.getCity() + "\n");
						sb.append("城市编码 : " + location.getCityCode() + "\n");
						sb.append("区            : " + location.getDistrict() + "\n");
						sb.append("区域 码   : " + location.getAdCode() + "\n");
						sb.append("地    址    : " + location.getAddress() + "\n");
						sb.append("兴趣点    : " + location.getPoiName() + "\n");
						//定位完成的时间
						sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
					}
					//Log.i(TAG, sb.toString());

					CaiyunWeather caiyunWeather = new CaiyunWeather();
					caiyunWeather.get(location.getLongitude() + "," + location.getLatitude());

					// 定位成功
					//System.out.println(location.getCity());
					city = formatCity(location.getCity());
					intent = new Intent();
					intent.putExtra("city", city);
					SelectCity.this.setResult(1, intent);
					SelectCity.this.finish();
					dialog.dismiss();
					locationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
				} else {
					//定位失败
					sb.append("定位失败" + "\n");
					sb.append("错误码:" + location.getErrorCode() + "\n");
					sb.append("错误信息:" + location.getErrorInfo() + "\n");
					sb.append("错误描述:" + location.getLocationDetail() + "\n");
				}
				//定位之后的回调时间
				sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
				//return sb.toString();
				Log.i(TAG, sb.toString());
			} else {
				//tvReult.setText("定位失败，loc is null");
				// 定位失败
				builder = new Builder(SelectCity.this);
				builder.setTitle("提示");
				builder.setMessage("自动定位失败。");
				builder.setPositiveButton("重试",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								if (Utils.checkNetwork(SelectCity.this) == false) {
									Toast.makeText(SelectCity.this,
											"网络异常,请检查网络设置", Toast.LENGTH_SHORT)
											.show();
									return;
								}
								SelectCity.this.dialog.show();
								requestLocation();
							}
						});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Toast.makeText(SelectCity.this, "取消" + which, Toast.LENGTH_SHORT).show();
					}
				});
				builder.setCancelable(false);
				builder.show();
			}
		}
	};


	/**
	 * 请求位置信息
	 */
	private void requestLocation() {
		if (locationClient == null) {
			initLocation();
		} else {
			locationClient.startLocation();
		}
	}

	/**
	 * 将位置信息转换为城市
	 *
	 * @param addr
	 *            位置
	 * @return 城市名称
	 */
	private String formatCity(String addr) {
		String city = null;
		if (addr.contains("北京市") && addr.contains("区")) {
			city = addr.substring(addr.indexOf("市") + 1, addr.indexOf("区"));
		} else if (addr.contains("县")) {
			city = addr.substring(addr.indexOf("市") + 1, addr.indexOf("县"));
		} else {
			int start = addr.indexOf("市");
			int end = addr.lastIndexOf("市");
			if (start == end) {
				if (addr.contains("省")) {
					city = addr.substring(addr.indexOf("省") + 1,
							addr.indexOf("市"));
				} else if (addr.contains("市")) {
					city = addr.substring(0, addr.indexOf("市"));
				}
			} else {
				city = addr.substring(addr.indexOf("市") + 1,
						addr.lastIndexOf("市"));
			}
		}
		return city;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * finish Activity前判断是否结束主Activity
	 */
	private void back() {
		intent = getIntent();
		if ("".equals(intent.getStringExtra("city"))) {
			Weather.context.finish();
		}
		SelectCity.this.finish();
		//System.exit(0);
	}


	@Override
	protected void onResume() {
		super.onResume();
		// 页面埋点，需要使用Activity的引用，以便代码能够统计到具体页面名
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
