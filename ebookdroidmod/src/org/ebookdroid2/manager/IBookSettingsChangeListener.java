package org.ebookdroid2.manager;

public interface IBookSettingsChangeListener {
    void onBookSettingsChanged(BookSettings oldSettings, BookSettings newSettings, BookSettings.Diff diff);
}
