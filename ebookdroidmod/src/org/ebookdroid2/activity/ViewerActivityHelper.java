package org.ebookdroid2.activity;

import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.AppSettings.DocumentViewType;
import org.ebookdroid2.manager.AppSettings.ToastPosition;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.Bookmark;
import org.ebookdroid2.manager.CacheManager;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.manager.SettingsManager;
import org.ebookdroid2.model.ExtraWrapper;
import org.ebookdroid2.view.IView;
import org.ebookdroid2.view.ManualCropView;
import org.ebookdroid2.view.PageViewZoomControls;
import org.ebookdroid2.view.SearchControls;
import org.mupdfdemo2.activity.OnChangeMergeModeListener;
import org.mupdfdemo2.activity.OnExitUploadListener;
import org.mupdfdemo2.activity.OnTitleChangeListener;
import org.mupdfdemo2.model.SessionData;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.iteye.weimingtom.ebdmod.R;

public class ViewerActivityHelper {
	private final static boolean D = false;
	private final static String TAG = "ViewerActivityHelper";
	
	public static final boolean USE_OPTIONS_MENU = false; //FIXME:显示选项菜单
	
	public final static String INTENT_KEY_FILETITLE = "INTENT_KEY_FILETITLE";
	public static final String INTENT_KEY_GOTOPAGE = "INTENT_KEY_GOTOPAGE";
	public static final String INTENT_KEY_ISSAVE = "INTENT_KEY_ISSAVE"; //是否从批注模式中返回过来
    
	public static final String MENU_ITEM_SOURCE = "source";
    
    public static final String ACTIVITY_RESULT_DATA = "activityResultData";
    public static final String ACTIVITY_RESULT_CODE = "activityResultCode";
    public static final String ACTIVITY_RESULT_ACTION_ID = "activityResultActionId";
    private static final int REQUEST_PDF = 2001;
    
	public static final DisplayMetrics DM = new DisplayMetrics();
    public static boolean sTitleVisible = false; //FXIME:这个外部用到
    private static boolean sFullscreen;
    
    private AlertDialog.Builder mAlertBuilder;
    public IView view; //FIXME:这个外部用到
    private Toast pageNumberToast;
    private Toast zoomToast;
    private PageViewZoomControls zoomControls;
    private SearchControls searchControls;
    private FrameLayout frameLayoutEbookdroidMain;
    private boolean menuClosedCalled;
    private ManualCropView cropControls;
    private ViewerActivityController controller;
    //新增
    public OnChangeMergeModeListener onChangeMergeMode;
    public OnTitleChangeListener onTitleChange;
    public OnExitUploadListener onExitUpload;
    
    private boolean isOffline = false; //是否离线模式（是否询问上传），默认是非离线 
    
    public ViewerActivityHelper() {
    	
    }
    
    protected ViewerActivityController createController(final Activity _this) {
        return new ViewerActivityController(_this, this);
    }
    
    public void onCreatePre(final Bundle savedInstanceState, final Activity _this) {
        if (D) {
        	Log.e(TAG, "onCreate(): " + _this.getIntent());
        }
        onApplicationInit(_this);
        restoreController(_this);
        getController(_this).beforeCreate(_this);
    }
    
    public void onCreatePost(final Bundle savedInstanceState, final Activity _this) {
    	_this.getWindowManager().getDefaultDisplay().getMetrics(DM);
        if (D) {
        	Log.e(TAG, "XDPI=" + DM.xdpi + ", YDPI=" + DM.ydpi);
        }
        //菜单
        Button buttonEbookdroidContextMenu = (Button) _this.findViewById(R.id.buttonEbookdroidContextMenu);
        buttonEbookdroidContextMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				view.getView().showContextMenu();
			}
        });
        
        //大纲
        Button buttonEbookdroidOutline = (Button) _this.findViewById(R.id.buttonEbookdroidOutline);
        buttonEbookdroidOutline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).showOutline();
			}
        });
        
        //关闭
        Button buttonEbookdroidClose = (Button) _this.findViewById(R.id.buttonEbookdroidClose);
        buttonEbookdroidClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).closeActivity();
			}
        });
        
        //缩放
        Button buttonEbookdroidZoom = (Button) _this.findViewById(R.id.buttonEbookdroidZoom);
        buttonEbookdroidZoom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).toggleControls(
					getZoomControls(_this),  null);
			}
        });
        
        
        //---------------------------------------------
        
        //搜索
        Button buttonEbookdroidSearch = (Button) _this.findViewById(R.id.buttonEbookdroidSearch);
        buttonEbookdroidSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).toggleControls(
					getSearchControls(_this),  null);
			}
        });
        
        //添加书签
        Button buttonEbookdroidBookmark = (Button) _this.findViewById(R.id.buttonEbookdroidBookmark);
        buttonEbookdroidBookmark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).showBookmarkDialog();
			}
        });
        
        //转至页面
        Button buttonEbookdroidGoto = (Button) _this.findViewById(R.id.buttonEbookdroidGoto);
        buttonEbookdroidGoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (D) {
					Log.e(TAG, "buttonEbookdroidGoto");
				}
				getController(_this).showGotoDialog();
			}
        });
        
        //手工剪裁
        Button buttonEbookdroidCrop = (Button) _this.findViewById(R.id.buttonEbookdroidCrop);
        buttonEbookdroidCrop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getController(_this).toggleCropPages();
			}
        });
        //重新加载
        Button buttonEbookdroidReload = (Button) _this.findViewById(R.id.buttonEbookdroidReload);
        buttonEbookdroidReload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				controller.getDocumentController().redrawView();
				getController(_this).toggleCropPages();
				Toast.makeText(_this, "重新加载", Toast.LENGTH_SHORT).show();
			}
        });
        
        //-----------------------
        //mainmenu_outline//大纲
        //mainmenu_search//搜索
	    //mainmenu_bookmark//添加书签
        //mainmenu_goto_page //转到页面
        //-----------------------
        // mainmenu_fullscreen // 全屏幕
        // mainmenu_showtitle // 显示标题
        // mainmenu_nightmode // 日间/夜间
        // mainmenu_splitpages // 拆分页
        // mainmenu_croppages //裁剪页面
        // mainmenu_crop //手工剪裁
        // mainmenu_thumbnail // 设置缩略图
        // mainmenu_zoom // 缩放
        //-----------------------
        
        frameLayoutEbookdroidMain = (FrameLayout) _this.findViewById(R.id.framelayoutEbookdroidMain);

        view = DocumentViewType.create(AppSettings.current().viewType, getController(_this));
        if (D) {
        	Log.e(TAG, "viewer view=" + view);
        }
        _this.registerForContextMenu(view.getView());

        fillInParent(frameLayoutEbookdroidMain, view.getView());
        
        frameLayoutEbookdroidMain.addView(view.getView());
        frameLayoutEbookdroidMain.addView(getZoomControls(_this));
        frameLayoutEbookdroidMain.addView(getManualCropControls(_this));
        frameLayoutEbookdroidMain.addView(getSearchControls(_this));

        getController(_this).afterCreate();
        mAlertBuilder = new AlertDialog.Builder(_this);
    }
    
    public static View fillInParent(final View parent, final View view) {
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return view;
    }
    
    public void onResumePre(final Activity _this) {
    	if (D) {
        	Log.e(TAG, "onResume()");
        }
        getController(_this).beforeResume();
    }
    
    public void onResumePost(final Activity _this) {
    	getController(_this).afterResume();
    }
    
    public void onPausePre(final Activity _this) {
        if (D) {
        	Log.e(TAG, "onPause(): " + _this.isFinishing());
        }
        getController(_this).beforePause();
    }
    
    public void onPausePost(final Activity _this) {
    	 getController(_this).afterPause();
    }
    
    public void onWindowFocusChanged(final boolean hasFocus, final Activity _this) {
        if (hasFocus && this.view != null) {
        	setFullScreenMode(_this, view.getView(), AppSettings.current().fullScreen);
        }
    }
    
    public void onDestroyPre(final Activity _this) {
        final boolean finishing = _this.isFinishing();
        if (D) {
        	Log.e(TAG, "onDestroy(): " + finishing);
        }
        getController(_this).beforeDestroy(finishing);
    }
    
    public void onDestroyPost(final Activity _this) {
    	final boolean finishing = _this.isFinishing();
    	getController(_this).afterDestroy(finishing);
        onActivityClose(finishing);
    }
    
    public void onPostCreatePre(final Activity _this) {
    	getController(_this).beforePostCreate();
    }
    
    public void onPostCreatePost(final Activity _this, MuPDFCore core) {
    	getController(_this).afterPostCreate(core);
    }
    
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo, final Activity _this) {
        menu.clear();
        menu.setHeaderTitle("菜单");
        final MenuInflater inflater = _this.getMenuInflater();
        inflater.inflate(R.menu.ebookdroid_mainmenu_context, menu);
        updateMenuItems(menu, _this);
    }
    
    public void onOptionsMenuClosed(final Menu menu, final Activity _this) {
        menuClosedCalled = true;
        onMenuClosed(_this);
        view.changeLayoutLock(false);
    }
    
    public void onPanelClosedPre(final int featureId, final Menu menu) {
    	menuClosedCalled = false;
    }
    
    public void onPanelClosedPost(final int featureId, final Menu menu, final Activity _this) {
        if (!menuClosedCalled) {
        	_this.onOptionsMenuClosed(menu);
        }
    }
    
    public boolean dispatchKeyEvent(final KeyEvent event, final Activity _this) {
    	if (USE_OPTIONS_MENU) {
	        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
	            if (!hasNormalMenu()) {
	                //getController().getOrCreateAction(R.id.actions_openOptionsMenu).run();
	                getController(_this).openOptionsMenu();
	            	return true;
	            }
	        }
    	}
    	if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
    		_this.onBackPressed();
    		return true;
    	}
        if (getController(_this).dispatchKeyEvent(event)) {
            return true;
        }
        return false;
    }

    public void setFullScreenMode(final Activity activity, final View view, final boolean fullScreen) {
    	sFullscreen = fullScreen;
    	final Window w = activity.getWindow();
    	if (fullScreen) {
    		w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	} else {
    		w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
	}
    
    protected IView createView(final Activity _this) {
        return DocumentViewType.create(AppSettings.current().viewType, getController(_this));
    }

    public void currentPageChanged(final String pageText, final String bookTitle, final Activity _this, final String pageText1, final String pageText2) {
        if (pageText == null || pageText.length() == 0) {
            return;
        }
        final AppSettings app = AppSettings.current();
        if (isTitleVisible(_this) && app.pageInTitle) {
            Intent intent = _this.getIntent();
        	String fileTitle = null;
        	if (intent != null) {
        		fileTitle = intent.getStringExtra(INTENT_KEY_FILETITLE);
        	}
        	if (fileTitle != null && fileTitle.length() > 0) {
        		_this.setTitle("(" + pageText + ")" + fileTitle);
        		if (onTitleChange != null) {
        			//onTitleChange.OnTitleChange("(" + pageText + ")" + fileTitle);
        			onTitleChange.OnTitleChange("当前第" + pageText1 + "页/共" + pageText2 + "页");
        		}
        	} else {
        		_this.setTitle("" + pageText + " ");
        		if (onTitleChange != null) {
        			//onTitleChange.OnTitleChange("" + pageText + " ");
        			onTitleChange.OnTitleChange("当前第" + pageText1 + "页/共" + pageText2 + "页");
        		}
        	}
        	return;
        }

        if (app.pageNumberToastPosition == ToastPosition.Invisible) {
            return;
        }
        if (pageNumberToast != null) {
            pageNumberToast.setText(pageText);
        } else {
            pageNumberToast = Toast.makeText(_this, pageText, Toast.LENGTH_SHORT);
        }
        pageNumberToast.setGravity(app.pageNumberToastPosition.position, 0, 0);
        pageNumberToast.show();
    }

    public boolean isTitleVisible(final Activity activity) {
    	return sTitleVisible;
    }
    
    public void zoomChanged(final float zoom, final Activity _this) {
        if (getZoomControls(_this).isShown()) {
            return;
        }

        final AppSettings app = AppSettings.current();

        if (app.zoomToastPosition == ToastPosition.Invisible) {
            return;
        }

        final String zoomText = String.format("%.2f", zoom) + "x";

        if (zoomToast != null) {
            zoomToast.setText(zoomText);
        } else {
            zoomToast = Toast.makeText(_this, zoomText, Toast.LENGTH_SHORT);
        }

        zoomToast.setGravity(app.zoomToastPosition.position, 0, 0);
        zoomToast.show();
    }
    
    public PageViewZoomControls getZoomControls(Activity _this) {
        if (zoomControls == null) {
            zoomControls = new PageViewZoomControls(_this, getController(_this).getZoomModel());
            zoomControls.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
        return zoomControls;
    }

    public SearchControls getSearchControls(final Activity _this) {
        if (searchControls == null) {
            searchControls = new SearchControls(_this, this);
        }
        return searchControls;
    }

    public ManualCropView getManualCropControls(final Activity _this) {
        if (cropControls == null) {
            cropControls = new ManualCropView(getController(_this));
        }
        return cropControls;
    }
    
    protected boolean hasNormalMenu() {
        return true;
    }

    //FIXME:去掉这个方法
    protected void updateMenuItems(final Menu menu, final Activity _this) {
        final AppSettings as = AppSettings.current();

        setMenuItemChecked(menu, as.fullScreen, R.id.mainmenu_fullscreen);
        setMenuItemVisible(menu, false, R.id.mainmenu_showtitle);
        setMenuItemChecked(menu, getZoomControls(_this).getVisibility() == View.VISIBLE, R.id.mainmenu_zoom);

        final BookSettings bs = getController(_this).getBookSettings();
        if (bs == null) {
            return;
        }

        //FIXME:
        setMenuItemChecked(menu, bs.nightMode, R.id.mainmenu_nightmode);
        setMenuItemChecked(menu, bs.cropPages, R.id.mainmenu_croppages);
        
        final MenuItem navMenu = menu.findItem(R.id.mainmenu_nav_menu);
        if (navMenu != null) {
            final SubMenu subMenu = navMenu.getSubMenu();
            subMenu.removeGroup(R.id.actions_goToBookmarkGroup);
            if (AppSettings.current().showBookmarksInMenu && 
            	bs.bookmarks != null && 
            	!bs.bookmarks.isEmpty()) {
                for (final Bookmark b : bs.bookmarks) {
                    addBookmarkMenuItem(subMenu, b);
                }
            }
        }

    }

    protected void addBookmarkMenuItem(final Menu menu, final Bookmark b) {
        final MenuItem bmi = menu.add(R.id.actions_goToBookmarkGroup, R.id.actions_goToBookmark, Menu.NONE, b.name);
        setMenuItemExtra(bmi, "bookmark", b);
    }
    
    //FIXME:去掉这个方法
    public void onMenuClosed(final Activity activity) {
    	if (sFullscreen) {
    		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    }
    
    //FIXME:可能导致空白页！！！
    public void onLowMemory() {
        BitmapManager.clear("on Low Memory: ");
    }
    
    public void showToastText(final Activity _this, final int duration, final String resId, final Object... args) {
        Toast.makeText(_this.getApplicationContext(), String.format(resId, args), duration).show();
    }
    
    //FIXME:这里的处理过时了，可以删掉
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, final Activity _this) {
    	if (requestCode == REQUEST_PDF) {
			Intent intent = new Intent(_this.getIntent());
			if (intent != null) {
				
			}
		}
        if (resultCode == Activity.RESULT_CANCELED) {
            return true;
        }
        if (data != null) {
            final int actionId = data.getIntExtra(ACTIVITY_RESULT_ACTION_ID, 0);
            if (actionId != 0) {
            	
            }
        }
        return false;
    }
    
    //FIXME:这里异步，不好合并到别的代码
    //FIXME:这里的代码最好全部删除
	public boolean onExit(final Activity _this) {
		Intent myIntent = _this.getIntent();
    	boolean isSave = false;
		if (myIntent != null) {
			isSave = myIntent.getBooleanExtra(INTENT_KEY_ISSAVE, false); //从批注模式中返回
    	}
		//FIXME:离线模式不需要上传
		if (this.isOffline) {
			isSave = false;
		}
		if (isSave) {
			//询问是否上传到服务器
			AlertDialog alert = mAlertBuilder.create();
			//alert.setTitle("MuPDF");
			alert.setTitle("上传批注");
			alert.setMessage("是否上传文件到服务器");
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					if (which == AlertDialog.BUTTON_POSITIVE) {
						if (onExitUpload != null) {
							onExitUpload.onExitUpload();
						}
						_this.finish(); //FIXME:这里跳过了return后的操作，假设已经做完了
					} else if (which == AlertDialog.BUTTON_NEGATIVE) {
						_this.finish(); //FIXME:这里跳过了return后的操作，假设已经做完了
					}
				}
			};
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "上传并返回", listener);
			alert.setButton(AlertDialog.BUTTON_NEUTRAL, "取消", listener);
			alert.setButton(AlertDialog.BUTTON_NEGATIVE, "不保存直接返回", listener);
			alert.show(); 
			return true;
		} else {
			//_this.finish();
			return false;
		}
	}
    
    //@Override
    public void onTerminate() {
        SettingsManager.onTerminate();
    }
    
    public void onActivityClose(final boolean finishing) {
        if (finishing && !SettingsManager.hasOpenedBooks()) {
            onTerminate();
            if (D) {
            	Log.e(TAG, "Application finished");
            }
            //FIXME:这里可以执行杀进程，System.exit(0);
        }
    }
    
    private void onApplicationInit(final Activity _this) {
        SettingsManager.init(_this.getApplicationContext());
        CacheManager.init(_this.getApplicationContext());
    }
    
    //FIXME:这里的代码移动到外边，或通过参数来返回
    public final ViewerActivityController restoreController(final Activity _this) {
        final Object last = _this.getLastNonConfigurationInstance();
        if (last != null) {
	        if (last instanceof SessionData) {
	        	this.controller = ((SessionData)last).controller;
	        	return controller;
	        }
        } else {
        	this.controller = null;
        	return controller;
        }
        return null;
    }
    
    public final ViewerActivityController getController(final Activity _this) {
        if (controller == null) {
            controller = createController(_this);
        }
        return controller;
    }
    
    //FIXME:删掉菜单响应
    public boolean onContextItemSelected(final MenuItem item, final Activity _this) {
    	switch (item.getItemId()) {
        case R.id.mainmenu_zoom:
        	this.getController(_this).toggleControls(this.getZoomControls(_this),  null);
        	return true;
        	
        case R.id.mainmenu_crop:
        	this.getController(_this).toggleControls(this.getManualCropControls(_this),  DocumentViewMode.SINGLE_PAGE);
        	return true;
    	
        case R.id.actions_toggleTouchManagerView: //FIXME:
//        	this.getController().toggleControls(this.getTouchView(),  null);
        	return true;
        	
        case R.id.mainmenu_search:
        	this.getController(_this).toggleControls(this.getSearchControls(_this),  null);
        	return true;
        }
    	
    	//原来actions.xml的菜单，可能暂时还没有入口，先放在这里，
    	//以后再做一个入口菜单，否则无法被调用
    	switch (item.getItemId()) {
    	case R.id.actions_quickZoom:
	    	{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.quickZoom();
			}
    		return true;
    		
    	case R.id.actions_zoomToColumn:
	    	{
//	    		AbstractViewController ctrl = (AbstractViewController)(this.getController().getDocumentController());
//	        	ctrl.zoomToColumn(0, 0); //FIXME:找不到参数在哪里？
			}
    		return true;
    		
    	case R.id.actions_leftTopCorner:
	    	{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.scrollToCorner(0, 0);
			}
			return true;
			
    	case R.id.actions_leftBottomCorner:
	    	{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.scrollToCorner(0, 1);
			}
			return true;
			
    	case R.id.actions_rightTopCorner:
			{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.scrollToCorner(1, 0);
			}
			return true;
			
    	case R.id.actions_rightBottomCorner:
			{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.scrollToCorner(1, 1);
			}
    		return true;
    		
    	case R.id.actions_verticalConfigScrollUp:
    		{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.verticalConfigScroll(-1);
    		}
    		return true;

    	case R.id.actions_verticalConfigScrollDown:
    		{
	    		AbstractViewController ctrl = (AbstractViewController)(this.getController(_this).getDocumentController());
	        	ctrl.verticalConfigScroll(+1);
    		}
    		return true;
        }
    	
        if (onMenuItemSelected(item, _this)) {
            return true;
        }
        return false;
    }
    
    //FIXME:删掉菜单响应
    protected boolean onMenuItemSelected(final MenuItem item, final Activity _this) {
    	//FIXME:
        final int actionId = item.getItemId();
    	//FIXME:
    	Log.e(TAG, ">>>>>>>>>>>>>>actionId: " + actionId + " not handled!!!!");
    	switch (actionId) {
        case R.id.mainmenu_outline:
        	this.getController(_this).showOutline();
        	return true;
        
        case R.id.mainmenu_goto_page:
        	this.getController(_this).showGotoDialog();
        	return true;
        	
        case R.id.mainmenu_booksettings:
        	this.getController(_this).showBookSettings();
        	return true;
        
        case R.id.mainmenu_settings:
        	this.getController(_this).showAppSettings();
        	return true;
       
        case R.id.mainmenu_fullscreen:
        	this.getController(_this).toggleFullScreen();
        	return true;

        case R.id.mainmenu_showtitle:
        	this.getController(_this).toggleTitleVisibility();
        	return true;
        
        case R.id.mainmenu_nightmode:
        	this.getController(_this).toggleNightMode();
        	return true;
        	
        case R.id.mainmenu_splitpages:
        	this.getController(_this).toggleSplitPages();
        	return true;

        case R.id.mainmenu_croppages:
        	this.getController(_this).toggleCropPages();
        	return true;
        	
        case R.id.mainmenu_bookmark:
        	this.getController(_this).showBookmarkDialog();
        	return true;
        	
        case R.id.mainmenu_zoom:
        	this.getController(_this).toggleControls(this.getZoomControls(_this), null);
        	return true;
        	
        case R.id.mainmenu_search:
        	this.getController(_this).toggleControls(this.getSearchControls(_this), null);
        	return true;
        	
        case R.id.mainmenu_crop:
        	this.getController(_this).toggleControls(this.getManualCropControls(_this), DocumentViewMode.SINGLE_PAGE);
        	return true;
        
        case R.id.mainmenu_close:
        	this.getController(_this).closeActivity();
        	return true;
        	
        case R.id.mainmenu_about:
        	this.showAbout();
        	return true;
        }
        return false;
    }

    //FIXME:删掉这里
    protected void setMenuItemExtra(final MenuItem item, final String name, final Object data) {
        Intent intent = item.getIntent();
        if (intent == null) {
            intent = new Intent();
            item.setIntent(intent);
        }
        intent.putExtra(name, new ExtraWrapper(data));
    }

    //FIXME:删掉这里
    protected void setMenuItemVisible(final Menu menu, final boolean visible, final int viewId) {
        final MenuItem v = menu.findItem(viewId);
        if (v != null) {
            v.setVisible(visible);
        }
    }
    
    //FIXME:删掉这里
    protected void setMenuItemChecked(final Menu menu, final boolean checked, final int viewId) {
        final MenuItem v = menu.findItem(viewId);
        if (v != null) {
            v.setChecked(checked);
        }
    }

    public final void onButtonClick(final View view) {
        final int actionId = view.getId();
        Log.e(TAG, ">>>>>>>>>>>>>>onButtonClick: " + actionId + " not handled!!!!");
    }

    //R.id.mainmenu_about)
    public void showAbout() {
    	
    }
    
    //新增
    public int getCurrentPagePosition() { 
    	if (this.controller != null) {
	    	return this.controller.getCurrentPagePosition();
    	} else {
    		return -1;
    	}
    }
    
    public void setIsOffline(boolean isOffline) {
    	this.isOffline = isOffline;
    }
}
