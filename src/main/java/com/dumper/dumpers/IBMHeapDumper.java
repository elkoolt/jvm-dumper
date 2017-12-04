package com.dumper.dumpers;

import java.lang.reflect.Method;

/**
 * Dumper for IBM JVM
 * 
 * @author ksalnis
 * 
 */
public class IBMHeapDumper {

	/**
	 * Initialize heap dump through reflection
	 * 
	 * @param fileName
	 */
	public static void createHeapDump() {
		try {
			Class<?> clazz = Class.forName("com.ibm.jvm.Dump");
			Method m = clazz.getMethod("HeapDump");
			m.invoke(null);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

}
