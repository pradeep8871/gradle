package com.planning.controller;

import com.planning.appConfig.OptaNotFoundException;
import com.planning.appConfig.ResponseHandler;
import com.planning.service.GraphHopperService;
import com.planning.service.KieService;
import com.planning.service.TaskPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/opta")
public class TaskPlanningController {

	private static final Logger log = LoggerFactory.getLogger(TaskPlanningController.class);

	@Autowired
	private TaskPlanningService taskPlanningService;
	@Autowired
	GraphHopperService graphHopperService;
	@Autowired
	KieService optaPlannerService;

	@RequestMapping(value = "/submitXml", method = RequestMethod.POST)
	Map<String, Object> submitXml(@RequestBody Map requestData) {
		log.info("call");
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false,
				taskPlanningService.getsolutionbyXml((String) requestData.get("xml")));// taskPlanningService.getAllPlanning());
	}

	@RequestMapping(value = "/getProblemIds", method = RequestMethod.GET)
	Map<String, Object> getProblemIds() {
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false,
				taskPlanningService.getAllPlanning());
	}

	@RequestMapping(value = "/getStatus", method = RequestMethod.GET)
	Map<String, Object> getStatus(@RequestParam String id) {
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false,
				taskPlanningService.getPlanningProblemByid(id));
	}

	/*@RequestMapping(value = "/getSolution", method = RequestMethod.GET)
	Map<String, Object> getSolution(@RequestParam String id) {
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false,
				taskPlanningService.getSolutionById(id));
	}*/

	@RequestMapping(value = "/deleteKieContainer", method = RequestMethod.POST)
	Map<String, Object> deleteKieContainer(@RequestParam String id) {
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false, null);
	}

	@RequestMapping(value = "/makeLocationData", method = RequestMethod.POST)
	Map<String, Object> makeLocationData() {
		graphHopperService.readLocations();
		return ResponseHandler.generateResponse("save Data sucessFully", HttpStatus.ACCEPTED, false, null);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleTodoNotFound(OptaNotFoundException ex) {
		log.error("Handling error with message: {}", ex.getMessage());
	}
}
