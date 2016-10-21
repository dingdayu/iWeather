package com.dingxiaoyu.iweather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.meizu.cloud.pushsdk.PushManager;

public class SplashActivity extends Activity {
	
	private final int SPLASH_DISPLAY_LENGHT = 3000; // 延迟六秒  
	boolean isFirstIn = false;
	private static final String TAG = "iWeather";
    private static final int GO_HOME = 1000;
    private static final int GO_GUIDE = 1001;
    private static final String SHAREDPREFERENCES_NAME = "first_pref"; 
    
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.welcome);  
        Log.i(TAG, "onCreate");



        // 注册推送
        String id = "110036";
        String Key = "c3c85e639cf74cbc8fbd3b90e6802d46";
        String Secret = "12d430dc7a94469dac446aaf3ed25130";
        PushManager.register(this, id, Key);
        String pushid = PushManager.getPushId(this);
        Log.i(TAG, "PushManager：register");
        Log.i(TAG, "getPushId：" + pushid);

        //StatService.setDebugOn(true);

        init();
    }
    
    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
    	
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GO_HOME:
                goHome();
                break;
            case GO_GUIDE:
                goGuide();
                break;
            }
            super.handleMessage(msg);
        }
    };
    
    private void init() {
        // 读取SharedPreferences中需要的数据
        // 使用SharedPreferences来记录程序的使用次数
        SharedPreferences preferences = getSharedPreferences(
                SHAREDPREFERENCES_NAME, MODE_PRIVATE);

        // 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirstIn = preferences.getBoolean("isFirstIn", true);
        Log.i(TAG, "init:" + isFirstIn);
        // 判断程序与第几次运行，如果是第一次运行则跳转到引导界面，否则跳转到主界面
        if (!isFirstIn) {
            // 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
        	Log.i(TAG, "init:GO_HOME");
            mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DISPLAY_LENGHT);
        } else {
        	Log.i(TAG, "init:GO_GUIDE");
            mHandler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DISPLAY_LENGHT);
        }

    }
    
    private void goGuide() {
    	Log.i(TAG, "goGuide");
        Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
        Log.i(TAG, "goGuide");
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }
    private void goHome() {
    	Log.i(TAG, "goHome");
        Intent intent = new Intent(SplashActivity.this, Welcome.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面埋点，需要使用Activity的引用，以便代码能够统计到具体页面名
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

}
