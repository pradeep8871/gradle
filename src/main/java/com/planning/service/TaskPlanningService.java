package com.planning.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.planning.enums.PlanningStatus;
import com.planning.repository.TaskPlanningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.planning.domain.Affinity;
import com.kairos.planning.domain.AvailabilityRequest;
import com.kairos.planning.domain.Citizen;
import com.kairos.planning.domain.Employee;
import com.kairos.planning.domain.Location;
import com.kairos.planning.domain.LocationInfo;
import com.kairos.planning.domain.Skill;
import com.kairos.planning.domain.Task;
import com.kairos.planning.domain.TaskType;
import com.kairos.planning.domain.UnavailabilityRequest;
import com.kairos.planning.domain.Vehicle;
import com.kairos.planning.solution.TaskPlanningSolution;
import com.planning.domain.LocationDistance;
import com.planning.domain.PlanningProblem;
import com.thoughtworks.xstream.XStream;

@Service
public class TaskPlanningService {

	private static final Logger log = LoggerFactory.getLogger(TaskPlanningService.class);

	@Autowired
	private TaskPlanningRepository taskPlanningRepository;
	@Autowired
	private GraphHopperService graphHopperService;
	@Autowired
	KieService optaPlannerService;
	static{
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
	    System.setProperty("user.timezone", "UTC");
	}
	public void saveData() {

		// repository.save();
	}

	public void parseXmlToObject() {

		getObjectFromJson();
		// saveDatatoDB();
	}

	public Employee getObjectFromJson() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
		// mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			File file = new File("/media/pradeep/bak/multiOpta/task-planning/src/main/resources/task.json");
			return mapper.readValue(file, Employee.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Map> loadUnsolvedSolutionFromXML() {
		XStream xstream = new XStream();
		xstream.processAnnotations(Employee.class);
		xstream.ignoreUnknownElements();
		xstream.setMode(XStream.ID_REFERENCES);
		List<Employee> employees = (List<Employee>) xstream
				.fromXML(new File("/media/pradeep/bak/multiOpta/task-planning/src/main/resources/data/emp.xml"));
		List<Map> updatedEmployees = new ArrayList<Map>();
		List<Map> tasks = new ArrayList<>();
		int i = 1;
		for (Employee emp : employees) {
			Map<String, Object> map = new HashMap<>();
			makeEmployeeList(tasks, emp.getNextTask());
			map.put("employeeName", emp.getName());
			map.put("employeeNumber", i);
			map.put("employeeId", emp.getId());
			map.put("availableTime", emp.getAvailabilityList().get(0));
			map.put("nextTasks", tasks);
			tasks = new ArrayList<>();
			updatedEmployees.add(map);
			i++;
		}

		return updatedEmployees;
	}

	/*public void submitXmltoKie(Map requestData) {
		String containerid = (String) requestData.get("id");
		if (getPlanningProblemByid(containerid) == null) {
			TaskPlanningSolution taskPlanningSolution = makeTaskPlanningObjectFromXml((String) requestData.get("xml"));
			updateTaskplanning(taskPlanningSolution);
			String xml = makeXMLfromData(taskPlanningSolution);
			String response = optaPlannerService.makeKieContainer(containerid);
			if (response.contains("SUCCESS")) {
				response = optaPlannerService.makeKieServer(containerid);
				if (response.contains("NOT_SOLVING")) {
					optaPlannerService.xmlSendToKieServer(xml, containerid);
					savePlanningProblem(containerid);
				}
			}
		}
	}*/

	private TaskPlanningSolution makeTaskPlanningObjectFromXml(String xml) {
		XStream xstream = new XStream();
		xstream.processAnnotations(Employee.class);
		xstream.processAnnotations(Citizen.class);
		xstream.processAnnotations(Task.class);
		xstream.processAnnotations(TaskType.class);
		xstream.processAnnotations(Skill.class);
		xstream.processAnnotations(Vehicle.class);
		xstream.processAnnotations(TaskPlanningSolution.class);
		xstream.processAnnotations(Affinity.class);
		xstream.processAnnotations(Location.class);
		xstream.processAnnotations(AvailabilityRequest.class);
		xstream.processAnnotations(UnavailabilityRequest.class);
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.ignoreUnknownElements();
		TaskPlanningSolution taskPlanningSolution = (TaskPlanningSolution) xstream.fromXML(xml);
		// taskPlanningSolution.setLocationList(graphHopperService.getLocationData(taskPlanningSolution.getLocationList()));
		return taskPlanningSolution;
	}

	private PlanningProblem savePlanningProblem(String id) {
		PlanningProblem planningProblem = new PlanningProblem();
		planningProblem.setPlanningId(id);
		planningProblem.setStatus(PlanningStatus.SOLVING);
		return (PlanningProblem) taskPlanningRepository.save(planningProblem);
	}

	public String makeXMLfromData(TaskPlanningSolution taskPlanningSolution) {
		XStream xStream = new XStream();
		xStream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		String xml = xStream.toXML(taskPlanningSolution);
		try {
			PrintWriter out = new PrintWriter("/media/pradeep/bak/multiOpta/optaPlanningTask/unplannedTask.xml");
			out.println(xml);
			out.close();
			System.out.println("file complete");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		return xml;
	}

	public void updateTaskplanning(TaskPlanningSolution taskPlanningSolution) {
		List<Task> tasks = new ArrayList<>();
		List<Location> locations = new ArrayList<>();
		List<LocationDistance> locationDistances = taskPlanningRepository.findAll(LocationDistance.class);
		for (Employee emp : taskPlanningSolution.getEmployeeList()) {
			Location location = getLocationWithDistanceData(locationDistances, emp.getLocation());
			emp.setLocation(location);
			if (!locations.contains(location))
				locations.add(location);
		}
		for (Vehicle vehicle : taskPlanningSolution.getVehicleList()) {
			vehicle.setLocation(getLocationWithDistanceData(locationDistances, vehicle.getLocation()));
		}
		for (Task task : taskPlanningSolution.getTaskList()) {
			Location location = getLocationWithDistanceData(locationDistances, task.getLocation());
			if (location != null && location.getLocationInfos() != null) {
				task.setLocation(location);
				if (!locations.contains(location))
					locations.add(location);
				tasks.add(task);
			}
		}
		taskPlanningSolution.setLocationList(locations);
		taskPlanningSolution.setTaskList(tasks);
	}

	public Map getsolutionbyXml(String xml) {
		taskPlanningRepository.save(new PlanningProblem());
		XStream xstream = new XStream();
		xstream.processAnnotations(TaskPlanningSolution.class);
		xstream.ignoreUnknownElements();
		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		//xstream.setMode(XStream.ID_REFERENCES);
		
		TaskPlanningSolution solution = (TaskPlanningSolution) xstream.fromXML(xml);
		/*
		 * PlanningProblem planningProblem = getPlanningProblemByid(id);
		 * planningProblem.setStatus(PlanningStatus.SOLVED);
		 * taskPlanningRepository.save(planningProblem);
		 */
		List<Map> updatedEmployees = new ArrayList<Map>();
		List<Map> availableButNotAssigned = new ArrayList<Map>();
		List<Map> tasksList = new ArrayList<>();
		
		List<Map> unAvailableEmp = new ArrayList<>();
		int i = 1;
		int assignedEmp=0, availableEmps=0;
		for (Employee emp : solution.getEmployeeList()) {
			if(emp.getNextTask()!=null && emp.getAvailabilityList().size()>0){
				Map<String, Object> map = new HashMap<>();
				List<Map> tasks = new ArrayList<>();
				makeEmployeeList(tasks, emp.getNextTask());
				map.put("employeeName", emp.getName());
				map.put("employeeNumber", i);
				map.put("vehicleId", emp.getVehicle()==null?-1:emp.getVehicle().getId());
				map.put("employeeId", emp.getId());
				map.put("employeeLocation", emp.getLocation());
				map.put("availableTime", emp.getAvailabilityList().get(0));
				map.put("plannedTime", emp.getWorkIntervalAsString());
				map.put("nextTasks", tasks);
				updatedEmployees.add(map);
				assignedEmp++;
				availableEmps++;
				i++;
			}else if(emp.getAvailabilityList().size()>0){
				Map<String, Object> map = new HashMap<>();
				map.put("employeeName", emp.getName());
				map.put("employeeNumber", i);
				map.put("vehicleId", emp.getVehicle()==null?-1:emp.getVehicle().getId());
				map.put("employeeId", emp.getId());
				map.put("employeeLocation", emp.getLocation());
				map.put("availableTime", emp.getAvailabilityList().get(0));
				map.put("plannedTime", emp.getWorkIntervalAsString());
				map.put("nextTasks", new ArrayList<>());
				availableButNotAssigned.add(map);
				availableEmps++;
			}
			else{
				Map<String,Object> map = new HashMap<>();
				map.put("employeeName", emp.getName());
				map.put("employeeNumber", i);
				map.put("employeeId", emp.getId());
				unAvailableEmp.add(map);
			}
		}
		for (Task task : solution.getTaskList()) {
			if(task.getEmployee()==null){
				Map<String,Object> map = new HashMap<>();
				map.put("id", task.getId());
				map.put("hardcontrants", task.getBrokenHardConstraints());
				map.put("taskLocation", task.getLocation());
				map.put("taskName", task.getTaskName());
				map.put("citizenName", task.getCitizen().getName());
				map.put("startTime", task.getInitialStartTime().toDate());
				map.put("endTime", task.getInitialEndTime().toDate());
				tasksList.add(map);
			}
		}
		Collections.sort(tasksList, new Comparator<Map>(){

			@Override
			public int compare(Map arg0, Map arg1) {
				//((Date)arg0.get("startTime"))
				return ((Date)arg0.get("startTime")).compareTo((Date)arg1.get("startTime"));
			}
			
			
		});
		updatedEmployees.addAll(availableButNotAssigned);
		Map<String,Object> resp = new HashMap<>();
		resp.put("taskListSize", solution.getTaskList().size());
		resp.put("citizenList", solution.getCitizenList().size());
		resp.put("employeeList", solution.getEmployeeList().size());
		resp.put("locationList", solution.getLocationList().size());
		resp.put("vehicleList", solution.getVehicleList().size());
		resp.put("assignedEmp", assignedEmp);
		resp.put("unAssignEmp", unAvailableEmp.size());
		resp.put("plannerScore", solution.getScore().toString());
		
		resp.put("avialableEmp", availableEmps);
		resp.put("unassignTaskList", tasksList.size());
		resp.put("unassignTask", tasksList);
		resp.put("unavailableEmployees", unAvailableEmp);
		
		resp.put("emplyees", updatedEmployees);
		return resp;
	}
	

	private Location getLocationWithDistanceData(List<LocationDistance> locationDistances, Location location) {
		List<LocationInfo> locationInfos = new ArrayList<>();
		for (LocationDistance locationDistance : locationDistances) {
			if (location.getLatitude() == 0.0000000000000000)
				return null;
			if (locationDistance.getFirstLocation().getId().equals(location.getId())) {
				LocationInfo locationInfo = new LocationInfo();
				locationInfo.setLocationId(locationDistance.getSecondLocation().getId());
				locationInfo.setDistance(locationDistance.getDistance());
				locationInfo.setTime(locationDistance.getTime());
				locationInfos.add(locationInfo);
			}
			location.setLocationInfos(locationInfos);
		}

		return location;
	}

	private Location getOptaLocationByTask(List<Location> locations, Task task) {
		for (Location location : locations) {
			if (task.getLocation().getLatitude() == location.getLatitude()
					&& task.getLocation().getLongitude() == location.getLongitude())
				return location;
		}
		return null;
	}

	public void makeEmployeeList(List<Map> tasks, Task nextTask) {
		if (nextTask != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", nextTask.getId());
			map.put("taskName", nextTask.getTaskName());
			map.put("citizenName", nextTask.getCitizen().getName());
			map.put("startTime", nextTask.getInitialStartTime().toDate());
			map.put("plannedStartTime", nextTask.getPlannedStartTime().toDate());
			map.put("plannedEndTime", nextTask.getPlannedEndTime().toDate());
			map.put("brokenhardcontrants", nextTask.getBrokenHardConstraintsSum()<0);
			map.put("hardcontrants", nextTask.getBrokenHardConstraints());
			map.put("endTime", nextTask.getInitialEndTime().toDate());
			map.put("taskLocation", nextTask.getLocation());
			map.put("arrivaltime", nextTask.getDrivingMinutesFromPreviousTaskOrEmployee());
			map.put("waitingtime", nextTask.getWaitingMinutes());
			if(nextTask.getNextTask()==null) map.put("timeReachToUnit", nextTask.getTimeToReachBackUnit());
			tasks.add(map);
			makeEmployeeList(tasks, nextTask.getNextTask());
		}
	}

	public List<PlanningProblem> getAllPlanning() {
		return (List<PlanningProblem>) taskPlanningRepository.getAll(PlanningProblem.class);
	}

	private void saveDatatoDB(List<Task> tasks) {
		taskPlanningRepository.save(tasks);
		log.info("data saved succesfully");
	}

/*
	public List<Map> getSolutionById(String id) {
		String xml = optaPlannerService.getSolutionFromKieServer(id);
		if (!xml.contains("NOT_SOLVING"))
			return null;
		String solutionString = "<com.kairos.planning.solution.TaskPlanningSolution>"
				+ (xml.substring(xml.indexOf("<vehicleList>"), xml.indexOf("</solver-instance>")))
						.replace("</best-solution>", "</com.kairos.planning.solution.TaskPlanningSolution>");
		XStream xstream = new XStream();
		xstream.processAnnotations(TaskPlanningSolution.class);
		xstream.ignoreUnknownElements();
		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		TaskPlanningSolution solution = (TaskPlanningSolution) xstream.fromXML(solutionString);
		PlanningProblem planningProblem = getPlanningProblemByid(id);
		planningProblem.setStatus(PlanningStatus.SOLVED);
		taskPlanningRepository.save(planningProblem);
		List<Map> updatedEmployees = new ArrayList<Map>();
		List<Map> tasks = new ArrayList<>();
		int i = 1;
		for (Employee emp : solution.getEmployeeList()) {
			Map<String, Object> map = new HashMap<>();
			makeEmployeeList(tasks, emp.getNextTask());
			map.put("employeeName", emp.getName());
			map.put("employeeNumber", i);
			map.put("employeeId", emp.getId());
			if (!emp.getAvailabilityList().isEmpty())
				map.put("availableTime", emp.getAvailabilityList().get(0));
			map.put("nextTasks", tasks);
			tasks = new ArrayList<>();
			updatedEmployees.add(map);
			i++;
		}
		return updatedEmployees;

	}
*/

	public PlanningProblem getPlanningProblemByid(String id) {
		return (PlanningProblem) taskPlanningRepository.findOne("planningId", id, PlanningProblem.class);
	}
}
