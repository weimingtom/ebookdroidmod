package org.ebookdroid2.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自旋锁
 *
 */
public class Flag {
	private final AtomicBoolean flag;

	public Flag() {
		this(false);
	}

	public Flag(final boolean initial) {
		flag = new AtomicBoolean(initial);
	}

	public boolean get() {
		return flag.get();
	}

	public synchronized void set() {
		if (flag.compareAndSet(false, true)) {
			this.notifyAll();
		}
	}

	public synchronized void clear() {
		if (flag.compareAndSet(true, false)) {
			this.notifyAll();
		}
	}

	public synchronized boolean waitFor(final TimeUnit unit, final long timeout) {
		try {
			unit.timedWait(this, timeout);
		} catch (final InterruptedException ex) {
			Thread.interrupted();
		}
		return get();
	}
}
