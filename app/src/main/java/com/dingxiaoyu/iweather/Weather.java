package com.dingxiaoyu.iweather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dingxiaoyu.iweather.util.Utils;
import com.umeng.analytics.MobclickAgent;

import org.weixvn.wae.manager.EngineManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SimpleDateFormat")
public class Weather extends Activity {

	final private String DATE_KEY[] = {"date_0", "date_1", "date_2", "date_3"};
	final private String WEATHER_KEY[] = {"weather_0", "weather_1",
			"weather_2", "weather_3"};
	final private String WIND_KEY[] = {"wind_0", "wind_1", "wind_2", "wind_3"};
	final private String TEMPERATURE_KEY[] = {"temperature_0",
			"temperature_1", "temperature_2", "temperature_3"};
	public static Handler handler;
	public static Weather context;
	private String[] dateArray, weatherArray, windArray, temperatureArray;
	private SharedPreferences sp;
	private LinearLayout weatherBg;
	private LinearLayout titleBarLayout;
	private LinearLayout changeCity;
	private TextView cityText;
	private TextView descriptionText;
	private ImageView share;
	private ImageView about;
	private static ImageView refresh;
	private static ProgressBar refreshing;
	private TextView updateTimeText;
	private ScrollView scrollView;
	private LinearLayout currentWeatherLayout;
	private ImageView weatherIcon;
	private TextView currentTemperatureText;
	private TextView currentWeatherText;
	private TextView temperatureText;
	private TextView windText;
	private TextView dateText;
	private ListView weatherForecastList;
	private Intent intent;
	private Time time;
	private static Runnable run;
	private Builder builder;
	private String currentWeekDay;
	private static String city;
	private static String description;
	private String currentTemperature;
	private int index = 0;
	private long currentTime = System.currentTimeMillis() + (1000 * 60 * 10);

	private String TAG = "Weather";

	// 记步
	private SensorManager mSensorManager;
	private Sensor mStepSensor;
	private Sensor mStepDetector;
	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather);

		EngineManager.getInstance().setContext(this.getApplicationContext())
				.setDB(null);
		weatherBg = (LinearLayout) findViewById(R.id.weather_bg);
		titleBarLayout = (LinearLayout) findViewById(R.id.title_bar_layout);
		changeCity = (LinearLayout) findViewById(R.id.change_city_layout);
		cityText = (TextView) findViewById(R.id.city);
		descriptionText = (TextView) findViewById(R.id.description);
		share = (ImageView) findViewById(R.id.share);
		about = (ImageView) findViewById(R.id.about);
		refresh = (ImageView) findViewById(R.id.refresh);
		refreshing = (ProgressBar) findViewById(R.id.refreshing);
		updateTimeText = (TextView) findViewById(R.id.update_time);
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		currentWeatherLayout = (LinearLayout) findViewById(R.id.current_weather_layout);
		weatherIcon = (ImageView) findViewById(R.id.weather_icon);
		currentTemperatureText = (TextView) findViewById(R.id.current_temperature);
		currentWeatherText = (TextView) findViewById(R.id.current_weather);
		temperatureText = (TextView) findViewById(R.id.temperature);
		windText = (TextView) findViewById(R.id.wind);
		dateText = (TextView) findViewById(R.id.date);
		weatherForecastList = (ListView) findViewById(R.id.weather_forecast_list);
		changeCity.setOnClickListener(new ButtonListener());
		share.setOnClickListener(new ButtonListener());
		about.setOnClickListener(new ButtonListener());
		refresh.setOnClickListener(new ButtonListener());
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/HelveticaNeueLTPro-Lt.ttf");
		currentTemperatureText.setTypeface(face);
		setCurrentWeatherLayoutHight();
		handler = new MyHandler();
		context = this;
		time = new Time();
		run = new Runnable() {

			@Override
			public void run() {
				refreshing(false);
				Toast.makeText(Weather.this, "网络超时,请稍候再试", Toast.LENGTH_SHORT)
						.show();
			}
		};
		sp = getSharedPreferences("weather", Context.MODE_PRIVATE);
		if ("".equals(sp.getString("city", ""))) {
			intent = new Intent();
			intent.setClass(Weather.this, SelectCity.class);
			intent.putExtra("city", "");
			Weather.this.startActivityForResult(intent, 100);
			updateTimeText.setText("— — 更新");
			weatherBg.setBackgroundResource(R.drawable.bg_na);
			scrollView.setVisibility(View.GONE);
		} else {
			initData();
			updateWeatherImage();
			updateWeatherInfo();
		}

		// 记步
		mTextView = (TextView) findViewById(R.id.text_step);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		mSensorManager.registerListener(mSensorEventListener, mStepSensor,
				SensorManager.SENSOR_DELAY_UI);
		int step = getStep();
		Log.i(TAG, "registerListener getStep:" + step);
		mTextView.setText(Integer.toString(step));

		// 通知栏
		initNotificat();
	}

	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		MobclickAgent.onResume(this);
		mSensorManager.registerListener(mSensorEventListener, mStepSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		MobclickAgent.onPause(this);
		mSensorManager.unregisterListener(mSensorEventListener);
	}

	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		private int mStep;

		/**
		 * 传感器精度的改变
		 * @param sensor
		 * @param accuracy
         */
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.i(TAG, "onAccuracyChanged ： accuracy " + accuracy);
		}

		/**
		 * 传感器报告新的值
		 * @param event
         */
		@Override
		public void onSensorChanged(SensorEvent event) {
			Log.i(TAG, "onSensorChanged");
			mStep = getStep();

			if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
				if (event.values[0] == 1.0f) {
					mStep++;
				}
				Log.i(TAG, "TYPE_STEP_DETECTOR mStep " + (int) mStep);
			}
			Log.i(TAG, "timestamp:" + event.timestamp + " accuracy:" + event.accuracy + " sensor:" + event.sensor);

			setStep(mStep);
			mTextView.setText(Integer.toString(getStep()));
			updateNotificat("今日步数:" + Integer.toString(getStep()) + " " + description);
		}
	};

	public void setStep(int step)
	{
		// 使用SharedPreferences来记录程序的使用次数
		SharedPreferences preferences = getSharedPreferences(
				"step", MODE_PRIVATE);

		Editor editor = preferences.edit();
		Date dt = new Date();
		SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");

		String data = matter1.format(dt);

		// 存入数据
		editor.putInt(data, step);
		// 提交修改
		editor.commit();
	}

	public int getStep()
	{
		SharedPreferences preferences = getSharedPreferences(
				"step", MODE_PRIVATE);
		Date dt = new Date();
		SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");

		String data = matter1.format(dt);
		int setp = preferences.getInt(data,0);
		return setp;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1 && !data.getStringExtra("city").equals(city)) {
			city = data.getStringExtra("city");
			cityText.setText(city);
			updateTimeText.setText("— — 更新");
			weatherBg.setBackgroundResource(R.drawable.bg_na);
			scrollView.setVisibility(View.GONE);
			if (Utils.checkNetwork(Weather.this) == false) {
				Toast.makeText(Weather.this, "网络异常,请检查网络设置", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			updateWeather();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * 主线程与更新天气的线程间通讯
	 */
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			refreshing(false);
			switch (msg.what) {
				case 1:
					handler.removeCallbacks(run);
					Bundle bundle = msg.getData();
					dateArray = bundle.getStringArray("date");
					weatherArray = bundle.getStringArray("weather");
					windArray = bundle.getStringArray("wind");
					temperatureArray = bundle.getStringArray("temperature");
					city = bundle.getString("city");
					description = bundle.getString("description");
					currentTemperature = bundle.getString("current_temperature");
					saveData();
					initData();
					updateWeatherImage();
					updateWeatherInfo();
					break;
				case 2:
					builder = new Builder(Weather.this);
					builder.setTitle("提示");
					builder.setMessage("没有查询到[" + city + "]的天气信息。");
					builder.setPositiveButton("重试",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									intent = new Intent();
									intent.setClass(Weather.this, SelectCity.class);
									Weather.this
											.startActivityForResult(intent, 100);
								}
							});
					builder.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									finish();
								}
							});
					builder.setCancelable(false);
					builder.show();
					break;
				case 0:
					Toast.makeText(Weather.this, "更新失败,请稍候再试", Toast.LENGTH_SHORT)
							.show();
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		dateArray = new String[4];
		weatherArray = new String[4];
		windArray = new String[4];
		temperatureArray = new String[4];
		for (int i = 0; i < 4; i++) {
			dateArray[i] = sp.getString(DATE_KEY[i], "");
			weatherArray[i] = sp.getString(WEATHER_KEY[i], "");
			windArray[i] = sp.getString(WIND_KEY[i], "");
			temperatureArray[i] = sp.getString(TEMPERATURE_KEY[i], "");
		}
		city = sp.getString("city", "");
		description = sp.getString("description", "");
		currentTemperature = sp.getString("current_temperature", "");
		time.setToNow();
		switch (time.weekDay) {
			case 0:
				currentWeekDay = "周日";
				break;
			case 1:
				currentWeekDay = "周一";
				break;
			case 2:
				currentWeekDay = "周二";
				break;
			case 3:
				currentWeekDay = "周三";
				break;
			case 4:
				currentWeekDay = "周四";
				break;
			case 5:
				currentWeekDay = "周五";
				break;
			case 6:
				currentWeekDay = "周六";
				break;
			default:
				break;
		}
		for (int i = 0; i < 4; i++) {
			if (dateArray[i].equals(currentWeekDay)) {
				index = i;
			}
		}
	}

	/**
	 * 更新背景图片和天气图标
	 */
	private void updateWeatherImage() {

		updateNotificat("今日步数:" + Integer.toString(getStep()) + " " + description);
		//更新通知
		updateNotificat("今日步数:" + Integer.toString(getStep()) + " " + description);
		scrollView.setVisibility(View.VISIBLE);
		String currentWeather = weatherArray[index];
		if (currentWeather.contains("转")) {
			currentWeather = currentWeather.substring(0,
					currentWeather.indexOf("转"));
		}
		time.setToNow();
		if (currentWeather.contains("晴")) {
			if (time.hour >= 7 && time.hour < 19) {
				weatherBg.setBackgroundResource(R.drawable.bg_fine_day);
				weatherIcon.setImageResource(R.drawable.weather_img_fine_day);
			} else {
				weatherBg.setBackgroundResource(R.drawable.bg_fine_night);
				weatherIcon.setImageResource(R.drawable.weather_img_fine_night);
			}
		} else if (currentWeather.contains("多云")) {
			if (time.hour >= 7 && time.hour < 19) {
				weatherBg.setBackgroundResource(R.drawable.bg_cloudy_day);
				weatherIcon.setImageResource(R.drawable.weather_img_cloudy_day);
			} else {
				weatherBg.setBackgroundResource(R.drawable.bg_cloudy_night);
				weatherIcon
						.setImageResource(R.drawable.weather_img_cloudy_night);
			}
		} else if (currentWeather.contains("阴")) {
			weatherBg.setBackgroundResource(R.drawable.bg_overcast);
			weatherIcon.setImageResource(R.drawable.weather_img_overcast);
		} else if (currentWeather.contains("雷")) {
			weatherBg.setBackgroundResource(R.drawable.bg_thunder_storm);
			weatherIcon.setImageResource(R.drawable.weather_img_thunder_storm);
		} else if (currentWeather.contains("雨")) {
			weatherBg.setBackgroundResource(R.drawable.bg_rain);
			if (currentWeather.contains("小雨")) {
				weatherIcon.setImageResource(R.drawable.weather_img_rain_small);
			} else if (currentWeather.contains("中雨")) {
				weatherIcon
						.setImageResource(R.drawable.weather_img_rain_middle);
			} else if (currentWeather.contains("大雨")) {
				weatherIcon.setImageResource(R.drawable.weather_img_rain_big);
			} else if (currentWeather.contains("暴雨")) {
				weatherIcon.setImageResource(R.drawable.weather_img_rain_storm);
			} else if (currentWeather.contains("雨夹雪")) {
				weatherIcon.setImageResource(R.drawable.weather_img_rain_snow);
			} else if (currentWeather.contains("冻雨")) {
				weatherIcon.setImageResource(R.drawable.weather_img_sleet);
			} else {
				weatherIcon
						.setImageResource(R.drawable.weather_img_rain_middle);
			}
		} else if (currentWeather.contains("雪")
				|| currentWeather.contains("冰雹")) {
			weatherBg.setBackgroundResource(R.drawable.bg_snow);
			if (currentWeather.contains("小雪")) {
				weatherIcon.setImageResource(R.drawable.weather_img_snow_small);
			} else if (currentWeather.contains("中雪")) {
				weatherIcon
						.setImageResource(R.drawable.weather_img_snow_middle);
			} else if (currentWeather.contains("大雪")) {
				weatherIcon.setImageResource(R.drawable.weather_img_snow_big);
			} else if (currentWeather.contains("暴雪")) {
				weatherIcon.setImageResource(R.drawable.weather_img_snow_storm);
			} else if (currentWeather.contains("冰雹")) {
				weatherIcon.setImageResource(R.drawable.weather_img_hail);
			} else {
				weatherIcon
						.setImageResource(R.drawable.weather_img_snow_middle);
			}
		} else if (currentWeather.contains("雾")) {
			weatherBg.setBackgroundResource(R.drawable.bg_fog);
			weatherIcon.setImageResource(R.drawable.weather_img_fog);
		} else if (currentWeather.contains("霾")) {
			weatherBg.setBackgroundResource(R.drawable.bg_haze);
			weatherIcon.setImageResource(R.drawable.weather_img_fog);
		} else if (currentWeather.contains("沙尘暴")
				|| currentWeather.contains("浮尘")
				|| currentWeather.contains("扬沙")) {
			weatherBg.setBackgroundResource(R.drawable.bg_sand_storm);
			weatherIcon.setImageResource(R.drawable.weather_img_sand_storm);
		} else {
			weatherBg.setBackgroundResource(R.drawable.bg_na);
			weatherIcon.setImageResource(R.drawable.weather_img_fine_day);
		}
	}

	/**
	 * 更新界面（天气信息）
	 */
	@SuppressLint("SimpleDateFormat")
	private void updateWeatherInfo() {
		cityText.setText(city);
		descriptionText.setText(description);
		currentTemperatureText.setText(currentTemperature);
		currentWeatherText.setText(weatherArray[index]);
		temperatureText.setText(temperatureArray[index]);
		windText.setText(windArray[index]);
		Time time = new Time();
		time.setToNow();
		String date = new SimpleDateFormat("MM/dd").format(new Date());
		dateText.setText(currentWeekDay + " " + date);
		String updateTime = sp.getString("update_time", "");
		if (Integer.parseInt(updateTime.substring(0, 4)) == time.year
				&& Integer.parseInt(updateTime.substring(5, 7)) == time.month + 1
				&& Integer.parseInt(updateTime.substring(8, 10)) == time.monthDay) {
			updateTime = "今天" + updateTime.substring(updateTime.indexOf(" "));
			updateTimeText.setTextColor(getResources().getColor(R.color.white));
		} else {
			updateTime = updateTime.substring(5).replace("-", "月")
					.replace(" ", "日 ");
			updateTimeText.setTextColor(getResources().getColor(R.color.red));
			// 超过一天没有更新天气，自动帮用户更新
			if (Utils.checkNetwork(this) == true) {
				updateWeather();
			}
		}
		updateTimeText.setText(updateTime + " 更新");
		weatherForecastList.setAdapter(new MyAdapter(this));
		Utils.setListViewHeightBasedOnChildren(weatherForecastList);
	}

	private void initNotificat()
	{
		Intent notificationIntent = new Intent(this, Weather.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("爱天气")//设置通知栏标题
				.setContentText("今日步数：" + getStep()) //设置通知栏显示内容
		.setContentIntent(contentIntent) //设置通知栏点击意图
		//  .setNumber(number) //设置通知集合的数量
			.setTicker("天气已更新") //通知首次出现在通知栏，带上升动画效果的
			.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
			.setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
		//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
			.setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
			.setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
			//Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
			.setSmallIcon(R.drawable.ic_launcher);//设置通知小ICON

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_NO_CLEAR;
		//用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(1, notification);
	}

	private void updateNotificat(String str)
	{
		Intent notificationIntent = new Intent(this, Weather.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("爱天气")//设置通知栏标题
				.setContentText(str) //设置通知栏显示内容
				.setContentIntent(contentIntent) //设置通知栏点击意图
				//  .setNumber(number) //设置通知集合的数量
				.setTicker("天气已更新") //通知首次出现在通知栏，带上升动画效果的
				.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
				.setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
				//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
				.setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
				.setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
				//Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
				.setSmallIcon(R.drawable.ic_launcher);//设置通知小ICON

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_NO_CLEAR;
		//用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(1, notification);
	}

	/**
	 * 设置布局的高度（铺满屏幕）
	 */
	private void setCurrentWeatherLayoutHight() {
		// 通知栏高度
		int statusBarHeight = 0;
		try {
			statusBarHeight = getResources().getDimensionPixelSize(
					Integer.parseInt(Class
							.forName("com.android.internal.R$dimen")
							.getField("status_bar_height")
							.get(Class.forName("com.android.internal.R$dimen")
									.newInstance()).toString()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// 屏幕高度
		@SuppressWarnings("deprecation")
		int displayHeight = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getHeight();
		// title bar LinearLayout高度
		titleBarLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		int titleBarHeight = titleBarLayout.getMeasuredHeight();

		LayoutParams linearParams = (LayoutParams) currentWeatherLayout
				.getLayoutParams();
		linearParams.height = displayHeight - statusBarHeight - titleBarHeight;
		currentWeatherLayout.setLayoutParams(linearParams);
	}

	/**
	 * 更新天气
	 */
	public static void updateWeather() {
		refreshing(true);
		handler.postDelayed(run, 60 * 1000);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				com.dingxiaoyu.iweather.web.CaiyunWeather cai = new com.dingxiaoyu.iweather.web.CaiyunWeather();
				cai.getLocation(city);

			}
		});
		thread.start();
	}

	/**
	 * 保存天气信息
	 */
	private void saveData() {
		String updateTime = new SimpleDateFormat("yyyy-MM-dd")
				.format(new Date());
		Time time = new Time();
		time.setToNow();
		String hour, minute;
		hour = time.hour + "";
		minute = time.minute + "";
		if (hour.length() < 2) {
			hour = "0" + hour;
		}
		if (minute.length() < 2) {
			minute = "0" + minute;
		}
		updateTime = updateTime + " " + hour + ":" + minute;
		String upTime = hour + ":" + minute;
		Editor editor = sp.edit();
		editor.putString("update_time", updateTime);
		//Widget 上面显示的时间
		editor.putString("up_time", upTime);

		for (int i = 0; i < 4; i++) {
			editor.putString(DATE_KEY[i], dateArray[i]);
			editor.putString(WEATHER_KEY[i], weatherArray[i]);
			editor.putString(WIND_KEY[i], windArray[i]);
			editor.putString(TEMPERATURE_KEY[i], temperatureArray[i]);
		}
		editor.putString("city", city);
		editor.putString("description", description);
		editor.putString("current_temperature", currentTemperature);

		//设置过期时间
		editor.putLong("validTime", currentTime);
		editor.commit();
	}

	/**
	 * 刷新时显示进度条
	 *
	 * @param isRefreshing 是否正在刷新
	 */
	private static void refreshing(boolean isRefreshing) {
		if (isRefreshing) {
			refresh.setVisibility(View.GONE);
			refreshing.setVisibility(View.VISIBLE);
		} else {
			refresh.setVisibility(View.VISIBLE);
			refreshing.setVisibility(View.GONE);
		}
	}

	@SuppressLint("InflateParams")
	class MyAdapter extends BaseAdapter {

		private Context mContext;

		private MyAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return getData().size();
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
						R.layout.weather_forecast_item, null);
				holder = new ViewHolder();
				holder.date = (TextView) convertView
						.findViewById(R.id.weather_forecast_date);
				holder.img = (ImageView) convertView
						.findViewById(R.id.weather_forecast_img);
				holder.weather = (TextView) convertView
						.findViewById(R.id.weather_forecast_weather);
				holder.temperature = (TextView) convertView
						.findViewById(R.id.weather_forecast_temperature);
				holder.wind = (TextView) convertView
						.findViewById(R.id.weather_forecast_wind);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Typeface face = Typeface.createFromAsset(getAssets(),
					"fonts/fangzhenglantingxianhe_GBK.ttf");
			holder.date.setText(getData().get(position).get("date").toString());
			holder.img.setImageResource((Integer) getData().get(position).get(
					"img"));
			holder.weather.setText(getData().get(position).get("weather")
					.toString());
			holder.temperature.setText(getData().get(position)
					.get("temperature").toString());
			holder.temperature.setTypeface(face);
			holder.wind.setText(getData().get(position).get("wind").toString());
			return convertView;
		}

	}

	class ViewHolder {
		TextView date;
		ImageView img;
		TextView weather;
		TextView temperature;
		TextView wind;
	}

	/**
	 * 获取天气预报信息
	 *
	 * @return 天气预报list
	 */
	private ArrayList<HashMap<String, Object>> getData() {
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < 4; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (dateArray[i].equals(currentWeekDay)) {
				map.put("date", "今天");
			} else {
				map.put("date", dateArray[i]);
			}
			map.put("img", getWeatherImg(weatherArray[i]));
			map.put("weather", weatherArray[i]);
			map.put("temperature", temperatureArray[i]);
			map.put("wind", windArray[i]);
			list.add(map);
		}
		return list;
	}

	/**
	 * 根据天气信息设置天气图片
	 *
	 * @param weather 天气信息
	 * @return 对应的天气图片id
	 */
	public int getWeatherImg(String weather) {
		int img = 0;
		if (weather.contains("转")) {
			weather = weather.substring(0, weather.indexOf("转"));
		}
		if (weather.contains("晴")) {
			img = R.drawable.weather_icon_fine;
		} else if (weather.contains("多云")) {
			img = R.drawable.weather_icon_cloudy;
		} else if (weather.contains("阴")) {
			img = R.drawable.weather_icon_overcast;
		} else if (weather.contains("雷")) {
			img = R.drawable.weather_icon_thunder_storm;
		} else if (weather.contains("小雨")) {
			img = R.drawable.weather_icon_rain_small;
		} else if (weather.contains("中雨")) {
			img = R.drawable.weather_icon_rain_middle;
		} else if (weather.contains("大雨")) {
			img = R.drawable.weather_icon_rain_big;
		} else if (weather.contains("暴雨")) {
			img = R.drawable.weather_icon_rain_storm;
		} else if (weather.contains("雨夹雪")) {
			img = R.drawable.weather_icon_rain_snow;
		} else if (weather.contains("冻雨")) {
			img = R.drawable.weather_icon_sleet;
		} else if (weather.contains("小雪")) {
			img = R.drawable.weather_icon_snow_small;
		} else if (weather.contains("中雪")) {
			img = R.drawable.weather_icon_snow_middle;
		} else if (weather.contains("大雪")) {
			img = R.drawable.weather_icon_snow_big;
		} else if (weather.contains("暴雪")) {
			img = R.drawable.weather_icon_snow_storm;
		} else if (weather.contains("冰雹")) {
			img = R.drawable.weather_icon_hail;
		} else if (weather.contains("雾") || weather.contains("霾")) {
			img = R.drawable.weather_icon_fog;
		} else if (weather.contains("沙尘暴") || weather.contains("浮尘")
				|| weather.contains("扬沙")) {
			img = R.drawable.weather_icon_sand_storm;
		} else {
			img = R.drawable.weather_icon_fine;
		}
		return img;
	}

	class ButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.change_city_layout:
					intent = new Intent();
					intent.setClass(Weather.this, SelectCity.class);
					startActivityForResult(intent, 100);
					break;
				case R.id.share:

					String updateTime = new SimpleDateFormat("yyyy-MM-dd")
							.format(new Date());
					String shareStr = "[" + updateTime + "] " + description + "\r\n -- 爱天气";

					intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, "好友分享");
					intent.putExtra(Intent.EXTRA_TEXT, shareStr);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(Intent.createChooser(intent, "好友分享"));
					break;
				case R.id.about:
					LayoutInflater inflater = getLayoutInflater();
					View dialogLayout = inflater.inflate(R.layout.weather_dialog, null);
					TextView version = (TextView) dialogLayout
							.findViewById(R.id.version);
					version.setText("V " + Utils.getVersion(Weather.this));
					builder = new Builder(Weather.this);
					builder.setTitle("关于");
					builder.setView(dialogLayout);
					builder.setPositiveButton("确定", null);
					builder.setCancelable(false);
					builder.show();
					break;
				case R.id.refresh:
					if (Utils.checkNetwork(Weather.this) == false) {
						Toast.makeText(Weather.this, "网络异常,请检查网络设置",
								Toast.LENGTH_SHORT).show();
						return;
					}
					updateWeather();
					break;
				default:
					break;
			}
		}

	}

	/**
	 * 菜单、返回键响应
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//exitBy2Click();

			PackageManager pm = getPackageManager();
			ResolveInfo homeInfo =
					pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				ActivityInfo ai = homeInfo.activityInfo;
				Intent startIntent = new Intent(Intent.ACTION_MAIN);
				startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
				startActivitySafely(startIntent);
				return true;
			} else
				return super.onKeyDown(keyCode, event);
		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer timer = null;
		if (isExit == false) {
			isExit = true;
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false;
				}
			}, 2000);
		} else {
			finish();
			System.exit(0);
		}
	}

	private void startActivitySafely(Intent intent) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "null",
					Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, "null",
					Toast.LENGTH_SHORT).show();
		}
	}

}
