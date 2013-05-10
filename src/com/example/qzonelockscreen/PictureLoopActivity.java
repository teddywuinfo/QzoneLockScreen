package com.example.qzonelockscreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qzone.io.AsyncImageLoader;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

public class PictureLoopActivity extends Activity {
    private Activity mActivity = null;  
    private ViewFlipper viewFlipper = null;  
    private KeyguardManager.KeyguardLock mKeyguardLock;
    private GestureDetector gestureDetector;
    private String uin;
    private String skey;
    private String currPhotoKey;
    private List<String> arr = new ArrayList<String>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
        setContentView(R.layout.activity_picture_loop);
        Intent intent = this.getIntent();
        
        SharedPreferences userInfo = getSharedPreferences("user_info", 0); 
		skey = userInfo.getString("skey", "");
		uin = userInfo.getString("uin", "");
		
        
        viewFlipper = (ViewFlipper)findViewById(R.id.viewflipper);
        
        mActivity = this;
        getUserPhoto();
        class MyGestureListener implements  OnGestureListener {

			@Override
			public boolean onDown(MotionEvent arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				Log.d("mmm1", e1.getX() + "");
				Log.d("mmm2", e2.getX() + "");
				
				if (e2.getX() - e1.getX() > 10) {            // �������һ���������ҳ���
		            Animation rInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_right_in);  // ���һ���������Ľ���Ч��alpha  0.1 -> 1.0��  
		            Animation rOutAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_right_out); // ���һ����Ҳ໬���Ľ���Ч��alpha 1.0  -> 0.1��  
		  
		            viewFlipper.setInAnimation(rInAnim);  
		            viewFlipper.setOutAnimation(rOutAnim);  
		            viewFlipper.showPrevious();
		        } else if (e2.getX() - e1.getX() < -10) {        // �������󻬶����ҽ������  
		            Animation lInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_in);       // ���󻬶�������Ľ���Ч��alpha 0.1  -> 1.0��  
		            Animation lOutAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_out);     // ���󻬶��Ҳ໬���Ľ���Ч��alpha 1.0  -> 0.1��  
		            viewFlipper.setInAnimation(lInAnim);  
		            viewFlipper.setOutAnimation(lOutAnim);  
		            viewFlipper.showNext();
		            
		        }  
				ImageView img = (ImageView) viewFlipper.getCurrentView();
				int index = img.getId() - 10000;
				String url = arr.get(index);
				String[] parts = url.split("\\/");
				String _k = parts[4] + "/" + parts[5];
				currPhotoKey = "http://user.qzone.qq.com/" + uin + "/photo/" + _k;
				String rid = arr.get(index);
	            showImage(img, rid);
//				
		        return true;  
			}

			@Override
			public void onLongPress(MotionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean onScroll(MotionEvent arg0, MotionEvent arg1,
					float arg2, float arg3) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onShowPress(MotionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean onSingleTapUp(MotionEvent arg0) {
				// TODO Auto-generated method stub
				return false;
			}
        
        }
        gestureDetector = new GestureDetector(new MyGestureListener()); 
        
        
        Button btn = (Button)findViewById(R.id.btnLove);
        btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);  
//				WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");  
//				mWakelock.acquire();  
//				mWakelock.release();
//	
//				PictureLoopActivity.this.finish();
				
				/*
				POST http://w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk=1633468507 HTTP/1.1
					Host: w.qzone.qq.com
					Connection: keep-alive
					Content-Length: 650
					Cache-Control: max-age=0
					Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*\/*;q=0.8
					Origin: http://user.qzone.qq.com
					User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31
					Content-Type: application/x-www-form-urlencoded
					Accept-Encoding: gzip,deflate,sdch
					Accept-Language: en-US,en;q=0.8
					Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
					Cookie: pvid=2654090050; __unam=7178160-13db0269e30-42f303ca-2; randomSeed=423809; __Q_w_s_hat_seed=1; __Q_w_s__QZN_TodoMsgCnt=1; hot_feeds_key=00865610516e2eb4; luin=o0089502611; lskey=0001000098bd6d49f33c2c798c4f23711499c1b8fe5ce2d7b0e15f12f428f2ff4bc9f893e2e2a2124e83e081; ptui_loginuin=519059754; __Q_w_s_wgt_sd=1; pt2gguin=o0089502611; uin=o0089502611; skey=@mUxp0qttc; qz_gdt=dkuzn7JORMsp7m5FyHlJqywJkvibSHc1bAG_EEzdsDIv2!VSouLLeY9Zn4tfcX_LENfEwiHuEpg; __Q_w_s__appDataSeed=3; showMsgBubble=yes; gdtlife=7099633,2|6086472,2|9371376,1|9082161,1|8203285,1|9360643,1|; Loading=Yes; qzspeedup=sdch; qqmusic_uin=34567890; qqmusic_key=34567890; qqmusic_fromtag=6; ptisp=ctc; pgv_pvid=2654090050; o_cookie=89502611; pgv_info=ssid=s7732751070

					qzreferrer=http%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Finfocenter%23%21app%3D4%26via%3DQZ.HashRefresh
					&opuin=89502611
					&unikey=http%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Fphoto%2Fe62c5c9d-a534-46c9-94bb-b09ffa654571%2FNDJ0k7NVBdi7H09kB0QKY6raNml7AAA%21%5E%7C%7C%5Ehttp%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Fbatchphoto%2Fe62c5c9d-a534-46c9-94bb-b09ffa654571%2F0%5E%7C%7C%5E1
					&curkey=http%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Fphoto%2Fe62c5c9d-a534-46c9-94bb-b09ffa654571%2FNDJ0k7NVBdi7H09kB0QKY6raNml7AAA%21%5E%7C%7C%5Ehttp%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Fbatchphoto%2Fe62c5c9d-a534-46c9-94bb-b09ffa654571%2F0%5E%7C%7C%5E1
					&from=2
					&fupdate=1
					&face=0
					
				*/	
				new Thread(new Runnable() {

					@Override
					public void run() {
						Looper.prepare();
						
						HttpPost req = new HttpPost("http://w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk=" + getACSRFToken(skey));
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2); 
						nameValuePairs.add(new BasicNameValuePair("qzreferrer", "http%3A%2F%2Fuser.qzone.qq.com%2F89502611%2Finfocenter%23%21app%3D4%26via%3DQZ.HashRefresh"));
						nameValuePairs.add(new BasicNameValuePair("opuin", uin));
						nameValuePairs.add(new BasicNameValuePair("unikey", currPhotoKey));
						nameValuePairs.add(new BasicNameValuePair("curkey", currPhotoKey));
						nameValuePairs.add(new BasicNameValuePair("from", "1"));
						nameValuePairs.add(new BasicNameValuePair("fupdate", "1"));
						nameValuePairs.add(new BasicNameValuePair("face", "0"));
						req.addHeader("Host", "w.qzone.qq.com");
						req.addHeader("Referer", "http://user.qzone.qq.com/" + uin);
						req.addHeader("Cookie", "pt2gguin=o00" + uin + "; randomSeed=181567; pgv_pvid=; __Q_w_s__QZN_TodoMsgCnt=1; __Q_w_s_hat_seed=1; pgv_info=ssid=s8917362880; uin=o00" + uin + "; skey=" + skey + "; ptisp=ctc; Loading=Yes; gdtlife=9175760,1|9370229,1|; qq_photo_key=64da037f2bfe03c8d805499406b3fa65");
						req.addHeader("Host", "app.photo.qq.com");
						req.addHeader("Host", "app.photo.qq.com");
						req.addHeader("Host", "app.photo.qq.com");
						req.addHeader("Host", "app.photo.qq.com");
						
						try {
							req.setEntity(new UrlEncodedFormEntity(nameValuePairs)); 
		//					HttpGet req = new HttpGet(sb.toString());
							DefaultHttpClient client = new DefaultHttpClient ();
					
							// TODO Auto-generated method stub
							HttpResponse res = client.execute(req);
							
//							String content =new String(EntityUtils.toByteArray(res.getEntity()),"UTF-8");
							if (res.getStatusLine().getStatusCode() == 200) {
								String line = new String(EntityUtils.toByteArray(res.getEntity()),"UTF-8");
								if (line.indexOf("\"message\":\"succ\"") > -1) {
									Message msg = new Message();
									Bundle b = new Bundle();
									b.putString("code", "0");
									msg.setData(b);
									doLikeHanlder.sendMessage(msg);
									
								}
							}
					
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch(Exception e) {
							Log.d("e", e.toString());
						}
					}
					
				}).start();
			}
		});
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return false;
	}
	
	private void showImage(final ImageView img, String rid) {
		AsyncImageLoader asynImageLoader = new AsyncImageLoader(PictureLoopActivity.this);
//		asynImageLoader.setCallbackHanlder(new Handler() {
//			@Override
//	        public void handleMessage(Message msg) {
//				Bundle b = msg.getData();
//	        	String loaded = b.getString("loaded");
//	        	if (loaded.equals("1")) {
//	        		//
//	        		
//	        	}
//			}
//		});
		asynImageLoader.showImageAsyn(img, rid, 0);  
//		
	}
	
	class PhotoListHandler extends Handler {
		public PhotoListHandler() {
        }

        public PhotoListHandler(Looper L) {
            super(L);
        }
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
        	String line = b.getString("content");
			JSONObject json;
			try {
				json = new JSONObject(line);
				JSONObject data = json.getJSONObject("data");
				if (data != null) {
					JSONArray photolist = data.getJSONArray("photos");
					for (int i = 0; i < photolist.length(); i++) {
						JSONObject photo = photolist.getJSONObject(i);
						JSONObject large_image = photo.getJSONObject("large_image");
						arr.add(large_image.getString("url"));
					}
					int sz = arr.size();
			        for (int i = 0; i < sz; i++) {          // ���ͼƬԴ  
			            ImageView iv = new ImageView(PictureLoopActivity.this);
			            iv.setId(i + 10000);
			            viewFlipper.addView(iv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
			        }  
			        if (sz > 0) {
			        	ImageView im = (ImageView) findViewById(10000);
			        	if (im != null) {
			        		int index = 0;
			        		String url = arr.get(index);
							String[] parts = url.split("\\/");
							String _k = parts[4] + "/" + parts[5];
							currPhotoKey = "http://user.qzone.qq.com/" + uin + "/photo/" + _k;
			        		String sid = arr.get(0);
			        		showImage(im, sid);
			        	}
			        }
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e) {
				Log.d("json", e.toString());
			}
		}
	}
	
	class DoLikeHandler extends Handler {
		public DoLikeHandler() {
        }

        public DoLikeHandler(Looper L) {
            super(L);
        }
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String code = b.getString("code");
			if (code.equals("0")) {
				Toast.makeText(getApplicationContext(), "“赞”成功！",Toast.LENGTH_SHORT).show();
				Animation lInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_in);       // ���󻬶�������Ľ���Ч��alpha 0.1  -> 1.0��  
	            Animation lOutAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_out);     // ���󻬶��Ҳ໬���Ľ���Ч��alpha 1.0  -> 0.1��  
	            viewFlipper.setInAnimation(lInAnim);  
	            viewFlipper.setOutAnimation(lOutAnim);  
	            viewFlipper.showNext();
				ImageView img = (ImageView) viewFlipper.getCurrentView();
				int index= img.getId() - 10000;
				String url = arr.get(index);
				String[] parts = url.split("\\/");
				String _k = parts[4] + "/" + parts[5];
				currPhotoKey = "http://user.qzone.qq.com/" + uin + "/photo/" + _k;
				String rid = arr.get(index);
	            showImage(img, rid);
			}
		}
	}
	private PhotoListHandler photoListHanlder = new PhotoListHandler();
	private DoLikeHandler doLikeHanlder = new DoLikeHandler();
	
	private void getUserPhoto() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				Log.d("skey", skey);
				Log.d("uin", uin);
				StringBuilder sb = new StringBuilder();
				sb.append("http://app.photo.qq.com/cgi-bin/app/fcg_list_friend_photo");
				sb.append("?");
				sb.append("inCharset=gbk&");
				sb.append("outCharset=gbk&");
				sb.append("hostUin=" + uin + "&");
				sb.append("notice=0&");
				sb.append("callbackFun=&");
				sb.append("format=json&");
				sb.append("plat=qzone&");
				sb.append("source=qzone&");
				sb.append("start=0&");
				sb.append("cache=0&");
				sb.append("appid=4&");
				sb.append("uin=" + uin + "&");
				sb.append("refer=qzone&");
				sb.append("json_esc=1&");
				sb.append("num=16&");
				sb.append("userOffset=0&");
				sb.append("picoffset=0&");
				
				sb.append("r=" + Math.random() + "&");
				sb.append("g_tk=" + getACSRFToken(skey));
				
				// TODO Auto-generated method stub
				HttpGet req = new HttpGet(sb.toString());
				req.addHeader("Host", "app.photo.qq.com");
				req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:20.0) Gecko/20100101 Firefox/20.0");
				req.addHeader("Accept", "*/*");
				req.addHeader("Accept-Language", "en-US,en;q=0.5");
				req.addHeader("Referer", "http://user.qzone.qq.com/" + uin);
				req.addHeader("Cookie", "pt2gguin=o00" + uin + "; pgv_pvid=6631541939; uin=o00" + uin + "; skey=" + skey + "; ptisp=ctc; pgv_info=ssid=s5085373040");
				req.addHeader("Accept-Language", "en-US,en;q=0.5");
				DefaultHttpClient client = new DefaultHttpClient ();
				String line = "";
				try {
					HttpResponse res = client.execute(req);
					if (res.getStatusLine().getStatusCode() == 200) {
						line = new String(EntityUtils.toByteArray(res.getEntity()),"GB2312");
						Message msg = new Message();
						Bundle b = new Bundle();// 存放数据
						b.putString("content", line);
			            msg.setData(b);
			            photoListHanlder.sendMessage(msg); 
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(Exception e) {
					Log.d("eee", e.toString());
				}
			}
		}).start();
	}
	
	private int getACSRFToken(String str) {
		int hash = 5381;

		for(int i = 0, len = str.length(); i < len; ++i){
			hash += (hash << 5) + (int)str.charAt(i);
		}

		return hash & 0x7fffffff;
	}
}
