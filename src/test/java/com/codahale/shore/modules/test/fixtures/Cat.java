package com.codahale.shore.modules.test.fixtures;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Cat {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	public Integer getId() {
		return id;
	}
}
