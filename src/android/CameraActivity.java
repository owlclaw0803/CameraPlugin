package org.apache.cordova.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Camera Activity Class. Configures Android camera to take picture and show it.
 */
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	
	private CameraPreview mPreview;

	private Bitmap takenBitmap = null;
	private ImageView takenPicture;
	private RelativeLayout reviewPage;
	private String mainFilePath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getResources().getIdentifier("cameraplugin", "layout", getPackageName()));

		// Create a Preview and set it as the content of activity.
	    mPreview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
		
		LinearLayout preview = (LinearLayout)findViewById(getResources().getIdentifier("camera_preview", "id", getPackageName()));
		takenPicture = (ImageView) findViewById(getResources().getIdentifier("takenPicture", "id", getPackageName()));
		reviewPage = (RelativeLayout)findViewById(getResources().getIdentifier("reviewPage", "id", getPackageName()));
		
		preview.addView(mPreview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// Add a listener to the Capture button
		ImageView captureButton = (ImageView) findViewById(getResources().getIdentifier("button_capture", "id", getPackageName()));
		captureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mPreview.takePhoto(CameraActivity.this);
			}
		});
		
		ImageView cancelButton = (ImageView) findViewById(getResources().getIdentifier("button_cancel", "id", getPackageName()));
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
		Button btnRetake = (Button) findViewById(getResources().getIdentifier("btnRetake", "id", getPackageName()));
		btnRetake.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reviewPage.setVisibility(View.GONE);
				mPreview.mCamera.startPreview();
			}
		});
		
		Button btnUsePhoto = (Button) findViewById(getResources().getIdentifier("btnUsePhoto", "id", getPackageName()));
		btnUsePhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				takenBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		        byte[] data = stream.toByteArray();
		        
				Uri fileUri = (Uri) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);

				File pictureFile = new File(fileUri.getPath());

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.d(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, "Error accessing file: " + e.getMessage());
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		
		Uri fileUri = (Uri) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);
		String filepath = fileUri.getPath();
		String ext = filepath.substring(filepath.lastIndexOf("cache")); 
		mainFilePath = filepath.substring(0, filepath.length()-ext.length());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	public void proceedWithBitmap(Bitmap bitmap, Camera camera){
		takenBitmap = bitmap;
		mPreview.mCamera.stopPreview();
		//takenPicture.setImageBitmap(bitmap);
		//reviewPage.setVisibility(View.VISIBLE);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		takenBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        
		long currentDateTimeString = (new Date()).getTime();
		
		String filepath = mainFilePath+currentDateTimeString+".jpg";
		
		File pictureFile = new File(filepath);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
		
		mPreview.mCamera.startPreview();
	}
}