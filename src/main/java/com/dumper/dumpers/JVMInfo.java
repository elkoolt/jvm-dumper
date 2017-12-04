package com.dumper.dumpers;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 
 * @author ksalnis
 *
 */
public class JVMInfo {

	private static JVMInfo instance = null;
	private static RuntimeMXBean runtimeMxBean;
	private static OperatingSystemMXBean operatingSystemMXBean;
	private static Runtime runtime;

	private static DecimalFormat decFormat = new DecimalFormat(".##");
	private static int MB = 1024*1024;
	
	public static JVMInfo getInstance() {

		if (instance == null) {
			instance = new JVMInfo();
		}

		runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		runtime = Runtime.getRuntime();

		return instance;
	}

	public String getJvmOpts() {
		StringBuilder sb = new StringBuilder();
		List<String> arguments = runtimeMxBean.getInputArguments();

		for (String s : arguments) {
			sb.append(s).append(" ");
		}
		return sb.toString();
	}

	public String getJavaVersion() {
		return String.format("%s (%s, %s) - %s", runtimeMxBean.getVmName(), runtimeMxBean.getVmVersion(),
				System.getProperty("java.vm.info"), runtimeMxBean.getVmVendor());
	}

	public String getJVMHome() {
		return System.getProperty("java.home");
	}

	public String getClassPath() {
		return runtimeMxBean.getClassPath();
	}

	public int getAvailableCores() {
		return operatingSystemMXBean.getAvailableProcessors();
	}

	public String getOSArchitecture() {
		return operatingSystemMXBean.getArch();
	}

	public String getOSName() {
		return String.format("%s - v. %s", operatingSystemMXBean.getName(), operatingSystemMXBean.getVersion());
	}
	
	public String getUpTime() {
		return decFormat.format(runtimeMxBean.getUptime()/60000.00);
	}
	
	public String getPid() {
		return runtimeMxBean.getName();
	}
	
	public String getUsedMemory() {
		return String.valueOf((runtime.totalMemory() - runtime.freeMemory())/MB);
	}
	
	public String getFreeMemory() {
		return String.valueOf(runtime.freeMemory()/MB);
	}
	
	public String getTotalMemory() {
		return String.valueOf(runtime.totalMemory()/MB);
	}

	public String getMaxMemory() {
		return String.valueOf(runtime.maxMemory()/MB);
	}

}
