package org.ebookdroid2.activity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.ebookdroid2.codec.OutlineLink;
import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.core.ViewContollerStub;
import org.ebookdroid2.dialog.GoToPageDialog;
import org.ebookdroid2.dialog.OutlineDialog;
import org.ebookdroid2.listener.CurrentPageListener;
import org.ebookdroid2.listener.DecodingProgressListener;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.Bookmark;
import org.ebookdroid2.manager.DocumentViewMode;
import org.ebookdroid2.manager.IBookSettingsChangeListener;
import org.ebookdroid2.manager.SettingsManager;
import org.ebookdroid2.model.DecodingProgressModel;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.model.NavigationHistory;
import org.ebookdroid2.model.SearchModel;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.model.ZoomModel;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.page.PageIndex;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.task.BookLoadTask;
import org.ebookdroid2.task.SearchTask;
import org.ebookdroid2.view.IView;
import org.ebookdroid2.view.ManualCropView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.iteye.weimingtom.ebdmod.R;

/*
FIXME:请测试带密码的PDF文件

//activity.setRequestedOrientation(newSettings.rotation.getOrientation());
//setFullScreenMode(activity, activity.view.getView(), newSettings.fullScreen);
//setTitleVisible(activity, newSettings.showTitle, false);
//activity.view.getView().setKeepScreenOn(newSettings.keepScreenOn);
//PagesInMemory
//getDocumentController().updateMemorySettings();
*/
public class ViewerActivityController implements IActivityController,
        DecodingProgressListener, CurrentPageListener, IBookSettingsChangeListener {
	private final static boolean D = false;
	private final static String TAG = "ViewerActivityController";
	
    private static final AtomicLong SEQ = new AtomicLong();
    
    private final long id;
    private final AtomicReference<IViewController> ctrl = new AtomicReference<IViewController>(ViewContollerStub.STUB);
    private ZoomModel zoomModel;
    private DecodingProgressModel progressModel;
    public DocumentModel documentModel;
    public SearchModel searchModel;
    private String bookTitle;
    private int scheme;
    private int codecType;
    public final Intent intent; //FIXME:这里写得不好
    private int loadingCount = 0;
    private String m_fileName; //FIXME:这个变量需要去掉，用core代替
    private final NavigationHistory history;
    private String currentSearchPattern;
    public BookSettings bookSettings;
    public ViewerActivityHelper helper;
    
    protected Activity m_managedComponent;
    
    public ViewerActivityController(final Activity activity, final ViewerActivityHelper helper) {
    	this.helper = helper;
    	this.m_managedComponent = activity;
        this.id = SEQ.getAndIncrement();
        this.intent = activity.getIntent();
        SettingsManager.addBookSettingsChangeListener(this);
        this.history = new NavigationHistory(this);
    }

    public void beforeCreate(final Activity activity) {
        if (D) {
            Log.e(TAG, "beforeCreate(): ");
        }
        if (getManagedComponent() != activity) {
            setManagedComponent(activity);
        }
        final AppSettings newSettings = AppSettings.current();
        activity.setRequestedOrientation(newSettings.rotation.getOrientation());
        setTitleVisible(activity, newSettings.showTitle, true);
    }
    
    //设置标题栏显示
    public static void setTitleVisible(final Activity activity, final boolean visible, final boolean firstTime) {
        if (firstTime) {
            try {
                final Window window = activity.getWindow();
                if (!visible) {
                    window.requestFeature(Window.FEATURE_NO_TITLE);
                } else {
                    window.requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                    activity.setProgressBarIndeterminate(true);
                    activity.setProgressBarIndeterminateVisibility(true);
                    window.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, 1);
                }
                ViewerActivityHelper.sTitleVisible = visible;
            } catch (final Throwable th) {
                th.printStackTrace();
            	if (D) {
            		Log.e(TAG, "Error on requestFeature call: " + th.getMessage());
            	}
            }
        }
    }

    public void afterCreate() {
        if (D) {
        	Log.e(TAG, "afterCreate()");
        }
        final Activity activity = getManagedComponent();
        final AppSettings appSettings = AppSettings.current();
        setFullScreenMode(activity, this.helper.view.getView(), appSettings.fullScreen);
        if (++loadingCount == 1) {
            documentModel = ActivityControllerStub.DM_STUB;
            searchModel = new SearchModel(this);
            if (intent == null) {
            	showErrorDlg("错误:\n%s", intent);
                return;
            }
            final Uri data = intent.getData();
            if (data == null) {
            	showErrorDlg("数据丢失:\n%s", intent);
                return;
            }
            scheme = DocumentModel.getScheme(intent);
            if (scheme == DocumentModel.SCHEME_UNKNOWN) {
            	showErrorDlg("不支持非本地文件");
                return;
            }
            bookTitle = DocumentModel.getResourceName(activity.getContentResolver(), data);
            codecType = DocumentModel.getCodecByUri(bookTitle);
            if (codecType == DocumentModel.CODEC_UNKNOWN) {
                bookTitle = DocumentModel.getDefaultResourceName(data, "");
                codecType = DocumentModel.getCodecByUri(bookTitle);
            }
            if (codecType == DocumentModel.CODEC_UNKNOWN) {
                final String type = intent.getType();
                if (D) {
                	Log.e(TAG, "Book mime type: " + type);
                }
                if (type != null && type.length() > 0) {
                    codecType = DocumentModel.getCodecByMimeType(type);
                }
            }
            if (D) {
            	Log.e(TAG, "Book codec type: " + codecType);
            	Log.e(TAG, "Book title: " + bookTitle);
            }
            if (codecType == DocumentModel.CODEC_UNKNOWN) {
            	showErrorDlg("不支持的文件类型:\n%s", data);
                return;
            }
            documentModel = new DocumentModel(codecType);
            documentModel.addListener(ViewerActivityController.this);
            progressModel = new DecodingProgressModel();
            progressModel.addListener(ViewerActivityController.this);
            final Uri uri = data;
            m_fileName = retrieve(activity.getContentResolver(), uri);
            bookSettings = SettingsManager.create(id, m_fileName, false, intent);
            SettingsManager.applyBookSettingsChanges(null, bookSettings);
        }
    }
    
    public static String retrieve(final ContentResolver resolver, final Uri uri) {
        if (uri.getScheme().equals("file")) {
            return uri.getPath();
        }
        final Cursor cursor = resolver.query(uri, new String[] { "_data" }, null, null, null);
        if ((cursor != null) && cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        throw new RuntimeException("Can't retrieve path from uri: " + uri.toString());
    }
    
    public void beforePostCreate() {
        if (D) {
        	Log.e(TAG, "beforePostCreate()");
        }
    }

    public void afterPostCreate(MuPDFCore core) {
        if (D) {
        	Log.e(TAG, "afterPostCreate()");
        }
        setWindowTitle();
        if (loadingCount == 1 && 
        	documentModel != ActivityControllerStub.DM_STUB) {
            startDecoding(core);
        }
    }

    public void reload(MuPDFCore core) {
    	setWindowTitle();
        startDecoding(core);
    }
    
    public void startDecoding(MuPDFCore core) {
        this.helper.view.post(new BookLoadTask(this, core));
    }

    public void beforeResume() {
        if (D) {
        	Log.e(TAG, "beforeResume()");
        }
    }

    public void afterResume() {
        if (D) {
        	Log.e(TAG, "afterResume()");
        }
    }

    public void beforePause() {
        if (D) {
        	Log.e(TAG, "beforePause()");
        }
    }

    public void afterPause() {
        if (D) {
        	Log.e(TAG, "afterPause()");
        }
    }

    public void beforeDestroy(final boolean finishing) {
        if (D) {
        	Log.e(TAG, "beforeDestroy(): " + finishing);
        }
        if (finishing) {
        	//FIXME:这里可能崩溃view==null
        	if (this.helper.view != null) {
        		this.helper.view.onDestroy();
        	}
            if (documentModel != null) {
                documentModel.recycle();
            }
            SettingsManager.removeBookSettingsChangeListener(this);
            BitmapManager.clear("on finish");
        }
    }

    public void afterDestroy(final boolean finishing) {
        if (D) {
        	Log.e(TAG, "afterDestroy()");
        }
        getDocumentController().onDestroy();
    }

    public void showErrorDlg(final String msgId, final Object... args) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getManagedComponent());
        builder.setTitle("应用程序错误");
        builder.setMessage(String.format(msgId, args));
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				closeActivity();
			}
        });
        builder.show();
    }

    protected IViewController switchDocumentController(final BookSettings bs) {
        if (bs != null) {
            try {
                final IViewController newDc = DocumentViewMode.create(bs.viewMode, this, this.helper);
                if (newDc != null) {
                    final IViewController oldDc = ctrl.getAndSet(newDc);
                    getZoomModel().removeListener(oldDc);
                    getZoomModel().addListener(newDc);
                    return ctrl.get();
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            	if (D) {
                	Log.e(TAG, "Unexpected error: ", e);
                }
            }
        }
        return null;
    }

    @Override
    public void decodingProgressChanged(final int currentlyDecoding) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                	//FIXME:这里为PDF加载中的转圈
                    final Activity activity = getManagedComponent();
                    activity.setProgressBarIndeterminateVisibility(currentlyDecoding > 0);
                    activity.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                            currentlyDecoding == 0 ? 10000 : currentlyDecoding);
                } catch (final Throwable e) {
                	
                }
            }
        };
        getView().post(r);
    }

    @Override
    public void currentPageChanged(final PageIndex oldIndex, final PageIndex newIndex) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final int pageCount = documentModel.getPageCount();
                String pageText = "";
                String pageText1 = "";
                String pageText2 = "";
                if (pageCount > 0) {
                    final int offset = bookSettings != null ? bookSettings.firstPageOffset : 1;
                    if (offset == 1) {
                        pageText = (newIndex.viewIndex + 1) + "/" + pageCount;
                    } else {
                        pageText = offset + "/" + (newIndex.viewIndex + offset) + "/" + (pageCount - 1 + offset);
                    }
                    pageText1 = (newIndex.viewIndex + 1) + "";
                    pageText2 = (pageCount) + "";
                }
                ViewerActivityController.this.helper.currentPageChanged(pageText, bookTitle, getManagedComponent(), pageText1, pageText2);
                SettingsManager.currentPageChanged(bookSettings, oldIndex, newIndex);
            }
        };
        getView().post(r);
    }

    @Override
    public void runOnUiThread(final Runnable r) {
        final FutureTask<Object> task = new FutureTask<Object>(r, null);
        try {
            getActivity().runOnUiThread(task);
            task.get();
        } catch (final InterruptedException ex) {
            Thread.interrupted();
        } catch (final ExecutionException ex) {
            ex.printStackTrace();
        } catch (final Throwable th) {
            th.printStackTrace();
        }
    }

    //FIXME:这里设置标题栏，最好改成可以定制
    //这里可能需要修改为能够同步到ActionBar的标题
    public void setWindowTitle() {
    	bookTitle = cleanupTitle(bookTitle);
        final Activity activity = getManagedComponent();
        if (documentModel != null) {
        	final int pageCount = documentModel.getPageCount();
            if (pageCount > 0) {
	            int pageIndex = documentModel.getCurrentViewPageIndex();
            	String fileTitle = null;
            	if (intent != null) {
            		fileTitle = intent.getStringExtra(ViewerActivityHelper.INTENT_KEY_FILETITLE);
            	}
            	if (fileTitle != null && fileTitle.length() > 0) {
            		activity.setTitle("(" + (pageIndex + 1) + "/" + pageCount + ")" + fileTitle);
            	} else {
            		activity.setTitle("" + (pageIndex + 1) + "/" + pageCount);
            	}
            }
        }
    }
    
	public static String cleanupTitle(final String in) {
		String out = in;
		//FIXME:新增，防止为null值
		if (in != null) {
			try {
				out = in.substring(0, in.lastIndexOf('.'));
				out = out.replaceAll("\\(.*?\\)|\\[.*?\\]", "")
					.replaceAll("_", " ").replaceAll(".fb2$", "").trim();
			} catch (final IndexOutOfBoundsException e) {
				
			}
		} else {
			out = "";
		}
		return out;
	}

	//FIXME:打开选项菜单
	//R.id.actions_openOptionsMenu
    public void openOptionsMenu() {
    	openOptionsMenu(getManagedComponent(), this.helper.view.getView());
    }
    public void openOptionsMenu(final Activity activity, final View view) {
    	activity.openOptionsMenu();
    }
    
    //FIXME:点击大纲条目后跳转
    //R.id.actions_gotoOutlineItem
    public void gotoOutlineItem(AdapterView<?> parent, View view, int position, long id) {
    	final OutlineLink link = (OutlineLink)parent.getAdapter().getItem(position);
    	if (link == null) {
            return;
        }
        if (link.targetPage != -1) {
            final int pageCount = documentModel.decodeService.getPageCount();
            if (link.targetPage < 1 || link.targetPage > pageCount) {
            	this.helper.showToastText(getManagedComponent(), 2000, "无效的页码。有效页码范围: 1-%d", pageCount);
            } else {
                getDocumentController().goToLink(link.targetPage - 1, link.targetRect,
                        AppSettings.current().storeOutlineGotoHistory);
            }
            return;
        }
        if (link.targetUrl != null) {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(link.targetUrl));
            getManagedComponent().startActivity(i);
        }
    }

    @Override
    public void jumpToPage(final int viewIndex, final float offsetX, final float offsetY, final boolean addToHistory) {
        if (addToHistory) {
            history.update();
        }
        getDocumentController().goToPage(viewIndex, offsetX, offsetY);
    }

    //FIXME:显示大纲对话框
    //R.id.mainmenu_outline)
    public void showOutline() {
        final List<OutlineLink> outline = 
        	documentModel.decodeService.getOutline();
        if ((outline != null) && (outline.size() > 0)) {
            final OutlineDialog dlg = new OutlineDialog(this, outline);
            dlg.show();
        } else {
        	//FIXME:这里的调试信息要改
        	this.helper.showToastText(getManagedComponent(), 
        		Toast.LENGTH_SHORT, "没有大纲文件");
        }
    }

    //FIXME:启动搜索任务
    //R.id.actions_doSearch
    //R.id.actions_doSearchBack
    public final void doSearch(final Editable value, 
    		Object text, String forward) {
        final String newPattern = (value != null ? value.toString() : 
        	(text != null ? text.toString() : ""));
        final String oldPattern = currentSearchPattern;
        currentSearchPattern = newPattern;
        new SearchTask(this).execute(newPattern, oldPattern, forward);
    }

    //FIXME:打开页面跳转对话框
    //R.id.mainmenu_goto_page
    public void showGotoDialog() {
    	if (D) {
			Log.e(TAG, "showGotoDialog buttonEbookdroidGoto");
		}
        final GoToPageDialog dlg = new GoToPageDialog(this);
        dlg.show();
    }

    //FIXME:单文件设置
    //R.id.mainmenu_booksettings
    public void showBookSettings() {
    	
    }

    //FIXME:全局设置
    //R.id.mainmenu_settings
    public void showAppSettings() {
    	
    }

    //FIXME:全屏
    //R.id.mainmenu_fullscreen
    public void toggleFullScreen() {
    	
    }

    //FIXME:显示隐藏标题栏
    //R.id.mainmenu_showtitle
    public void toggleTitleVisibility() {
    	//FIXME:
    }

    //FIXME:夜间模式
    //R.id.mainmenu_nightmode)
    public void toggleNightMode() {
        SettingsManager.toggleNightMode(bookSettings);
    }

    //FIXME:分割页面
    //R.id.mainmenu_splitpages)
    public void toggleSplitPages() {
        SettingsManager.toggleSplitPages(bookSettings);
    }

    //FIXME:剪切页面
    //R.id.mainmenu_croppages)
    public void toggleCropPages() {
        SettingsManager.toggleCropPages(bookSettings);
    }
    
    //FIXME:添加书签
    //R.id.mainmenu_bookmark
    public void showBookmarkDialog() {
        final int page = documentModel.getCurrentViewPageIndex();
        final String message = "输入书签描述";
        final BookSettings bs = getBookSettings();
        final int offset = bs != null ? bs.firstPageOffset : 1;
        final EditText input = (EditText) LayoutInflater.from(getManagedComponent()).inflate(R.layout.ebookdroid_bookmark_edit, null);
        input.setText("页面" + " " + (page + offset));
        input.selectAll();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getManagedComponent());
        builder.setTitle("添加书签").setMessage(message).setView(input);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				addBookmark(input.getText());
			}
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//do nothing
			}
        }).show();
    }

    //FIXME:添加书签
    //R.id.actions_addBookmark
    public void addBookmark(final Editable value) {
        final String name = value.toString();
        final Page page = documentModel.getCurrentPageObject();
        if (page != null) {
            final ViewState state = ViewState.get(getDocumentController());
            final PointF pos = state.getPositionOnPage(page);
            bookSettings.bookmarks.add(new Bookmark(name, documentModel.getCurrentIndex(), pos.x, pos.y));
            Collections.sort(bookSettings.bookmarks);
            SettingsManager.storeBookSettings(bookSettings);
            //FIXME:这里没有刷新菜单
            state.release();
        }
    }

    //跳转书签
    //R.id.actions_goToBookmark)
    public void goToBookmark(final Bookmark b) {
        if (b == null) {
            return;
        }
        final Page actualPage = b.page.getActualPage(getDocumentModel(), bookSettings);
        if (actualPage != null) {
            jumpToPage(actualPage.index.viewIndex, b.offsetX, b.offsetY, AppSettings.current().storeGotoHistory);
        }
    }

    @Override
    public ZoomModel getZoomModel() {
        if (zoomModel == null) {
            zoomModel = new ZoomModel();
        }
        return zoomModel;
    }

    @Override
    public DecodeService getDecodeService() {
        return documentModel != null ? documentModel.decodeService : null;
    }

    @Override
    public DecodingProgressModel getDecodingProgressModel() {
        return progressModel;
    }

    @Override
    public DocumentModel getDocumentModel() {
        return documentModel;
    }

    @Override
    public final SearchModel getSearchModel() {
        return searchModel;
    }

    @Override
    public final IViewController getDocumentController() {
        return ctrl.get();
    }

    @Override
    public final Context getContext() {
        return getManagedComponent();
    }

    @Override
    public final IView getView() {
        return this.helper.view;
    }

    @Override
    public final Activity getActivity() {
        return getManagedComponent();
    }

    @Override
    public final BookSettings getBookSettings() {
        return bookSettings;
    }

    @Override
    public final Object/*IActionController*/ getActionController() {
        return this;
    }

    //FIXME:切换浮动视图
    //R.id.mainmenu_zoom
    //R.id.actions_toggleTouchManagerView
    //R.id.mainmenu_search
    //R.id.mainmenu_crop
    public void toggleControls(final View view, final DocumentViewMode mode) {
        if (mode != null && bookSettings != null && bookSettings.viewMode != mode) {
        	final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(String.format("这个动作只可用于视图模式%s", mode.getResValue()));
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface arg0, int arg1) {
    				//do nothing
    			}
            }).show();
            builder.show();
            return;
        }
        toggleControls(view);
        if (view instanceof ManualCropView) {
            final ManualCropView mcv = (ManualCropView) view;
            if (mcv.getVisibility() == View.VISIBLE) {
                mcv.initControls();
            }
        }
        //FIXME:这里没有刷新菜单
    }

    //FIXME:按键事件，后退按键
    public final boolean dispatchKeyEvent(final KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        if (this.helper.getSearchControls(getManagedComponent()).getVisibility() == View.VISIBLE) {
            if (action == KeyEvent.ACTION_DOWN && 
            	keyCode == KeyEvent.KEYCODE_BACK) {
                //切换搜索视图
            	toggleControls(this.helper.getSearchControls
            		(getManagedComponent()), null);
            	return true;
            }
            return false;
        }
        if (getDocumentController().dispatchKeyEvent(event)) {
            return true;
        }
        //FIXME:点击后退后退按键
        if (action == KeyEvent.ACTION_DOWN && 
        	keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getRepeatCount() == 0) {
                if (this.helper.getManualCropControls(getManagedComponent()).isShown()) {
                    toggleControls(this.helper.getManualCropControls(
                    	getManagedComponent()));
                } else {
                    if (history.goBack()) {
                        return true;
                    }
                    //FIXME:是否有关闭确定
                    if (AppSettings.current().confirmClose) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getManagedComponent());
                        builder.setTitle("关闭…");
                        builder.setMessage("确定关闭当前视图？");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface arg0, int arg1) {
                				closeActivity();
                			}
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface arg0, int arg1) {
                				//do nothing
                			}
                        }).show();
                    } else {
                        this.closeActivity();
                    }
                }
            }
            return true;
        }
        return false;
    }

    //FIXME:退出
    //R.id.mainmenu_close)
    public void closeActivity() {
    	if (D) {
    		Log.e(TAG, "closeActivity====>001");
    	}
        if (documentModel != null) {
        	if (D) {
        		Log.e(TAG, "closeActivity====>002");
        	}
        	documentModel.recycle();
        }
        if (D) {
    		Log.e(TAG, "closeActivity====>003");
    	}
        SettingsManager.releaseBookSettings(id, bookSettings);
        if (D) {
    		Log.e(TAG, "closeActivity====>004");
    	}
        getManagedComponent().finish();
        if (D) {
    		Log.e(TAG, "closeActivity====>005");
    	}
    }

    //FIXME:设置全屏模式，做成按钮？
    private static boolean sFullscreen;
    public void setFullScreenMode(final Activity activity, final View view, final boolean fullScreen) {
    	sFullscreen = fullScreen;
    	final Window w = activity.getWindow();
    	if (fullScreen) {
    		w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	} else {
    		w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
	}
    
    //FIXME:这个方法需要提取出来
    @Override
    public void onBookSettingsChanged(final BookSettings oldSettings, final BookSettings newSettings,
            final BookSettings.Diff diff) {
        if (newSettings == null) {
            return;
        }
        boolean redrawn = false;
        if (diff.isViewModeChanged() || diff.isSplitPagesChanged() || diff.isCropPagesChanged()) {
            redrawn = true;
            final IViewController newDc = switchDocumentController(newSettings);
            if (!diff.isFirstTime() && newDc != null) {
                newDc.init(null);
                newDc.show();
            }
        }
        if (diff.isRotationChanged()) {
        	getManagedComponent().setRequestedOrientation(newSettings.getOrientation(AppSettings.current()));
        }
        if (diff.isFirstTime()) {
            getZoomModel().initZoom(newSettings.getZoom());
        }
        final IViewController dc = getDocumentController();
        if (!redrawn && (diff.isEffectsChanged())) {
            redrawn = true;
            dc.toggleRenderingEffects();
        }
        if (!redrawn && diff.isPageAlignChanged()) {
            dc.setAlign(newSettings.pageAlign);
        }
        if (diff.isAnimationTypeChanged()) {
            dc.updateAnimationType();
        }
        currentPageChanged(PageIndex.NULL, documentModel.getCurrentIndex());
        //FIXME:这里没有刷新菜单
    }

    public String getFileName() {
    	return this.m_fileName;
    }

    @Override
    public Activity getManagedComponent() {
        return m_managedComponent;
    }

    public void setManagedComponent(final Activity component) {
        m_managedComponent = component;
    }    
    
    private static void toggleControls(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }
    
    //新增
    public int getCurrentPagePosition() { 
    	if (documentModel != null) {
	    	int pageIndex = documentModel.getCurrentViewPageIndex();
	    	return pageIndex;
    	} else {
    		return -1;
    	}
    }
}
