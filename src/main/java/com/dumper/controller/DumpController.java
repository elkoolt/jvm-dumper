package com.dumper.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dumper.dao.ClobEntity;
import com.dumper.dumpers.DelayedThreadDumpInitializer;
import com.dumper.dumpers.IBMHeapDumper;
import com.dumper.dumpers.JVMInfo;
import com.dumper.dumpers.OracleHeapDumper;
import com.dumper.dumpers.ThreadDumper;
import com.dumper.dumpers.ThreadsAnalyzer;
import com.dumper.lookup.ThreadStatesLookup;
import com.dumper.model.FormData;
import com.dumper.services.ClobService;
import com.dumper.utils.Utils;

/**
 * Main controller class
 * 
 * @author ksalnis
 *
 */
@Controller
public class DumpController {

	private ClobService clobService;
	private ThreadDumper threadDumper;
	private ThreadsAnalyzer threadsAnalyzer;

	private static final String JVM_MEMORY_HEAP_NAME = "heap";
	private static final Boolean DUMP_ONLY_LIVE_OBJECTS = true;
	private static final String THREAD_DUMP_NAME = "threadStack";
	private static final String THREAD_DUMPS = "threadDumps";
	private static final String DUMP_DIR_NAME = "dump";
	private static final String FORM_MODEL = "formData";
	private static final String DEFAULT = "default";
	private static final String ORACLE = "Oracle Corporation";
	private static final String IBM = "IBM Corporation";

	public void setClobService(ClobService clobService) {
		this.clobService = clobService;
	}

	/**
	 * Initial page loader
	 * 
	 * @param formData
	 * @return
	 */
	@RequestMapping(value = { "/dumper" }, method = { RequestMethod.GET })
	public String loadMainPage(FormData formData, Model model) {
		model.addAttribute("threadStates", new ThreadStatesLookup().findAll());
		return "dumper";
	}

	/**
	 * Validates inputed data and redirects request to responsible mapper
	 * 
	 * @param formData
	 * @param bindingResult
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = { "/dumper" }, method = { RequestMethod.POST })
	public String validateFormData(@Valid @ModelAttribute(FORM_MODEL) FormData formData, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {

		model.addAttribute("threadStates", new ThreadStatesLookup().findAll());
		
		boolean isFailure = bindingResult.getFieldValue("enabledHighlight").toString().equals(Boolean.TRUE.toString())
				&& bindingResult.getFieldValue("textToHighlight").toString().length() <= 2;

		if (isFailure || bindingResult.hasFieldErrors("threadCount") || bindingResult.hasFieldErrors("minDepth")
				|| bindingResult.hasFieldErrors("filteringRegex")) {
			return "dumper";
		}

		redirectAttributes.addFlashAttribute(FORM_MODEL, formData);
		if (formData.getThreadCount() == 1) {
			return "redirect:/resources/thread_dump";
		} else {
			return "redirect:/resources/thread_dump_list";
		}
	}

	/**
	 * Dumps JVM memory dump and returns to caller
	 * 
	 * @param guid
	 * @param request
	 * @param response
	 * @throws ArchiveException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/memory_dump" }, method = { RequestMethod.GET }, produces = { "application/zip" })
	public void getMemoryDump(@RequestParam(value = "guid", required = true) String guid, HttpServletRequest request,
			HttpServletResponse response) throws ArchiveException, IOException {
		Cookie cookie = new Cookie("guidcookie", guid);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60);

		ServletContext context = request.getServletContext();
		String appPath = context.getRealPath("");

		String jvmVendorName = System.getProperty("java.vendor");
		String fileName = null;

		if (jvmVendorName.contains(ORACLE)) {
			fileName = String.format("%s.hprof", JVM_MEMORY_HEAP_NAME);
		} else if (jvmVendorName.contains(IBM)) {
			fileName = String.format("%s.phd", JVM_MEMORY_HEAP_NAME);
		}

		String fullZipFilePath = null;

		if (jvmVendorName.contains(ORACLE)) {

			String dumpDir = String.format("%s%s%s%s%s%s", appPath, File.separator, DUMP_DIR_NAME, File.separator,
					UUID.randomUUID().toString(), File.separator);
			String fullHeapPath = String.format("%s%s", dumpDir, fileName);
			fullZipFilePath = String.format("%s%s.zip", dumpDir, JVM_MEMORY_HEAP_NAME);

			FileUtils.forceMkdir(new File(dumpDir));

			OracleHeapDumper.dumpHeap(fullHeapPath, DUMP_ONLY_LIVE_OBJECTS);

			Utils.zipDirectory(dumpDir, fullZipFilePath);

		} else if (jvmVendorName.contains(IBM)) {

			String heapDir = System.getProperty("user.dir");
			String filePattern = Utils.generatePhdFilePattern();

			Utils.deletePhdFile(new File(heapDir), filePattern);

			IBMHeapDumper.createHeapDump();

			String fullHeapFileDir = Utils.matchPhdFileName(new File(heapDir), filePattern);

			fullZipFilePath = String.format("%s.zip", fullHeapFileDir);

			Utils.zipFile(fullHeapFileDir, fullZipFilePath);
		}

		response.setContentType("application/zip");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addCookie(cookie);
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=\"%s.zip\"", JVM_MEMORY_HEAP_NAME));

		InputStream inputStream = new FileInputStream(new File(fullZipFilePath));

		IOUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
		IOUtils.closeQuietly(inputStream);
	}

	/**
	 * Dumps thread stack trace and returns to caller
	 * 
	 * @param formData
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping(value = { "/thread_dump" }, method = { RequestMethod.GET })
	public List<String> threadDump(FormData formData) throws IOException {

		threadDumper = ThreadDumper.getInstance();

		// if nothing was set to the regex form, pass default value
		if (formData.getFilteringRegex().equals(DEFAULT)) {
			formData.setFilteringRegex("");
		}

		String threadDump = threadDumper.getThreadDump(formData.getMinDepth(), formData.getFilteringRegex(),
				formData.isEnabledLinesFiltering(), formData.isEnabledHighlight(), formData.getTextToHighlight(), formData.getThreadStatesForm())
				.toString();
		threadDumper.setThreadOutput(threadDump);

		// store thread dump and thread count
		List<String> threadParams = new ArrayList<String>(
				Arrays.asList(threadDump, String.valueOf(threadDumper.getThreadCount())));

		return threadParams;
	}

	/**
	 * Downloads thread dump in plain text
	 * 
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = { "/get_one_thread_dump" }, method = { RequestMethod.GET }, produces = {
			"application/txt" })
	public void getOneThreadDump(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.txt\"", THREAD_DUMP_NAME));

		String threadOutput = threadDumper.getThreadOutput();
		threadOutput = Utils.removeHtmlTags(threadOutput);

		InputStream inputStream = org.apache.commons.io.IOUtils.toInputStream(threadOutput, "UTF-8");
		IOUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
		IOUtils.closeQuietly(inputStream);
	}

	/**
	 * Dumps requested amount of thread dumps
	 * 
	 * @param formData
	 * @param request
	 * @param response
	 * @return
	 * @throws ArchiveException
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping(value = { "/thread_dump_list" }, method = { RequestMethod.GET })
	public List<String> getThreadDumpList(@ModelAttribute(FORM_MODEL) FormData formData, HttpServletRequest request,
			HttpServletResponse response, Model model) throws ArchiveException, IOException {

		ServletContext context = request.getServletContext();
		String appPath = context.getRealPath("");
		threadDumper = ThreadDumper.getInstance();
		threadsAnalyzer = ThreadsAnalyzer.getInstance();

		String dumpDir = String.format("%s%s%s%s%s%s", appPath, File.separator, DUMP_DIR_NAME, File.separator,
				UUID.randomUUID().toString(), File.separator);

		FileUtils.forceMkdir(new File(dumpDir));

		threadDumper.setThreadListDirPath(dumpDir);

		// CountDownLatch stops main thread and waits for the
		// DelayedThreadDumpInitializer thread's finish
		final CountDownLatch latch = new CountDownLatch(1);

		// if nothing was set to the regex form, pass default value
		if (formData.getFilteringRegex().equals(DEFAULT)) {
			formData.setFilteringRegex("");
		}

		DelayedThreadDumpInitializer delayedThreadDumper = new DelayedThreadDumpInitializer(formData.getThreadCount(),
				formData.getMinDepth(), formData.getFilteringRegex(), formData.isEnabledLinesFiltering(),
				formData.isEnabledHighlight(), dumpDir, latch, formData.getTextToHighlight(), formData.getThreadStatesForm());
		delayedThreadDumper.doThreadDumping();

		try {
			latch.await(); // main thread is waiting on CountDownLatch to finish
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/txt");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return threadDumper.getThreadOutputArray();
	}

	/**
	 * Returns stuck threads
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/get_stuck_threads" }, method = { RequestMethod.GET })
	public LinkedHashSet<String> getStuckThreads() {
		if (threadsAnalyzer.getStuckThreads() != null && !threadsAnalyzer.getStuckThreads().isEmpty()) {
			return threadsAnalyzer.getStuckThreads();
		}
		
		return new LinkedHashSet<String>();
	}

	/**
	 * Zips thread dumps txt files and returns to caller
	 * 
	 * @param response
	 * @throws ArchiveException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/get_zipped_thread_dump" }, method = { RequestMethod.GET })
	public void getZippedThreadDumpList(HttpServletResponse response) throws ArchiveException, IOException {

		String dumpDir = threadDumper.getThreadListDirPath();

		String fullZipFilePath = String.format("%s%s.zip", dumpDir, THREAD_DUMPS);

		Utils.zipDirectory(dumpDir, fullZipFilePath);
		response.setContentType("application/zip");
		response.setStatus(HttpServletResponse.SC_OK);
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", THREAD_DUMPS));

		InputStream inputStream = new FileInputStream(new File(fullZipFilePath));
		IOUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
		IOUtils.closeQuietly(inputStream);
	}

	/**
	 * Writes thread dump to database
	 * 
	 * @return
	 */
	@RequestMapping(value = { "/write_to_db" }, method = { RequestMethod.GET })
	public String writeThreadDumpToDB() {
		String threadOutput = threadDumper.getThreadOutput();
		threadOutput = Utils.removeHtmlTags(threadOutput);
		if (clobService != null) {
			clobService.saveClob(threadOutput);
		}
		return "redirect:/resources/dumper";
	}

	/**
	 * Deletes record from database by given id
	 * 
	 * @param id
	 */
	@RequestMapping(value = { "/delete" }, method = { RequestMethod.GET })
	public String deleteRecordFromDB(@RequestParam("id") Long id) {
		clobService.deleteRecord(id);
		return "redirect:/resources/dumper";
	}

	/**
	 * JVM Information
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "/get_jvm_info" }, method = { RequestMethod.GET })
	@ResponseBody
	public Map<String, String> handleUserRequest () {
		JVMInfo jvmInfo = JVMInfo.getInstance();

		Map<String, String> jvmInfoMap = new LinkedHashMap<String, String>();
		jvmInfoMap.put("JAVA_OPTS:", jvmInfo.getJvmOpts());
		jvmInfoMap.put("Java Version:", jvmInfo.getJavaVersion());
		jvmInfoMap.put("JAVA_HOME:", jvmInfo.getJVMHome());
		jvmInfoMap.put("java.class.path:", jvmInfo.getClassPath());
		jvmInfoMap.put("Available cores: ", String.valueOf(jvmInfo.getAvailableCores()));
		jvmInfoMap.put("OS Architecture:", jvmInfo.getOSArchitecture());
		jvmInfoMap.put("OS Name:", jvmInfo.getOSName());
		jvmInfoMap.put("Up Time:", jvmInfo.getUpTime());
		jvmInfoMap.put("PID:", String.format("%s minutes", jvmInfo.getPid()));
		jvmInfoMap.put("Used Heap Memory:", String.format("%s MB", jvmInfo.getUsedMemory()));
		jvmInfoMap.put("Free Heap Memory:", String.format("%s MB", jvmInfo.getFreeMemory()));
		jvmInfoMap.put("Allocated Heap Memory:", String.format("%s MB", jvmInfo.getTotalMemory()));
		jvmInfoMap.put("Max/Limit Heap Memory:", String.format("%s MB", jvmInfo.getMaxMemory()));
		
		return jvmInfoMap;
	}

	/**
	 * Defining model with data from DB
	 * 
	 * @return
	 */

	@ModelAttribute("clobs")
	public List<ClobEntity> process() {
		if (clobService != null) {
			if (clobService.getAllClobs() != null && !clobService.getAllClobs().isEmpty()) {
				return clobService.getAllClobs();
			}
		}
		return null;
	}

	/**
	 * Generates globally unique identifier (GUID)
	 * 
	 * @param guid
	 * @return
	 */
	@ModelAttribute("guid")
	public String getGuid() {
		return UUID.randomUUID().toString();
	}

}