package com.dumper.dumpers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.dumper.utils.Utils;
import com.google.common.collect.LinkedListMultimap;

/**
 * Runs thread dump method for defined count with 5 seconds delay
 * 
 * @author ksalnis
 *
 */
public class DelayedThreadDumpInitializer {

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final CountDownLatch latch;
	private static final String THREAD_STACK = "threadStack";
	private int time = 0;
	private int i = 0;
	private String dir;
	private int threadCount;
	private int minDepth;
	private String regex;
	private boolean isEnabledLinesFiltering; 
	private boolean isEnabledHighlight;
	private String textToHighlight;
	private List<String> threadStates;
	private List<String> threadOutputArray = new LinkedList<String>();
	private LinkedListMultimap<String, String> threadParts = LinkedListMultimap.create();
	private LinkedListMultimap<String, String> threadPartsByLine = LinkedListMultimap.create();

	public DelayedThreadDumpInitializer(int threadCount, int minDepth, String regex, boolean isEnabledLinesFiltering, boolean isEnabledHighlight, String dir, CountDownLatch latch, String textToHighlight, List<String> threadStates) {
		this.threadCount = threadCount;
		this.dir = dir;
		this.latch = latch;
		this.minDepth = minDepth;
		this.regex = regex;
		this.isEnabledLinesFiltering = isEnabledLinesFiltering;
		this.isEnabledHighlight = isEnabledHighlight;
		this.textToHighlight = textToHighlight;
		this.threadStates = threadStates;
		if (threadCount != 1) {
			time = 5 * (threadCount - 1);
		}
	}

	public void doThreadDumping() {
		final ThreadsAnalyzer threadsAnalyzer = ThreadsAnalyzer.getInstance();
		final ThreadDumper threadDumper = ThreadDumper.getInstance();
		final Runnable run = new Runnable() {
			@Override
			public void run() {
				i++;

				String fileName = String.format("%s%s[%s].txt", dir, THREAD_STACK, i);
				String threadDump = threadDumper.getThreadDump(minDepth, regex, isEnabledLinesFiltering, isEnabledHighlight, textToHighlight, threadStates).toString();
	
				String cleanedThreadDump = Utils.removeHtmlTags(threadDump);

				String escapedThreadDump = Utils.escapeHtml(threadDump.replaceAll("<pre class=\"pre-threads\">", "").replaceAll("</pre>",""));
	
				Utils.writeThreadDumpToFile(fileName, cleanedThreadDump);
				// writes threads count and dump to array
				threadOutputArray.add(String.format("%s|%s", threadDumper.getThreadCount(), threadDump));
				// stores parsed thread parts: thread name/full stack
				threadParts.putAll(threadsAnalyzer.getThreadParts(escapedThreadDump));
				// stores parsed threads parts: thread name/full stack by line 
				threadPartsByLine.putAll(threadsAnalyzer.getThreadPartsByLine(escapedThreadDump));
			}
		};
		
		final ScheduledFuture<?> handler = scheduler.scheduleAtFixedRate(run, 0, 5, TimeUnit.SECONDS);
		scheduler.schedule(new Runnable() {
			public void run() {
				threadDumper.setThreadOutputArray(threadOutputArray);
				
				if(threadCount >= 2) {
					for(int i = threadCount; i !=1; i--) {
						threadsAnalyzer.setStuckThreads(ThreadsAnalyzer.getInstance().getStuckThreads(threadParts, threadPartsByLine, i));
					}
				}
				
				handler.cancel(true);
				scheduler.shutdown();
				latch.countDown();
			}
		}, time, TimeUnit.SECONDS);
	}
}