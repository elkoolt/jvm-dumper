package com.dumper.dao;

import java.util.List;

/**
 * Data access and management
 * 
 * @author ksalnis
 *
 */
public interface ClobDao {

	ClobEntity findClobById(Long id);

	List<ClobEntity> findAll();

	boolean deleteById(Long id);
}
