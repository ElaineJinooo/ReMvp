package com.remvp.library.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类
 */
public class NetUtil {
	/**
	 * 网络未连接，无网络类型
	 */
	public static final int TYPE_NONE = 0;

	/**
	 * WIFI类型
	 */
	public static final int TYPE_WIFI = 1;

	/**
	 * WAP 2G类型
	 */
	public static final int TYPE_WAP = 2;

	/**
	 * NET 3G类型
	 */
	public static final int TYPE_NET = 3;

	/**
	 * 获取当前活动网络的类型
	 * 
	 * @param context
	 * @return
	 */
	public static int getActiveNetworkType(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return TYPE_NONE;
		}
		int nType = networkInfo.getType();

		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String netInfo = networkInfo.getExtraInfo();
			if (netInfo != null) {
				if (netInfo.toLowerCase().equals("cmnet")) {
					return TYPE_NET;
				} else {
					return TYPE_WAP;
				}
			} else {
				return TYPE_NET;
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			return TYPE_WIFI;
		}
		return TYPE_NONE;

	}

	/**
	 * 判断是否连接了网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnect(Context context) {
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
}
