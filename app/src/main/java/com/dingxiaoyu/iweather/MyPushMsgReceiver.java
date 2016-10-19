package com.dingxiaoyu.iweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RadioButton;

import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;


public class MyPushMsgReceiver extends MzPushMessageReceiver {
    public MyPushMsgReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(TAG, "iweather onReceive ");
        super.onReceive(context, intent);
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onRegister(Context context, String pushid) {
        //应用在接受返回的pushid
        Log.i(TAG, "iweather onRegister " + pushid);
    }

    @Override
    public void onMessage(Context context, String s) {
        //接收服务器推送的消息
        Log.i(TAG, "iweather onMessage " + s);
    }

    @Override
    public void onUnRegister(Context context, boolean b) {
        //调用PushManager.unRegister(context）方法后，会在此回调反注册状态
        Log.i(TAG, "iweather onUnRegister ");
    }

    //设置通知栏小图标
//    @Override
//    public PushNotificationBuilder onUpdateNotificationBuilder(PushNotificationBuilder pushNotificationBuilder) {
//        pushNotificationBuilder.setmStatusbarIcon(R.drawable.icon);
//    }

    @Override
    public void onPushStatus(Context context,PushSwitchStatus pushSwitchStatus) {
        //检查通知栏和透传消息开关状态回调
        Log.i(TAG, "iweather onPushStatus ");
    }

    @Override
    public void onRegisterStatus(Context context,RegisterStatus registerStatus) {
        Log.i(TAG, "iweather onRegisterStatus " + registerStatus);
        //新版订阅回调
    }

    @Override
    public void onUnRegisterStatus(Context context,UnRegisterStatus unRegisterStatus) {
        Log.i(TAG,"iweather onUnRegisterStatus "+unRegisterStatus);
        //新版反订阅回调
    }

    @Override
    public void onSubTagsStatus(Context context,SubTagsStatus subTagsStatus) {
        Log.i(TAG, "iweather onSubTagsStatus " + subTagsStatus);
        //标签回调
    }

    @Override
    public void onSubAliasStatus(Context context,SubAliasStatus subAliasStatus) {
        Log.i(TAG, "iweather onSubAliasStatus " + subAliasStatus);
        //别名回调
    }
}
