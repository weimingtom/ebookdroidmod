package org.ebookdroid2.util;

import java.util.Iterator;

public interface TLIterator<E> extends Iterator<E>, Iterable<E> {
    void release();
}
