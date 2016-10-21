package com.dingxiaoyu.iweather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.meizu.cloud.pushsdk.PushManager;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatCustomLogger;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;

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
        initMTAConfig(true);

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


    /**
     * 根据不同的模式，建议设置的开关状态，可根据实际情况调整，仅供参考。
     *
     * @param isDebugMode
     *            根据调试或发布条件，配置对应的MTA配置
     */
    private void initMTAConfig(boolean isDebugMode) {
        if (isDebugMode) { // 调试时建议设置的开关状态
            // 查看MTA日志及上报数据内容
            StatConfig.setDebugEnable(true);
            // 禁用MTA对app未处理异常的捕获，方便开发者调试时，及时获知详细错误信息。
            StatConfig.setAutoExceptionCaught(false);
            // StatConfig.setEnableSmartReporting(false);
            // Thread.setDefaultUncaughtExceptionHandler(new
            // UncaughtExceptionHandler() {
            //
            // @Override
            // public void uncaughtException(Thread thread, Throwable ex) {
            // logger.error("setDefaultUncaughtExceptionHandler");
            // }
            // });
            // 调试时，使用实时发送
            StatConfig.setStatSendStrategy(StatReportStrategy.BATCH);
            // // 是否按顺序上报
            // StatConfig.setReportEventsByOrder(false);
            // // 缓存在内存的buffer日志数量,达到这个数量时会被写入db
            // StatConfig.setNumEventsCachedInMemory(30);
            // // 缓存在内存的buffer定期写入的周期
            // StatConfig.setFlushDBSpaceMS(10 * 1000);
            // // 如果用户退出后台，记得调用以下接口，将buffer写入db
            // StatService.flushDataToDB(getApplicationContext());

            // StatConfig.setEnableSmartReporting(false);
            // StatConfig.setSendPeriodMinutes(1);
            // StatConfig.setStatSendStrategy(StatReportStrategy.PERIOD);
        } else { // 发布时，建议设置的开关状态，请确保以下开关是否设置合理
            // 打开debug开关，可查看mta上报日志或错误
            // 发布时，请务必要删除本行或设为false
            StatConfig.setDebugEnable(false);
            // 根据情况，决定是否开启MTA对app未处理异常的捕获
            StatConfig.setAutoExceptionCaught(true);
            // 选择默认的上报策略
            StatConfig.setStatSendStrategy(StatReportStrategy.INSTANT);
        }

        // registerActivity(this);

        // 获取MTA MID等信息
        // 用户自定义UserId
        // StatConfig.setCustomUserId(this, "1234");
        //initActivity();
        //MidService.enableDebug(true);
        //
        // StatSpecifyReportedInfo mtaWxReportInfo = new
        // StatSpecifyReportedInfo();
        // mtaWxReportInfo.setAppKey("Aqc222222");
        // Context context = this.getApplicationContext();
        // try {
        // StatServiceImpl.startStatService(context, null,
        // StatConstants.VERSION, mtaWxReportInfo);
        // } catch (MtaSDkException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // // 授权登陆前
        // long startAuthSec = System.currentTimeMillis();
        // // 授权登陆过程。。。
        // // .....
        // // 登陆结束，计算登陆时间和上报
        // // MTA计算以秒为单位，所以需要转换一下
        // int authSec = (int) ((System.currentTimeMillis() - startAuthSec) /
        // 1000);
        // // 终端最好纠正下不合理的时候，以免上报异常数据
        // if (authSec > 20000) {
        // authSec = 1;
        // }
        // // 事件命名为"auth"
        // String eventId = "auth";
        // int authType = 1; // 举例说明：1:同意授权；2：拒绝授权；3：放弃授权
        // Properties eventProp = new Properties();
        // // 设置登陆结果：同意、拒绝、放弃等
        // eventProp.setProperty("type", "" + authType);
        // // StatServiceImpl.trackCustomEndKVEvent(context, eventId, eventProp,
        // // mtaWxReportInfo);
        // // 上报自定义时长事件
        // StatServiceImpl.trackCustomKVTimeIntervalEvent(context, eventId,
        // eventProp, authSec, mtaWxReportInfo);
        //
        // // androidManifest.xml指定本activity最先启动，因此，MTA的初始化工作需要在onCreate中进行
        // // 为了使得MTA配置及时生效，请确保MTA配置在调用StatService之前已被调用。
        // // 推荐是在Activity.onCreate处初始化MTA设置
        // // 根据不同的模式：调试或发布，初始化MTA设置
        // initMTAConfig(true);

        StatCustomLogger clogger = new StatCustomLogger() {
             private static final String TAG = "CustomLogger";

             @Override
             public void debug(Object msg) {
             if (msg != null) {
             Log.d(TAG, msg.toString());
             }
             }

             @Override
             public void error(Object msg) {
             if (msg != null) {
             Log.e(TAG, msg.toString());
             }
             }

             @Override
             public void error(Exception e) {
             if (e != null) {
             Log.e(TAG, "", e);
             }
             }

             @Override
             public void info(Object msg) {
             if (msg != null) {
             Log.i(TAG, msg.toString());
             }
             }

             @Override
             public void verbose(Object msg) {
             if (msg != null) {
             Log.v(TAG, msg.toString());
             }
             }

             @Override
             public void warn(Object msg) {
             if (msg != null) {
             Log.w(TAG, msg.toString());
             }
             }
        };
        StatConfig.setCustomLogger(clogger);
        /**
         * 调用MTA一般需要3步： 1：配置manifest.xml权限 2：调用StatConfig相关的配置接口配置MTA
         * 3:调用StatService相关的接口，开始统计！
         */

        // StatCommonHelper.getLogger().setLogLevel(Log.VERBOSE);
        // 初始化并启动MTA
        // 第三方SDK必须按以下代码初始化MTA，其中appkey为规定的格式!!!
        // 其它普通的app可自行选择是否调用
         // 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
         // 用于MTA SDK版本冲突检测
         try {
            StatService.startStatService(this, null, StatConstants.VERSION);
         } catch (MtaSDkException e) {
             Log.e(TAG,"MtaSDkException : " + e.getMessage());
            e.printStackTrace();
         }

        StatService.trackCustomEvent(this, "onLaunch");

        // // 获取在线参数
        // String onlineValue = StatConfig.getCustomProperty("onlineKey");
        // if(onlineValue.equalsIgnoreCase("on")){
        // // do something
        // }else{
        // // do something
        // }
    }

}
