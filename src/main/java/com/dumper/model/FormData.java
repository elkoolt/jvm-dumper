package com.dumper.model;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Form/model backing object
 * 
 * @author ksalnis
 * 
 */
@Component
public class FormData {

	@NotNull(message = "Please enter value")
	@Min(value = 1, message = "Value should be greater than or equal to 1")
	@Max(value = 30, message = "Value should be less than or equal to 30")
	@Value("${threadCount}")
	private Integer threadCount;

	@NotNull(message = "Please enter value")
	@Min(value = 0, message = "Value should be greater than or equal to 0")
	@Value("${minDepth}")
	private Integer minDepth;

	@Size(min = 2, message = "Regex size should be greater than or equal to 2")
	@Value("${filteringRegex}")
	private String filteringRegex;

	@Value("${enabledHighlight}")
	private boolean enabledHighlight;
	
	@Value("${enabledLinesFiltering}")
	private boolean enabledLinesFiltering;

	@Size(min = 2, message = "TextHighlight size should be greater than or equal to 2")
	@Value("${textToHighlight}")
	private String textToHighlight;
	
	private List<String> threadStatesForm;

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public Integer getMinDepth() {
		return minDepth;
	}

	public void setMinDepth(int minDepth) {
		this.minDepth = minDepth;
	}

	public String getFilteringRegex() {
		return filteringRegex;
	}

	public void setFilteringRegex(String filteringRegex) {
		this.filteringRegex = filteringRegex;
	}
	
	public boolean isEnabledLinesFiltering() {
		return enabledLinesFiltering;
	}

	public void setEnabledLinesFiltering(boolean enabledLinesFiltering) {
		this.enabledLinesFiltering = enabledLinesFiltering;
	}
	
	public boolean isEnabledHighlight() {
		return enabledHighlight;
	}

	public void setEnabledHighlight(boolean enabledHighlight) {
		this.enabledHighlight = enabledHighlight;
	}

	public String getTextToHighlight() {
		return textToHighlight;
	}

	public void setTextToHighlight(String textToHighlight) {
		this.textToHighlight = textToHighlight;
	}
	
	public List<String> getThreadStatesForm() {
		return threadStatesForm;
	}

	public void setThreadStatesForm(List<String> threadStatesForm) {
		this.threadStatesForm = threadStatesForm;
	}

	@Override
	public String toString() {
		return "FormData [ThreadCount=" + this.threadCount + "MinDepth=" + this.minDepth + "FilteringRegex="
				+ this.filteringRegex + "EnabledLinesFiltering=" + this.enabledLinesFiltering + "EnabledHighlight=" + this.enabledHighlight + "TextToHighlight="
				+ this.textToHighlight + "ThreadStatesForm=" + this.threadStatesForm + "]";
	}
}
