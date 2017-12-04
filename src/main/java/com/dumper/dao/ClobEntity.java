package com.dumper.dao;

import java.sql.Clob;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dumper.utils.Utils;

/**
 * Clob Entity
 * 
 * @author ksalnis
 *
 */
@Entity
public class ClobEntity {

	public ClobEntity() {
		setCreatedOn(Utils.getCurrentTimestamp());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CLOB_SEQ")
	@SequenceGenerator(name = "CLOB_SEQ", sequenceName = "CLOB_SEQ", allocationSize = 1)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdOn", nullable = false)
	private Date createdOn;

	private Clob clobData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Clob getClobData() {
		return clobData;
	}

	public void setClobData(Clob clobData) {
		this.clobData = clobData;
	}
}
