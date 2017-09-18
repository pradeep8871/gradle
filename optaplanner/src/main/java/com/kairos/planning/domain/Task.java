package com.kairos.planning.domain;

import java.util.Arrays;

import com.kairos.planning.executioner.TaskPlanningGenerator;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.*;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias("Task")
@PlanningEntity(difficultyComparatorClass = TaskDifficultyComparator.class)
public class Task  extends TaskOrEmployee {
	public static final int DRIVING_TIME_MULTIPLIER = 1;
	Logger log= LoggerFactory.getLogger(this.getClass());
	private TaskType taskType;
	// Planning variables: changes during planning, between score calculations.
    @PlanningVariable(valueRangeProviderRefs = {"taskRange","employeeRange"},
            graphType = PlanningVariableGraphType.CHAINED)
    private TaskOrEmployee previousTaskOrEmployee;
    private String taskName;

  	public void setSlaDuration(int slaDuration) {
  		this.slaDuration = slaDuration;
  	}

	public Long[] getBrokenHardConstraints() {
		return brokenHardConstraints;
	}
	public Long getBrokenHardConstraintsSum() {
		Long sum=0l;
		if(brokenHardConstraints!=null){
			sum=Arrays.stream(brokenHardConstraints).mapToLong(Long::longValue).sum();
		}
		return sum;
	}

	public void setBrokenHardConstraints(Long[] brokenHardConstraints) {
		this.brokenHardConstraints = brokenHardConstraints;
	}

	private Long[] brokenHardConstraints;

    
    
    
    
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	//@PlanningVariable(valueRangeProviderRefs = {"employeeRange"})
	@AnchorShadowVariable(sourceVariableName = "previousTaskOrEmployee")
	private Employee employee;
    private String routeId;

    public Task() {
       // this.plannedStartTime=this.initialStartTime;
    }

    public Task(Long id, TaskOrEmployee previousTaskOrEmployee, Vehicle vehicle, int duration, Location location, Integer priority, TaskType taskType) {
        this.id=id;
        this.previousTaskOrEmployee = previousTaskOrEmployee;
       // this.vehicle = vehicle;
        //this.employee = employee;
        this.duration = duration;
        this.location = location;
        this.priority=priority;
        this.taskType=taskType;

    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

   // @CustomShadowVariable(variableListenerClass = TaskChangeListener.class,
	 //      sources = {@PlanningVariableReference(variableName = "previousTaskOrVehicle")})
    private Route route;
	//@XStreamAlias("initialStartTime")
    @XStreamConverter(TaskPlanningGenerator.JodaTimeConverter.class)
	private DateTime initialStartTime;
    @XStreamConverter(TaskPlanningGenerator.JodaTimeConverter.class)
    private DateTime initialEndTime;

	/*
	This is sum of reaching time and waiting time
	 */
    public DateTime getPlannedStartTime() {
        return plannedStartTime;
    }
    public Interval getPlannedInterval() {
        return new Interval(plannedStartTime, plannedStartTime.plusMinutes(duration));
    }
    public DateTime getPlannedEndTime() {
        return plannedStartTime.plusMinutes(duration);
    }

    public void setPlannedStartTime(DateTime plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }
    @CustomShadowVariable(variableListenerClass = StartTimeVariableListener.class,
           sources = {@PlanningVariableReference(variableName = "previousTaskOrEmployee")})
    private DateTime plannedStartTime;


	public DateTime getInitialStartTime() {
		return initialStartTime;
	}

	public void setInitialStartTime(DateTime initialStartTime) {
		this.initialStartTime = initialStartTime;
	}

	public DateTime getInitialEndTime() {
		return initialEndTime;
	}

	public void setInitialEndTime(DateTime initialEndTime) {
		this.initialEndTime = initialEndTime;
	}
    public DateTime getEarliestStartTime() {
        return initialStartTime.minusMinutes((int) slaDuration);
    }
	public DateTime getLatestStartTime() {
		return initialStartTime.plusMinutes((int) slaDuration);
	}
    public Interval getPossibleStartInterval() {
        return new Interval(getEarliestStartTime(),getLatestStartTime());
    }
    public boolean isPlannedInPossibleInterval(){
	    return getPossibleStartInterval().contains(getPlannedStartTime()) || getPossibleStartInterval().getStart().isEqual(getPlannedStartTime())
                || getPossibleStartInterval().getEnd().isEqual(getPlannedStartTime());
    }

	private boolean locked;
    private int indexInTaskType;
	public int getIndexInTaskType() {
		return indexInTaskType;
	}
	public void setIndexInTaskType(int indexInTaskType) {
		this.indexInTaskType = indexInTaskType;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public TaskType getTaskType() {
		return taskType;
	}
	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}
	public Citizen getCitizen() {
		return citizen;
	}
	public void setCitizen(Citizen citizen) {
		this.citizen = citizen;
	}
	public TaskOrEmployee getPreviousTaskOrEmployee() {
		return previousTaskOrEmployee;
	}
	public void setPreviousTaskOrEmployee(TaskOrEmployee previousTaskOrEmployee) {
		this.previousTaskOrEmployee = previousTaskOrEmployee;
	}
	/*public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}*/
	private Citizen citizen;
	private Integer priority;
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	private Long id;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
    public String getLabel(){
    	return id+"_"+priority+"_("+initialStartTime.toString("HH:mm")+"_"+initialEndTime.toString("HH:mm")+")P:"
				+plannedStartTime.toString("HH:mm")+"_"+getPlannedEndTime().toString("HH:mm")+"PiD:"+","+getIntervalIncludingArrivalAndWaiting().getStart().toString("HH:mm")+"_"+
                getIntervalIncludingArrivalAndWaiting().getEnd().toString("HH:mm")+",Dis:"+getDrivingMinutesFromPreviousTaskOrEmployee()+",wait:"
				+getWaitingMinutes();
    }

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	private Integer duration;

	public Integer getSlaDuration() {
		return slaDuration;
	}

	public void setSlaDuration(Integer slaDuration) {
		this.slaDuration = slaDuration;
	}

	private Integer slaDuration;
    public Integer getDuration() {
		//return getInterval().toDuration().getStandardMinutes();
		return duration;
	}
	public Long getDurationIncludingArrivalTime(){
		return getIntervalIncludingArrivalAndWaiting().toDuration().getStandardMinutes();
	}
	public Interval getInitialInterval(){
    	return new Interval(new DateTime(initialStartTime),new DateTime(initialEndTime));
	}
	public Integer getWaitingMinutes(){
			return getPlannedReachingTime().isAfter(getEarliestStartTime())?0
                    :new Interval(getPlannedReachingTime(),getEarliestStartTime()).toDuration().toStandardMinutes().getMinutes();
	}
	public Interval getIntervalIncludingArrivalAndWaiting(){

		/*if(this.getId().equals(2210575l) && previousTaskOrEmployee instanceof  Task && ((Task)previousTaskOrEmployee).getId().equals(2008028l)){
			System.out.println("?????????"+getDistanceFrom(previousTaskOrEmployee)+","+employee.getVehicle() .getSpeedKmpm()+","+getDistanceFrom(previousTaskOrEmployee) / employee.getVehicle() .getSpeedKmpm());
		}*/
		//TODO: this can not just be end time of last task
		/*DateTime taskStartTimeIncludingDriving= null;//previousTaskOrEmployee instanceof Task?new DateTime(((Task) previousTaskOrEmployee).getEndTime()):null;
		DateTime taskEndTime=getPlannedEndTime();
		if(taskStartTimeIncludingDriving==null || !taskStartTimeIncludingDriving.isBefore(taskEndTime)){
			taskStartTimeIncludingDriving=new DateTime(initialStartTime).minusMinutes((int)(getDrivingMinutesFromPreviousTaskOrEmployee())
			);
		}*/
		//DateTime taskStartTimeIncludingDriving=previousTaskOrVehicle instanceof Vehicle?new DateTime(startTime).minusMinutes(getReachingTime()):
		//		new DateTime(((Task)previousTaskOrVehicle).getEndTime());
        //Interval initalInterval = getInitialInterval();
        Interval interval = new Interval(getPlannedStartTime().minusMinutes(getDrivingMinutesFromPreviousTaskOrEmployee()+getWaitingMinutes()) ,getPlannedEndTime());
		return interval;
	}
	private Location location;

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public Vehicle getVehicle() {
		return employee==null?null:employee.getVehicle() ;
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	public Long getDistanceFromPreviousTaskOrEmployee() {
        if (previousTaskOrEmployee == null) {
            throw new IllegalStateException("This method must not be called when the previousTaskOrVehicle ("
                    + previousTaskOrEmployee + ") is not initialized yet.");
        }
        return getDistanceFrom(previousTaskOrEmployee);
    }
    public boolean isAfter(Task prevTask){
		//return !this.getIntervalIncludingArrival().getStart().isBefore(prevTask.getIntervalIncludingArrival().getEnd());
       // return !this.getIntervalIncludingArrivalAndWaiting().getStart().isBefore(prevTask.getIntervalIncludingArrivalAndWaiting().getEnd());
        return this.getIntervalIncludingArrivalAndWaiting().getStart().isAfter(prevTask.getIntervalIncludingArrivalAndWaiting().getEnd()) ||
                this.getIntervalIncludingArrivalAndWaiting().getStart().isEqual(prevTask.getIntervalIncludingArrivalAndWaiting().getEnd());
	}

	/*
	In minutes
	 */
	public int getDrivingMinutesFromPreviousTaskOrEmployee() {
		if (previousTaskOrEmployee == null) {
			throw new IllegalStateException("This method must not be called when the previousTaskOrVehicle ("
					+ previousTaskOrEmployee + ") is not initialized yet.");
		}
		if (employee.getVehicle() == null) {
			throw new IllegalStateException("This method must not be called when the vehicle ("
					+ employee + ") is not initialized yet.");
		}
		/*if(this.getId().equals(1979693l) && previousTaskOrEmployee instanceof  Task && ((Task)previousTaskOrEmployee).getId().equals(2047751l)){
			System.out.println("?????????"+getDistanceFrom(previousTaskOrEmployee)+","+employee.getVehicle() .getSpeedKmpm()+","+getDistanceFrom(previousTaskOrEmployee) / employee.getVehicle() .getSpeedKmpm());
		}*/
		int drivingTime = (int) Math.ceil((getDistanceFrom(previousTaskOrEmployee) / employee.getVehicle() .getSpeedKmpm())* DRIVING_TIME_MULTIPLIER);
		return drivingTime;
	}
	public DateTime getPlannedReachingTime(){
	    return previousTaskOrEmployee instanceof Task ? ((Task) previousTaskOrEmployee).getPlannedEndTime().plusMinutes(getDrivingMinutesFromPreviousTaskOrEmployee())
                :plannedStartTime.minusMinutes(getDrivingMinutesFromPreviousTaskOrEmployee());
    }
	public Long getDistanceFrom(TaskOrEmployee previousTaskOrEmployee) {
		/*if(previousTaskOrEmployee.getLocation().getId().equals(0l) && this.getLocation().getId().equals(13l) ||
				previousTaskOrEmployee.getLocation().getId().equals(13l) && this.getLocation().getId().equals(0l)){
			System.out.println("????????????"+previousTaskOrEmployee.getLocation().getDistanceFrom(this.getLocation()));
		}*/
		return previousTaskOrEmployee.getLocation().getDistanceFrom(this.getLocation());
	}
	public String toString(){
		return "T:"+id+"-"+priority+"-"+getDuration()+"-"+location.name;//+"-"+taskType.getRequiredSkillList();
	}
	public int getMissingSkillCount() {
        if (employee==null) {
            return 0;
        }
        int count = 0;
        for (Skill skill : taskType.getRequiredSkillList()) {
            if (!employee.getSkillSet().contains(skill)) {
                count++;
            }
        }
        return count;
    }
	public int getMissingSkillCountForEmployee(Employee employee) {
		if (employee == null) {
			return 0;
		}
		int count = 0;
		for (Skill skill : taskType.getRequiredSkillList()) {
			if (!employee.getSkillSet().contains(skill)) {
				count++;
			}
		}
		return count;
	}
	/*public int getMissingSkillCountForVehicle() {
		if (employee == null) {
			return 0;
		}
		if (vehicle== null) {
			return 0;
		}
		int count = 0;
		for (Skill skill : vehicle.getRequiredSkillList()) {
			if (!employee.getSkillSet().contains(skill)) {
				count++;
			}
		}
		return count;
	}*/
	/*public Long getEmployeeId(){
		return employee.getId();
	}*/
	public void setNextTask(Task nextTask){
	    this.nextTask=nextTask;
    }

    public boolean canAssignedEmployeeWork(){
		Interval interval=this.getIntervalIncludingArrivalAndWaiting();
		/*if(this.getId().equals(1979693l) && previousTaskOrEmployee instanceof  Task && ((Task)previousTaskOrEmployee).getId().equals(2047751l)){
			System.out.println("?????????"+getDistanceFrom(previousTaskOrEmployee)+","+employee.getVehicle() .getSpeedKmpm()+","+getDistanceFrom(previousTaskOrEmployee) / employee.getVehicle() .getSpeedKmpm());
		}*/
		return employee!=null && employee.canWorkThisInterval(interval)
				&& ((previousTaskOrEmployee instanceof Task)? !new DateTime(((Task)previousTaskOrEmployee).getInitialEndTime()).isAfter(interval.getStart()):true);
	}
	public Long getMinutesExceedingAvailability(){
		/*if(this.getId().equals(2210575l) && previousTaskOrEmployee instanceof  Task && ((Task)previousTaskOrEmployee).getId().equals(2008028l)){
			System.out.println("?????????"+getDistanceFrom(previousTaskOrEmployee)+","+employee.getVehicle() .getSpeedKmpm()+","+getDistanceFrom(previousTaskOrEmployee) / employee.getVehicle() .getSpeedKmpm());
		}*/
    	return employee==null?0l: employee.getExceedingMinutesForTaskInterval(this.getIntervalIncludingArrivalAndWaiting())
				;
	}
	public Long getMinutesExceedingAvailabilityForReachingUnit(){
		return employee==null?0l: employee.getExceedingMinutesForTaskInterval(getReachBackUnitInterval());
	}
	public boolean canAssignedEmployeeReachBack(){
		return (!isLastTaskOfRoute() || employee.canWorkThisInterval(getReachBackUnitInterval()));
	}
	public boolean canEmployeeWork(Employee employee){
		return employee!=null && employee.canWorkThisInterval(this.getIntervalIncludingArrivalAndWaiting()) ;
	}
	public boolean canEmployeeReachBack(Employee employee){
		return (!isLastTaskOfRoute() || employee.canWorkThisInterval(getReachBackUnitInterval()));
	}

	public boolean isLastTaskOfRoute(){
    	return nextTask==null;
	}
	public Interval getReachBackUnitInterval(){
		Interval interval = new Interval(getPlannedEndTime(),getPlannedEndTime().plusMinutes(getTimeToReachBackUnit().intValue()));
		return interval;
	}
	/*
	Return minutes
	 */
	public Long getTimeToReachBackUnit(){
		return Math.round(getDistanceFrom(employee)/employee.getVehicle().getSpeedKmpm());
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
}

