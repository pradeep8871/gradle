package com.kairos.planning.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@XStreamAlias("Availability")
public class AvailabilityRequest {

	private Long id;
	private String startTimeString;
	private String endTimeString;
	static DateTimeFormatter formatter = null;
	static{
		formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
		//formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	}
	/*public AvailabilityRequest(){

	}*/
	
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
	public DateTime getStartDateTime(){
		return DateTime.parse(getStartTimeString(),formatter);
	}
	public DateTime getEndDateTime(){
		return DateTime.parse(getEndTimeString(),formatter);
	}
	public void setEndTimeString(String endTimeString) {
		this.endTimeString = endTimeString;
	}
	public long getMinutes(){
		//08/06/2017 11:00:00
		//LocalDateTime startDateTime = LocalDateTime.parse(startTimeString, formatter);
		//LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, formatter);
		return getInterval().toDuration().getStandardMinutes();
	}
	public Interval getInterval(){
		return new Interval(getStartDateTime(),getEndDateTime());
	}
	@Override
	public String toString() {
		return "AvailabilityRequest [startTimeString=" + startTimeString + ", endTimeString=" + endTimeString+ "]";
	}
	public String getIntervalAsString(){
		return ""+DateTime.parse(getStartTimeString(),formatter).toString("HH:mm")+"-"+DateTime.parse(getEndTimeString(),formatter).toString("HH:mm");
	}
}
