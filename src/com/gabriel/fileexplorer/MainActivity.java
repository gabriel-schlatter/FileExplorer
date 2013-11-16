//package com.gabriel.fileexplorer;
//
//import java.io.File;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.app.Activity;
//import android.content.Intent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.MenuItem.OnMenuItemClickListener;
//import android.webkit.MimeTypeMap;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//public class MainActivity extends Activity {
//
//  String path;
//  FileExplorerView fileExplorer;
//  MenuItem pasteItem;
//
//  @Override
//  protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.activity_main);
//    fileExplorer = (FileExplorerView) findViewById(R.id.file_explorer);
//    path = Environment.getExternalStorageDirectory().getAbsolutePath();
//
//  }
//
//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    // Inflate the menu; this adds items to the action bar if it is present.
//    getMenuInflater().inflate(R.menu.main, menu);
//    // Clear history function
//    pasteItem = menu.findItem(R.id.paste);
//    pasteItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//      @Override
//      public boolean onMenuItemClick(MenuItem item) {
//        fileExplorer.paste();
//        return false;
//      }
//    });
//    setupFileExplorerView(path);
//
//    // searchItem = menu.findItem(R.id.action_search);
//    // mSearchView = (SearchView) searchItem.getActionView();
//    // mSearchView.setOnQueryTextListener(this);
//    // mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener()
//    // {
//    //
//    // @Override
//    // public void onFocusChange(View v, boolean hasFocus) {
//    // if (!hasFocus) {
//    // searchItem.collapseActionView();
//    // setRequestedOrientation(4);
//    // }
//    // else {
//    // setRequestedOrientation(utils.getScreenOrientation());
//    // }
//    // }
//    // });
//
//    return true;
//  }
//
//  private void setupFileExplorerView(String path) {
//
//    TextView tvPath = (TextView) findViewById(R.id.tv_path);
//    tvPath.setText(path);
//
//    ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
//
//    // fileExplorer = new FileExplorerView(this);
//    fileExplorer.init(path, tvPath, btnBack, pasteItem);
//    fileExplorer
//        .setFileOpenedListener(new FileExplorerView.OnFileOpenedListener() {
//          @Override
//          public void onFileOpened(String path) {
//            Intent intent = new Intent();
//            intent.setAction(android.content.Intent.ACTION_VIEW);
//            File file = new File(path);
//
//            MimeTypeMap mime = MimeTypeMap.getSingleton();
//            String ext = file.getName().substring(
//                file.getName().indexOf(".") + 1);
//            String type = mime.getMimeTypeFromExtension(ext);
//
//            intent.setDataAndType(Uri.fromFile(file), type);
//
//            startActivity(intent);
//          }
//        });
//  }
//}
