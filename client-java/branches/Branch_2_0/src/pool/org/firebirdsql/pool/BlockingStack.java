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
 */
public class BlockingStack {

	/**
	 * Container class for objects in the stack.
	 */
	protected class Node {
		private Node next = null;

		private Object object = null;

		/**
		 * Creates a node with null object and null next node.
		 */
		public Node() {
			object = null;
			next = null;
		}

		/**
		 * Creates a node with the specified object and null next node.
		 * 
		 * @param object
		 */
		public Node(Object object) {
			this.object = object;
		}

		/**
		 * Creates a Node with the specific object and next node.
		 * 
		 * @param object
		 * @param next
		 */
		public Node(Object object, Node next) {
			this.object = object;
			this.next = next;

		}

		/**
		 * @return Returns the next.
		 */
		public Node getNext() {
			return next;
		}

		/**
		 * @return Returns the object.
		 */
		public Object getObject() {
			return object;
		}

		/**
		 * @param next
		 *            The next to set.
		 */
		public void setNext(Node next) {
			this.next = next;
		}

		/**
		 * @param object
		 *            The object to set.
		 */
		public void setObject(Object object) {
			this.object = object;
		}

	}

	/**
	 * Actual top of the stack.
	 */
	protected Node top = null;

	/**
	 * Number of threads waiting for an object.
	 */
	protected int waiting = 0;

	protected Object topLock = new Object();

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

	protected Object extract() {
		Object result = null;
		synchronized (topLock) {
			if (top != null) {
				Node item = top;
				top = item.getNext();
				result = item.getObject();
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

		Object result = extract();

		if (result == null)
			synchronized (topLock) {
				try {
					waiting++;
					topLock.wait();
					result = extract();
				} catch (InterruptedException e) {
					waiting--;
					topLock.notify();
					throw e;
				}
			}

		return result;
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
	public Object pop(long msec) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();

		Object result = extract();

		if (result == null)
			synchronized (topLock) {
				try {
					long wait = (msec <= 0) ? 0 : msec;
					long start = System.currentTimeMillis();
					while (wait > 0) {
						waiting++;
						topLock.wait(msec);
						wait = msec - (System.currentTimeMillis() - start);
						result = extract();
					}
				} catch (InterruptedException e) {
					waiting--;
					topLock.notify();
					throw e;
				}
			}

		return result;
	}

	/**
	 * Push an object onto the stack. If the stack is unavailable, wait until it
	 * becomes available.
	 * 
	 * @param item
	 *            to be pushed onto the stack.
	 * @return true is successful, false otherwise.
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
			if (waiting > 0) {
				topLock.notify();
			}
		}
	}
}
