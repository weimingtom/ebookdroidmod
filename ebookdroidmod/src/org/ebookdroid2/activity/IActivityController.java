package org.ebookdroid2.activity;

import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.model.DecodingProgressModel;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.model.SearchModel;
import org.ebookdroid2.model.ZoomModel;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.view.IView;

import android.app.Activity;
import android.content.Context;

public interface IActivityController {
    Context getContext();
    Activity getActivity();
    BookSettings getBookSettings();
    DecodeService getDecodeService();
    DocumentModel getDocumentModel();
    SearchModel getSearchModel();
    IView getView();
    IViewController getDocumentController();
    //FIXME:返回类型是IActionController?
    Object/*IActionController*/ getActionController(); 
    ZoomModel getZoomModel();
    DecodingProgressModel getDecodingProgressModel();
    void jumpToPage(int viewIndex, float offsetX, float offsetY, boolean addToHistory);
    void runOnUiThread(Runnable r);
    //IActionController
    Activity getManagedComponent();
}
