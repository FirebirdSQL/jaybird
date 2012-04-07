/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.pool;

/**
 * Object stack that implements a blocking LIFO (last-in-first-out) stack. The
 * implementation will block when the list is empty.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class BlockingStack {

	/**
	 * Container class for objects in the stack.
	 */
	private static class Node {
		private final Node next;

		private final Object object;

		/**
		 * Creates a Node with the specific object and next node.
		 * 
		 * @param object
		 * @param next
		 */
		private Node(Object object, Node next) {
			this.object = object;
			this.next = next;
		}

		private Node getNext() {
			return next;
		}

		private Object getObject() {
			return object;
		}
	}

	/**
	 * Actual top of the stack.
	 */
	private Node top = null;

	private final Object topLock = new Object();

	/**
	 * Checks to see if the stack is empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		synchronized (topLock) {
			return (top == null);
		}
	}

	/**
	 * Return, but do not remove, the object at the top of the stack.
	 * 
	 * @return the object at the top of the stack, null if stack is empty.
	 */
	public Object peek() {
		synchronized (topLock) {
			return top == null ? null : top.getObject();
		}
	}

	private Object extract() {
		Object result = null;
		synchronized (topLock) {
			if (top != null) {
				Node item = top;
				top = item.getNext();
				result = item.getObject();
			}
			if (top != null) {
				topLock.notify();
			}
		}
		return result;
	}

	/**
	 * Return and remove the object at the top of the stack. If the stack is
	 * empty, wait until an object exists.
	 * 
	 * @return the object at the top of the stack.
	 */
	public Object pop() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		
		synchronized (topLock) {
			while (isEmpty()) {
				try {
					topLock.wait();
				} catch (InterruptedException e) {
					topLock.notify();
					throw e;
				}
			}
			return extract();
		}
	}

	/**
	 * Return and remove the object at the top of the stack only if it is
	 * available within the specified number of milliseconds, otherwise return
	 * null.
	 * 
	 * @param msec
	 *            to wait for an object before returning null.
	 * @return the object at the top of the stack.
	 * @throws InterruptedException
	 */
	public Object pop(final long msec) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		
		long wait = msec;
		long start = System.currentTimeMillis();
		
		synchronized (topLock) {
			while (isEmpty() && wait > 0) {
				try {
					topLock.wait(wait);
					wait = msec - (System.currentTimeMillis() - start);
				} catch (InterruptedException e) {
					topLock.notify();
					throw e;
				}
			}
			return extract();
		}
	}

	/**
	 * Push an object onto the stack. If the stack is unavailable, wait until it
	 * becomes available.
	 * 
	 * @param item
	 *            to be pushed onto the stack.
	 * @throws InterruptedException
	 */
	public void push(Object item) throws InterruptedException {
		if (item == null)
			throw new IllegalArgumentException();
		if (Thread.interrupted())
			throw new InterruptedException();
		
		synchronized (topLock) {
			top = new Node(item, top);
			// Notify waiting threads.
			topLock.notify();
		}
	}
}
