package org.yavdr.grafdroid;

import java.io.IOException;
import java.sql.SQLException;

import org.yavdr.grafdroid.core.GrafDroidApplication;
import org.yavdr.grafdroid.dao.GrafDroidDBHelper;
import org.yavdr.grafdroid.dao.pojo.Vdr;
import org.yavdr.grafdroid.dao.pojo.VdrAddress;
import org.yavdr.grafdroid.tcp.GraphTFTHeader;
import org.yavdr.grafdroid.tcp.GraphTFTListener;
import org.yavdr.grafdroid.tcp.TcpServiceHandler;

import com.j256.ormlite.dao.Dao;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class GrafDroidActivity extends Activity implements GraphTFTListener,
		OnTouchListener, OnClickListener {
	/** Called when the activity is first created. */
	private ImageView image;
	private Thread th;
	private TcpServiceHandler handler;
	// private WakeLock wakeLock;
	private KeyguardLock lock;
	private MotionEvent lastEvent;
	private int viewWidth;
	private int viewHeight;
	private Dao<Vdr, String> vdrDao;
	//private long lastKlick = Long.MIN_VALUE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Window w = this.getWindow(); // in Activity's onCreate() for instance
		w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		/*
		 * w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 * 
		 * WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 */
		setContentView(R.layout.main);

		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
		lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		lock.disableKeyguard();

		/*
		 * PowerManager powerManager = (PowerManager)
		 * getSystemService(Context.POWER_SERVICE); wakeLock =
		 * powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
		 * "Full Wake Lock");
		 */

		// Set up the window layout
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// setContentView(R.layout.main);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.custom_title);

		// Set up the custom title
		// mTitle = (TextView) findViewById(R.id.title_left_text);
		// mTitle.setText(R.string.app_name);
		// mTitle = (TextView) findViewById(R.id.title_right_text);

		// TcpServiceHandler handler=new TcpServiceHandler(this);
		// handler.execute("192.168.178.24");

		image = (ImageView) findViewById(R.id.imageView1);

		image.setOnTouchListener(this);
		image.setFocusableInTouchMode(true);
		image.setOnClickListener(this);

		GrafDroidDBHelper dbHelper = new GrafDroidDBHelper(
				getApplicationContext());

		try {
			vdrDao = dbHelper.getVdrDao();
		} catch (SQLException e) {
			vdrDao = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		lock.reenableKeyguard();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			Vdr currentVdr = ((GrafDroidApplication) getApplication())
					.getCurrentVdr();

			if (currentVdr == null) {
				Intent intent = new Intent(
						"org.yavdr.grafdroid.intent.action.MANAGEVDR");
				startActivity(intent);
			} else {

				if (currentVdr.isOnline()) {
					handler = new TcpServiceHandler(this, this, currentVdr);
					th = new Thread(handler);
					th.start();
				} else {
					Intent intent = new Intent(
							"org.yavdr.grafdroid.intent.action.MANAGEVDR");
					intent.putExtra("offline", true);
					startActivity(intent);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		// wakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (handler != null)
			handler.stop();
		try {
			if (th != null)
				th.join();
		} catch (InterruptedException e) {
		}
		// wakeLock.release();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}

	public void callCompleted(GraphTFTHeader header, byte[] msg) {
		switch (header.command) {
		case GraphTFTHeader.WELCOME:
			Log.d("TCP", "Got Welcome");

			break;
		case GraphTFTHeader.DATA:
			Log.d("TCP", "Got Data");

			Bitmap bmp = BitmapFactory.decodeByteArray(msg, 0, msg.length);
			if (bmp != null) {
				image.setImageBitmap(bmp);

			}

			break;
		case GraphTFTHeader.LOGOUT:
			Log.d("TCP", "Got Logout");
			break;
		}
		return;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			//long now = System.currentTimeMillis();
			//if (now < lastKlick + 250) // doppel klick
				finish();
			//else {
			//	lastKlick = now;
			//	handler.sendMouseEvent(0, 0, 3, 0, 0);
			//}
			return true;
			/*
			 * case KeyEvent.KEYCODE_VOLUME_UP: if (mBound) mService.keyVolUp();
			 * return true; case KeyEvent.KEYCODE_VOLUME_DOWN: if (mBound)
			 * mService.keyVolDown(); return true; case KeyEvent.KEYCODE_MENU:
			 * if (mBound) mService.keyMenu(); return true;
			 */
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	public boolean onTouch(View v, MotionEvent event) {
		lastEvent = event;
		return false;
	}

	public void onClick(View v) {
		int viewWidth = image.getWidth();
		int viewHeight = image.getHeight();

		Drawable drawable = image.getDrawable();
		Rect imageBounds = drawable.getBounds();

		// original height and width of the bitmap
		int intrinsicHeight = drawable.getIntrinsicHeight();
		int intrinsicWidth = drawable.getIntrinsicWidth();

		// height and width of the visible (scaled) image
		int scaledHeight = imageBounds.height();
		int scaledWidth = imageBounds.width();

		// Find the ratio of the original image to the scaled image
		// Should normally be equal unless a disproportionate scaling
		// (e.g. fitXY) is used.
		float heightRatio = intrinsicHeight / scaledHeight;
		float widthRatio = intrinsicWidth / scaledWidth;

		// do whatever magic to get your touch point
		// MotionEvent event;

		// get the distance from the left and top of the image bounds
		int scaledImageOffsetX = (int) (lastEvent.getX() - imageBounds.left);
		int scaledImageOffsetY = (int) (lastEvent.getY() - imageBounds.top);

		// scale these distances according to the ratio of your scaling
		// For example, if the original image is 1.5x the size of the scaled
		// image, and your offset is (10, 20), your original image offset
		// values should be (15, 30).
		int originalImageOffsetX = (int) (scaledImageOffsetX * widthRatio);
		int originalImageOffsetY = (int) (scaledImageOffsetY * heightRatio);

		if (viewWidth / intrinsicWidth < viewHeight / intrinsicHeight) {
			int imageHeight = (int) (scaledHeight * (1.0 * viewWidth / intrinsicWidth));
			double ratio = 1.0 * intrinsicWidth / viewWidth;

			originalImageOffsetY -= (viewHeight - imageHeight) / 2;
			originalImageOffsetX *= ratio;
			originalImageOffsetY *= ratio;
		} else {
			int imageWidth = (int) (scaledWidth * (1.0 * viewHeight / intrinsicHeight));
			double ratio = 1.0 * intrinsicHeight / viewHeight;

			originalImageOffsetX -= (viewWidth - imageWidth) / 2;
			originalImageOffsetX *= ratio;
			originalImageOffsetY *= ratio;

		}

		if (originalImageOffsetX > 0 && originalImageOffsetX < intrinsicWidth
				&& originalImageOffsetY > 0
				&& originalImageOffsetY < intrinsicHeight) {
			handler.sendMouseEvent(originalImageOffsetX, originalImageOffsetY,
					1, 0, 0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.grafdroid, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.manage_vdr:
			Intent intent = new Intent(
					"org.yavdr.grafdroid.intent.action.MANAGEVDR");
			startActivity(intent);
			return true;
		case R.id.exit:
			finish();
			return true;
		case R.id.help:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}