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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import org.firebirdsql.common.ThreadInterrupter;

import junit.framework.TestCase;

/**
 * Unit tests for the BlockingStack class.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestBlockingStack extends TestCase {
	private class StackAccess implements Runnable {
		private long startTime = 0;

		private int objectNumber = 0;

		private final Thread t;

		public StackAccess(int objectNumber, long startTime) {
			this.objectNumber = objectNumber;
			this.startTime = startTime;
			t = new Thread(this, "StackAccess" + objectNumber);
			t.start();
		}

		public void run() {
			try {
				do {
					Thread.sleep(10);
				} while (System.currentTimeMillis() <= startTime);

				Integer item = (Integer) stack.pop();
				System.out.println("Popped object #" + objectNumber + " in "
						+ (System.currentTimeMillis() - startTime)
						+ " ms : Object value " + item);
				Thread.sleep(random.nextInt(1000));
				stack.push(item);
				System.out.println("Pushed object #" + objectNumber + " in "
						+ (System.currentTimeMillis() - startTime)
						+ " ms : Object value " + item);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Random random = new Random();

	private BlockingStack stack = new BlockingStack();

	private void initStack() throws Exception {
		stack.push(Integer.valueOf(1));
		stack.push(Integer.valueOf(2));
		stack.push(Integer.valueOf(3));
		stack.push(Integer.valueOf(4));
		stack.push(Integer.valueOf(5));
	}

	/**
	 * Test if concurrent pop and then push does not block threads.
	 * <p>
	 * Test will fail if the StackAccess threads used in the test have not
	 * completed within 5 seconds.
	 * </p>
	 * 
	 * @throws Exception
	 */
	public void testMultiThreadStackAccess() throws Exception {
		initStack();

		ArrayList accessors = new ArrayList();
		long startTime = System.currentTimeMillis() + 1000;
		for (int count = 0; count < 10; count++) {
			accessors.add(new StackAccess(count, startTime));
		}

		Timer timer = new Timer("waitLimit");
		timer.schedule(new ThreadInterrupter(), 5000);

		try {
			// Wait for StackAccess threads to complete
			for (int idx = 0; idx < accessors.size(); idx++) {
				((StackAccess) accessors.get(idx)).t.join();
			}
		} catch (InterruptedException e) {
			// waitLimit exceeded, interrupt StackAccess threads
			for (int idx = 0; idx < accessors.size(); idx++) {
				((StackAccess) accessors.get(idx)).t.interrupt();
			}
			fail("Test did not complete within expected run-time, most likely one or more StackAccess threads are blocked");
		}
		timer.cancel();
	}

	/**
	 * Test single-threaded use of {@link BlockingStack#pop()} for returning
	 * elements in order.
	 * 
	 * @throws Exception
	 */
	public void testPop_singleThread_noLimit() throws Exception {
		initStack();

		Integer value = (Integer) stack.peek();
		assertEquals("Peek should return first item in stack which is 5", 5,
				value.intValue());

		for (int expectedValue = 5; expectedValue > 0; expectedValue--) {
			value = (Integer) stack.pop();
			assertEquals("Unexpected item popped from stack", expectedValue,
					value.intValue());
		}

		value = (Integer) stack.pop(0);
		assertNull("Stack should be empty, null return", value);
	}

	/**
	 * Test single-threaded use of {@link BlockingStack#pop(long)} for returning 
	 * elements in order.
	 * 
	 * @throws Exception
	 */
	public void testPop_singleThread_timeLimit() throws Exception {
		initStack();

		Integer value = (Integer) stack.peek();
		assertEquals("Peek should return first item in stack which is 5", 5,
				value.intValue());

		for (int expectedValue = 5; expectedValue > 0; expectedValue--) {
			value = (Integer) stack.pop(100);
			assertEquals("Unexpected item popped from stack", expectedValue,
					value.intValue());
		}

		value = (Integer) stack.pop(0);
		assertNull("Stack should be empty, null return", value);
	}

	/**
	 * Ensure that a successful use of {@link BlockingStack#pop()} returns a
	 * pushed value when another Thread waiting for the same stack has been
	 * interrupted.
	 * <p>
	 * Background: the original implementation of {@link BlockingStack#pop()}
	 * did not wait in a loop. If two (or more) threads were waiting in pop, and
	 * one of them was interrupted, then the notified Thread could return null
	 * instead of continuing to wait for an object to become available.
	 * <p>
	 * 
	 * @throws Exception
	 */
	public void testPop_empty_interrupt() throws Exception {
		Runnable popRunnable = new Runnable() {
			public void run() {
				try {
					stack.pop();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		Thread wait1 = new Thread(popRunnable, "wait1");

		final List resultHolder = Collections.synchronizedList(new ArrayList());
		Runnable resultRunnable = new Runnable() {
			public void run() {
				try {
					resultHolder.add(stack.pop());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		Thread wait2 = new Thread(resultRunnable, "wait2");

		wait1.start();
		wait2.start();
		// Ensure wait1 and wait2 get a chance to run (and block)
		Thread.sleep(10);
		wait1.interrupt();

		// Ensure wait2 gets a chance to wake up before we push an object
		Thread.sleep(10);
		Object marker = new Object();
		stack.push(marker);
		wait2.join();

		assertEquals("Expected pushed value to have been popped", marker,
				resultHolder.get(0));
	}

	/**
	 * Test if use of {@link BlockingStack#pop(long)} does not consume entire
	 * stack if it is filled afterwards.
	 * <p>
	 * Background: In the original implementation, the pop call to BlockingStack
	 * would continue to remove objects from the stack until either the stack
	 * was empty, or the timeout expired. And then return the latest item
	 * removed or null.
	 * </p>
	 * 
	 * @throws Exception
	 */
	public void testPop_emptyAtStart_timeLimit() throws Exception {
		final List resultHolder = Collections.synchronizedList(new ArrayList());
		Runnable resultRunnable = new Runnable() {
			public void run() {
				try {
					resultHolder.add(stack.pop(1000));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};

		Thread wait1 = new Thread(resultRunnable, "wait1");
		wait1.start();
		// Ensure wait1 gets a chance to run (and block)
		Thread.sleep(10);

		stack.push(Integer.valueOf(1));
		wait1.join();

		assertEquals("Expected pushed value to have been popped",
				Integer.valueOf(1), resultHolder.get(0));
	}
}
