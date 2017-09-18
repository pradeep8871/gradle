package com.kairos.planning.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("UnavailabilityList")
public class UnavailabilityRequest {
	private Long id;
    private String startTimeString;
    private String endTimeString;
    private int weight;

    public UnavailabilityRequest() {
    }

    public UnavailabilityRequest(Long id,int weight,String startTimeString,String endTimeString) {
        this.weight = weight;
        this.endTimeString = endTimeString;
        this.startTimeString = startTimeString;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartTimeString() {
        return startTimeString;
    }

    public void setStartTimeString(String startTimeString) {
        this.startTimeString = startTimeString;
    }

    public String getEndTimeString() {
        return endTimeString;
    }

    public void setEndTimeString(String endTimeString) {
        this.endTimeString = endTimeString;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
