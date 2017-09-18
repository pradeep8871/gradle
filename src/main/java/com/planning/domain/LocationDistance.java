package com.planning.domain;

import org.springframework.data.annotation.Id;

import com.kairos.planning.domain.Location;

public class LocationDistance {
	
	@Id
	private String id;
	private Location firstLocation;
	private Location secondLocation;
	private double distance;
	private double time;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public Location getFirstLocation() {
		return firstLocation;
	}
	public void setFirstLocation(Location firstLocation) {
		this.firstLocation = firstLocation;
	}
	public Location getSecondLocation() {
		return secondLocation;
	}
	public void setSecondLocation(Location secondLocation) {
		this.secondLocation = secondLocation;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}

	
	
}
