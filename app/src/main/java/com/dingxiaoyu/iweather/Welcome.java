package com.dingxiaoyu.iweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);

		Intent intent = new Intent(Welcome.this, Weather.class);
		Welcome.this.startActivity(intent);
		Welcome.this.finish();

		//可异步执行任务
//		Runnable run = new Runnable() {
//
//			@Override
//			public void run() {
//				Intent intent = new Intent(Welcome.this, Weather.class);
//				Welcome.this.startActivity(intent);
//				Welcome.this.finish();
//			}
//		};
//		Handler handler = new Handler();
//		handler.postDelayed(run, 1 * 1000);
	}

}
