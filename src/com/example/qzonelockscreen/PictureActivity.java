package com.example.qzonelockscreen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WloginLastLoginInfo;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.util;

import com.example.qzonelockscreen.R;
import com.qzone.login.LoginHelper;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends Activity {
	private EditText uinText;
	private EditText passText;
	private TextView reg;
	private TextView findPswd;
	public static long mAppid = 549000923;
	public static LoginHelper mLoginHelper = null;
	public static void showDialog(Context context, String strMsg) {  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);   
        builder.setTitle("QQ通行证");  
        builder.setMessage(strMsg);  
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                         dialog.dismiss();
                    }       
                });  
         
        builder.show();      
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		Button btnLogin = (Button)findViewById(R.id.btnLogin);
		mLoginHelper = new LoginHelper(getApplicationContext());
        mLoginHelper.SetListener(mListener);
        uinText = (EditText) findViewById(R.id.uin);
        
		passText = (EditText) findViewById(R.id.pass);
		// TODO Auto-generated method stub
		  
		SharedPreferences userInfo = getSharedPreferences("user_info", 0); 
		String uin = userInfo.getString("uin", "");
		if (!uin.equals("")) {
			uinText.setText(uin);
			doLogin();
		}
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if("".equals(uinText.getText().toString().trim())) 
    			{
    				showDialog(PictureActivity.this, "您都还没有输帐号!");  
    				return;
    			}
    			else if("".equals(passText.getText().toString().trim()))
    			{ 
    				showDialog(PictureActivity.this, "不输密码不给登录!");
    				return;
    			}
				doLogin();
			}
		});
	}
	
	private void doLogin() {
		WUserSigInfo sigInfo = new WUserSigInfo();
		WloginLastLoginInfo info = mLoginHelper.GetLastLoginInfo(); 
		if (true/*mLoginHelper.IsNeedLoginWithPasswd(uinText.getText().toString(), mAppid)*/) {
			sigInfo._userPasswdSig = mLoginHelper.GetA1ByAccount(uinText.getText().toString(), mAppid);
			if (info != null && sigInfo._userPasswdSig != null) {
				mLoginHelper.GetStWithPasswd(uinText.getText().toString(), mAppid, "", sigInfo, 0);
				//mLoginHelper.GetOpenKeyWithPasswd(name.getText().toString(), mAppid, "", sigInfo, 0);
			} else { 
				
				int i = mLoginHelper.GetStWithPasswd(uinText.getText().toString(), mAppid, passText.getText().toString(), sigInfo, 0);
				Log.d("i", "" + i);
				//mLoginHelper.GetOpenKeyWithPasswd(name.getText().toString(), mAppid, pswd.getText().toString(), sigInfo, 0);
			}   
				//mLoginHelper.GetLocalSig(name.getText().toString(), mAppid);
			
		}
		else {
			mLoginHelper.GetStWithoutPasswd(uinText.getText().toString(), mAppid, mAppid, sigInfo, 0);
		}
	}

	WtloginListener mListener = new WtloginListener()
	{
		@Override
		public void OnGetStWithPasswd(String userAccount, long dwSrcAppid, int dwMainSigMap, long dwSubDstAppid,  String userPasswd, WUserSigInfo userSigInfo, int ret)
		{
			if(ret == util.S_GET_IMAGE)
			{
				
			}
			else if(ret == util.S_SUCCESS)
			{
				
				final String _sKey = new String(userSigInfo._sKey);;
				final String uin = userAccount;
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Intent intent = new Intent();
						intent.setAction("com.example.qzonelockscreen.LOCKER");
						intent.putExtra("SKEY", _sKey);
						intent.putExtra("UIN", uin);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startService(intent);
					}
					
				}).start();
				Toast.makeText(getApplicationContext(), "您已登录成功，下次锁屏将会展示您的好友照片",Toast.LENGTH_SHORT).show();
				Intent intent2 = new Intent(PictureActivity.this, PictureLoopActivity.class);
				startActivity(intent2);
				PictureActivity.this.finish();
			}
			else if(ret == util.E_NO_RET)
			{
				Toast.makeText(getApplicationContext(), "登录出错，请重新登录",Toast.LENGTH_SHORT).show();
			}  
			else
			{
				
			}
		}
		
		@Override
		public void OnGetStWithoutPasswd(String userAccount, long dwSrcAppid, long dwDstAppid,int dwMainSigMap,long dwSubDstAppid, WUserSigInfo userSigInfo, int ret)
		{ 
		
			if (ret == util.S_SUCCESS) 
			{
				
			}
			else if(ret == util.E_NO_RET)
			{
				
			}
			else 
			{
				
			}			 
		}		
		 
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_picture, menu);
		return true;
	}
	
	private String getHTTPGetContent(String url) {
		String line = "";
		try{
			HttpGet req = new HttpGet(url);
			
			
			HttpResponse res = new DefaultHttpClient().execute(req);
			
			int code = res.getStatusLine().getStatusCode();
			if(code == 200) {
				HttpEntity ent = res.getEntity();
				InputStream reader = ent.getContent();
				BufferedReader bf = new BufferedReader(new InputStreamReader(reader));
				String cl;
				while((cl = bf.readLine()) != null) {
					line += cl + "\n";
				}
				if (line.length() > 0) {
					line = line.substring(0, line.length() - 1);
				}
			}
		}catch(Exception e){
			String es = e.getMessage();
			System.out.print(es);
		}
		return line;
	}
}
