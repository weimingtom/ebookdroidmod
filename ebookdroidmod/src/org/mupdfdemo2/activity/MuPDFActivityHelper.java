package org.mupdfdemo2.activity;

import java.util.concurrent.Executor;

import org.ebookdroid2.activity.ViewerActivityHelper;
import org.mupdfdemo2.adapter.MuPDFPageAdapter;
import org.mupdfdemo2.model.Hit;
import org.mupdfdemo2.model.OutlineActivityData;
import org.mupdfdemo2.model.SessionData;
import org.mupdfdemo2.task.AsyncTask;
import org.mupdfdemo2.task.SearchTask;
import org.mupdfdemo2.task.SearchTaskResult;
import org.mupdfdemo2.view.MuPDFReaderView;
import org.mupdfdemo2.view.MuPDFView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.OutlineItem;
import com.iteye.weimingtom.ebdmod.R;

/**
 * TODO:
 * 不允许加密的PDF，因为不支持批注，判断方法：
 * core.fileFormat().startsWith("PDF") && core.isUnencryptedPDF() && !core.wasOpenedFromBuffer())
 *
 * Toast.makeText使用其他形式显示
 * 高亮垃圾桶
 * 高亮按钮
 */
public class MuPDFActivityHelper {
	private final static boolean D = false;
	private final static String TAG = "MuPDFActivityHelper";
	
	//FIXME:利用pref文件保存状态，但有问题，所以设为false
	public final static boolean IS_SAVE_STATE = false;
	public final static String STATE_KEY_FILENAME = "FileName";//FIXME:这个名字可能有冲突
	public final static String SHARE_PREF_NAME = "mupdf_viewer_share_pref";//FIXME:这个名字可能有冲突 
	public final static String SHARE_PREF_KEY_PAGE_PREFIX = "page"; //FIXME:这个名字可能有冲突
	//FIXME:输入的当前页面
	public final static String INTENT_GOTO_PAGE = "INTENT_GOTO_PAGE";
	//FIXME:输出的当前页码
	public final static String INTENT_DATA_PAGE = "INTENT_DATA_PAGE"; 
	private final int OUTLINE_REQUEST = 0;
	//FIXME:按钮模式，可能没用
	private final static int BUTTON_MODE_NONE = 0;
	private final static int BUTTON_MODE_COPYTEXT = 1;
	private final static int BUTTON_MODE_SEARCH = 2;
	private final static int BUTTON_MODE_ANNOT_SELECT = 3;
	private final static int BUTTON_MODE_ANNOT_DELETE = 4;
	private final static int BUTTON_MODE_ANNOT_PEN = 5;
	private int buttonMode = BUTTON_MODE_NONE;
	
	private boolean mFirstOpen = false; //是否第一次打开	
	private AsyncTask<Void, Void, Void> mSaveAnnotTask;
	private TextView textViewPdfTitle;
	public MuPDFCore core = null; //文件状态
	private String mFileName; //文件名
	private MuPDFReaderView mDocView; //界面
	private int mPageSliderRes;	
	private SearchTask mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private TextView textViewJump;
	private SeekBar seekBarJump;
	//新增
	private boolean mSinglePageMode = false;
	public OnChangeMergeModeListener onChangeMergeMode;
	private LinearLayout linearLayoutAnnot;
	private LinearLayout linearLayoutMenu;
	private LinearLayout linearLayoutSelect;
	
	private final static class ThreadPerTaskExecutor implements Executor {
		public void execute(Runnable r) {
			new Thread(r).start();
		}
	}
	
	public MuPDFActivityHelper() {
		
	}
	
	public MuPDFCore openFile(String path, MuPDFCore core_) {
		if (D) {
			Log.e(TAG, "Trying to open " + path);
		}
		this.core = core_;
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
		if (core != null) {
			OutlineActivityData.set(null); //删除旧的大纲缓存
		}
		return core;
	}
	
	//如果返回false，不需要再做加载工作
	public boolean onCreatePre(final Bundle savedInstanceState, final Activity _this) {
		if (D) {
			Log.e(TAG, "onCreatePre 001=============>");
		}
		_this.setTitle("返回阅读模式");
		_this.setResult(Activity.RESULT_CANCELED);
		mAlertBuilder = new AlertDialog.Builder(_this);
		//FIXME:这个操作有问题
		SearchTaskResult.set(null);
		//FIXME:==============================================================================
		if (core == null) {
			if (D) {
				Log.e(TAG, "onCreatePre 002=============>");
			}
			if (_this.getLastNonConfigurationInstance() != null) {
				core = ((SessionData)_this.getLastNonConfigurationInstance()).core;
			} else {
				core = null;
			}
			if (core != null) {
				if (D) {
					Log.e(TAG, "onCreatePre 003-1=============>");
				}
				if (savedInstanceState != null && 
					savedInstanceState.containsKey(STATE_KEY_FILENAME)) {
					mFileName = savedInstanceState.getString(STATE_KEY_FILENAME);
				}
				return true; //不需要加载core
			} else {
				if (D) {
					Log.e(TAG, "onCreatePre 003-2=============>");
				}
				mFirstOpen = true; // 第一次打开这个文件
				return false; //需要加载core
			}
		} else {
			if (D) {
				Log.e(TAG, "onCreatePre 004=============>");
			}
			return true; //不需要加载core
		}
	}
	
	public void onCreate(final Bundle savedInstanceState, final Activity _this, MuPDFCore _core) {
		core = _core;
		if (core == null) {
			return;
		}
		createUI(savedInstanceState, _this);
	}
	
	//创建容器视图
	public void createUI(Bundle savedInstanceState, final Activity _this) {
		if (core == null) {
			return;
		}
		mDocView = new MuPDFReaderView(_this) {
			//切换页面
			@Override
			protected void onMoveToChild(int i) {
				if (core == null) {
					return;
				}
				if (textViewJump != null) {
					textViewJump.setText(String.format("%d / %d", i + 1, core.countPages()));
					seekBarJump.setMax((core.countPages() - 1) * mPageSliderRes);
					seekBarJump.setProgress(i * mPageSliderRes);				
				}
				super.onMoveToChild(i);
			}
			
			//点击显示顶部栏
			@Override
			protected void onTapMainDocArea() {
				
			}

			//滑动隐藏顶部栏
			@Override
			protected void onDocMotion() {

			}

			//切换模式
			@Override
			protected void onHit(Hit item) {
				switch (buttonMode) {
				case BUTTON_MODE_ANNOT_SELECT:
					if (item == Hit.Annotation) {
						//点到批注，切换到删除确定模式
						buttonMode = BUTTON_MODE_ANNOT_DELETE;
					}
					//没有点到，模式保持不变
					break;
					
				case BUTTON_MODE_ANNOT_DELETE:
					//点一次批注再点批注外面的话，取消删除，返回原来的批注删除状态
					buttonMode = BUTTON_MODE_ANNOT_SELECT;
					// fall through
				default:
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null) {
						pageView.deselectAnnotation();
					}
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(_this, core));
		mSearchTask = new SearchTask(_this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				mDocView.setDisplayedViewIndex(result.pageNumber);
				mDocView.resetupChildren();
			}
		};
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;
		_this.setTitle("返回阅读模式");
		// FIXME:
		int defaultPage = 0; // 默认跳转的页数
		Intent intent = _this.getIntent();
		if (intent != null) {
			defaultPage = intent.getIntExtra(INTENT_GOTO_PAGE, 0);
		}
		if (D) {
			Log.e(TAG, "=======================>defaultPage=" + defaultPage);
		}
		// 页数
		// Reenstate last state if it was recorded
		SharedPreferences prefs = _this.getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
		if (mFirstOpen) {
			// FIXME:
			mDocView.setDisplayedViewIndex(defaultPage);
		} else {
			mDocView.setDisplayedViewIndex(prefs.getInt(SHARE_PREF_KEY_PAGE_PREFIX + mFileName, defaultPage));
		}
		Button buttonBack = (Button)_this.findViewById(R.id.buttonBack);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!onExit(_this)) {
					_this.finish();
				}
			}
		});
		textViewPdfTitle = (TextView) _this.findViewById(R.id.textViewPdfTitle);
		textViewPdfTitle.setText(mFileName);		
		FrameLayout frameLayoutMode = (FrameLayout) _this.findViewById(R.id.frameLayoutMode);
		linearLayoutMenu = (LinearLayout) _this.findViewById(R.id.linearLayoutMenu);
		final LinearLayout linearLayoutCopy = (LinearLayout) _this.findViewById(R.id.linearLayoutCopy);
		Button buttonCopy = (Button) _this.findViewById(R.id.buttonCopy);
		buttonCopy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDocView.setMode(MuPDFReaderView.Mode.Selecting);
				Toast.makeText(_this, 
					"请选择文本", Toast.LENGTH_SHORT).show();
				
				linearLayoutCopy.setVisibility(View.VISIBLE);
				linearLayoutMenu.setVisibility(View.INVISIBLE);
			}
		});
		Button buttonCopyCancel = (Button) _this.findViewById(R.id.buttonCopyCancel);
		buttonCopyCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
				if (pageView != null) {
					pageView.deselectText();
					pageView.cancelDraw();
				}
				mDocView.setMode(MuPDFReaderView.Mode.Viewing);
				
				linearLayoutCopy.setVisibility(View.INVISIBLE);
				linearLayoutMenu.setVisibility(View.VISIBLE);
			}
		});
		Button buttonCopyOK = (Button) _this.findViewById(R.id.buttonCopyOK);
		buttonCopyOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
				boolean success = false;
				if (pageView != null) {
					success = pageView.copySelection();
				}
				Toast.makeText(_this, 
					success ? "已复制到剪贴板" : "未选择文本", 
					Toast.LENGTH_SHORT).show();
				
				linearLayoutCopy.setVisibility(View.INVISIBLE);
				linearLayoutMenu.setVisibility(View.VISIBLE);
			}
		});
		
		final LinearLayout linearLayoutSearch = (LinearLayout) _this.findViewById(R.id.linearLayoutSearch);
		final EditText editTextSearch = (EditText) _this.findViewById(R.id.editTextSearch);
		Button buttonSearch = (Button) _this.findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				linearLayoutSearch.setVisibility(View.VISIBLE);
				linearLayoutMenu.setVisibility(View.INVISIBLE);
				
				editTextSearch.requestFocus();
				InputMethodManager imm = (InputMethodManager) _this.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(editTextSearch, 0);
				}
			}
		});		
		Button buttonSearchCancel = (Button) _this.findViewById(R.id.buttonSearchCancel);
		buttonSearchCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				linearLayoutSearch.setVisibility(View.INVISIBLE);
				linearLayoutMenu.setVisibility(View.VISIBLE);
				
				InputMethodManager imm = (InputMethodManager) _this.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
				}
				SearchTaskResult.set(null);
				mDocView.resetupChildren();
			}
		});
		Button buttonSearchBackward = (Button) _this.findViewById(R.id.buttonSearchBackward);
		buttonSearchBackward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int displayPage = mDocView.getDisplayedViewIndex();
				SearchTaskResult r = SearchTaskResult.get();
				int searchPage = r != null ? r.pageNumber : -1;
				mSearchTask.go(editTextSearch.getText().toString(), -1, displayPage, searchPage);
			}
		});
		Button buttonSearchForward = (Button) _this.findViewById(R.id.buttonSearchForward);
		buttonSearchForward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int displayPage = mDocView.getDisplayedViewIndex();
				SearchTaskResult r = SearchTaskResult.get();
				int searchPage = r != null ? r.pageNumber : -1;
				mSearchTask.go(editTextSearch.getText().toString(), 1, displayPage, searchPage);
			}
		});
		editTextSearch.addTextChangedListener(new TextWatcher() {
			//搜索框输入文本事件
			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				if (SearchTaskResult.get() != null && 
					!editTextSearch.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
		});
		//回车搜素
		editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					int displayPage = mDocView.getDisplayedViewIndex();
					SearchTaskResult r = SearchTaskResult.get();
					int searchPage = r != null ? r.pageNumber : -1;
					mSearchTask.go(editTextSearch.getText().toString(), 1, displayPage, searchPage);
				}
				return false;
			}
		});
		editTextSearch.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && 
					keyCode == KeyEvent.KEYCODE_ENTER) {
					int displayPage = mDocView.getDisplayedViewIndex();
					SearchTaskResult r = SearchTaskResult.get();
					int searchPage = r != null ? r.pageNumber : -1;
					mSearchTask.go(editTextSearch.getText().toString(), 1, displayPage, searchPage);
				}
				return false;
			}
		});
		
		linearLayoutSelect = (LinearLayout) _this.findViewById(R.id.linearLayoutSelect);
		Button buttonSelect = (Button) _this.findViewById(R.id.buttonSelect);
		buttonSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonSelect();
			}
		});
		Button buttonSelectCancel = (Button) _this.findViewById(R.id.buttonSelectCancel);
		buttonSelectCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonSelectCancel();
			}
		});
		Button buttonSelectOK = (Button) _this.findViewById(R.id.buttonSelectOK);
		buttonSelectOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonSelectOK(_this);
			}
		});
		final LinearLayout linearLayoutJump = (LinearLayout) _this.findViewById(R.id.linearLayoutJump);
		textViewJump = (TextView) _this.findViewById(R.id.textViewJump);
		seekBarJump = (SeekBar) _this.findViewById(R.id.seekBarJump);
		Button buttonJump = (Button) _this.findViewById(R.id.buttonJump);
		buttonJump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int index = mDocView.getDisplayedViewIndex();
				seekBarJump.setMax((core.countPages() - 1) * mPageSliderRes);
				seekBarJump.setProgress(index * mPageSliderRes);
				
				linearLayoutJump.setVisibility(View.VISIBLE);
				linearLayoutMenu.setVisibility(View.INVISIBLE);
			}
		});
		Button buttonJumpCancel = (Button) _this.findViewById(R.id.buttonJumpCancel);
		buttonJumpCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				linearLayoutJump.setVisibility(View.INVISIBLE);
				linearLayoutMenu.setVisibility(View.VISIBLE);
			}
		});
		seekBarJump.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (core == null) {
					return;
				}
				int index = (progress + mPageSliderRes / 2) / mPageSliderRes;
				textViewJump.setText(String.format("%d / %d", index + 1,
					core.countPages()));
			}
		});
		linearLayoutAnnot = (LinearLayout) _this.findViewById(R.id.linearLayoutAnnot);
		Button buttonAnnotCancel = (Button) _this.findViewById(R.id.buttonAnnotCancel);
		buttonAnnotCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonAnnotCancel();
			}
		});
		Button buttonAnnotOK = (Button) _this.findViewById(R.id.buttonAnnotOK);
		buttonAnnotOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonAnnotOK(_this);
			}
		});
		Button buttonPen = (Button) _this.findViewById(R.id.buttonPen);
		buttonPen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onButtonPen();
			}
		});
		Button buttonOutline = (Button) _this.findViewById(R.id.buttonOutline);
		buttonOutline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onOpenOutline(_this);
			}
		});
		RelativeLayout layout = (RelativeLayout)_this.findViewById(R.id.contentRelativeLayout);
		layout.addView(mDocView);
	}
		
	private void onOpenOutline(final Activity _this) {
		if (core != null && core.hasOutline()) {
			OutlineItem outline[] = core.getOutline();
			if (D) {
				Log.e(TAG, "=============> outline.len = " + (outline != null ? outline.length : 0) + ", outline == " + outline);
			}
			if (outline != null) {
				OutlineActivityData.get().items = outline;
				Intent intent = new Intent(_this, OutlineActivity.class);
				_this.startActivityForResult(intent, OUTLINE_REQUEST);
			}
		} else {
			Toast.makeText(_this, "没有大纲", Toast.LENGTH_SHORT).show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUTLINE_REQUEST:
			if (resultCode >= 0) {
				mDocView.setDisplayedViewIndex(resultCode);
			}
			break;
		}
	}
		
	public MuPDFCore onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}
	
	public void onSaveInstanceState(Bundle outState, Activity _this) {
		//FIXME:存在这里有问题
		if (!IS_SAVE_STATE) {
			if (mFileName != null && mDocView != null) {
				outState.putString(STATE_KEY_FILENAME, mFileName); //最近的文件名
				SharedPreferences prefs = _this.getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt(SHARE_PREF_KEY_PAGE_PREFIX + mFileName, mDocView.getDisplayedViewIndex()); //文件名对应的页数
				edit.commit();
			}
		}
	}
	
	public void onPause(Activity _this) {
		if (mSearchTask != null) {
			mSearchTask.stop();
		}
		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = _this.getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt(SHARE_PREF_KEY_PAGE_PREFIX + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}
	
	public void onDestroy(Activity _this) {
		if (mDocView != null) {
			mDocView.applyToChildren(new MuPDFReaderView.ViewMapper() {
				public void applyToView(View view) {
					((MuPDFView) view).releaseBitmaps();
				}
			});
		}
		if (core != null) {
			core.onDestroy();
		}
		core = null;
	}

	public boolean onExit(final Activity _this) {
		this.buttonMode = BUTTON_MODE_NONE;
		//FIXME:新增，如果mSinglePageMode为true，则不做任何保存提示
		if (core != null && 
			(core.hasChanges() || core.isModified == true) && 
			!this.mSinglePageMode) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == AlertDialog.BUTTON_POSITIVE) {
						saveAnnot(_this, null);
					} else if (which == AlertDialog.BUTTON_NEGATIVE) {
						Intent intent = new Intent();
						intent.putExtra(INTENT_DATA_PAGE, mDocView.getDisplayedViewIndex());
						_this.setResult(Activity.RESULT_CANCELED, intent);
						_this.finish();
					}
				}
			};
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle("保存批注");
			alert.setMessage("文档已变更，保存变更吗？");
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "是", listener);
			alert.setButton(AlertDialog.BUTTON_NEUTRAL, "取消", listener);
			alert.setButton(AlertDialog.BUTTON_NEGATIVE, "否", listener);
			alert.show();
			return true;
		}
		return false;
	}

	//保存批注
	public void saveAnnot(final Activity _this, final Runnable _action) {
		if (mSaveAnnotTask != null) {
			mSaveAnnotTask.cancel(true);
			mSaveAnnotTask = null;
		}
		mSaveAnnotTask = new AsyncTask<Void, Void, Void>() {
			private ProgressDialog progressDialog = null;
			
			@Override
			public void onPreExecute() {
				progressDialog = new ProgressDialog(_this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(true);
				progressDialog.setTitle("保存文件");
				progressDialog.setMessage("正在保存批注到文件");
				progressDialog.setCancelable(false);
				progressDialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				if (core != null) {
					core.save();
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
				if (_action == null) {
					Intent intent = new Intent();
					intent.putExtra(INTENT_DATA_PAGE, mDocView.getDisplayedViewIndex());
					_this.setResult(Activity.RESULT_OK, intent);
					_this.finish();
				} else {
					_action.run();
				}
			}
		};
		mSaveAnnotTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}
	
	//FIXME:新增
	public void enableSinglePageMode(boolean singlePageMode, 
			int singlePagePosition) {
		this.mSinglePageMode = singlePageMode;
		if (mDocView != null) {
			Adapter adapter = mDocView.getAdapter();
			if (adapter instanceof MuPDFPageAdapter) {
				MuPDFPageAdapter pageAdapter = (MuPDFPageAdapter)adapter;
				pageAdapter.enableSinglePage(
						singlePageMode, singlePagePosition);
				if (singlePageMode) {
					//需要刷新一下，跳转到第0页，否则不起作用
					mDocView.setDisplayedViewIndex(0);
				} else {
					mDocView.setDisplayedViewIndex(singlePagePosition);
				}
			}
		}
	}
	
	public void onButtonPen() {
		//FIXME:批注模式
		mDocView.setMode(MuPDFReaderView.Mode.Drawing);
		linearLayoutAnnot.setVisibility(View.VISIBLE);
		linearLayoutMenu.setVisibility(View.INVISIBLE);
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT_PEN);
		}
	}
	
	public void onButtonSelect() {
		linearLayoutSelect.setVisibility(View.VISIBLE);
		linearLayoutMenu.setVisibility(View.INVISIBLE);

		buttonMode = BUTTON_MODE_ANNOT_SELECT;
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT_SELECT);
		}
	}
	
	//取消保存当前页的批注
	public void onButtonAnnotCancel() {
		linearLayoutAnnot.setVisibility(View.INVISIBLE);
		linearLayoutMenu.setVisibility(View.VISIBLE);
	
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectText();
			pageView.cancelDraw();
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT);
		}
	}
	
	//保存当前页的批注
	public void onButtonAnnotOK(Activity _this) {
		linearLayoutAnnot.setVisibility(View.INVISIBLE);
		linearLayoutMenu.setVisibility(View.VISIBLE);
		//FIXME:保存批注
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		boolean success = false;
		if (pageView != null) {
			success = pageView.saveDraw();
			if (D) {
				Log.e(TAG, "=================> saveDraw() success == " + success);
			}
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		if (_this != null) {
			Intent intent = _this.getIntent();
			if (intent != null) {
				intent.putExtra(ViewerActivityHelper.INTENT_KEY_ISSAVE, true);
			}
		}
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT);
		}
	}
	
	//取消删除选择的批注
	public void onButtonSelectCancel() {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectAnnotation();
		}
		
		linearLayoutSelect.setVisibility(View.INVISIBLE);
		linearLayoutMenu.setVisibility(View.VISIBLE);
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT);
		}
	}
	
	//删除选择的批注
	public void onButtonSelectOK(Activity _this) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deleteSelectedAnnotation();
		}
		linearLayoutSelect.setVisibility(View.INVISIBLE);
		linearLayoutMenu.setVisibility(View.VISIBLE);
		if (_this != null) {
			Intent intent = _this.getIntent();
			if (intent != null) {
				intent.putExtra(ViewerActivityHelper.INTENT_KEY_ISSAVE, true);
			}
		}
		if (this.onChangeMergeMode != null) {
			this.onChangeMergeMode.onChangeMergeMode(MuPDFActivity.MERGE_MODE_ANNOT);
		}
	}
}
