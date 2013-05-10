package com.example.qzonelockscreen;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.client.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.AndroidCharacter;
import android.util.Log;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class LockService extends Service {
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("service", "create...");
	}
	
	@Override
	public void onDestroy() {
		
	}
	
	private void doUnLock() {
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);  
		WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");  
		mWakelock.acquire();  
		mWakelock.release();
	}
	
	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String act = arg1.getAction();
			if (act.equals(Intent.ACTION_SCREEN_ON)) {
				doUnLock();
				Intent intent = new Intent(LockService.this, PictureLoopActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.putStringArrayListExtra("ALBUMN", albumns);
//				intent.putExtra("UIN", uin);
//				intent.putExtra("SKEY", skey);
				startActivity(intent);
				
			}
			else if (act.equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d("off", "screen_off");
			}
		}
     };
     
     
	@Override
	public void onStart(Intent intent, int startId) {
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        String skey = intent.getStringExtra("SKEY");
        String uin = intent.getStringExtra("UIN");
        
        SharedPreferences userInfo = getSharedPreferences("user_info", 0);
		Editor ed = userInfo.edit();
		ed.putString("skey", skey);
		ed.putString("uin", uin);
		ed.commit();
        this.registerReceiver(myBroadcastReceiver, filter);
        
	}
	
	private int getACSRFToken(String str) {
		int hash = 5381;

		for(int i = 0, len = str.length(); i < len; ++i){
			hash += (hash << 5) + (int)str.charAt(i);
		}

		return hash & 0x7fffffff;
	}
}
