package org.mupdfdemo2.activity;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.mupdfdemo2.adapter.ChoosePDFAdapter;
import org.mupdfdemo2.model.ChoosePDFItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.iteye.weimingtom.ebdmod.BaseActivity;
import com.iteye.weimingtom.ebdmod.R;

public class ChoosePDFActivity extends BaseActivity {
	private final static boolean ENABLE_TEST = false;
	private final static String TEST_FILE = "/mnt/sdcard/Download/glm.pdf";
	
	//是否启动MUPDF调试模式
	private final static boolean IS_MUPDF_DEBUG = false; 
	
	private final static String STATE_KEY_ISSTARTED = "isStarted";
	public final static String PICK_KEY_FILE = "com.artifex.mupdfdemo.PICK_KEY_FILE";
	//退出类型
	private final static int PURPOSE_OPEN = 0;
	private final static int PURPOSE_PICK = 1;
	
	private static File mDirectory;
	private static Map<String, Integer> mPositions = 
			new HashMap<String, Integer>();
	
	private File mParent;
	private File[] mDirs;
	private File[] mFiles;
	private Handler mHandler;
	private Runnable mUpdateFiles;
	private ChoosePDFAdapter adapter;
	private int mPurpose = PURPOSE_OPEN;
	private ListView listViewContent;
	private boolean isStarted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null &&
			savedInstanceState.getBoolean(STATE_KEY_ISSTARTED, false)) {
			isStarted = true;
		}
		this.setContentView(R.layout.ebookdroid_mupdf_activity_choose);
		this.setupActionBar();
		
		listViewContent = (ListView) this.findViewById(R.id.listViewContent);
		mPurpose = PICK_KEY_FILE.equals(getIntent().getAction()) ? PURPOSE_PICK : PURPOSE_OPEN;
		String storageState = Environment.getExternalStorageState();
		//FIXME:这里的处理需要修改
		if (!Environment.MEDIA_MOUNTED.equals(storageState)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("没有存储介质");
			builder.setMessage("存储介质在设备和 PC 上共同使用，会导致该存储介质在设备上无法被访问");
			AlertDialog alert = builder.create();
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "关闭", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			alert.show();
			return;
		}
		if (mDirectory == null) {
			mDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}
		adapter = new ChoosePDFAdapter(getLayoutInflater());
		listViewContent.setAdapter(adapter);
		listViewContent.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				ChoosePDFActivity.this.onListItemClick(l, v, position, id);
			}
		});
		mHandler = new Handler();
		mUpdateFiles = new Runnable() {
			public void run() {
				getResources();
				String appName = "MuPDF";
				String version = "1.9a (git build)";
				String title = "%s%s:%s";
				setTitle(String.format(title, appName, version, mDirectory));
				mParent = mDirectory.getParentFile();
				mDirs = mDirectory.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
				if (mDirs == null) {
					mDirs = new File[0];
				}
				mFiles = mDirectory.listFiles(new FileFilter() {
					public boolean accept(File file) {
						if (file.isDirectory()) {
							return false;
						}
						String fname = file.getName().toLowerCase();
						switch (mPurpose) {
						case PURPOSE_OPEN:
						case PURPOSE_PICK:
							if (fname.endsWith(".pdf")) {
								return true;
							}
							return false;
							
						default:
							return false;
						}
					}
				});
				if (mFiles == null) {
					mFiles = new File[0];
				}
				Arrays.sort(mFiles, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(
								arg1.getName());
					}
				});
				Arrays.sort(mDirs, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(
								arg1.getName());
					}
				});
				adapter.clear();
				if (mParent != null) {
					//getString(R.string.parent_directory)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.PARENT, "[向上一级]"));
				}
				for (File f : mDirs) {
					adapter.add(new ChoosePDFItem(ChoosePDFItem.DIR, f.getName()));
				}
				for (File f : mFiles) {
					adapter.add(new ChoosePDFItem(ChoosePDFItem.DOC, f.getName()));
				}
				lastPosition();
			}
		};
		mHandler.post(mUpdateFiles);
		FileObserver observer = new FileObserver(mDirectory.getPath(),
				FileObserver.CREATE | FileObserver.DELETE) {
			public void onEvent(int event, String path) {
				mHandler.post(mUpdateFiles);
			}
		};
		observer.startWatching();
		if (ENABLE_TEST) {
			if (!isStarted) { //第二次的话就不启动了
				test();
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putBoolean(STATE_KEY_ISSTARTED, true);
		}
	}

	private void lastPosition() {
		String p = mDirectory.getAbsolutePath();
		if (mPositions.containsKey(p)) {
			listViewContent.setSelection(mPositions.get(p));
		}
	}

	protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
		mPositions.put(mDirectory.getAbsolutePath(), 
			listViewContent.getFirstVisiblePosition());
		if (position < (mParent == null ? 0 : 1)) {
			mDirectory = mParent;
			mHandler.post(mUpdateFiles);
			return;
		}
		position -= (mParent == null ? 0 : 1);
		if (position < mDirs.length) {
			mDirectory = mDirs[position];
			mHandler.post(mUpdateFiles);
			return;
		}
		position -= mDirs.length;
		Uri uri = Uri.fromFile(mFiles[position]);
		Intent intent = new Intent(this, MuPDFActivity.class);
		intent.putExtra(MuPDFActivity.EXTRA_MUPDF_DEBUG, IS_MUPDF_DEBUG);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		switch (mPurpose) {
		case PURPOSE_OPEN:
			startActivity(intent);
			break;
			
		case PURPOSE_PICK:
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mDirectory != null) {
			mPositions.put(mDirectory.getAbsolutePath(), 
				listViewContent.getFirstVisiblePosition());
		}
	}
	
	private void test() {
		Uri uri = Uri.fromFile(new File(TEST_FILE));
		Intent intent = new Intent(this, MuPDFActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
	}
	
	private void setupActionBar() {
		actionBar.setIcon(R.drawable.icon_detail);
		actionBar.setTitle(MuPDFActivity.TITLE_CHOOSE_PDF);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
    @Override
    public boolean onOptionsItemSelected(
    	com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	return true;
        }
        return false;
    }
}
