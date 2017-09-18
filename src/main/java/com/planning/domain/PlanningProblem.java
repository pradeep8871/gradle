package com.planning.domain;

import org.springframework.data.annotation.Id;

import com.planning.enums.PlanningStatus;

public class PlanningProblem {

	@Id
	private String id;
	private String planningId;
	private PlanningStatus status;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPlanningId() {
		return planningId;
	}
	public void setPlanningId(String planningId) {
		this.planningId = planningId;
	}
	public PlanningStatus getStatus() {
		return status;
	}
	public void setStatus(PlanningStatus status) {
		this.status = status;
	}
	
	
}
