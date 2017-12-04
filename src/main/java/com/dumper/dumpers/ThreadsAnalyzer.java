package com.dumper.dumpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * Class responsible for thread analysis: finds stuck/duplicate threads.
 * 
 * @author ksalnis
 *
 */
public class ThreadsAnalyzer {

	private static final String CONTENT_PATTERN = "\n(\\s{8}at\\s(.*?)[\n\r])+";
	private static final String CONTENT_PATTERN_BY_LINE = "\n\\s{8}at\\s(.*?).+";
	private static final String HEADER_PATTERN = "&quot;.*?[\n\r].*State.*";

	private LinkedHashSet<String> stuckThreads;
	
	private LinkedHashSet<String> dublicateThreads = new LinkedHashSet<String>();
	
	private Multimap<String, String> itemsCache = ArrayListMultimap.create();

	private static ThreadLocal<ThreadsAnalyzer> context = new ThreadLocal<ThreadsAnalyzer>() {
		@Override
		protected ThreadsAnalyzer initialValue() {
			return new ThreadsAnalyzer();
		};
	};

	private ThreadsAnalyzer() {
	}

	public static ThreadsAnalyzer getInstance() {
		return context.get();
	}

	public LinkedHashSet<String> getStuckThreads() {
		return stuckThreads;
	}

	public void setStuckThreads(LinkedHashSet<String> stuckThreads) {
		this.stuckThreads = stuckThreads;
	}

	/**
	 * Parses thread dump into key/value (thread name/stacktrace)
	 * 
	 * @param threadDump
	 * @return
	 */
	public LinkedListMultimap<String, String> getThreadParts(String threadDump) {

		LinkedListMultimap<String, String> threadsMultiMap = LinkedListMultimap.create();

		Pattern headerPattern = Pattern.compile(HEADER_PATTERN);
		Pattern contentPattern = Pattern.compile(CONTENT_PATTERN);

		Matcher headerMatcher = headerPattern.matcher(threadDump);
		Matcher contentMattcher = contentPattern.matcher(threadDump);

		while (headerMatcher.find() && contentMattcher.find()) {
			threadsMultiMap.put(headerMatcher.group(0), contentMattcher.group(0));
		}

		return threadsMultiMap;
	}

	/**
	 * Parses thread dump into key/value (thread name/stacktrace by line)
	 * Removes first 4 lines of stack and adds to map
	 * 
	 * @param threadDump
	 * @return
	 */
	public LinkedListMultimap<String, String> getThreadPartsByLine(String threadDump) {

		LinkedListMultimap<String, String> threadsMultiMap = LinkedListMultimap.create();

		Pattern headerPattern = Pattern.compile(HEADER_PATTERN);
		Pattern contentPattern = Pattern.compile(CONTENT_PATTERN);
		Pattern contentPatternByLine = Pattern.compile(CONTENT_PATTERN_BY_LINE);

		Matcher headerMatcher = headerPattern.matcher(threadDump);
		Matcher contentMattcher = contentPattern.matcher(threadDump);

		while (headerMatcher.find() && contentMattcher.find()) {
			Matcher contentByLineMattcher = contentPatternByLine.matcher(contentMattcher.group(0));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; contentByLineMattcher.find(); i++) {
				if (i > 3) {
					sb.append(contentByLineMattcher.group(0));
				}
			}
			threadsMultiMap.put(headerMatcher.group(0), sb.toString());
		}

		return threadsMultiMap;
	}

	/**
	 * Finds out repetitive threads in all thread dumps: fully equal and partly equal
	 * 
	 * @param threadsMultiMap
	 * @param totalThreads
	 * @return
	 */
	
	
	
	public LinkedHashSet<String> getStuckThreads(LinkedListMultimap<String, String> threadParts,
			LinkedListMultimap<String, String> threadPartsByLine, int totalThreads) {

		if (threadParts.isEmpty() && threadPartsByLine.isEmpty())
			return null;

		Set<String> keySet = threadParts.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		List<String> keysTmp = new ArrayList<String>();

		// iterates through fully parsed thread dump
		while (keyIterator.hasNext()) {
			String threadNameKey = (String) keyIterator.next();

			List<String> stackTraceValues = threadParts.get(threadNameKey);

			for (String stack : stackTraceValues) {
				// stack must repeat defined frequency - totalThreads, here it
				// should be in all threads
				if (Collections.frequency(stackTraceValues, stack) >= totalThreads) {

					// if thread is already in the whole dump cycle list, skip it.
					Collection<String> threadStack = itemsCache.get(threadNameKey);
			        if (threadStack != null && threadStack.contains(stack)) {
			        	continue;
			        }
			        itemsCache.put(threadNameKey, stack);
					dublicateThreads.add((totalThreads)+ " threads has this stacktrace " + threadNameKey + stack);
					// marks already added stuck threads
					keysTmp.add(threadNameKey);
				}
			}
		}

		Set<String> keySetPartly = threadPartsByLine.keySet();
		Iterator<String> keyIteratorPartly = keySetPartly.iterator();
		//TODO: make comparison not only with first 4 lines removed, but more sophisticated approach
		while (keyIteratorPartly.hasNext()) {
			String threadNameKey = (String) keyIteratorPartly.next();
			// skip thread which is already in the list
			if (keysTmp.contains(threadNameKey)) {
				continue;
			}

			List<String> stackTraceValues = threadPartsByLine.get(threadNameKey);

			for (String stack : stackTraceValues) {
				if (Collections.frequency(stackTraceValues, stack) >= totalThreads) {

					if (StringUtils.hasLength(stack)) {
						dublicateThreads.add(String.format("\n%s", threadNameKey) + stack);
					}
				}
			}
		}

		return dublicateThreads;
	}

}