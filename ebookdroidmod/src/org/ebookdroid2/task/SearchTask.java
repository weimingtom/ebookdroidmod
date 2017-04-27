package org.ebookdroid2.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.ebookdroid2.activity.ViewerActivityController;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.model.SearchModel;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.view.SearchControls;
import org.mupdfdemo2.task.AsyncTask;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

//FIXME:搜索任务
public class SearchTask extends AsyncTask<String, String, RectF> 
	implements SearchModel.ProgressCallback, DialogInterface.OnCancelListener {
	private final static boolean D = false;
	private final static String TAG = "SearchTask";
	
    private ProgressDialog progressDialog;
    private final AtomicBoolean continueFlag = new AtomicBoolean(true);
    private String pattern;
    private Page targetPage = null;
    private final ViewerActivityController controller;
    
    public SearchTask(ViewerActivityController controller) {
    	this.controller = controller;
    }
    
    @Override
    public void onCancel(final DialogInterface dialog) {
    	this.controller.documentModel.decodeService.stopSearch(pattern);
        continueFlag.set(false);
    }

    @Override
    public void searchStarted(final int pageIndex) {
        final int offset = 
        	this.controller.bookSettings != null ? 
        	this.controller.bookSettings.firstPageOffset : 1;
        publishProgress(String.format("页面 %d…", pageIndex + offset));
    }

    @Override
    public void searchFinished(final int pageIndex) {
    }

    @Override
    protected RectF doInBackground(final String... params) {
        try {
            final int length = (params != null ? params.length : 0);
            pattern = length > 0 ? params[0] : null;
            final boolean forward = length >= 3 ? 
            	Boolean.parseBoolean(params[2]) : true;
            this.controller.searchModel.setPattern(pattern);
            final RectF current = forward ? 
            	this.controller.searchModel.moveToNext(this) : 
            	this.controller.searchModel.moveToPrev(this);
            targetPage = this.controller.searchModel.getCurrentPage();
            if (D) {
            	Log.e(TAG, "SearchTask.doInBackground(): " + targetPage + " " + current);
            }
            return current;
        } catch (final Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final RectF result) {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (final Throwable th) {
            }
        }
        if (result != null) {
            final RectF newRect = new RectF(result);
            final SearchControls sc = this.controller.helper
            		.getSearchControls(this.controller.getManagedComponent());
            final int controlsHeight = 3 + sc.getActualHeight();
            final float pageHeight = targetPage.getBounds(
            	this.controller.getZoomModel().getZoom()).height();
            newRect.offset(0, -(controlsHeight / pageHeight));
            this.controller.getDocumentController()
            	.goToLink(targetPage.index.docIndex, newRect,
            		AppSettings.current().storeSearchGotoHistory);
        } else {
            Toast.makeText(this.controller.getManagedComponent(), 
            	"没有找到文本", Toast.LENGTH_LONG).show();
        }
        this.controller.getDocumentController().redrawView();
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        final int length = (values != null ? values.length : 0);
        if (length == 0) {
            return;
        }
        final String last = values[length - 1];
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(
            	this.controller.getManagedComponent(), "", last, true);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setOnCancelListener(this);
        } else {
            progressDialog.setMessage(last);
        }
    }
}
