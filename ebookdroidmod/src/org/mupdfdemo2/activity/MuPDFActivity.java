package org.mupdfdemo2.activity;

import java.io.File;
import java.util.concurrent.Executor;

import org.ebookdroid2.activity.ViewerActivityHelper;
import org.mupdfdemo2.model.SessionData;
import org.mupdfdemo2.task.AsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.iteye.weimingtom.ebdmod.BaseActivity;
import com.iteye.weimingtom.ebdmod.R;

/**
 * 批注模式：MuPDFReaderView.Mode.Drawing
 * FIXME:core加载时阻塞进程(*)
 * FIXME:页面大小缓存问题
 * FIXME:core不缓存问题
 * FIXME:设置标题冲突问题
 * FIXME:标题栏转圈
 * FIXME:旋转状态保存
 * FIXME:上传提示
 * FIXME:超链接高亮着色不一致
 * FIXME:进入批注状态后自动缩放到原有缩放率（退出也是）
 * FIXME:离线时似乎还是会提示上传
 * FIXME:目前屏蔽掉了链接功能：MuPDFCore.DISABLE_GET_PAGE_LINKS
 * FIXME:未测试特殊情况下的文件（破损或者单页）
 * FIXME:选择->删除 此时还可以选择别的批注（重现需要2个以上的批注笔划）
 * FIXME:测试带大纲和带目录链接的PDF文件
 * FIXME:跳转对话框改良
 * 
 * FIXME:消除对话框：文档已变更，保存变更吗？
 * FIXME:获取页面大小（太慢）
 * FIXME:最初进去的时候黑屏
 */
public class MuPDFActivity extends BaseActivity {
	private final static boolean D = true;
	private final static String TAG = "MuPDFActivity";
	
	private final static boolean BUTTON_VERBOSE = true; //更多的动作栏菜单
	public final static boolean MY_MOD = true; //我的修改
	private final static boolean USE_HORIZ = false; //是否在启动时使用横向批注模式
	
	public final static String EXTRA_MUPDF_DEBUG = "EXTRA_MUPDF_DEBUG";
	
    public final static String TITLE_CHOOSE_PDF = "选择PDF文件";
    public final static String TITLE_OPEN_PDF = "阅读器";
    public final static String TITLE_PDF_OUTLINE = "大纲";
	
	private MuPDFActivityHelper helper = new MuPDFActivityHelper();
	private ViewerActivityHelper helperEbd = new ViewerActivityHelper();
	private volatile MuPDFCore core_ = null;
	private LinearLayout linearLayoutMergeMupdf;
	private LinearLayout linearLayoutMergeEbookdroid;
	//
	private FrameLayout frameLayoutMode;
	private LinearLayout linearLayoutEbookdroidMode;
	private LinearLayout linearLayoutMergeMode;
	
	public final static int MERGE_MODE_READER = 0;
	public final static int MERGE_MODE_ANNOT = 1;
	public final static int MERGE_MODE_ANNOT_PEN = 2;
	public final static int MERGE_MODE_ANNOT_SELECT = 3;
	private int mergeMode = 0;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
	        //横向
	        //setContentView(R.layout.file_list_landscape);
	    } else {
	        //竖向
	        //setContentView(R.layout.file_list);
	    }
	    if (true) {
	    	//后退会批注查看状态
	    	if (this.mergeMode == MERGE_MODE_ANNOT_PEN) {
	    		helper.onButtonAnnotOK(this);
	    	}
	    	//FIXME:可以这样刷新，但可能会把原来的批注冲走（上面的onButtonAnnotOK是为了保存一下）
		    linearLayoutMergeMupdf.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (linearLayoutMergeMupdf.getVisibility() == View.VISIBLE) {
						toggleAnnotMode(true); //转屏幕后刷新一下
					}
				}
		    }, 500);
	    }
	}
	
	public OnChangeMergeModeListener onChangeMergeMode = new OnChangeMergeModeListener() {
		@Override
		public void onChangeMergeMode(int mode) {
			if (/*mergeMode == MERGE_MODE_READER && */mode == MERGE_MODE_ANNOT) {
				MuPDFActivity.this.actionBar.setTitle("返回阅读"/*"批注模式"*/);
			} else if (mergeMode == MERGE_MODE_ANNOT && mode == MERGE_MODE_READER) {
				MuPDFActivity.this.actionBar.setTitle("阅读器");
			} else if (mergeMode == MERGE_MODE_ANNOT && mode == MERGE_MODE_ANNOT_PEN) {
				MuPDFActivity.this.actionBar.setTitle("保存批注");
			} else if (mergeMode == MERGE_MODE_ANNOT && mode == MERGE_MODE_ANNOT_SELECT) {
				MuPDFActivity.this.actionBar.setTitle("取消选择");
			}
			mergeMode = mode;
			MuPDFActivity.this.supportInvalidateOptionsMenu();
		}
	};
	public OnTitleChangeListener onTitleChange = new OnTitleChangeListener() {
		@Override
		public void OnTitleChange(String title) {
			if (mergeMode == MERGE_MODE_READER) {
				MuPDFActivity.this.actionBar.setTitle(title);
			}
		}
	};
	public OnExitUploadListener onExitUpload = new OnExitUploadListener() {
		@Override
		public void onExitUpload() {
			onExit2();
		}
	};
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		helperEbd.setIsOffline(this.getIsUploadOffLine()); //非离线
		helperEbd.onCreatePre(savedInstanceState, this);
		super.onCreate(savedInstanceState);
		this.setupActionBar();
		
		this.setContentView(R.layout.ebookdroid_mupdf_activity_merge);
		if (D) {
			Log.e(TAG, "onCreate 001==============>");
		}
		helper.onChangeMergeMode = onChangeMergeMode;
		helperEbd.onChangeMergeMode = onChangeMergeMode;
		helperEbd.onTitleChange = onTitleChange;
		helperEbd.onExitUpload = onExitUpload;
		
		linearLayoutMergeMupdf = (LinearLayout) this.findViewById(R.id.linearLayoutMergeMupdf);
		linearLayoutMergeEbookdroid = (LinearLayout) this.findViewById(R.id.linearLayoutMergeEbookdroid);
		frameLayoutMode = (FrameLayout) this.findViewById(R.id.frameLayoutMode);
		linearLayoutEbookdroidMode = (LinearLayout) this.findViewById(R.id.linearLayoutEbookdroidMode);
		linearLayoutMergeMode = (LinearLayout) this.findViewById(R.id.linearLayoutMergeMode);
		
		boolean isMupdfDebug = false;
		if (this.getIntent() != null) {
			Intent intent = this.getIntent();
			isMupdfDebug = intent.getBooleanExtra(EXTRA_MUPDF_DEBUG, false);
		}
		if (!isMupdfDebug) {
			frameLayoutMode.setVisibility(View.GONE);
			linearLayoutEbookdroidMode.setVisibility(View.GONE);
			linearLayoutMergeMode.setVisibility(View.GONE);
		} else {
			frameLayoutMode.setVisibility(View.VISIBLE);
			linearLayoutEbookdroidMode.setVisibility(View.VISIBLE);
			linearLayoutMergeMode.setVisibility(View.VISIBLE);
		}
		
		if (!USE_HORIZ) {
			//纵向查看模式
			linearLayoutMergeMupdf.setVisibility(View.INVISIBLE);
			linearLayoutMergeEbookdroid.setVisibility(View.VISIBLE);
			linearLayoutMergeEbookdroid.bringToFront();
		} else {
			//横向批注模式
			linearLayoutMergeMupdf.setVisibility(View.VISIBLE);
			linearLayoutMergeEbookdroid.setVisibility(View.INVISIBLE);
			linearLayoutMergeMupdf.bringToFront();	
			helper.enableSinglePageMode(true, 0);
		}
		Button buttonMergeEbookdroid = (Button) this.findViewById(R.id.buttonMergeEbookdroid);
		buttonMergeEbookdroid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				linearLayoutMergeMupdf.setVisibility(View.INVISIBLE);
				linearLayoutMergeEbookdroid.setVisibility(View.VISIBLE);
				linearLayoutMergeEbookdroid.bringToFront();
			}
		});
		Button buttonMergeMupdf = (Button) this.findViewById(R.id.buttonMergeMupdf);
		buttonMergeMupdf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				linearLayoutMergeMupdf.setVisibility(View.VISIBLE);
				linearLayoutMergeEbookdroid.setVisibility(View.INVISIBLE);
				linearLayoutMergeMupdf.bringToFront();
			}
		});
		Button buttonMergeAnnot = (Button) this.findViewById(R.id.buttonMergeAnnot);
		buttonMergeAnnot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (linearLayoutMergeMupdf.getVisibility() == View.INVISIBLE) {
					toggleAnnotMode(true);
				} else {
					toggleAnnotMode(false);
				}
			}
		});
		loadPdf(this, savedInstanceState);
	}
	
	//加载pdf
	private AsyncTask<Void, Void, Void> mLoadPdfTask;
	private final static class ThreadPerTaskExecutor implements Executor {
		public void execute(Runnable r) {
			new Thread(r).start();
		}
	}
	public void loadPdf(final Activity _this, final Bundle savedInstanceState) {
		if (mLoadPdfTask != null) {
			mLoadPdfTask.cancel(true);
			mLoadPdfTask = null;
		}
		mLoadPdfTask = new AsyncTask<Void, Void, Void>() {
			private ProgressDialog progressDialog = null;
			private boolean isLoadCore = false;
			private String path_ = null;
			
			@Override
			public void onPreExecute() {
				progressDialog = new ProgressDialog(_this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(true);
				progressDialog.setTitle("加载文件");
				progressDialog.setMessage("正在加载文件");
				progressDialog.setCancelable(false);
				progressDialog.show();
				
				if (D) {
					Log.e(TAG, "onCreate 002==============>");
				}
				helperEbd.onCreatePost(savedInstanceState, _this);
				if (D) {
					Log.e(TAG, "onCreate 003==============>");
				}
				//FXIME:这个是耗时操作
				if (!helper.onCreatePre(savedInstanceState, _this)) {
					if (D) {
						Log.e(TAG, "onCreate 004-1==============>");
					}
					Intent intent = _this.getIntent();
					if (Intent.ACTION_VIEW.equals(intent.getAction())) {
						Uri uri = intent.getData();
						if (D) {
							Log.e(TAG, "URI to open is: " + uri);
						}
						path_ = Uri.decode(uri.getEncodedPath());
						if (path_ == null) {
							path_ = uri.toString();
						}
					}
					isLoadCore = true;
				} else {
					if (D) {
						Log.e(TAG, "onCreate 004-2==============>");
					}
					core_ = helper.core; //from helper.onCreatePre
					if (D) {
						Log.e(TAG, "002 this.core_ = " + core_.hashCode());
					}
				}
			}

			@Override
			protected Void doInBackground(Void... params) {
				if (isLoadCore) {
					try {
						core_ = new MuPDFCore(path_);
						if (D) {
							Log.e(TAG, "001 this.core_ = " + core_.hashCode());
						}
					} catch (Exception e) {
						e.printStackTrace();
					} catch (java.lang.OutOfMemoryError e) {
						e.printStackTrace();
					}
					if (core_ != null) {
						helper.openFile(path_, core_); //FIXME:最好能移动到里面
					}
				}
				if (core_ != null && core_.needsPassword()) {
					//需要密码
					core_ = null;
				}
				if (core_ != null && core_.countPages() == 0) {
					core_ = null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					try {
						progressDialog.dismiss();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					progressDialog = null;
				}
				if (D) {
					Log.e(TAG, "onCreate 005==============>");
				}
				if (core_ == null) {
					if (D) {
						Log.e(TAG, "onCreate 006-1==============>");
					}
					AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(_this);
					AlertDialog alert = mAlertBuilder.create();
					alert.setTitle("无法打开文档");
					alert.setButton(AlertDialog.BUTTON_POSITIVE, "关闭", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					alert.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
					alert.show();
					return;
				} else {
					if (D) {
						Log.e(TAG, "onCreate 006-2==============>");
					}
					helper.onCreate(savedInstanceState, _this, core_);
				}
				helperEbd.onPostCreatePost(_this, core_);
			}
		};
		mLoadPdfTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	private void toggleAnnotMode(boolean isOpen) {
		if (isOpen) {
			linearLayoutMergeMupdf.setVisibility(View.VISIBLE);
			linearLayoutMergeEbookdroid.setVisibility(View.INVISIBLE);
			linearLayoutMergeMupdf.bringToFront();
			int page = helperEbd.getCurrentPagePosition();
			if (D) {
				Log.e(TAG, "buttonMergeAnnot page:" + page);
			}
			//跳到横向阅读器的当前页
			helper.enableSinglePageMode(true, page);
		} else {
			if (linearLayoutMergeMupdf.getVisibility() == View.VISIBLE) {
				Runnable action = new Runnable() {
					@Override
					public void run() {
						//FIXME:这个是耗时操作
						if (core_ != null) {
							try {
								core_.reload();
							} catch (Exception e) {
								e.printStackTrace();
								//FIXME:这里应该是弹出对话框
								Toast.makeText(MuPDFActivity.this, 
									"重新加载失败", Toast.LENGTH_SHORT).show();
							}
						}
						linearLayoutMergeMupdf.setVisibility(View.INVISIBLE);
						linearLayoutMergeEbookdroid.setVisibility(View.VISIBLE);
						linearLayoutMergeEbookdroid.bringToFront();	
						helperEbd.getController(MuPDFActivity.this)
							.toggleCropPages();
						onChangeMergeMode.onChangeMergeMode(MERGE_MODE_READER);
					}
				};
				helper.saveAnnot(MuPDFActivity.this, action);
			}
		}
	}

//    @Override
//    protected void onPostCreate(final Bundle savedInstanceState) {
//        helperEbd.onPostCreatePre(this);
//        super.onPostCreate(savedInstanceState);
//        helperEbd.onPostCreatePost(this, core_);
//    }
	
	@Override
    protected void onResume() {
        helperEbd.onResumePre(this);
        super.onResume();
        helperEbd.onResumePost(this);
    }
	
    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        helperEbd.onWindowFocusChanged(hasFocus, this);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		helper.onActivityResult(requestCode, resultCode, data);
		if (helperEbd.onActivityResult(requestCode, resultCode, data, this)) {
			return;
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
        if (MuPDFActivityHelper.IS_SAVE_STATE) {
			SessionData data = new SessionData();
	        data.core = helper.onRetainNonConfigurationInstance();
	        data.controller = helperEbd.getController(this);
			return data;
        } else {
        	return super.onRetainNonConfigurationInstance();
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		helper.onSaveInstanceState(outState, this);
	}

	@Override
	protected void onPause() {
		helperEbd.onPausePre(this);
		super.onPause();
		helperEbd.onPausePost(this);
		helper.onPause(this);
	}

	@Override
	public void onDestroy() {
		helper.onDestroy(this);
		helperEbd.onDestroyPre(this);
        super.onDestroy();
        helperEbd.onDestroyPost(this);
    }
    
	@Override
	public void onBackPressed() {
    	if (this.mergeMode == MERGE_MODE_READER) {
    		onBackPressedNormal();
    	} else if (this.mergeMode == MERGE_MODE_ANNOT) {
    		toggleAnnotMode(false);
    		//onChangeMergeMode.onChangeMergeMode(MERGE_MODE_READER);
    	} else if (this.mergeMode == MERGE_MODE_ANNOT_PEN) {
    		helper.onButtonAnnotOK(this);
    	} else if (this.mergeMode == MERGE_MODE_ANNOT_SELECT) {
    		helper.onButtonSelectCancel();
    	}
	}
	
	private void onBackPressedNormal() {
    	boolean result1 = helperEbd.onExit(this);
		boolean result2 = helper.onExit(this);
		if (!result1 && !result2) {
			super.onBackPressed();
		}
	}
	
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        helperEbd.onCreateContextMenu(menu, v, menuInfo, this);
    }
    
    @Override
    public void onPanelClosed(final int featureId, final Menu menu) {
        helperEbd.onPanelClosedPre(featureId, menu);
    	super.onPanelClosed(featureId, menu);
    	helperEbd.onPanelClosedPost(featureId, menu, this);
    }
    
    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        helperEbd.onOptionsMenuClosed(menu, this);
    }
    
    @Override
    public final boolean dispatchKeyEvent(final KeyEvent event) {
    	if (helperEbd.dispatchKeyEvent(event, this)) {
    		return true;
    	} else {
    		return super.dispatchKeyEvent(event);
    	}
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        helperEbd.onLowMemory();
    }
    
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
    	if (helperEbd.onContextItemSelected(item, this)) {
    		return true;
    	} else {
    		return super.onContextItemSelected(item);
        }
    }
    
	private void setupActionBar() {
		actionBar.setIcon(R.drawable.icon_detail);
		actionBar.setTitle(MuPDFActivity.TITLE_OPEN_PDF);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
    @Override
    public boolean onOptionsItemSelected(
    	com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	//finish();
        	onBackPressed();
        	return true;
        	
        case R.id.ebookdroid__gotopage:
        	this.helperEbd.getController(this).showGotoDialog();
        	return true;
        	
        case R.id.ebookdroid__annotation:
        	toggleAnnotMode(true);
        	onChangeMergeMode.onChangeMergeMode(MERGE_MODE_ANNOT);
        	return true;
        	
        	
        case R.id.ebookdroid__annotation_back:
        	toggleAnnotMode(false);
        	//onChangeMergeMode.onChangeMergeMode(MERGE_MODE_READER);
        	break;
        	
        case R.id.ebookdroid__annotation_pen:
        	helper.onButtonPen();
        	break;
        	
        case R.id.ebookdroid__annotation_sel:
        	helper.onButtonSelect();
        	break;
        	
        case R.id.ebookdroid__annotation_pen_cancel:
        	helper.onButtonAnnotCancel();
        	break;
        	
        case R.id.ebookdroid__annotation_pen_ok:
        	helper.onButtonAnnotOK(this);
        	break;
        	
        case R.id.ebookdroid__annotation_sel_cancel:
        	helper.onButtonSelectCancel();
        	break;
        
        case R.id.ebookdroid__annotation_sel_ok:
        	helper.onButtonSelectOK(this);
        	break;	
        }
        return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(
    	final com.actionbarsherlock.view.Menu menu) {
    	
    	menu.clear();
    	if (mergeMode == MERGE_MODE_READER) {
	    	menu.add(0, R.id.ebookdroid__gotopage, 0, "转至页面")
		    		.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
		    		/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
	    	menu.add(0, R.id.ebookdroid__annotation, 0, "批注功能")
				.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
					/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
    	} else if (mergeMode == MERGE_MODE_ANNOT) {
			if (BUTTON_VERBOSE) {
//		    	menu.add(0, R.id.ebookdroid__annotation_back, 0, "返回")
//					.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
//			    		/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
			}
			menu.add(0, R.id.ebookdroid__annotation_sel, 0, "删除批注")
				.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
					/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
			menu.add(0, R.id.ebookdroid__annotation_pen, 0, "开始批注")
				.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
					/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
    	} else if (mergeMode == MERGE_MODE_ANNOT_PEN) {
			menu.add(0, R.id.ebookdroid__annotation_pen_cancel, 0, "取消批注")
				.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
					/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
			if (BUTTON_VERBOSE) {
		    	menu.add(0, R.id.ebookdroid__annotation_pen_ok, 0, "保存批注")
					.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
			    		/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);    
			}
    	} else if (mergeMode == MERGE_MODE_ANNOT_SELECT) {
    		if (BUTTON_VERBOSE) {
				menu.add(0, R.id.ebookdroid__annotation_sel_cancel, 0, "取消选择")
					.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
						/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
    		}
	    	menu.add(0, R.id.ebookdroid__annotation_sel_ok, 0, "删除选择")
				.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS 
		    		/*| MenuItem.SHOW_AS_ACTION_WITH_TEXT*/);
		}
    	return super.onCreateOptionsMenu(menu);
    }
	
	//判断是否需要上传文件
	private void onExit2() {
		Intent myIntent = this.getIntent();
    	if (myIntent != null) {
    		String pdffilepath = null;
        	pdffilepath = myIntent.getStringExtra("pdffilepath");
    		if (myIntent.getBooleanExtra(ViewerActivityHelper.INTENT_KEY_ISSAVE, false) && 
    			pdffilepath != null) {
    			this.onUploadProcess(pdffilepath);
    		}
    	}
	}

}
