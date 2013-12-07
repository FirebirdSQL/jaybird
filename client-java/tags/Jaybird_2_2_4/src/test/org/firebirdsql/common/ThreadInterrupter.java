package org.firebirdsql.common;

import java.util.TimerTask;

/**
 * Helper class to interrupt a Thread from a Timer.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class ThreadInterrupter extends TimerTask {
	
	private final Thread toInterrupt;
	
	/**
	 * Create a ThreadInterrupter for the current thread.
	 */
	public ThreadInterrupter() {
		this(Thread.currentThread());
	}
	
	/**
	 * Create a ThreadInterrupter for the specified Thread.
	 * 
	 * @param toInterrupt The Thread to interrupt
	 */
	public ThreadInterrupter(Thread toInterrupt) {
		this.toInterrupt = toInterrupt;
	}

	@Override
	public void run() {
		toInterrupt.interrupt();
	}
}
