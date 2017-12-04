package com.dumper.dumpers;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

/**
 * Dumper for ORACLE JVM
 * 
 * @author ksalnis
 *
 */
public class OracleHeapDumper {
	// This is the name of the HotSpot Diagnostic MBean
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

	// Field to store the hotspot diagnostic MBean
	private static volatile Object hotspotMBean;

	/**
	 * Call this method from your application whenever you want to dump the heap
	 * snapshot into a file.
	 * 
	 * @param fileName
	 *            name of the heap dump file
	 * @param live
	 *            flag that tells whether to dump only the live objects
	 */
	public static void dumpHeap(String fileName, boolean live) {
		// initialize hotspot diagnostic MBean
		initHotspotMBean();
		try {
			Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
			Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
			m.invoke(hotspotMBean, fileName, live);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

	/**
	 * Initialize the hotspot diagnostic MBean field
	 */
	private static void initHotspotMBean() {
		if (hotspotMBean == null) {
			synchronized (OracleHeapDumper.class) {
				if (hotspotMBean == null) {
					hotspotMBean = getHotspotMBean();
				}
			}
		}
	}

	/**
	 * Get the hotspot diagnostic MBean from the platform MBean server
	 * 
	 * @return
	 */
	private static Object getHotspotMBean() {
		try {
			Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			Object bean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, clazz);
			return bean;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}
}