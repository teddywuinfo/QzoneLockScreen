package com.example.qzonelockscreen;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class LockLayer {
	 private Activity mActivty;
	 private WindowManager mWindowManager;
	 private View mLockView;  
	 private LayoutParams mLockViewLayoutParams; 

	 public LockLayer(Activity act) {
		 mActivty = act;
		 init();    
	 }
	 
	 private void init() {
		 mWindowManager = mActivty.getWindowManager();
		 mLockViewLayoutParams = new LayoutParams();
		 mLockViewLayoutParams.width = LayoutParams.MATCH_PARENT;
		 mLockViewLayoutParams.height = LayoutParams.MATCH_PARENT;
		 mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;   
		 mLockViewLayoutParams.flags = 1280;   	
	 }
	 
	 public void lock() {
		 if (mLockView != null) {
			 mWindowManager.addView(mLockView, mLockViewLayoutParams);
		 }
	 }
	 
	 public void unlock() {
		 if (mWindowManager != null) {
			 mWindowManager.removeView(mLockView);
		 }
	 }
	 
	 public void setLockView(View v) {
		 mLockView = v;
	 }
}
