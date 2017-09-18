package com.kairos.planning.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Citizen")
public class Citizen {
	private Address address;
	private String name;
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public Citizen(Long id,String name,Address address) {
		this.name=name;
		this.id=id;
		this.address = address;
	}
	public String toString(){
		return name;
	}

}
