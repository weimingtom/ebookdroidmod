package org.ebookdroid2.dialog;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.adapter.BookmarkAdapter;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.Bookmark;
import org.ebookdroid2.model.DocumentModel;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.task.SeekBarIncrementHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.iteye.weimingtom.ebdmod.R;

/**
 * FIXME:如果想制造崩溃，可在这里引用别的layout的id
 */
public class GoToPageDialog extends Dialog {
	//FIXME:这个可以用来测试崩溃
	private final static boolean TEST_CRASH = false; 
	
	//FIXME: 不需要最大化了
    private final static boolean MAX_DIALOG = false; 
	
    private final IActivityController base;
    private final SeekBarIncrementHandler handler;
    private BookmarkAdapter adapter;
    private Bookmark current;
    private int offset;
    
    public GoToPageDialog(final IActivityController base) {
        super(base.getContext());
        this.setContentView(R.layout.ebookdroid_gotopage);
        this.base = base;
        this.handler = new SeekBarIncrementHandler();
        final BookSettings bs = base.getBookSettings();
        this.offset = (bs != null ? bs.firstPageOffset : 1);
        this.setTitle("转至页面"); //FIXME:这个标题
        final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
        final EditText editText = (EditText) findViewById(R.id.pageNumberTextEdit);
        this.findViewById(R.id.bookmark_add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showAddBookmarkDlg(arg0);
			}
		});
        this.findViewById(R.id.bookmark_remove_all).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDeleteAllBookmarksDlg();
			}
		});
        if (TEST_CRASH) {
        	//FIXME:这个会导致崩溃，因为不在当前layout里面
            this.findViewById(R.id.bookmark_remove).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//FIXME:R.id.bookmark_remove
				}
			});
        }
        this.findViewById(R.id.goToButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				goToPageAndDismiss();
			}
		});
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView textView, final int actionId, final KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE)) {
                    if ((keyEvent == null || 
                    	keyEvent.getAction() == KeyEvent.ACTION_UP)) {
                        //FIXME:这里跳转未实现
                    	//FIXME:R.id.actions_gotoPage
                    	//IActionController.VIEW_PROPERTY=textView
                    }
                    return true;
                }
                return false;
            }
        });
        this.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onDialogCancel();
			}
		});
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                updateControls(progress, false);
            }
        });
        handler.init(new SeekBarIncrementHandler.DialogBridge(this), seekbar, R.id.seekbar_minus, R.id.seekbar_plus);  
    }

	@Override
    protected void onStart() {
        super.onStart();
        //FIXME: 不需要最大化了
        if (MAX_DIALOG) {
        	maximizeWindow(getWindow());
        }
        final DocumentModel dm = base.getDocumentModel();
        final Page lastPage = dm != null ? dm.getLastPageObject() : null;
        final int current = dm != null ? dm.getCurrentViewPageIndex() : 0;
        final int max = lastPage != null ? lastPage.index.viewIndex : 0;
        adapter = new BookmarkAdapter(this.getContext(), this, lastPage, base.getBookSettings());
        final ListView bookmarks = (ListView) findViewById(R.id.bookmarks);
        bookmarks.setAdapter(adapter);
        final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setMax(max);
        updateControls(current, true);
    }
	
	public static void maximizeWindow(final Window window) {
		window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	}

    @Override
    protected void onStop() {
        final ListView bookmarks = (ListView) findViewById(R.id.bookmarks);
        bookmarks.setAdapter(null);
        adapter = null;
        //FIXME:刷新上下文菜单
    }

    //FIXME:跳转按钮
    //R.id.goToButton
    public void goToPageAndDismiss() {
        if (navigateToPage()) {
            dismiss();
        }
    }

    //FIXME:设置书签页码
    //R.id.actions_setBookmarkedPage)
    public void updateControls(View view) {
        final Bookmark bookmark = (Bookmark) view.getTag();
        final Page actualPage = bookmark.page.getActualPage(base.getDocumentModel(), adapter.bookSettings);
        if (actualPage != null) {
            updateControls(actualPage.index.viewIndex, true);
        }
        current = bookmark;
    }

    //FIXME:显示删除书签对话框
    //R.id.actions_showDeleteBookmarkDlg
    public void showDeleteBookmarkDlg(final View view) {
        final Bookmark bookmark = view != null ? (Bookmark) view.getTag() : null;
        if (bookmark.service) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("删除书签");
        builder.setMessage("确定删除所有书签？");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				removeBookmark(bookmark);
			}
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//do nothing
			}
        }).show();
    }

    //FIXME:删除书签
    //R.id.actions_removeBookmark
    public void removeBookmark(final Bookmark bookmark) {
        adapter.remove(bookmark);
    }

    //FIXME:添加书签
    //R.id.bookmark_add
    public void showAddBookmarkDlg(View view) {
        final Context context = getContext();
        final Bookmark bookmark = (Bookmark) view.getTag();
        final EditText input = (EditText) LayoutInflater.from(getContext()).inflate(R.layout.ebookdroid_bookmark_edit, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("输入书签描述");
        builder.setView(input);
        if (bookmark == null) {
        	builder.setTitle("添加书签");
            final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
            final int viewIndex = seekbar.getProgress();
            input.setText("页面" + " " + (viewIndex + offset));
            input.selectAll();
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface arg0, int arg1) {
    				addBookmark(input.getText(), null, viewIndex);
    			}
            });
        } else {
            builder.setTitle("编辑书签");
            input.setText(bookmark.name);
            input.selectAll();
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface arg0, int arg1) {
    				addBookmark((Editable)input, bookmark, 0);
    			}
            });
        }
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//do nothing
			}
        }).show();
    }

    //FIXME:添加书签
    //R.id.actions_addBookmark)
    public void addBookmark(final Editable value, final Bookmark bookmark, final Integer viewIndex) {
        if (bookmark != null) {
            bookmark.name = value.toString();
            adapter.update(bookmark);
        } else {
            final Page page = base.getDocumentModel().getPageObject(viewIndex);
            adapter.add(new Bookmark(value.toString(), page.index, 0, 0));
            adapter.notifyDataSetChanged();
        }
    }

    //FIXME:删除所有
    //R.id.bookmark_remove_all
    //R.id.actions_showDeleteAllBookmarksDlg
    public void showDeleteAllBookmarksDlg() {
        if (!adapter.hasUserBookmarks()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("删除书签");
        builder.setMessage("确定删除所有书签?");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				deleteAllBookmarks();
			}
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//do nothing
			}
        }).show();
    }

    //FIXME:删除所有书签
    //R.id.actions_deleteAllBookmarks
    public void deleteAllBookmarks() {
        adapter.clear();
    }

    private void updateControls(final int viewIndex, final boolean updateBar) {
        final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
        final EditText editText = (EditText) findViewById(R.id.pageNumberTextEdit);
        editText.setText("" + (viewIndex + offset));
        editText.selectAll();
        if (updateBar) {
            seekbar.setProgress(viewIndex);
        }
        current = null;
    }

    private boolean navigateToPage() {
        if (current != null) {
            final Page actualPage = current.page.getActualPage(base.getDocumentModel(), adapter.bookSettings);
            if (actualPage != null) {
                base.jumpToPage(actualPage.index.viewIndex, current.offsetX, current.offsetY,
                        AppSettings.current().storeGotoHistory);
                return true;
            }
            return false;
        }
        final EditText text = (EditText) findViewById(R.id.pageNumberTextEdit);
        final int pageNumber = getEnteredPageIndex(text);
        final int pageCount = base.getDocumentModel().getPageCount();
        if (pageNumber < 0 || pageNumber >= pageCount) {
            final String msg = String.format("页数超出范围，可用范围: %d-%d", offset, pageCount - 1 + offset);
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            return false;
        }
        base.jumpToPage(pageNumber, 0, 0, AppSettings.current().storeGotoHistory);
        return true;
    }

    private int getEnteredPageIndex(final EditText text) {
        try {
            return Integer.parseInt(text.getText().toString()) - offset;
        } catch (final Exception e) {
        }
        return -1;
    }
    
    //FIXME:取消对话框
    //R.id.btnCancel
    public void onDialogCancel() {
    	this.dismiss();
    }
}
