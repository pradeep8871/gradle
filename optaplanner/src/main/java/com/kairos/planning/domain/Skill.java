package com.kairos.planning.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Skill")
public class Skill {

	private Long id;
	private String name;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "Skill [name=" + name + "]";
	}
	
}
