package com.dumper.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dumper.exceptions.ConnectionException;

/**
 * Clob DAO
 * 
 * @author ksalnis
 * 
 */
public class ClobJpaDao implements ClobDao {

	@Override
	public ClobEntity findClobById(Long id) {
		Session session = SessionFactoryHolder.createSession();
		ClobEntity xmlStoring = null;
		try {
			xmlStoring = (ClobEntity) session.get(ClobEntity.class, id);
			if (xmlStoring == null) {
				throw new RuntimeException("No records has been found for this criteria id = " + id);
			}
		} catch (HibernateException ex) {
			throw new ConnectionException(ex);
		} finally {
			session.close();
		}

		return xmlStoring;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ClobEntity> findAll() {
		Session session = SessionFactoryHolder.createSession();
		List<ClobEntity> clobEntries = null;
		try {
			clobEntries = (List<ClobEntity>) session.createQuery("from " + ClobEntity.class.getName()).list();
		} catch (HibernateException ex) {
			throw new ConnectionException(ex);
		} finally {
			session.close();
		}
		return clobEntries;

	}

	@Override
	public boolean deleteById(Long id) {
		Session session = SessionFactoryHolder.createSession();
		Transaction tx = session.beginTransaction();
		try {
			ClobEntity persistentInstance = (ClobEntity) session.get(ClobEntity.class, id);
			if (persistentInstance != null) {
				session.delete(persistentInstance);
				tx.commit();
				return true;
			}
		} catch (HibernateException e) {
			tx.rollback();
			throw new ConnectionException(e);
		} finally {
			session.flush();
			session.close();
		}
		return false;
	}
}
