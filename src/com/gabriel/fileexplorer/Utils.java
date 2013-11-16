package com.gabriel.fileexplorer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.gabriel.fileexplorer.FileExplorerActivity.FileAdapter;
import com.gabriel.fileexplorer.FileExplorerActivity.FileItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.Toast;

public class Utils {

	static Activity activity;

	public static int getScreenOrientation() {
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int orientation;
		// if the device's natural orientation is portrait:
		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				&& height > width
				|| (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
				&& width > height) {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			default:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			}
		}
		// if the device's natural orientation is landscape or if the device
		// is square:
		else {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			default:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			}
		}

		return orientation;
	}


}
