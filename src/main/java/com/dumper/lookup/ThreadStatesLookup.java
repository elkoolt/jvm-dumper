package com.dumper.lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ksalnis
 *
 */

public class ThreadStatesLookup {
	
	private Map<String, Thread.State> THREAD_STATES = new HashMap<String, Thread.State>(){
		
		private static final long serialVersionUID = 1L;
	{
		put("runnable", Thread.State.RUNNABLE);
		put("blocked", Thread.State.BLOCKED);
		put("waiting", Thread.State.WAITING);
		put("timed_waiting", Thread.State.TIMED_WAITING);
	}};

	public Collection<Thread.State> findAll() {
		return THREAD_STATES.values();
	}

	public Thread.State findOne(String id) {
		return THREAD_STATES.get(id);
	}

}
