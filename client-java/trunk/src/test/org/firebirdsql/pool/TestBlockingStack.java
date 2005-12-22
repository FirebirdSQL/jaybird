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
import java.util.Random;

import junit.framework.TestCase;

/**
 * Unit tests for the BlockingStack class.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class TestBlockingStack extends TestCase {
	private class stackAccess implements Runnable {
		private long startTime = 0;

		private int objectNumber = 0;

		private Thread t = new Thread(this);

		public stackAccess(int objectNumber, long startTime) {
			this.objectNumber = objectNumber;
			this.startTime = startTime;
			t.start();
		}

		public void run() {
			try {
				while (System.currentTimeMillis() != startTime)
					Thread.sleep(1);

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

	public void setUp() throws Exception {
		stack.push(new Integer(1));
		stack.push(new Integer(2));
		stack.push(new Integer(3));
		stack.push(new Integer(4));
		stack.push(new Integer(5));
	}

	public void testMultiThreadStackAccess() throws Exception {

		ArrayList accessors = new ArrayList();
		long startTime = System.currentTimeMillis() + 1000;
		for (int count = 0; count < 10; count++) {
			accessors.add(new stackAccess(count, startTime));
		}
		// The thread test should be take no longer than 5 seconds.
		Thread.sleep(2000);
		assertTrue("No exceptions.", true);
	}

	public void testStack() throws Exception {

		Integer value = (Integer) stack.peek();
		assertTrue("Peek should return first item in stack which is 5", value
				.intValue() == 5);

		value = (Integer) stack.pop();
		assertTrue("First item in stack should be 5", value.intValue() == 5);

		value = (Integer) stack.pop();
		assertTrue("First item in stack should be 4", value.intValue() == 4);

		value = (Integer) stack.pop();
		assertTrue("First item in stack should be 3", value.intValue() == 3);

		value = (Integer) stack.pop();
		assertTrue("First item in stack should be 2", value.intValue() == 2);

		value = (Integer) stack.pop();
		assertTrue("First item in stack should be 1", value.intValue() == 1);

		value = (Integer) stack.pop(0);
		assertTrue("Stack should be empty, null return", value == null);
	}
}
