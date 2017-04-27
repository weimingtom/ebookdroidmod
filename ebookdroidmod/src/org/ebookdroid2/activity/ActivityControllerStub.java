package org.ebookdroid2.activity;

import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.core.ViewContollerStub;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.model.DecodingProgressModel;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.model.SearchModel;
import org.ebookdroid2.model.ZoomModel;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.view.IView;
import org.ebookdroid2.view.ViewStub;

import android.app.Activity;
import android.content.Context;

public class ActivityControllerStub implements IActivityController {
    public static final ActivityControllerStub STUB = new ActivityControllerStub();
    public static final DocumentModel DM_STUB = new DocumentModel(0);
    public static final ZoomModel ZM_STUB = new ZoomModel();

    private SearchModel SEARCH_STUB = new SearchModel(this);
    
    private ActivityControllerStub() {
        this.initAbstractComponentController(null);
    }

    @Override
    public Context getContext() {
        return m_managedComponent.getApplicationContext();
    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public DecodeService getDecodeService() {
        return null;
    }

    @Override
    public BookSettings getBookSettings() {
        return null;
    }

    @Override
    public DocumentModel getDocumentModel() {
        return DM_STUB;
    }

    @Override
    public IView getView() {
        return ViewStub.STUB;
    }

    @Override
    public IViewController getDocumentController() {
        return ViewContollerStub.STUB;
    }

    @Override
    public Object getActionController() {
        return null;
    }

    @Override
    public ZoomModel getZoomModel() {
        return ZM_STUB;
    }

    @Override
    public DecodingProgressModel getDecodingProgressModel() {
        return null;
    }

    @Override
    public void jumpToPage(final int viewIndex, final float offsetX, final float offsetY, final boolean addToHistory) {
    }

    @Override
    public SearchModel getSearchModel() {
        return SEARCH_STUB;
    }

    @Override
    public void runOnUiThread(final Runnable r) {
    }
    
    protected Activity m_managedComponent;

    protected void initAbstractComponentController(final Activity managedComponent) {
    	m_managedComponent = managedComponent;
    }

    @Override
    public Activity getManagedComponent() {
        return m_managedComponent;
    }
}
