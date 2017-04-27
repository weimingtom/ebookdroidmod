package org.ebookdroid2.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.ebookdroid2.activity.ViewerActivityController;
import org.ebookdroid2.activity.ViewerActivityHelper;
import org.ebookdroid2.listener.IProgressIndicator;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.page.PageIndex;
import org.mupdfdemo2.task.AsyncTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;

//FIXME:加载文件任务
public class BookLoadTask extends AsyncTask<String, String, Throwable> 
	implements IProgressIndicator, Runnable, OnCancelListener {
	private final static boolean D = false;
	private final static String TAG = "BookLoadTask";
	
    protected final Context context;
    protected final String startProgressStringId;
    protected final boolean cancellable;
    protected final AtomicBoolean continueFlag = new AtomicBoolean(true);
    protected AlertDialog progressDialog;
    private final MuPDFCore core;
    private final ViewerActivityController controller;
    
    public BookLoadTask(ViewerActivityController controller, MuPDFCore _core) {
        this.controller = controller;
    	this.context = this.controller.getManagedComponent();
        this.startProgressStringId = "载入中… 请稍候";
        this.cancellable = false;
        this.core = _core;
    }

    @Override
    public void run() {
        execute(" ");
    }

    @Override
    protected Throwable doInBackground(final String... params) {
        if (D) {
        	Log.e(TAG, "BookLoadTask.doInBackground(): start");
        }
        //FIXME:这里的异常写得不好
        try {
            if (D) {
            	Log.e(TAG, "BookLoadTask.doInBackground(): start 2");
            }
            this.controller.getView().waitForInitialization();
            if (D) {
            	Log.e(TAG, "BookLoadTask.doInBackground(): ====" + this.core);
            }
            if (this.core == null) {
            	throw new Exception("打开文档错误"); //FIXME:这里报错，写得不好
            }
            this.controller.documentModel.open(this.core);
            this.controller.getDocumentController().init(this);
            return null;
        } catch (final Exception e) {
            e.printStackTrace();
        	if (D) {
        		Log.e(TAG, e.getMessage(), e);
        	}
            return e;
        } catch (final Throwable th) {
            th.printStackTrace();
        	if (D) {
        		Log.e(TAG, "BookLoadTask.doInBackground(): Unexpected error", th);
        	}
            return th;
        } finally {
            if (D) {
            	Log.e(TAG, "BookLoadTask.doInBackground(): finish");
            }
        }
    }

    @Override
    protected void onPostExecute(Throwable result) {
        if (D) {
        	Log.e(TAG, "BookLoadTask.onPostExecute(): start");
        }
        try {
            if (result == null) {
                try {
                	this.controller.getDocumentController().show();
                    final DocumentModel dm = this.controller.getDocumentModel();
                    this.controller.currentPageChanged(PageIndex.NULL, dm.getCurrentIndex());
                } catch (final Throwable th) {
                    result = th;
                }
            }
            closeProgressDialog();
            //FIXME:这里的异常写得不好
            if (result != null) {
                final String msg = result.getMessage();
                //FIXME:这里可以异常输出到日志文件中
                this.controller.showErrorDlg("发生异常:\n%s", msg);
            } else {
            	//这里可以做字体检查
            }
            if (this.controller.intent != null) {
            	int gotopage = this.controller.intent.getIntExtra(ViewerActivityHelper.INTENT_KEY_GOTOPAGE, -1);
            	if (gotopage >= 0) {
            		this.controller.jumpToPage(gotopage, 0, 0,
	                        AppSettings.current().storeGotoHistory);
            	}
            }
        } catch (final Throwable th) {
            th.printStackTrace();
        	if (D) {
            	Log.e(TAG, "BookLoadTask.onPostExecute(): Unexpected error", th);
            }
        	//FIXME:这里的报错可以输出到日志中
        } finally {
            if (D) {
            	Log.e(TAG, "BookLoadTask.onPostExecute(): finish");
            }
        }
    }

    @Override
    public void setProgressDialogMessage(final String resourceID, final Object... args) {
    	publishProgress(String.format(resourceID, args));
    }
    
    @Override
    protected void onPreExecute() {
        onProgressUpdate(startProgressStringId);
    }

    @Override
    public void onCancel(final DialogInterface dialog) {
        if (cancellable) {
            continueFlag.set(false);
            cancel(true);
        }
    }

    protected void closeProgressDialog() {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (final Throwable th) {
            }
            progressDialog = null;
        }
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        final int length = (values != null ? values.length : 0);
        if (length == 0) {
            return;
        }
        final String last = values[length - 1];
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new AlertDialog.Builder(context).setMessage(last).show();
            if (cancellable) {
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.setOnCancelListener(this);
            } else {
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
            }
        } else {
            progressDialog.setMessage(last);
        }
    }
}
