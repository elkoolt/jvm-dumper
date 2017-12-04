package com.dumper.services;

import java.util.List;

import com.dumper.dao.ClobEntity;

/**
 * 
 * @author ksalnis
 *
 */
public interface ClobService {

	public String getThreadDump(Long id);

	public List<ClobEntity> getAllClobs();

	public List<String> getAllThreadDumps();

	public void saveClob(String content);

	public void saveClobFromFile(String filePath);

	public void deleteRecord(Long id);
}
