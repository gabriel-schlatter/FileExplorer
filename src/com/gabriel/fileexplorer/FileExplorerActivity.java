package com.gabriel.fileexplorer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorerActivity extends ListActivity implements
		View.OnLongClickListener, View.OnClickListener {

	public static final String TYPE_PDF = "pdf";
	public static final String TYPE_JPG = "jpg";
	public static final String TYPE_JPEG = "jpeg";
	public static final String TYPE_PNG = "png";
	public static final String TYPE_GIF = "gif";
	public static final String TYPE_MP4 = "mp4";
	public static final String TYPE_MPG = "mpg";
	public static final String TYPE_MPEG = "mpeg";

	private ArrayList<FileItem> files;
	private FileAdapter adapter;

	private MenuItem pasteItem;

	private String currentPath;

	private TextView tvPath;
	private Activity context;
	private ProgressDialog progress;

	private ArrayList<FileItem> copyItems;
	private ArrayList<FileItem> cutItems;
	private ArrayList<FileItem> markedItems;

	private static Map<String, Integer> ICON_MAP = new HashMap<String, Integer>();

	static {
		ICON_MAP.put(TYPE_PDF, R.drawable.ic_pdf);
		ICON_MAP.put(TYPE_JPG, R.drawable.ic_pic);
		ICON_MAP.put(TYPE_JPEG, R.drawable.ic_pic);
		ICON_MAP.put(TYPE_PNG, R.drawable.ic_pic);
		ICON_MAP.put(TYPE_GIF, R.drawable.ic_pic);
		ICON_MAP.put(TYPE_MP4, R.drawable.ic_video);
		ICON_MAP.put(TYPE_MPG, R.drawable.ic_video);
		ICON_MAP.put(TYPE_MPEG, R.drawable.ic_video);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fileexplorer);

		tvPath = (TextView) findViewById(R.id.tv_path);
		context = this;
		Utils.activity = this;

		ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (currentPath.length() > 2) {
					File pathFile = new File(currentPath);
					if (pathFile != null) {
						currentPath = pathFile.getParent();
						if (currentPath != null)
							openDirectory(currentPath);
					}
				}
			}
		});

		files = new ArrayList<FileItem>();
		copyItems = new ArrayList<FileItem>();
		cutItems = new ArrayList<FileItem>();
		markedItems = new ArrayList<FileItem>();
		currentPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		refreshPathTextView();
		populateFilesList(currentPath);
		setListAdapter(new FileAdapter(this));
		adapter = (FileAdapter) getListAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// Clear history function
		pasteItem = menu.findItem(R.id.paste);
		pasteItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (!copyItems.isEmpty()) {
					for (FileItem i : copyItems) {
						CopyTask task = new CopyTask(i, new File(currentPath));
						task.execute();
					}
					copyItems.clear();
				} else {
					Toast.makeText(getBaseContext(), "nothing to copy",
							Toast.LENGTH_SHORT).show();
				}
				if (!cutItems.isEmpty()) {
					for (FileItem i : cutItems) {
						CopyAndDeleteTask task = new CopyAndDeleteTask(i, new File(currentPath));
						task.execute();
					}
					cutItems.clear();
				} else {
					Toast.makeText(getBaseContext(), "nothing to cut",
							Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		// searchItem = menu.findItem(R.id.action_search);
		// mSearchView = (SearchView) searchItem.getActionView();
		// mSearchView.setOnQueryTextListener(this);
		// mSearchView.setOnQueryTextFocusChangeListener(new
		// OnFocusChangeListener()
		// {
		//
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// if (!hasFocus) {
		// searchItem.collapseActionView();
		// setRequestedOrientation(4);
		// }
		// else {
		// setRequestedOrientation(utils.getScreenOrientation());
		// }
		// }
		// });

		return true;
	}

	@Override
	public void onClick(View v) {
		FileItem item = (FileItem) getListAdapter().getItem(
				getListView().getPositionForView(v));
		if (item.isDirectory()) {
			openDirectory(item.getAdress());
		} else {
			openFile(item.getAdress());
		}
	}

	@Override
	public boolean onLongClick(View v) {
		showOptionsDialog((FileItem) getListAdapter().getItem(
				getListView().getPositionForView(v)));
		return true;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	private void populateFilesList(String directory) {
		File f = new File(directory);
		File[] filesList = f.listFiles();
		if (filesList != null && filesList.length != 0)
			for (File file : filesList) {
				files.add(new FileItem(file.getName(), file.getAbsolutePath(),
						file.isDirectory()));
			}

	}

	private Drawable getIconForFile(String path) {

		String ext = path.substring(path.lastIndexOf(".") + 1);

		if (ICON_MAP.get(ext) != null) {
			return getResources().getDrawable(ICON_MAP.get(ext));
		} else
			return getResources().getDrawable(R.drawable.ic_file);
	}

	public void openFile(String path) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(path);

		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = file.getName().substring(file.getName().indexOf(".") + 1);
		String type = mime.getMimeTypeFromExtension(ext);

		intent.setDataAndType(Uri.fromFile(file), type);

		startActivity(intent);
	}

	public void refreshPathTextView() {
		tvPath.setText(currentPath);
	}

	public void openDirectory(String path) {
		files.clear();
		populateFilesList(path);
		adapter.clear();
		for (FileItem item : files) {
			adapter.add(item);
		}
		adapter.notifyDataSetChanged();
		currentPath = path;
		refreshPathTextView();
	}

	private void showOptionsDialog(final FileItem file) {
		final CharSequence[] items = { "Open", "Copy", "Cut", "Delete" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(file.getFilename());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					if (file.isDirectory()) {
						openDirectory(file.getAdress());
					} else {
						openFile(file.getAdress());
					}
					break;
				case 1:
					if (!markedItems.isEmpty()) {
						cutItems.clear();
						copyItems.clear();
						for (FileItem i : markedItems) {
							copyItems.add(i);
						}
					} else {
						copyItems.clear();
						copyItems.add(file);
					}
					break;
				case 2:
					if (!markedItems.isEmpty()) {
						cutItems.clear();
						copyItems.clear();
						for (FileItem i : markedItems) {
							cutItems.add(i);
						}
					} else {
						cutItems.clear();
						cutItems.add(file);
					}
					break;
				case 3:
					DeleteRecursive(new File(file.getAdress()));
					openDirectory(currentPath);
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public class FileAdapter extends ArrayAdapter<FileItem> {

		private LayoutInflater inflater;

		public FileAdapter(Context context) {
			super(context, R.layout.file_list_item);
			this.inflater = getLayoutInflater();
			if (files != null)
				for (FileItem file : files) {
					this.add(file);
				}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			FileItem item = getItem(position);

			View row = inflater.inflate(R.layout.file_list_item, parent, false);
			ImageView ivFileIcon = (ImageView) row
					.findViewById(R.id.iv_file_icon);
			TextView tvFileName = (TextView) row.findViewById(R.id.tv_filename);

			tvFileName.setText(item.getFilename());
			if (item.isDirectory()) {
				ivFileIcon.setImageDrawable(getResources().getDrawable(
						R.drawable.folder_icon));

			} else {
				ivFileIcon.setImageDrawable(getIconForFile(item.getAdress()));
			}

			row.setOnLongClickListener((OnLongClickListener) context);
			row.setOnClickListener((OnClickListener) context);
			return row;
		}
	}

	public class FileItem {
		private String filename;
		private String adress;
		private boolean directory;
		private boolean marked;

		public FileItem(String filename, String address, boolean isDirectory) {
			this.filename = filename;
			this.adress = address;
			this.directory = isDirectory;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getAdress() {
			return adress;
		}

		public void setAdress(String adress) {
			this.adress = adress;
		}

		public boolean isDirectory() {
			return directory;
		}

		public void setDirectory(boolean isDirectory) {
			this.directory = isDirectory;
		}

		public boolean isMarked() {
			return marked;
		}

		public void setMarked(boolean marked) {
			this.marked = marked;
		}
	}

	public class CopyTask extends AsyncTask<Void, Void, Boolean> {

		private FileItem sourceLocation;
		private File targetLocation;

		public CopyTask(FileItem sourceLocation, File targetLocation) {
			this.sourceLocation = sourceLocation;
			this.targetLocation = targetLocation;
		}

		public void onPreExecute() {
			setRequestedOrientation(Utils.getScreenOrientation());
			progress = ProgressDialog
					.show(context, getString(R.string.copying),
							getString(R.string.is_copying));
		}

		protected Boolean doInBackground(Void... params) {
			copyDirectoryOneLocationToAnotherLocation(
					new File(sourceLocation.getAdress()),
					new File(targetLocation.getPath(), sourceLocation
							.getFilename()));

			return true;
		}

		protected void onPostExecute(Boolean result) {
			progress.dismiss();
			openDirectory(currentPath);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
	
	public class CopyAndDeleteTask extends AsyncTask<Void, Void, Boolean> {

		private FileItem sourceLocation;
		private File targetLocation;

		public CopyAndDeleteTask(FileItem sourceLocation, File targetLocation) {
			this.sourceLocation = sourceLocation;
			this.targetLocation = targetLocation;
		}

		public void onPreExecute() {
			setRequestedOrientation(Utils.getScreenOrientation());
			progress = ProgressDialog
					.show(context, getString(R.string.copying),
							getString(R.string.is_copying));
		}

		protected Boolean doInBackground(Void... params) {
			copyDirectoryOneLocationToAnotherLocation(
					new File(sourceLocation.getAdress()),
					new File(targetLocation.getPath(), sourceLocation
							.getFilename()));

			return true;
		}

		protected void onPostExecute(Boolean result) {
			progress.dismiss();
			DeleteRecursive(new File(sourceLocation.getAdress()));
			openDirectory(currentPath);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

	public void changeProgressMessage(final String message) {
		Runnable changeMessageTask = new Runnable() {
			@Override
			public void run() {
				progress.setMessage(getString(R.string.is_copying) + "\n"
						+ message);
			}
		};
		context.runOnUiThread(changeMessageTask);
	}

	public void copyDirectoryOneLocationToAnotherLocation(File sourceLocation,
			File targetLocation) {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i = 0; i < sourceLocation.listFiles().length; i++) {
				copyDirectoryOneLocationToAnotherLocation(new File(
						sourceLocation, children[i]), new File(targetLocation,
						children[i]));
			}
		} else {
			copyFile(sourceLocation, targetLocation);
		}
	}

	public boolean copyFile(File source, File dest) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {
			changeProgressMessage(dest.getName());
			bis = new BufferedInputStream(new FileInputStream(source));
			bos = new BufferedOutputStream(new FileOutputStream(dest, false));

			byte[] buf = new byte[1024];
			bis.read(buf);

			do {
				bos.write(buf);
			} while (bis.read(buf) != -1);
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				return false;
			}
		}

		return true;
	}

	// WARNING ! Inefficient if source and dest are on the same filesystem !
	public boolean moveFile(File source, File dest) {
		return copyFile(source, dest) && source.delete();
	}

	public static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory != null)
			if (fileOrDirectory.isDirectory())
				for (File child : fileOrDirectory.listFiles())
					DeleteRecursive(child);

		fileOrDirectory.delete();
	}

}
