package com.qzone.io;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class AsyncImageLoader {
	private static final String TAG = "AsynImageLoader";
	// 缓存下载过的图片的Map
	private Map<String, SoftReference<Bitmap>> caches;
	// 任务队列
	private List<Task> taskQueue;
	private boolean isRunning = false;
	private Handler callbackHandler;
	private Activity mActivity;
	public AsyncImageLoader(Activity activity){
		// 初始化变�?
		caches = new HashMap<String, SoftReference<Bitmap>>();
		taskQueue = new ArrayList<AsyncImageLoader.Task>();
		// 启动图片下载线程
		isRunning = true;
		mActivity = activity;
		new Thread(runnable).start();
	}
	
	/**
	 * 
	 * @param imageView �?��延迟加载图片的对�?
	 * @param url 图片的URL地址
	 * @param resId 图片加载过程中显示的图片资源
	 */
	public void showImageAsyn(final ImageView imageView, String url, int resId){
		imageView.setTag(url);
		Bitmap bitmap = loadImageAsyn(url, getImageCallback(imageView, resId));
		
		if(bitmap == null){
			imageView.setImageResource(resId);
		}else{
			imageView.setImageBitmap(bitmap);
		}
	}
	
	public Bitmap loadImageAsyn(String path, ImageCallback callback){
		// 判断缓存中是否已经存在该图片
		if(caches.containsKey(path)){
			// 取出软引�?
			SoftReference<Bitmap> rf = caches.get(path);
			// 通过软引用，获取图片
			Bitmap bitmap = rf.get();
			// 如果该图片已经被释放，则将该path对应的键从Map中移除掉
			if(bitmap == null){
				caches.remove(path);
			}else{
				// 如果图片未被释放，直接返回该图片
				Log.i(TAG, "return image in cache" + path);
				return bitmap;
			}
		}else{
			// 如果缓存中不常在该图片，则创建图片下载任�?
			Task task = new Task();
			task.path = path;
			task.callback = callback;
			Log.i(TAG, "new Task ," + path);
			if(!taskQueue.contains(task)){
				taskQueue.add(task);
				// 唤醒任务下载队列
				synchronized (runnable) {
					runnable.notify();
				}
			}
		}
		
		// 缓存中没有图片则返回null
		return null;
	}
	
	/**
	 * 
	 * @param imageView 
	 * @param resId 图片加载完成前显示的图片资源ID
	 * @return
	 */
	private ImageCallback getImageCallback(final ImageView imageView, final int resId){
		return new ImageCallback() {
			@Override
			public void loadImage(String path, Bitmap bitmap) {
				if(path.equals(imageView.getTag().toString())){
					imageView.setImageBitmap(bitmap);
					
					int w = 0, h = 0;
					try {
					 w = bitmap.getWidth();
					 h = bitmap.getHeight();
					}catch(Exception e) {
						Log.d("e", e.toString());
					}
			        int screenWidth  = mActivity.getWindowManager().getDefaultDisplay().getWidth();       // ��Ļ�?���أ��磺480px��  
			        int screenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
			        if (w <= screenWidth && h <= screenHeight) {
			        	imageView.setScaleType(ImageView.ScaleType.CENTER);
			        }
			        else {
			        	imageView.setScaleType(ImageView.ScaleType.MATRIX);
			        	Matrix matrix = new Matrix();
			        	int x = 0;
						int y = 0;
						int fx = 0, fy = 0;
						int dx = 0, dy = 0;
			        	LayoutParams para;
			            para = imageView.getLayoutParams();

			        	if (w < screenWidth) {
			        		x = (screenWidth - w ) / 2;
			        	}
			        	else {
			        		para.width = w;
			        		fx = w - screenWidth;
			        		dx = (fx / 50) * 1000;
			        	}
			        	if (h < screenHeight) {
			        		y = (screenHeight - h ) / 2;
			        	}
			        	else {
			        		para.height = h;
			        		fy = h - screenHeight;
			        		dy = (fy / 50) * 1000;
			        	}
			        	imageView.setLayoutParams(para);
			        	matrix.postTranslate(x, y);
			        	imageView.setImageMatrix(matrix);
			        	
//					            Animation rInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.view_single_pic);  // ���һ���������Ľ���Ч��alpha  0.1 -> 1.0��  
			          
//					            img.setAnimation(rInAnim);
//			        	final Animation translateAnimation = new TranslateAnimation(0, -fx, 0, -fy);
//			        	translateAnimation.setFillAfter(true);
//			        	translateAnimation.setRepeatCount(-1);
//			        	translateAnimation.setRepeatMode(Animation.REVERSE);
//			        	translateAnimation.setDuration(dx > 0 ? dx : dy);
//			        	translateAnimation.setStartTime(1000);
//			        	imageView.startAnimation(translateAnimation);
//			        	new Thread (new Runnable() {
//
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								try {
//									Thread.sleep(1000);
//									imageView.startAnimation(translateAnimation);
//								} catch (InterruptedException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							}
//			        		
//			        	}).start();
			        }
			        
				}else{
					imageView.setImageResource(resId);
				}
//				ImageLoaded(bitmap);
			}

//			@Override
//			public void ImageLoaded(Bitmap bitmap) {
//				// TODO Auto-generated method stub
//				if (callbackHandler != null) {
//					Message msg = new Message();
//		            Bundle b = new Bundle();// 存放数据
//		            b.putInt("width", bitmap.getWidth());
//		            b.putInt("height", bitmap.getHeight());
//		            b.putString("loaded", "1");
//		            msg.setData(b);
//					callbackHandler.handleMessage(msg);
//				}
//			}
		};
	}
	
	public void setCallbackHanlder(Handler handler) {
		callbackHandler = handler;
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// 子线程中返回的下载完成的任务
			Task task = (Task)msg.obj;
			// 调用callback对象的loadImage方法，并将图片路径和图片回传给adapter
			task.callback.loadImage(task.path, task.bitmap);
		}
		
	};
	
	private Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			while(isRunning){
				// 当队列中还有未处理的任务时，执行下载任务
				while(taskQueue.size() > 0){
					// 获取第一个任务，并将之从任务队列中删�?
					Task task = taskQueue.remove(0);
					// 将下载的图片添加到缓�?
					task.bitmap = PicUtil.getbitmap(task.path);
//					caches.put(task.path, new SoftReference<Bitmap>(task.bitmap));
					if(handler != null){
						// 创建消息对象，并将完成的任务添加到消息对象中
						Message msg = handler.obtainMessage();
						msg.obj = task;
						// 发�?消息回主线程
						handler.sendMessage(msg);
					}
				}
				
				//如果队列为空,则令线程等待
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};
	
	//回调接口
	public interface ImageCallback{
		void loadImage(String path, Bitmap bitmap);
	}
	
	class Task{
		// 下载任务的下载路�?
		String path;
		// 下载的图�?
		Bitmap bitmap;
		// 回调对象
		ImageCallback callback;
		
		@Override
		public boolean equals(Object o) {
			Task task = (Task)o;
			return task.path.equals(path);
		}
	}
}
