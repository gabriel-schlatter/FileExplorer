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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorerActivity extends ListActivity implements
    View.OnLongClickListener, View.OnClickListener {

  private ArrayList<FileItem> files;
  private FileAdapter adapter;

  private MenuItem pasteItem;

  private String currentPath;

  private TextView tvPath;
  private Activity context;

  private ArrayList<FileItem> copyItems;
  private ArrayList<FileItem> cutItems;
  private ArrayList<FileItem> markedItems;

  private static Map<String, Integer> ICON_MAP = new HashMap<String, Integer>();

  static {
    ICON_MAP.put(Constants.TYPE_PDF, R.drawable.ic_pdf);
    ICON_MAP.put(Constants.TYPE_JPG, R.drawable.ic_pic);
    ICON_MAP.put(Constants.TYPE_JPEG, R.drawable.ic_pic);
    ICON_MAP.put(Constants.TYPE_PNG, R.drawable.ic_pic);
    ICON_MAP.put(Constants.TYPE_GIF, R.drawable.ic_pic);
    ICON_MAP.put(Constants.TYPE_MP4, R.drawable.ic_video);
    ICON_MAP.put(Constants.TYPE_MPG, R.drawable.ic_video);
    ICON_MAP.put(Constants.TYPE_MPEG, R.drawable.ic_video);
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
    currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
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
          MasterCopyTask task = new MasterCopyTask(copyItems, new File(
              currentPath), false);
          task.execute();
        }
        else {
          Toast.makeText(getBaseContext(), "nothing to copy",
              Toast.LENGTH_SHORT).show();
        }
        if (!cutItems.isEmpty()) {
          MasterCopyTask task = new MasterCopyTask(cutItems, new File(
              currentPath), true);
          task.execute();
        }
        else {
          Toast
              .makeText(getBaseContext(), "nothing to cut", Toast.LENGTH_SHORT)
              .show();
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
    if (!markedItems.isEmpty()) {
      if (markedItems.contains(item)) {
        markedItems.remove(item);
        adapter.markRow(v, false);
      }
      else {
        markedItems.add(item);
        adapter.markRow(v, true);
      }
    }
    else {
      if (item.isDirectory()) {
        openDirectory(item.getAdress());
      }
      else {
        openFile(item.getAdress());
      }
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
        files.add(new FileItem(file.getName(), file.getAbsolutePath(), file
            .isDirectory()));
      }

  }

  private Drawable getIconForFile(String path) {

    String ext = path.substring(path.lastIndexOf(".") + 1);

    if (ICON_MAP.get(ext) != null) {
      return getResources().getDrawable(ICON_MAP.get(ext));
    }
    else
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
            }
            else {
              openFile(file.getAdress());
            }
            break;
          case 1:
            if (!markedItems.isEmpty()) {
              cutItems.clear();
              copyItems.clear();
              for (FileItem i : markedItems) {
                copyItems.add(i);
                adapter.markRow(i, false);
              }
              markedItems.clear();
            }
            else {
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
                adapter.markRow(i, false);
              }
              markedItems.clear();
            }
            else {
              cutItems.clear();
              cutItems.add(file);
            }
            break;
          case 3:
            for (FileItem i : markedItems) {
              DeleteRecursive(new File(i.getAdress()));
            }
            markedItems.clear();
            openDirectory(currentPath);
            break;
        }
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  public class MasterCopyTask extends AsyncTask<Void, Void, Boolean> {

    private ArrayList<FileItem> sources;
    private File targetLocation;
    private ProgressDialog progress;
    private boolean deleteAfterCopy;

    public MasterCopyTask(ArrayList<FileItem> sources, File targetLocation,
        boolean deleteAfterCopy) {
      this.sources = sources;
      this.targetLocation = targetLocation;
      this.deleteAfterCopy = deleteAfterCopy;
    }

    public void onPreExecute() {
      setRequestedOrientation(Utils.getScreenOrientation());
      progress = ProgressDialog.show(context, getString(R.string.copying),
          getString(R.string.is_copying));
    }

    protected Boolean doInBackground(Void... params) {
      for (FileItem file : sources) {
        copyDirectoryOneLocationToAnotherLocation(new File(file.getAdress()),
            new File(targetLocation.getPath(), file.getFilename()), progress);
      }
      return true;
    }

    protected void onPostExecute(Boolean result) {
      progress.dismiss();
      if (!deleteAfterCopy) {
        sources.clear();
      }
      else {
        for (FileItem item : sources) {
          DeleteRecursive(new File(item.getAdress()));
        }
      }
      openDirectory(currentPath);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
  }

  public void changeProgressMessage(final String message,
      final ProgressDialog progress) {
    Runnable changeMessageTask = new Runnable() {
      @Override
      public void run() {
        progress.setMessage(getString(R.string.is_copying) + "\n" + message);
      }
    };
    context.runOnUiThread(changeMessageTask);
  }

  public void copyDirectoryOneLocationToAnotherLocation(File sourceLocation,
      File targetLocation, ProgressDialog progress) {

    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdir();
      }
      String[] children = sourceLocation.list();
      for (int i = 0; i < sourceLocation.listFiles().length; i++) {
        copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation,
            children[i]), new File(targetLocation, children[i]), progress);
      }
    }
    else {
      copyFile(sourceLocation, targetLocation, progress);
    }
  }

  public boolean copyFile(File source, File dest, ProgressDialog progress) {
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;

    try {
      changeProgressMessage(dest.getName(), progress);
      bis = new BufferedInputStream(new FileInputStream(source));
      bos = new BufferedOutputStream(new FileOutputStream(dest, false));

      byte[] buf = new byte[1024];
      bis.read(buf);

      do {
        bos.write(buf);
      }
      while (bis.read(buf) != -1);
    }
    catch (IOException e) {
      return false;
    }
    finally {
      try {
        if (bis != null)
          bis.close();
        if (bos != null)
          bos.close();
      }
      catch (IOException e) {
        return false;
      }
    }

    return true;
  }

  public static void DeleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory != null)
      if (fileOrDirectory.isDirectory())
        for (File child : fileOrDirectory.listFiles())
          DeleteRecursive(child);

    fileOrDirectory.delete();
  }

  public class FileAdapter extends ArrayAdapter<FileItem> {

    private boolean disableCheckedChangeListener;
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

      final View row = inflater.inflate(R.layout.file_list_item, parent, false);
      ImageView ivFileIcon = (ImageView) row.findViewById(R.id.iv_file_icon);
      TextView tvFileName = (TextView) row.findViewById(R.id.tv_filename);

      tvFileName.setText(item.getFilename());
      if (item.isDirectory()) {
        ivFileIcon.setImageDrawable(getResources().getDrawable(
            R.drawable.folder_icon));

      }
      else {
        ivFileIcon.setImageDrawable(getIconForFile(item.getAdress()));
      }

      CheckBox cbMarked = (CheckBox) row.findViewById(R.id.checkbox_marked);
      if (item.isMarked()) {
        row.setBackgroundColor(getResources().getColor(R.color.grey_selected));
        cbMarked.setChecked(true);
      }
      cbMarked.setOnCheckedChangeListener(new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
          if (!disableCheckedChangeListener) {
            if (isChecked) {
              markedItems.add(getItem(getListView().getPositionForView(row)));
            }
            else {
              markedItems
                  .remove(getItem(getListView().getPositionForView(row)));
            }
            markRow(row, isChecked);
          }
        }
      });

      row.setOnLongClickListener((OnLongClickListener) context);
      row.setOnClickListener((OnClickListener) context);
      return row;
    }

    public View getViewForPosition(int position) {
      int firstPosition = getListView().getFirstVisiblePosition()
          - getListView().getHeaderViewsCount(); // This is the same as child #0
      int wantedChild = position - firstPosition;
      // Say, first visible position is 8, you want position 10, wantedChild
      // will now be 2
      // So that means your view is child #2 in the ViewGroup:
      if (wantedChild < 0 || wantedChild >= getListView().getChildCount()) {
        // Log.w(TAG,
        // "Unable to get view for desired position, because it's not being displayed on screen.");
        return null;
      }
      // Could also check if wantedPosition is between
      // listView.getFirstVisiblePosition() and
      // listView.getLastVisiblePosition() instead.
      return getListView().getChildAt(wantedChild);
    }

    public void markRow(View row, boolean mark) {
      if (row != null) {
        if (mark) {
          row.setBackgroundColor(getResources().getColor(R.color.grey_selected));
        }
        else {
          row.setBackgroundDrawable(getResources().getDrawable(
              R.drawable.list_item_background_selector));
        }
        CheckBox cbMarked = (CheckBox) row.findViewById(R.id.checkbox_marked);
        disableCheckedChangeListener = true;
        cbMarked.setChecked(mark);
        disableCheckedChangeListener = false;
        int position = getListView().getPositionForView(row);
        FileItem item = getItem(position);
        item.setMarked(mark);
      }
    }

    public void markRow(FileItem item, boolean mark) {
      View row = getViewForPosition(getPosition(item));
      if (row != null) {
        if (mark) {
          row.setBackgroundColor(getResources().getColor(R.color.grey_selected));
        }
        else {
          row.setBackgroundDrawable(getResources().getDrawable(
              R.drawable.list_item_background_selector));
        }
        CheckBox cbMarked = (CheckBox) row.findViewById(R.id.checkbox_marked);
        disableCheckedChangeListener = true;
        cbMarked.setChecked(mark);
        disableCheckedChangeListener = false;
        item.setMarked(mark);
      }
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
}