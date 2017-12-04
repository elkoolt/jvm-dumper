package com.dumper.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * 
 * @author ksalnis
 *
 */
public class SessionFactoryHolder {
	private static SessionFactory sessionFactory = new Configuration().configure("jvm-hibernate.cfg.xml").buildSessionFactory();

	private SessionFactoryHolder() {
	}

	public static Session createSession() {
		return sessionFactory.openSession();
	}
}