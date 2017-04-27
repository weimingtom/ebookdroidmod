package org.ebookdroid2.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.util.Log;

/**
 * FIXME:invertMountPrefix是否有必要，可能有不兼容问题
 */
public class CacheManager {
	private final static boolean D = false;
	private final static String TAG = "CacheManager";	
	
    protected static Context s_context;
    protected static File s_cacheDir;
    //FIXME:用于invertMountPrefix
    private static ArrayList<String> mounts = new ArrayList<String>();
	private static ArrayList<String> mountsPR = new ArrayList<String>();
	private static ArrayList<String> aliases = new ArrayList<String>();
	private static ArrayList<String> aliasesPR = new ArrayList<String>();
	static {
		for (final File f : new File("/").listFiles()) {
			if (f.isDirectory()) {
				try {
					final String cp = f.getCanonicalPath();
					final String ap = f.getAbsolutePath();
					if (!cp.equals(ap)) {
						aliases.add(ap);
						aliasesPR.add(ap + "/");
						mounts.add(cp);
						mountsPR.add("/");
					}
				} catch (final IOException ex) {
					System.err.println(ex.getMessage());
				}
			}
		}
	}

    public static void init(final Context context) {
    	if (s_context == null) {
	        s_context = context;
	        s_cacheDir = context.getFilesDir();
	        if (D) {
	        	Log.e(TAG, "Default app cache dir: " + 
	        		(s_cacheDir != null ? s_cacheDir.getAbsolutePath() : null));
	        }
    	}
    }
    	
    public static File getDocumentFile(final String path) {
    	final String amd5 = md5(path);
        final File adcf = new File(s_cacheDir, amd5 + ".dcache");
        if (adcf.exists()) {
            return adcf;
        }
        final String mpath = invertMountPrefix(path);
        final String mmd5 = mpath != null ? md5(mpath) : null;
        final File mdcf = new File(s_cacheDir, mmd5 + ".dcache");
        if (mdcf.exists()) {
            return mdcf;
        }
        return adcf;
    }

    public static void clear(final String path) {
        if (path == null || path.length() == 0) {
            return;
        }
        final String amd5 = md5(path);
        final String mpath = invertMountPrefix(path);
        final String mmd5 = mpath != null ? md5(mpath) : null;
        if (amd5 != null) {
	        final String[] files = s_cacheDir != null ? s_cacheDir.list(new FilePrefixFilter(amd5 + ".")) : null;
	        if (files != null && files.length > 0) {
	            for (final String file : files) {
	                new File(s_cacheDir, file).delete();
	            }
	        }
        }
        if (mmd5 != null) {
            final String[] files2 = s_cacheDir != null ? s_cacheDir.list(new FilePrefixFilter(mmd5 + ".")) : null;
            if (files2 != null && files2.length > 0) {
                for (final String file : files2) {
                    new File(s_cacheDir, file).delete();
                }
            }
        }
    }
    
	private static String md5(final String in) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(in.getBytes());
			final byte[] a = digest.digest();
			final int len = a.length;
			final StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//FIXME:这个类可以简化
	public static class FilePrefixFilter implements FileFilter, FilenameFilter {
	    private final Set<String> prefixes;

	    public FilePrefixFilter(final Set<String> prefixes) {
	        this.prefixes = prefixes;
	    }

	    public FilePrefixFilter(final String... prefixes) {
	        this.prefixes = new HashSet<String>(Arrays.asList(prefixes));
	    }

	    @Override
	    public final boolean accept(final File file) {
	        return acceptImpl(file.getName().toLowerCase());
	    }

	    @Override
	    public boolean accept(final File dir, final String name) {
	        return acceptImpl(name.toLowerCase());
	    }

	    public boolean accept(final String name) {
	        if (name == null || name.length() == 0) {
	            return false;
	        }
	        if (!new File(name).exists()) {
	            return false;
	        }
	        return acceptImpl(name.toLowerCase());
	    }

	    protected boolean acceptImpl(final String name) {
	        boolean res = false;
	        for (final String prefix : prefixes) {
	            res |= acceptImpl(prefix, name);
	        }
	        return res;
	    }

	    protected boolean acceptImpl(final String prefix, final String name) {
	        return name != null && name.startsWith(prefix);
	    }

	    @Override
	    public boolean equals(final Object obj) {
	        if (this == obj) {
	            return true;
	        }
	        if (obj instanceof FilePrefixFilter) {
	            final FilePrefixFilter that = (FilePrefixFilter) obj;
	            return this.prefixes.equals(that.prefixes);
	        }
	        return false;
	    }

	    @Override
	    public int hashCode() {
	        return this.prefixes.hashCode();
	    }

	    @Override
	    public String toString() {
	        return this.getClass().getSimpleName() + prefixes;
	    }
	}

	//FIXME:不知道有什么用
	public static final String invertMountPrefix(final String fileName) {
		for (int i = 0, n = Math.min(aliases.size(), mounts.size()); i < n; i++) {
			final String alias = aliases.get(i);
			final String mount = mounts.get(i);
			if (fileName.equals(alias)) {
				return mount;
			}
			if (fileName.equals(mount)) {
				return alias;
			}
		}
		for (int i = 0, n = Math.min(aliasesPR.size(), mountsPR.size()); i < n; i++) {
			final String alias = aliasesPR.get(i);
			final String mount = mountsPR.get(i);
			if (fileName.startsWith(alias)) {
				return mount + fileName.substring(alias.length());
			}
			if (fileName.startsWith(mount)) {
				return alias + fileName.substring(mount.length());
			}
		}
		return null;
	}
}
