package com.dingxiaoyu.iweather;

import java.text.SimpleDateFormat;
import java.util.Date;




import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;

//AppWidgetProvider 是 BroadcastReceiver 的子类，
//本质是个 广播接收器，它专门用来接收来自 Widget组件的各种请求（用Intent传递过来），
//所以如果让我给他起名的话 我会给他命名为AppWidgetReceiver,每一个Widget都要有一个AppWidgetProvider.
public class WidgetProvider extends AppWidgetProvider {
	
	static String tag_action = "Widget.Button.Click";
	private static ImageView weatherIcon;
	private static Time time = new Time();
	
	//如果Widget自动更新时间到了、或者其他会导致Widget发生变化的事件发生，或者说Intent的值是android.appwidget.action.APPWIDGET_UPDATE，那么会调用onUpdate，下面三个方法类似
	 @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		 Log.i("XML","onUpdate");
		 
	        
		// final int N = appWidgetIds.length;  
	    // for (int i=0; i<N; i++) {  
	        // int appWidgetId = appWidgetIds[i];  
	        // updateAppWidget(context, appWidgetManager, appWidgetId);  
	    // } 
	     
	     	//这样在第一次运行时也能响应用户的单击事件
			getWeatherView(context);
			updateAppWidget(getWeatherView(context),context,appWidgetManager);
			
		 super.onUpdate(context, appWidgetManager, appWidgetIds);
		 
		//Intent intent = new Intent(context, MyService.class);
		//context.startService(intent);
		//启动一个自定义更新widget的后台服务
		context.startService(new Intent(context,WidgetProviderServer.class));
	}
	
	public static void updateAppWidget(RemoteViews views, Context context, 
			AppWidgetManager appWidgetManager) {
		Log.i("widget", "开始被调用更新天气页面");
		SharedPreferences shared = context.getSharedPreferences("weather", context.MODE_PRIVATE);
		long currentTime = System.currentTimeMillis();
		//得到天气缓冲文件中的有效期
		long vaildTime = shared.getLong("validTime", currentTime);
		//比较天气缓存文件中的有效期，如果超时了，则访问网络更新天气
		
		if(vaildTime <= currentTime)
			CaiyunWeather.updateWeather();
		else
			updateWeather(views, context);
		
		//更新时间
		Date date = new Date();
		SimpleDateFormat foramt = new SimpleDateFormat("HH:mm");
		String timeText = foramt.format(date);
		Log.i("widget update", "time："+currentTime);
		
		
        
		//更新内容
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProvider.class), views);  
		
	}


	/*
	 * 由缓存文件来得到天气信息
	 * 
	 * dingdayu(614422099@qq.com)
	 * 
	 */
	public static void updateWeather(RemoteViews views, Context context) {
		Log.i("widget", "开始更新天气页面");
		SharedPreferences sp = context.getSharedPreferences("weather", context.MODE_PRIVATE);
		
		String info=sp.getString("city", "");
		Log.i("widget city", info);
		views.setTextViewText(R.id.widget_city, info);
		
		info=sp.getString("updateTime", "");
		views.setTextViewText(R.id.widget_data01, info);
		
		info= sp.getString("current_temperature", "");
		views.setTextViewText(R.id.widget_temp, info);
		
		info= sp.getString("weather_0", "");
		views.setTextViewText(R.id.widget_weather, info);
		
		//String currentWeather = sp.getString("weather_0", "晴");
		//int currentWeatherId = updateWeatherImage( currentWeather);
		views.setImageViewResource(R.id.widget_icon, R.drawable.weather_img_fine_day);
		
		//刷新更新时间
		info= sp.getString("up_time", "");
		views.setTextViewText(R.id.widget_data02 , info);
	}
	
	/**
	 * 更新背景图片和天气图标
	 */
	private static int updateWeatherImage(String currentWeather) {
		
		if (currentWeather.contains("转")) {
			currentWeather = currentWeather.substring(0,
					currentWeather.indexOf("转"));
		}
		time.setToNow();
		int curen;
		if (currentWeather.contains("晴")) {
			if (time.hour >= 7 && time.hour < 19) {

				curen = R.drawable.weather_img_fine_day;
			} else {

				curen = R.drawable.weather_img_fine_night;
			}
		} else if (currentWeather.contains("多云")) {
			if (time.hour >= 7 && time.hour < 19) {

				curen = R.drawable.weather_img_cloudy_day;
			} else {

				curen = R.drawable.weather_img_cloudy_night;
			}
		} else if (currentWeather.contains("阴")) {

			curen = R.drawable.weather_img_overcast;
		} else if (currentWeather.contains("雷")) {

			curen = R.drawable.weather_img_thunder_storm;
		} else if (currentWeather.contains("雨")) {

			if (currentWeather.contains("小雨")) {
				curen = R.drawable.weather_img_rain_small;
			} else if (currentWeather.contains("中雨")) {
				curen = R.drawable.weather_img_rain_middle;
			} else if (currentWeather.contains("大雨")) {
				curen = R.drawable.weather_img_rain_big;
			} else if (currentWeather.contains("暴雨")) {
				curen = R.drawable.weather_img_rain_storm;
			} else if (currentWeather.contains("雨夹雪")) {
				curen = R.drawable.weather_img_rain_snow;
			} else if (currentWeather.contains("冻雨")) {
				curen = R.drawable.weather_img_sleet;
			} else {
				curen = R.drawable.weather_img_rain_middle;
			}
		} else if (currentWeather.contains("雪")
				|| currentWeather.contains("冰雹")) {

			if (currentWeather.contains("小雪")) {
				curen = R.drawable.weather_img_snow_small;
			} else if (currentWeather.contains("中雪")) {
				curen = R.drawable.weather_img_snow_middle;
			} else if (currentWeather.contains("大雪")) {
				curen = R.drawable.weather_img_snow_big;
			} else if (currentWeather.contains("暴雪")) {
				curen = R.drawable.weather_img_snow_storm;
			} else if (currentWeather.contains("冰雹")) {
				curen = R.drawable.weather_img_hail;
			} else {
				curen = R.drawable.weather_img_snow_middle;
			}
		} else if (currentWeather.contains("雾")) {

			curen = R.drawable.weather_img_fog;
		} else if (currentWeather.contains("霾")) {

			curen = R.drawable.weather_img_fog;
		} else if (currentWeather.contains("沙尘暴")
				|| currentWeather.contains("浮尘")
				|| currentWeather.contains("扬沙")) {

			curen = R.drawable.weather_img_sand_storm;
		} else {

			curen = R.drawable.weather_img_fine_day;
		}
		return curen;
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,  
            int appWidgetId) {  
    	Log.i("XML","updateAppWidget");  
        
        String date = new SimpleDateFormat("MM/dd hh:mm:ss").format(new Date());
          
        //获取 RemoteViews 对象
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);  
        //设置内容
        views.setTextViewText(R.id.widget_data01, date);
        //更新内容
        appWidgetManager.updateAppWidget(appWidgetId, views);  
    }  
  
	 
	//每个请求都会传递给onReceive方法，该方法根据Intent参数中的action类型来决定自己处理还是分发给下面四个特殊的方法。
	 @Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 Log.i("XML","onReceive");
		super.onReceive(context, intent);
		
		String date = new SimpleDateFormat("MM/dd hh:mm:ss").format(new Date());
		
        //获取 RemoteViews 对象
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);  
        //设置内容
        //views.setTextViewText(R.id.empty_view, date);  
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        updateAppWidget(views,context,appWidgetManager);
		//更新内容
        //appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProvider.class), views);  
	}
	 
	//返回widget中的布局视图对象
	public static RemoteViews getWeatherView(Context context){
		RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		//当击widget的主体来启动MainActivity返回到天气精灵的天气显示界面
		Intent intent = new Intent(context, WidgetProvider.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.weather_rootLayout, pendingIntent);
		
		return views;
	}
		
	 

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		 Log.i("XML","onDeleted");
		super.onDeleted(context, appWidgetIds);
		//关闭后台服务
		context.stopService(new Intent(context,WidgetProviderServer.class));
	}
	
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		 Log.i("XML","onEnabled");
		super.onEnabled(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		 Log.i("XML","onDisabled");
		super.onDisabled(context);
		//关闭后台服务
		context.stopService(new Intent(context,WidgetProviderServer.class));
	}

}
