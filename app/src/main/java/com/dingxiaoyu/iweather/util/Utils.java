package com.dingxiaoyu.iweather.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author dingxiaoyu
 * 
 */
public class Utils {

	/**
	 * 检查网络连接
	 * 
	 * @param context
	 *            context
	 * @return 是否连接到网络
	 */
	public static boolean checkNetwork(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {

					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 *            context
	 * @return 版本号
	 */
	public static String getVersion(Context context) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info.versionName;
	}

	/**
	 * 设置ListView高度
	 * 
	 * @param listView
	 *            要设置高度的ListView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}


	private static SimpleDateFormat sdf = null;

	/**
	 * 格式化时间
	 * @param l 时间
	 * @param strPattern 格式化文本
     * @return 格式化后时间
     */
	public synchronized static String formatUTC(long l, String strPattern) {
		if (TextUtils.isEmpty(strPattern)) {
			strPattern = "yyyy-MM-dd HH:mm:ss";
		}
		if (sdf == null) {
			try {
				sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
			} catch (Throwable e) {
			}
		} else {
			sdf.applyPattern(strPattern);
		}
		return sdf == null ? "NULL" : sdf.format(l);
	}

	// 获得当前包的sHA1
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            Log.i("Utils", result);
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

	/**
	 * 获取Manifest里面配置的渠道版本
	 */
	public String getManifestValue(Context context,String key) {
		String channel = "";
		try {
			channel = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return channel;
	}

	// 获取传感器列表
	private void getSensorList(Context context) {
		// 获取传感器管理器
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		// 获取全部传感器列表
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

		// 打印每个传感器信息
		StringBuilder strLog = new StringBuilder();
		int iIndex = 1;
		for (Sensor item : sensors) {
			strLog.append(iIndex + ".");
			strLog.append("	Sensor Type - " + item.getType() + "\r\n");
			strLog.append("	Sensor Name - " + item.getName() + "\r\n");
			strLog.append("	Sensor Version - " + item.getVersion() + "\r\n");
			strLog.append("	Sensor Vendor - " + item.getVendor() + "\r\n");
			strLog.append("	Maximum Range - " + item.getMaximumRange() + "\r\n");
			strLog.append("	Minimum Delay - " + item.getMinDelay() + "\r\n");
			strLog.append("	Power - " + item.getPower() + "\r\n");
			strLog.append("	Resolution - " + item.getResolution() + "\r\n");
			strLog.append("\r\n");
			iIndex++;
		}
		System.out.println(strLog.toString());
	}
}
