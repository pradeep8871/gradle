package com.planning.service;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.graphhopper.directions.api.client.ApiException;
import com.graphhopper.directions.api.client.api.GeocodingApi;
import com.graphhopper.directions.api.client.api.MatrixApi;
import com.graphhopper.directions.api.client.model.GeocodingResponse;
import com.graphhopper.directions.api.client.model.MatrixResponse;
import com.kairos.planning.domain.Location;
import com.kairos.planning.domain.LocationInfo;
import com.kairos.planning.solution.TaskPlanningSolution;
import com.planning.domain.LocationDistance;
import com.planning.repository.TaskPlanningRepository;
import com.thoughtworks.xstream.XStream;

@Service
public class GraphHopperService {
	Logger log = LoggerFactory.getLogger(GraphHopperService.class);

	@Autowired
	private TaskPlanningRepository taskPlanningRepository;

	// @Value("${graphhopper.key}")
	private String matrixKey = "a5e22e31-bdb5-443f-9aa9-a68c43b3b02e";

	public List<Location> getLocationDataWithLimit(List<Location> locations) {
		// getLatLongByAddress("");
		List<Location> locationlist = (List<Location>) taskPlanningRepository.findAll(Location.class);
		// makeLocationData(locations);
		MatrixResponse response = getMatrixData(locationlist.subList(0, 80));
		return assignLocationDataWithDistance(response, locationlist);
	}

	/*
	 * List<List<Location>> makeLocationsList(List<Location> locations){
	 * List<List<Location>> listOfLocationlist = new
	 * ArrayList<List<Location>>(); listOfLocationlist.add(locations.subList(0,
	 * 70)); listOfLocationlist.add(locations.subList(71, locations.size()));
	 * return listOfLocationlist; }
	 */

	public MatrixResponse getMatrixData(List<Location> locations) {
		MatrixApi api = new MatrixApi();
		List<String> point = getLatsLongsFromLocations(locations);
		String fromPoint = null;
		String toPoint = null;
		List<String> requiredFields = Arrays.asList("weights", "distances", "times");
		String vehicle = "car";
		MatrixResponse response = null;
		try {
			response = api.matrixGet(getApiKey(), point, fromPoint, toPoint, requiredFields, vehicle);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void makelocationData() {
		List<Location> locations = taskPlanningRepository.findAll(Location.class);
		List<LocationDistance> locationDistances = new ArrayList<>();
		for (Location location1 : locations) {
			for (Location location2 : locations) {
				if (!location1.equals(location2)) {
					MatrixResponse response = getdistance(location1, location2);
					if(response==null) continue;
					LocationDistance locationDistance = new LocationDistance();
					locationDistance.setFirstLocation(location1);
					locationDistance.setSecondLocation(location2);
					locationDistance.setDistance(response.getDistances().get(0).get(0).doubleValue() / 1000);
					locationDistance.setTime(response.getTimes().get(0).get(0).doubleValue());
					locationDistances.add(locationDistance);
				}
			}

		}
		taskPlanningRepository.saveList(locationDistances);
	}

	public MatrixResponse getdistance(Location location1, Location location2) {
		MatrixApi api = new MatrixApi();
		List<String> point = null;//getPoints(location1, location2);// getLatsLongsFromLocations(locations);
		String fromPoint =  new DecimalFormat("##.####").format(location1.getLongitude())+","+new DecimalFormat("##.####").format(location1.getLatitude());//"28.4595,77.0266";
		String toPoint =  location2.getLongitude()+","+location2.getLatitude();
		List<String> requiredFields = Arrays.asList("weights", "distances", "times");
		String vehicle = "car";
		MatrixResponse response = null;
		try {
			response = api.matrixGet(getApiKey(), point, fromPoint, toPoint, requiredFields, vehicle);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	private List<String> getPoints(Location location1, Location location2) {
		List<String> points = new ArrayList<>();
		points.add(location1.getLatitude() + "," + location1.getLongitude());
		points.add(location2.getLatitude() + "," + location2.getLongitude());
		return points;
	}

	public void readLocations() {
		XStream xstream = new XStream();
		xstream.processAnnotations(Location.class);
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.ignoreUnknownElements();
		TaskPlanningSolution solution = (TaskPlanningSolution) xstream
				.fromXML(new File("/media/pradeep/bak/Kairos/kairosCore/Task/optalo.xml"));
	//	taskPlanningRepository.saveList(solution.getLocationList());
		makelocationData();
	}

	private List<Location> assignLocationDataWithDistance(MatrixResponse response, List<Location> locations) {
		for (int i = 0; i < response.getDistances().size(); i++) {
			List<LocationInfo> locationInfos = new ArrayList<>();
			for (int k = 0; k < response.getDistances().get(i).size(); k++) {
				if (i == k)	continue;
				LocationInfo locationInfo = new LocationInfo();
				locationInfo.setDistance((response.getDistances().get(i).get(k).doubleValue()) / 1000);
				locationInfo.setTime(response.getTimes().get(i).get(k).doubleValue());
				locationInfo.setName(locations.get(k).getName());
				locationInfo.setLocationId(locations.get(k).getId());
				locationInfos.add(locationInfo);
			}
			locations.get(i).setLocationInfos(locationInfos);
		}
		return locations;
		// taskPlanningRepository.save(locations);
	}

	public List<Double> getLatLongByAddress(String address) {
		GeocodingApi geoApi = new GeocodingApi();
		address = "DK,Odense,Østergade,45 D";
		GeocodingResponse response;
		try {
			response = geoApi.geocodeGet(getApiKey(), address, "en", 1, false, "", "default");
			log.info("latitude " + response.getHits().get(0).getPoint().getLat() + " longitude "
					+ response.getHits().get(0).getPoint().getLat());
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getLatsLongsFromLocations(List<Location> locations) {
		List<String> allPoints = new ArrayList<>();
		for (Location location : locations) {
			allPoints.add(location.getLongitude() + "," + location.getLatitude());
		}
		return allPoints;
	}

	public String getApiKey() {
		/*
		 * Properties prop = new Properties(); try { prop.load(new
		 * FileInputStream(new File(
		 * "/media/pradeep/bak/multiOpta/task-planning/src/main/resources/taskplanner.properties"
		 * ))); String key = prop.getProperty("graphhopper.key");
		 * 
		 * } catch (IOException e) { e.printStackTrace(); }
		 */

		log.info(matrixKey);
		return matrixKey;
	}

	public void makeLocationData(List<Location> locations) {
		taskPlanningRepository.saveList(locations);
		/*
		 * List<String> allPoints = new ArrayList<>(); for (Location location :
		 * locations) { allPoints.add(location.getLatitude() + "," +
		 * location.getLongitude()); }
		 */
	}
}
