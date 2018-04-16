package com.dumper.dumpers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author ksalnis
 * 
 */
public class ThreadDumper {

	private static final int MAX_DEPTH = 150;
	private static final String DEFAULT_REGEX = "(.*)(.*)";

	private String threadOutput;
	private List<String> threadOutputArray;
	private String threadListDirPath;
	public int threadCount;

	private static ThreadLocal<ThreadDumper> context = new ThreadLocal<ThreadDumper>() {
		@Override
		protected ThreadDumper initialValue() {
			return new ThreadDumper();
		};
	};

	private ThreadDumper() {
	}

	public static ThreadDumper getInstance() {
		return context.get();
	}

	public String getThreadOutput() {
		return threadOutput;
	}

	public void setThreadOutput(String threadOutput) {
		this.threadOutput = threadOutput;
	}

	public List<String> getThreadOutputArray() {
		return threadOutputArray;
	}

	public void setThreadOutputArray(List<String> threadOutputArray) {
		this.threadOutputArray = threadOutputArray;
	}

	public String getThreadListDirPath() {
		return threadListDirPath;
	}

	public void setThreadListDirPath(String threadListDirPath) {
		this.threadListDirPath = threadListDirPath;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/**
	 * 
	 * Thread dump getter with filtering
	 * Call 'getThreadDump(0, "", false, false, "", new ArrayList<String>())' if no filteris is needed.
	 * 
	 * @param minDepth
	 *            - depth enables then >=1
	 * @param regex
	 *            - regex enabled then it is not empty, disabled then empty
	 *            string is passed - ""
	 * @param isLinesFilteringEnabled
	 *            - if 'true', truncates stack trace, leaves only matched lines
	 * @param isHighlightEnabled
	 *            - if 'true', adds red font color html tag.
	 * @param textToHighlight
	 *            - highlights passed text
	 * @return
	 */
	public StringBuilder getThreadDump(int minDepth, String regex, boolean isLinesFilteringEnabled, boolean isHighlightEnabled, String textToHighlight, List<String> threadStates) {

		regex = getFilterRegexes(regex);
		String textToHighlightArray[] = textToHighlight.replaceAll("\\s+", "").split(",");

		StringBuilder contentSb = new StringBuilder();
		StringBuilder headerSb = new StringBuilder();
		Set<Integer> threadNrWithPattern = new LinkedHashSet<Integer>();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), MAX_DEPTH);

		boolean isThereAnyContent = false;
		boolean isRegexEnabled = !regex.equals(DEFAULT_REGEX) ? true : false;
		int threadsCounter = 0;

		contentSb.append("<pre class=\"pre-threads\">");
		boolean isThreadsFilteringEnabled = false;
		// this part is used for marking threads with wanted filtering if regex
		// is set and separate lines filtering is turned off
		if (isRegexEnabled && !isLinesFilteringEnabled) {
			isThreadsFilteringEnabled = true;
			for (ThreadInfo threadInfo : threadInfos) {

				StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

				threadsCounter++;

				for (int i = 0; i < stackTraceElements.length; i++) {
					if (stackTraceElements[i].toString().toLowerCase().matches(regex)) {
						threadNrWithPattern.add(threadsCounter);
					}
				}
			}
		}

		threadsCounter = 0;

		for (ThreadInfo threadInfo : threadInfos) {

			StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

			threadsCounter++;

			Thread.State state = threadInfo.getThreadState();
			if(threadStates != null && !threadStates.isEmpty()) {
				if(!threadStates.contains(state.toString())) {
					continue;
				}
			}

			if (stackTraceElements.length >= minDepth) {

				for (int i = 0; i < stackTraceElements.length; i++) {

					if (threadNrWithPattern.contains(threadsCounter) || threadNrWithPattern.isEmpty() && !isThreadsFilteringEnabled) {

						boolean matchFound = stackTraceElements[i].toString().toLowerCase().matches(regex) ? true : false;

						if (i == 0) {

							headerSb.append('"');
							headerSb.append(threadInfo.getThreadName());
							headerSb.append("\" ");
							headerSb.append("\n   java.lang.Thread.State: ");
							headerSb.append(colorPicker(state));

							if (threadInfo.getLockName() != null) {
								headerSb.append(" on " + threadInfo.getLockName());
							}
						}

						// if regex is not fulfilled, skip this loop iteration
						// (this clause used for truncated filtering)
						if ((!matchFound && isLinesFilteringEnabled)) {
							continue;
						}

						isThereAnyContent = true;

						contentSb.append(headerSb);
						contentSb.append("\n        at ");

						boolean colorTag = false;
						boolean isAlreadyColored = false;

						// iterates through all text which was set to highlight
						for (String toHighlight : textToHighlightArray) {
							colorTag = stackTraceElements[i].toString().toLowerCase().contains(toHighlight.toLowerCase()) && isHighlightEnabled ? true : false;

							if (colorTag) {
								contentSb.append("<b><font color=\"red\">" + stackTraceElements[i] + "</font></b>");
								isAlreadyColored = true;
							}
						}

						if (!isAlreadyColored) {
							contentSb.append(stackTraceElements[i]);
						}

						headerSb.setLength(0);
					}
				}

				if (isThereAnyContent) {
					contentSb.append("\n\n");
					isThereAnyContent = false;
				}
			}

			headerSb.setLength(0);
		}

		// sets generated threads amount
		setThreadCount(threadInfos.length);
		contentSb.append("</pre>");

		// if there are no content
		if (contentSb.toString().equals("<pre class=\"pre-threads\"></pre>")) {
			contentSb.setLength(0);
			contentSb.append("<pre class=\"pre-threads\">No matching has been found</pre>");
		}
		return contentSb;
	}
	
	/**
	 * Regex parser for threads filtering
	 * 
	 * @param filterRegex
	 * @return
	 */
	private String getFilterRegexes(String filterRegex) {
		StringBuilder regex = new StringBuilder();
		String[] patterns = filterRegex.split(",");
		for (int i = 0; i < patterns.length; i++) {
			regex.append("(.*)" + patterns[i].trim().toLowerCase() + "(.*)");
			if (patterns.length > 1 && i != patterns.length - 1) {
				regex.append("|");
			}
		}
		return regex.toString();
	}

	/**
	 * Adds html color attribute depending on thread status
	 * 
	 * @param state
	 * @return
	 */
	public StringBuilder colorPicker(Thread.State state) {

		StringBuilder sBuilder = new StringBuilder();

		switch (state) {
		case RUNNABLE:
			sBuilder.append("<b><font color=\"39AC73\">" + state + "</font></b>");
			break;
		case BLOCKED:
			sBuilder.append("<b><font color=\"FF0000\">" + state + "</font></b>");
			break;
		case WAITING:
			sBuilder.append("<b><font color=\"FF9800\">" + state + "</font></b>");
			break;
		case TIMED_WAITING:
			sBuilder.append("<b><font color=\"00ACE6\">" + state + "</font></b>");
			break;
		default:
			sBuilder.append(state);
			break;
		}

		return sBuilder;
	}

}