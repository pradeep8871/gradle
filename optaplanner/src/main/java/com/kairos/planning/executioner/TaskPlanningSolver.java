package com.kairos.planning.executioner;

import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.*;

import com.kairos.planning.domain.*;
import com.kairos.planning.graphhopper.GraphHopper;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.ArrayUtils;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kairos.planning.solution.TaskPlanningSolution;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TaskPlanningSolver {
	public static String config = "com/kairos/planning/configuration/OutdoorTaskPlanning.solver.xml";
	Logger log= LoggerFactory.getLogger(TaskPlanningSolver.class);
	Solver<TaskPlanningSolution> solver;
	static{
        System.setProperty("user.timezone", "UTC");
    }
	public TaskPlanningSolver(){
		SolverFactory<TaskPlanningSolution> solverFactory = SolverFactory.createFromXmlResource(config);
		solver = solverFactory.buildSolver();
	}

	public void runSolver() {
		try {
			printSolvedSolution(getSolution());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private void printSolvedSolution(Object[] solution) {
		log.info("-------Printing Solution:-------");
		//List<Task> tasks= solution.getTaskList();
		//log.info("-------Tasks:-------");
		log.info(toDisplayString(solution));
		//tasks.forEach(task->log.info(task.toString()));
		log.info("-------Printing Solution Finished:-------");
		
	}
	public Object[] getSolution() {
		
		TaskPlanningSolution unsolvedSolution=getUnsolvedSolution();
		GraphHopper graphHopper = new GraphHopper();
		graphHopper.getLocationData(unsolvedSolution.getLocationList());

		log.info("Number of locations:"+unsolvedSolution.getLocationList().size());
		log.info("Number of Tasks:"+unsolvedSolution.getTaskList().size());
		log.info("Number of Vehicless:"+unsolvedSolution.getVehicleList().size());
		log.info("Number of Employees:"+unsolvedSolution.getEmployeeList().size());
		Long availableEmp=0l, availableMinutes=0l;
		for(Employee emp: unsolvedSolution.getEmployeeList()){
			availableMinutes+=emp.getAvailableMinutes();
			if(emp.getAvailableMinutes()>0){
				availableEmp++;
			}
		}
		log.info("Number of Available Employees:"+availableEmp);
		printUnsolvedSolution(unsolvedSolution);
		long start= System.currentTimeMillis();
		TaskPlanningSolution solution = solver.solve(unsolvedSolution);
		log.info("Solver took secs:"+(System.currentTimeMillis()-start)/1000);
		try {
			toXml(unsolvedSolution, null, "solution");
		}catch(Exception e){
			e.printStackTrace();
		}
		ScoreDirector director=solver.getScoreDirectorFactory().buildScoreDirector();
		director.setWorkingSolution(solution);
		Map<Task,Indictment> indictmentMap=director.getIndictmentMap();

		//printEntitiesThatBrokeContraints(indictmentMap);
		//return solver.getBestSolution();
		return new Object[]{solution,indictmentMap,director.getConstraintMatchTotals()};
	}

	private long sumHardScore(BendableLongScore score){
		long hardScoreSum=Arrays.stream(score.getHardScores()).count();
		return hardScoreSum;
	}

	private void printEntitiesThatBrokeContraints(Map<Task,Indictment> indictmentMap) {
		log.info("***********Indictment***********");
		indictmentMap.forEach((task,indictment)->{
			log.info(task.getLabel()+":"+indictment.getScoreTotal());

		});
		log.info("***********End of Indictment***********");
	}

	private void printDistanceMatrix(TaskPlanningSolution unsolvedSolution) {
		List<Location> locations = unsolvedSolution.getLocationList();
		locations.forEach(location -> {
			log.info("--["+location.getName()+"]--");
			locations.forEach(location1 -> {
				log.info(location1.getName()+":"+location.getDistanceFrom(location1)+","+location1.getDistanceFrom(location));
			});
		});
	}

	private void printUnsolvedSolution(TaskPlanningSolution unsolvedSolution) {
		//printDistanceMatrix(unsolvedSolution);
		List<Location> locations= unsolvedSolution.getLocationList();
		List<Citizen> citizens= unsolvedSolution.getCitizenList();
		List<Task> tasks= unsolvedSolution.getTaskList();
		List<Vehicle> vehicles= unsolvedSolution.getVehicleList();
		List<Employee> employees= unsolvedSolution.getEmployeeList();
		log.info("-------Printing problem dataset:-------");
		/*log.info("-------Locations:-------");
		locations.forEach(location->log.info(location.toString()));
		log.info("-------Citizens:-------");
		citizens.forEach(citizen->log.info(citizen.toString()));
		log.info("-------Vehicles:-------");
		vehicles.forEach(vehicle->log.info(vehicle.toString()));
		log.info("-------Tasks:-------");
		tasks.forEach(task->log.info(task.toString()));*/
		log.info("-------Employees:-------");
		employees.forEach(employee->{
			if(employee.getAvailableMinutes()>0)
				log.info(employee.getName()+":"+employee.getAvailableMinutes().toString()+employee.getAvailableMinutesAsString());
		});

		log.info("-------Printing problem dataset completed-------");
	}
	private TaskPlanningSolution getUnsolvedSolution() {
		//return new TaskPlanningGenerator().loadUnsolvedSolution();
		
		return new TaskPlanningGenerator().loadUnsolvedSolutionFromXML();
	}
	public String toDisplayString(Object[] array) {

		TaskPlanningSolution solution=(TaskPlanningSolution)array[0];
		unassignTaskFromUnavailableEmployees(solution);
		Map<Task,Indictment> indictmentMap =(Map<Task,Indictment>) array[1];
        Collection<ConstraintMatchTotal> constraintMatchTotals= (Collection<ConstraintMatchTotal>) array[2];
        constraintMatchTotals.forEach(constraintMatchTotal -> {
            log.info(constraintMatchTotal.getConstraintName()+":"+"Total:"+constraintMatchTotal.toString()+"=="+"Reason(entities):");
            constraintMatchTotal.getConstraintMatchSet().forEach(constraintMatch -> {
                constraintMatch.getJustificationList().forEach(o -> {
                    log.info("---"+o);
                });
            });

        });

        StringBuilder displayString = new StringBuilder();
        StringBuilder taskChain = new StringBuilder("Task Chain:\n");
        displayString.append("\nTask assignment:");
        Map<String,Long> empMins= new HashMap<String,Long>();
        Set<Long> processChainPivot= new HashSet<Long>();
		Map<Long,Route> routes= new HashMap<Long,Route>();
		solution.getTaskList().forEach(task->{
			task.setBrokenHardConstraints(ArrayUtils.toObject(((BendableLongScore) indictmentMap.get(task).getScoreTotal()).getHardScores()));
		});
		try {
			toXml(solution, indictmentMap,"solution");
		}catch(Exception e){
			e.printStackTrace();
		}

		log.info("---emp mins:---"+empMins);
		StringBuilder employeeRoute= new StringBuilder();
		employeeRoute.append("\nEmployee assignment:\n");
        solution.getEmployeeList().forEach(employee->{
        	if(employee.getNextTask()==null) return;
			employeeRoute.append("\nEmployee :"+employee+", Vehicle:"+employee.getVehicle()+", Interval:"+employee.getWorkIntervalAsString()+""
					+(indictmentMap.get(employee)==null?"":(Arrays.toString(((BendableLongScore) indictmentMap.get(employee).getScoreTotal()).getHardScores())))
					+"\n");
        	Task nextTask=employee.getNextTask();
			//Employee employee= vehicle.getEmployee();
			//vehicleRoute.append("Employee :"+employee+"\n");
        	while(nextTask!=null){
				employeeRoute.append(nextTask.getLabel());//employeeRoute.append(nextTask.getLabel()+"->["+nextTask.getVehicle()+"]");
				//vehicleRoute.append("-["+nextTask.getLabel()+"]-");
				//vehicleRoute.append("----");
				if(indictmentMap.containsKey(nextTask) && sumHardScore((BendableLongScore) indictmentMap.get(nextTask).getScoreTotal())>0){
					employeeRoute.append(""+Arrays.toString(((BendableLongScore) indictmentMap.get(nextTask).getScoreTotal()).getHardScores())+"");
				}
				employeeRoute.append("->");
        		nextTask=nextTask.getNextTask();
        	}
			employeeRoute.append("\n");
        });
        //return displayString.append("\n").append(taskChain).toString();
		return employeeRoute.toString();
    }

	private List<Task> unassignTaskFromUnavailableEmployees(TaskPlanningSolution solution) {
		List<Task> unassignedTask = new ArrayList<>();
		solution.getEmployeeList().forEach(employee->
		{
			if(employee.getAvailableMinutes()>0l) return;
			Task task =employee.getNextTask();
			Task nextTask= task;
			employee.setNextTask(null);

			while(nextTask!=null){
				nextTask.setEmployee(null);
				nextTask.setPreviousTaskOrEmployee(null);
				unassignedTask.add(nextTask);
				task=nextTask;
				nextTask=nextTask.getNextTask();
				task.setNextTask(null);
			}
		});
		log.info("Unassigned Tasks:"+unassignedTask.size());
		return unassignedTask;
	}

	private void toXml(TaskPlanningSolution solution, Map<Task, Indictment> indictmentMap, String fileName) throws Exception {
		XStream xstream = new XStream();
		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		xstream.toXML(solution,new PrintStream(new File("/media/pradeep/bak/solved.xml")));
	}
	/*public void writeXml(String xmlString) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
			// Use String reader
			Document document = builder.parse( new InputSource(
					new StringReader( xmlString ) ) );

			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			Source src = new DOMSource( document );
			Result dest = new StreamResult( new File("/media/pradeep/bak/solved.xml") );
			aTransformer.transform( src, dest );
	}
*/

}
