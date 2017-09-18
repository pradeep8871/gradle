package com.planning.appConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ResponseHandler {

	public static Map<String , Object> generateResponse(String message,HttpStatus status, boolean error,Object responseObj){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map.put("MESSAGE", message);
			map.put("STATUS", status);
			map.put("ERROR", error);
			map.put("DATA", responseObj);
			map.put("TIME_STAMP", new Date());
			return map;
		} catch (Exception e) {
			map.clear();
			map.put("MESSAGE", e.getMessage());
			map.put("STATUS", HttpStatus.INTERNAL_SERVER_ERROR);
			map.put("TIME_STAMP", new Date());			
			return map;
		}
		}
}
