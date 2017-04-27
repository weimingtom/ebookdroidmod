package org.ebookdroid2.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<E> extends AbstractCollection<E> 
	implements Cloneable, Serializable {
    private static final long serialVersionUID = 2340985798034038923L;
    private static final int MIN_INITIAL_CAPACITY = 8;

	private final ThreadLocal<DeqIterator> iterators = 
    		new ThreadLocal<DeqIterator>();
    private transient E[] elements;
    private transient int head;
    private transient int tail;

    private void allocateElements(final int numElements) {
        int initialCapacity = MIN_INITIAL_CAPACITY;
        if (numElements >= initialCapacity) {
            initialCapacity = numElements;
            initialCapacity |= (initialCapacity >>> 1);
            initialCapacity |= (initialCapacity >>> 2);
            initialCapacity |= (initialCapacity >>> 4);
            initialCapacity |= (initialCapacity >>> 8);
            initialCapacity |= (initialCapacity >>> 16);
            initialCapacity++;
            if (initialCapacity < 0) {
                initialCapacity >>>= 1;
            }
        }
        elements = (E[]) new Object[initialCapacity];
    }

    private void doubleCapacity() {
        assert head == tail;
        final int p = head;
        final int n = elements.length;
        final int r = n - p; 
        final int newCapacity = n << 1;
        if (newCapacity < 0) {
            throw new IllegalStateException("Sorry, deque too big");
        }
        final Object[] a = new Object[newCapacity];
        System.arraycopy(elements, p, a, 0, r);
        System.arraycopy(elements, 0, a, r, p);
        elements = (E[]) a;
        head = 0;
        tail = n;
    }

    private <T> T[] copyElements(final T[] a) {
        if (head < tail) {
            System.arraycopy(elements, head, a, 0, size());
        } else if (head > tail) {
            final int headPortionLen = elements.length - head;
            System.arraycopy(elements, head, a, 0, headPortionLen);
            System.arraycopy(elements, 0, a, headPortionLen, tail);
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    public ArrayDeque() {
        elements = (E[]) new Object[16];
    }

    public ArrayDeque(final int numElements) {
        allocateElements(numElements);
    }

    public ArrayDeque(final Collection<? extends E> c) {
        allocateElements(c.size());
        addAll(c);
    }

    public void addFirst(final E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        elements[head = (head - 1) & (elements.length - 1)] = e;
        if (head == tail) {
            doubleCapacity();
        }
    }

    public void addLast(final E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        elements[tail] = e;
        if ((tail = (tail + 1) & (elements.length - 1)) == head) {
            doubleCapacity();
        }
    }

    public boolean offerFirst(final E e) {
        addFirst(e);
        return true;
    }

    public boolean offerLast(final E e) {
        addLast(e);
        return true;
    }

    public E removeFirst() {
        final E x = pollFirst();
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    public E removeLast() {
        final E x = pollLast();
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    public E pollFirst() {
        final int h = head;
        final E result = elements[h]; 
        if (result == null) {
            return null;
        }
        elements[h] = null; 
        head = (h + 1) & (elements.length - 1);
        return result;
    }

    public E pollLast() {
        final int t = (tail - 1) & (elements.length - 1);
        final E result = elements[t];
        if (result == null) {
            return null;
        }
        elements[t] = null;
        tail = t;
        return result;
    }

    public E getFirst() {
        final E x = elements[head];
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    public E getLast() {
        final E x = elements[(tail - 1) & (elements.length - 1)];
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    public E peekFirst() {
        return elements[head]; // elements[head] is null if deque empty
    }

    public E peekLast() {
        return elements[(tail - 1) & (elements.length - 1)];
    }

    public boolean removeFirstOccurrence(final Object o) {
        if (o == null) {
            return false;
        }
        final int mask = elements.length - 1;
        int i = head;
        E x;
        while ((x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i + 1) & mask;
        }
        return false;
    }

    public boolean removeLastOccurrence(final Object o) {
        if (o == null) {
            return false;
        }
        final int mask = elements.length - 1;
        int i = (tail - 1) & mask;
        E x;
        while ((x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i - 1) & mask;
        }
        return false;
    }

    @Override
    public boolean add(final E e) {
        addLast(e);
        return true;
    }

    public boolean offer(final E e) {
        return offerLast(e);
    }

    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public void push(final E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    private void checkInvariants() {
        assert elements[tail] == null;
        assert head == tail ? elements[head] == null : (elements[head] != null && elements[(tail - 1)
                & (elements.length - 1)] != null);
        assert elements[(head - 1) & (elements.length - 1)] == null;
    }

    private boolean delete(final int i) {
        checkInvariants();
        final E[] elements = this.elements;
        final int mask = elements.length - 1;
        final int h = head;
        final int t = tail;
        final int front = (i - h) & mask;
        final int back = (t - i) & mask;
        if (front >= ((t - h) & mask)) {
            throw new ConcurrentModificationException();
        }
        if (front < back) {
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else { 
            	System.arraycopy(elements, 0, elements, 1, i);
                elements[0] = elements[mask];
                System.arraycopy(elements, h, elements, h + 1, mask - h);
            }
            elements[h] = null;
            head = (h + 1) & mask;
            return false;
        } else {
            if (i < t) { 
            	System.arraycopy(elements, i + 1, elements, i, back);
                tail = t - 1;
            } else { 
            	System.arraycopy(elements, i + 1, elements, i, mask - i);
                elements[mask] = elements[0];
                System.arraycopy(elements, 1, elements, 0, t);
                tail = (t - 1) & mask;
            }
            return true;
        }
    }

    @Override
    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    @Override
    public boolean isEmpty() {
        return head == tail;
    }

    @Override
    public TLIterator<E> iterator() {
        DeqIterator iter = iterators.get();
        if (iter == null) {
            iter = new DeqIterator();
            return iter;
        }
        iter.cursor = head;
        iter.fence = tail;
        iter.lastRet = -1;
        iterators.set(null);
        return iter;
    }

    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DeqIterator implements TLIterator<E> {
        private int cursor = head;
        private int fence = tail;
        private int lastRet = -1;

        private DeqIterator() {
        }

        @Override
        public boolean hasNext() {
            return cursor != fence;
        }

        @Override
        public E next() {
            if (cursor == fence) {
                throw new NoSuchElementException();
            }
            final E result = elements[cursor];
            if (tail != fence || result == null) {
                throw new ConcurrentModificationException();
            }
            lastRet = cursor;
            cursor = (cursor + 1) & (elements.length - 1);
            return result;
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            if (delete(lastRet)) { // if left-shifted, undo increment in next()
                cursor = (cursor - 1) & (elements.length - 1);
                fence = tail;
            }
            lastRet = -1;
        }

        @Override
        public Iterator<E> iterator() {
            return this;
        }

        @Override
        public void release() {
            iterators.set(this);
        }
    }

    private class DescendingIterator implements Iterator<E> {
        private int cursor = tail;
        private int fence = head;
        private int lastRet = -1;

        @Override
        public boolean hasNext() {
            return cursor != fence;
        }

        @Override
        public E next() {
            if (cursor == fence) {
                throw new NoSuchElementException();
            }
            cursor = (cursor - 1) & (elements.length - 1);
            final E result = elements[cursor];
            if (head != fence || result == null) {
                throw new ConcurrentModificationException();
            }
            lastRet = cursor;
            return result;
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            if (!delete(lastRet)) {
                cursor = (cursor + 1) & (elements.length - 1);
                fence = head;
            }
            lastRet = -1;
        }
    }

    @Override
    public boolean contains(final Object o) {
        if (o == null) {
            return false;
        }
        final int mask = elements.length - 1;
        int i = head;
        E x;
        while ((x = elements[i]) != null) {
            if (o.equals(x)) {
                return true;
            }
            i = (i + 1) & mask;
        }
        return false;
    }

    @Override
    public boolean remove(final Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public void clear() {
        final int h = head;
        final int t = tail;
        if (h != t) {
            head = tail = 0;
            int i = h;
            final int mask = elements.length - 1;
            do {
                elements[i] = null;
                i = (i + 1) & mask;
            } while (i != t);
        }
    }

    @Override
    public Object[] toArray() {
        return copyElements(new Object[size()]);
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        final int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        copyElements(a);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public ArrayDeque<E> clone() {
        try {
            final ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
            result.elements = (E[]) Array.newInstance(elements.getClass().getComponentType(), elements.length);
            System.arraycopy(elements, 0, result, 0, elements.length);
            return result;
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private void writeObject(final ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size());
        final int mask = elements.length - 1;
        for (int i = head; i != tail; i = (i + 1) & mask) {
            s.writeObject(elements[i]);
        }
    }

    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        final int size = s.readInt();
        allocateElements(size);
        head = 0;
        tail = size;
        for (int i = 0; i < size; i++) {
            elements[i] = (E) s.readObject();
        }
    }
}
