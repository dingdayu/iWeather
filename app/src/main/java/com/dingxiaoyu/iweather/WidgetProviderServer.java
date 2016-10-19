package com.dingxiaoyu.iweather;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProviderServer extends Service {
	
	static String tag_action = "Widget.Button.Click";
	//记录定时管理器
	AlarmManager alarm;
	PendingIntent pintent;

	/*
	 * 服务启动
	 * dingdayu(614422099@qq.com)
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	public void onStart(Intent intent, int startId){
		Log.i("widget Service", "onStart");
		ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
		// 点击按钮时
		
		String riqi = new SimpleDateFormat("yyyy/MM/dd/ E").format(new Date());
		String date = new SimpleDateFormat("HH:mm").format(new Date());
		Log.i("widget Service time",date);
		remoteViews.setTextViewText(R.id.widget_time, date);
		remoteViews.setTextViewText(R.id.widget_data01, riqi);
		
		// 定义一个Intent来发送按钮Action
		Intent prevInten = new Intent();
		prevInten.setAction(tag_action);
		// 用Intent实例化一个PendingIntent
		PendingIntent Pprevintent=PendingIntent.getService(this, 0, 
				prevInten, 0);
		// 给RemoteView上的Button设置按钮事件
		remoteViews.setOnClickPendingIntent(R.id.refresh, Pprevintent);
		
		
		RemoteViews views = WidgetProvider.getWeatherView(this);
		//得到AppWidgetManager widget管理器
		AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(this);
		
		//int[] appids=appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
		

		WidgetProvider.updateAppWidget(views, this, appWidgetManager);
		
		
		
		
		manager.updateAppWidget(thisWidget, remoteViews);
		
		
		
		pintent=PendingIntent.getService(this, 0, intent, 0);
		
		//计时器
		
		//获取当前时间
		Date date2 = new Date();
		long now =date2.getTime();
		
		//设置间隔时间
		int s=60;  //得到秒数
		int unit2 =s*1000;        //将时间精确到秒
		
		alarm=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
		//AlarmManager.RTC_WAKEUP设置服务在系统休眠时同样会运行
		//第三个参数是下一次启动service监听时间
		//alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 1000, pintent);
		//第二个参数是下一次启动service时间
		alarm.set(AlarmManager.RTC_WAKEUP, now+unit2, pintent);
	}
	
	/*
	 * 当widget中通过调用context.stopService方法来指定销毁service时，被调用
	 * dingdayu(614422099@qq.com)
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.i("widget Service","取消服务");
		//取消定时管理
		if(alarm!=null) {
			alarm.cancel(pintent);
			Log.i("widget Service","取消定时器");
		}
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
