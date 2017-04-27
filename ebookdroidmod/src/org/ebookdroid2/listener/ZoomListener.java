package org.ebookdroid2.listener;

public interface ZoomListener {
    void zoomChanged(float oldZoom, float newZoom, boolean committed);
}
