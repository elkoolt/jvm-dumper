package com.dumper.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.cache.annotation.Cacheable;

import com.dumper.dao.ClobDao;
import com.dumper.dao.ClobEntity;
import com.dumper.dao.SessionFactoryHolder;
import com.dumper.utils.Utils;

/**
 * 
 * @author ksalnis
 *
 */
public class ClobServiceImpl implements ClobService {

	private ClobDao clobDao;
	private static ClobEntity xmlStoring;

	// a setter method which injects the dependency
	public void setClobDao(ClobDao clobDao) {
		this.clobDao = clobDao;
	}

	@Cacheable("basecache")
	@Override
	public String getThreadDump(Long id) {
		try {
			Clob clob = clobDao.findClobById(id).getClobData();
			InputStream in = clob.getAsciiStream();
			StringWriter wr = new StringWriter();
			IOUtils.copy(in, wr);
			return wr.toString();
		} catch (SQLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public List<ClobEntity> getAllClobs() {
		return clobDao.findAll();
	}

	@Override
	public List<String> getAllThreadDumps() {
		List<String> dumpsList = new ArrayList<String>();
		StringWriter wr = new StringWriter();
		InputStream in = null;
		try {
			for (ClobEntity ce : getAllClobs()) {
				in = ce.getClobData().getAsciiStream();
				IOUtils.copy(in, wr);
				dumpsList.add(wr.toString());
			}
		} catch (SQLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return dumpsList;
	}

	@Override
	public void saveClob(String content) {

		xmlStoring = new ClobEntity();
		Clob clob = null;
		Session session = SessionFactoryHolder.createSession();
		Transaction tx = session.beginTransaction();
		try {
			clob = Hibernate.getLobCreator(session).createClob(content);
			xmlStoring.setClobData(clob);
			session.save(xmlStoring);
			tx.commit();

		} catch (HibernateException e) {
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			session.flush();
			session.close();
		}
	}

	@Override
	public void saveClobFromFile(String filePath) {

		xmlStoring = new ClobEntity();
		BufferedReader br = null;
		Clob clob = null;
		Session session = SessionFactoryHolder.createSession();
		Transaction tx = session.beginTransaction();
		try {
			br = new BufferedReader(new FileReader(filePath));
			clob = Utils.createClob(br, Integer.MAX_VALUE);
			xmlStoring.setClobData(clob);
			session.save(xmlStoring);
			tx.commit();

		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			session.flush();
			session.close();
		}
	}

	@Override
	public void deleteRecord(Long id) {
		clobDao.deleteById(id);
	}
}
