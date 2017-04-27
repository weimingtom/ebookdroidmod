package org.ebookdroid2.manager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ebookdroid2.curl.PageAnimationType;
import org.ebookdroid2.manager.BookSettings.BookRotationType;
import org.ebookdroid2.page.PageIndex;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBSettingsManager extends SQLiteOpenHelper {
	private final static boolean D = false;
	private final static String TAG = "DBSettingsManager";		
	
    public static final int DB_VERSION = 7;
    public static final int VERSION = 7;
    public static final String DB_BOOK_CREATE = "create table book_settings ("
			+ "book varchar(1024) primary key, "
	        + "last_updated integer not null, "
	        + "first_page_offset integer not null, "
	        + "doc_page integer not null, "
	        + "view_page integer not null, "
	        + "zoom integer not null, "
	        + "view_mode integer not null, "
	        + "page_align integer not null, "
	        + "page_animation integer not null, "
	        + "flags long not null, "
	        + "offset_x integer not null, "
	        + "offset_y integer not null, "
	        + "contrast integer not null, "
	        + "exposure integer not null, "
	        + "type_specific varchar(4096)"
	        + ");";
    public static final String DB_BOOKMARK_CREATE = "create table bookmarks ("
            + "book varchar(1024) not null, "
            + "doc_page integer not null, "
            + "view_page integer not null, "
            + "name varchar(1024) not null, "
            + "offset_x integer not null, "
            + "offset_y integer not null"
            + ");";
    //FIXME:书
    public static final String DB_BOOK_GET_ALL = "SELECT book, last_updated, first_page_offset, doc_page, view_page, zoom, view_mode, page_align, page_animation, flags, offset_x, offset_y, contrast, exposure, type_specific FROM book_settings ORDER BY book ASC";
    public static final String DB_BOOK_GET_RNT = "SELECT book, last_updated, first_page_offset, doc_page, view_page, zoom, view_mode, page_align, page_animation, flags, offset_x, offset_y, contrast, exposure, type_specific FROM book_settings where last_updated > 0 ORDER BY last_updated DESC";
    public static final String DB_BOOK_GET_ONE = "SELECT book, last_updated, first_page_offset, doc_page, view_page, zoom, view_mode, page_align, page_animation, flags, offset_x, offset_y, contrast, exposure, type_specific FROM book_settings WHERE book=?";
    public static final String DB_BOOK_STORE = "INSERT OR REPLACE INTO book_settings (book, last_updated, first_page_offset, doc_page, view_page, zoom, view_mode, page_align, page_animation, flags, offset_x, offset_y, contrast, exposure, type_specific) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DB_BOOK_DROP = "DROP TABLE IF EXISTS book_settings";
    public static final String DB_BOOK_CLEAR_RECENT = "UPDATE book_settings set last_updated = 0";
    public static final String DB_BOOK_DEL = "DELETE FROM book_settings WHERE book=?";
    public static final String DB_BOOK_REMOVE_BOOK_FROM_RECENT = "UPDATE book_settings set last_updated = 0 WHERE book=?";
    //FIXME:书签
    public static final String DB_BOOKMARK_DROP = "DROP TABLE bookmarks";
    public static final String DB_BOOKMARK_GET_ALL = "SELECT doc_page, view_page, name FROM bookmarks WHERE book = ? ORDER BY view_page ASC";
    public static final String DB_BOOKMARK_DEL_ALL = "DELETE FROM bookmarks WHERE book=?";
    public static final String DB_BOOKMARK_STORE = "INSERT OR REPLACE INTO bookmarks (book, doc_page, view_page, name, offset_x, offset_y) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String DB_BOOKMARKS_DEL = "DELETE FROM bookmarks";
    //FIXME:位属性
    public static final long F_SPLIT_PAGES = 1 << 0;
    public static final long F_CROP_PAGES = 1 << 1;
    public static final long F_NIGHT_MODE = 1 << 2;
    public static final long F_AUTO_LEVELS = 1 << 3;
    public static final long F_NIGHT_MODE_POS_IMAGES = 1 << 4;
    public static final long F_ROTAION_OVR = 1 << 5;
    public static final long F_ROTAION_LAND = 1 << 6;
    public static final long F_SPLIT_RTL = 1 << 7;      
    //FIXME:
    public static final float OFFSET_FACTOR = 100000.0f;

    private SQLiteDatabase upgragingInstance;
    private SQLiteDatabase m_db;

    public DBSettingsManager(final Context context) {
        super(context, context.getPackageName() + ".settings", null, DB_VERSION);
        try {
            m_db = getWritableDatabase();
        } catch (final Exception ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, "Unexpected DB error: ", ex);
            }
        }
    }

    public void close() {
        if (m_db != null) {
            try {
                m_db.close();
            } catch (Exception ex) {
            }
            m_db = null;
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        upgragingInstance = db;
        if (D) {
        	Log.e(TAG, "Upgrading from version " + oldVersion + " to version " + newVersion);
        }
        try {
            final Map<String, BookSettings> bookSettings = getAllBooks();
            deleteAll();
            onDestroy(db);
            onCreate(db);
            restoreBookSettings(bookSettings.values());
        } finally {
            upgragingInstance = null;
            if (D) {
            	Log.e(TAG, "Upgrade finished");
            }
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (upgragingInstance != null) {
            return upgragingInstance;
        }
        if (m_db != null && m_db.isOpen()) {
            return m_db;
        }
        if (D) {
        	Log.e(TAG, "New DB connection created: " + m_db);
        }
        m_db = super.getWritableDatabase();
        return m_db;
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (upgragingInstance != null) {
            return upgragingInstance;
        }
        if (m_db != null && m_db.isOpen()) {
            return m_db;
        }
        return super.getReadableDatabase();
    }

    synchronized void closeDatabase(final SQLiteDatabase db) {
        if (db != upgragingInstance && db != m_db) {
            try {
                db.close();
            } catch (final Exception ex) {
            }
            if (D) {
        	   Log.e(TAG, "DB connection closed: " + m_db);
            }
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DB_BOOK_CREATE);
        db.execSQL(DB_BOOKMARK_CREATE);
    }

    //FIXME:@Override
    public void onDestroy(final SQLiteDatabase db) {
        db.execSQL(DB_BOOK_DROP);
        db.execSQL(DB_BOOKMARK_DROP);
    }
    
    public Map<String, BookSettings> getAllBooks() {
        return getBookSettings(DB_BOOK_GET_ALL, true);
    }

    public Map<String, BookSettings> getRecentBooks(final boolean all) {
        return getBookSettings(DB_BOOK_GET_RNT, all);
    }

    public BookSettings getBookSettings(final String fileName) {
        return getBookSettings(DB_BOOK_GET_ONE, fileName);
    }    
    
    public final boolean storeBookSettings(final BookSettings bs) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                if (bs.lastChanged > 0) {
                    bs.lastUpdated = System.currentTimeMillis();
                }
                if (D) {
                	Log.e(TAG, "Store: " + bs.toJSON());
                }
                storeBookSettings(bs, db);
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }
    
    public boolean storeBookSettings(final List<BookSettings> list) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                for (final BookSettings bs : list) {
                    if (bs.lastChanged > 0) {
                        bs.lastUpdated = System.currentTimeMillis();
                    }
                    if (D) {
                    	Log.e(TAG, "Store: " + bs.toJSON());
                    }
                    storeBookSettings(bs, db);
                }
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }
    
    public final boolean restoreBookSettings(final Collection<BookSettings> c) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                for (final BookSettings bs : c) {
                    storeBookSettings(bs, db);
                }
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }
    
    public boolean clearRecent() {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                db.execSQL(DB_BOOK_CLEAR_RECENT, new Object[] {});
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }
    
    public void delete(final BookSettings current) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                db.execSQL(DB_BOOK_DEL, new Object[] { current.fileName });
                db.setTransactionSuccessful();
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Delete book settings failed: ", th);
            }
        }
    }

    public boolean deleteAll() {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                db.execSQL(DB_BOOK_DROP, new Object[] {});
                db.execSQL(DB_BOOKMARK_DROP, new Object[] {});
                onCreate(db);
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }

    public final boolean updateBookmarks(final BookSettings book) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                updateBookmarks(book, db);
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Update bookmarks failed: ", th);
            }
        }
        return false;
    }
    
    public boolean deleteBookmarks(final String book, final List<Bookmark> bookmarks) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                final Object[] delArgs = { book };
                db.execSQL(DB_BOOKMARK_DEL_ALL, delArgs);
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Deleting bookmarks failed: ", th);
            }
        }
        return false;
    }

    public boolean deleteAllBookmarks() {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                db.execSQL(DB_BOOKMARKS_DEL, new Object[] {});
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
        	if (D) {
            	Log.e(TAG, "Update book settings failed: ", th);
            }
        }
        return false;
    }

    public boolean removeBookFromRecents(final BookSettings bs) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                db.execSQL(DB_BOOK_REMOVE_BOOK_FROM_RECENT, new Object[] { bs.fileName });
                db.setTransactionSuccessful();
                return true;
            } finally {
                endTransaction(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Removing book from recents failed: ", th);
            }
        }
        return false;
    }

    protected void loadBookmarks(final BookSettings book, final SQLiteDatabase db) {
        loadBookmarks(book, db, DB_BOOKMARK_GET_ALL);
    }
    
    protected Bookmark createBookmark(final Cursor c) {
        int index = 0;
        final int docIndex = c.getInt(index++);
        final int viewIndex = c.getInt(index++);
        final String name = c.getString(index++);
        return new Bookmark(name, new PageIndex(docIndex, viewIndex), 0, 0);
    }
    
    final void close(final Cursor c) {
        try {
            c.close();
        } catch (final Exception ex) {
        }
    }
    
    protected final void loadBookmarks(final BookSettings book, final SQLiteDatabase db, final String query) {
        book.bookmarks.clear();
        String arg = book.fileName;
        if (arg == null || arg.length() == 0) {
        	arg = "";
        }
        final Cursor c = db.rawQuery(query, new String[] {arg});
        if (c != null) {
            try {
                for (boolean next = c.moveToFirst(); next; next = c.moveToNext()) {
                    final Bookmark bm = createBookmark(c);
                    book.bookmarks.add(bm);
                }
                if (D) {
                	Log.e(TAG, "Bookmarks loaded for " + book.fileName + ": " + book.bookmarks.size());
                }
            } finally {
                close(c);
            }
        }
    }
    
    protected final Map<String, BookSettings> getBookSettings(final String query, final boolean all) {
        final Map<String, BookSettings> map = new LinkedHashMap<String, BookSettings>();
        try {
            final SQLiteDatabase db = this.getReadableDatabase();
            try {
                final Cursor c = db.rawQuery(query, null);
                if (c != null) {
                    try {
                        for (boolean next = c.moveToFirst(); next; next = c.moveToNext()) {
                            final BookSettings bs = createBookSettings(c);
                            loadBookmarks(bs, db);
                            map.put(bs.fileName, bs);
                            if (!all) {
                                break;
                            }
                        }
                    } finally {
                        close(c);
                    }
                }
            } finally {
                this.closeDatabase(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Retrieving book settings failed: ", th);
            }
        }
        return map;
    }
    
    protected final BookSettings getBookSettings(final String query, final String fileName) {
        try {
            final SQLiteDatabase db = this.getReadableDatabase();
            try {
                final Cursor c = db.rawQuery(query, new String[] { fileName });
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            final BookSettings bs = createBookSettings(c);
                            loadBookmarks(bs, db);
                            return bs;
                        }
                    } finally {
                        close(c);
                    }
                }
            } finally {
                this.closeDatabase(db);
            }
        } catch (final Throwable th) {
        	th.printStackTrace();
            if (D) {
            	Log.e(TAG, "Retrieving book settings failed: ", th);
            }
        }
        return null;
    }
    
    protected long getFlags(final BookSettings bs) {
        return
        (bs.splitPages ? F_SPLIT_PAGES : 0) |
        (bs.cropPages ? F_CROP_PAGES : 0) |
        (bs.nightMode ? F_NIGHT_MODE : 0) |
        (bs.autoLevels ? F_AUTO_LEVELS : 0) |
        (bs.positiveImagesInNightMode ? F_NIGHT_MODE_POS_IMAGES : 0) |
        getRotationFlags(bs) |
        (bs.splitRTL ? F_SPLIT_RTL : 0);
    }
    
    protected void storeBookSettings(final BookSettings bs, final SQLiteDatabase db) {
        final Object[] args = new Object[] {
            bs.fileName,
            bs.lastUpdated,
            bs.firstPageOffset,
            bs.currentPage.docIndex,
            bs.currentPage.viewIndex,
            bs.zoom,
            bs.viewMode.ordinal(),
            bs.pageAlign.ordinal(),
            bs.animationType.ordinal(),
            getFlags(bs),
            (int) (bs.offsetX * OFFSET_FACTOR),
            (int) (bs.offsetY * OFFSET_FACTOR),
            bs.contrast,
            bs.exposure,
            bs.typeSpecific != null ? bs.typeSpecific.toString() : null
        };
        db.execSQL(DB_BOOK_STORE, args);
        updateBookmarks(bs, db);
    }

    protected void updateBookmarks(final BookSettings book, final SQLiteDatabase db) {
        final Object[] delArgs = { book.fileName };
        db.execSQL(DB_BOOKMARK_DEL_ALL, delArgs);
        for (final Bookmark bs : book.bookmarks) {
            final Object[] args = new Object[] {
                book.fileName,
                bs.page.docIndex,
                bs.page.viewIndex,
                bs.name,
                (int) (bs.offsetX * OFFSET_FACTOR),
                (int) (bs.offsetY * OFFSET_FACTOR)
            };
            db.execSQL(DB_BOOKMARK_STORE, args);
        }
        if (D) {
        	Log.e(TAG, "Bookmarks stored for " + book.fileName + ": " + book.bookmarks.size());
        }
    }
    
    protected long getRotationFlags(final BookSettings bs) {
        if (bs.rotation == null || bs.rotation == BookRotationType.UNSPECIFIED) {
            return 0;
        }
        return F_ROTAION_OVR | (bs.rotation == BookRotationType.LANDSCAPE ? F_ROTAION_LAND : 0);
    }
    
    protected BookSettings createBookSettings(final Cursor c) {
        int index = 0;
        final BookSettings bs = new BookSettings(c.getString(index++));
        bs.lastUpdated = c.getLong(index++);
        bs.firstPageOffset = c.getInt(index++);
        bs.currentPage = new PageIndex(c.getInt(index++), c.getInt(index++));
        bs.zoom = c.getInt(index++);
        bs.viewMode = DocumentViewMode.getByOrdinal(c.getInt(index++));
        bs.pageAlign = PageAlign.values()[c.getInt(index++)];
        bs.animationType = PageAnimationType.values()[c.getInt(index++)];
        setFlags(bs, c.getLong(index++));
        bs.offsetX = c.getInt(index++) / OFFSET_FACTOR;
        bs.offsetY = c.getInt(index++) / OFFSET_FACTOR;
        bs.contrast = c.getInt(index++);
        bs.exposure = c.getInt(index++);
        String str = c.getString(index++);
        if (str != null && str.length() > 0) {
            try {
                bs.typeSpecific = new JSONObject(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bs;
    }
    
    protected void setFlags(final BookSettings bs, final long flags) {
        bs.splitPages = (flags & F_SPLIT_PAGES) != 0;
        bs.cropPages = (flags & F_CROP_PAGES) != 0;
        bs.nightMode = (flags & F_NIGHT_MODE) != 0;
        bs.positiveImagesInNightMode = (flags & F_NIGHT_MODE_POS_IMAGES) != 0;
        bs.autoLevels = (flags & F_AUTO_LEVELS) != 0;
        if ((flags & F_ROTAION_OVR) != 0) {
            bs.rotation = (flags & F_ROTAION_LAND) != 0 ? BookRotationType.LANDSCAPE : BookRotationType.PORTRAIT;
        } else {
            bs.rotation = BookRotationType.UNSPECIFIED;
        }
        bs.splitRTL = (flags & F_SPLIT_RTL) != 0;
    }

    private void endTransaction(final SQLiteDatabase db) {
        try {
            db.endTransaction();
        } catch (final Exception ex) {
        	
        }
        this.closeDatabase(db);
    }
}
