package com.kairos.planning.executioner;

import org.junit.Test;


//@PropertySource("/media/pradeep/bak/multiOpta/task-planning/src/main/resources/taskplanner.properties")
public class TaskPlanningSolverTest {

	public void test() {
		//RequestedTask requestedTask =  new RequestedTask();
		//requestedTask.loadXMLFromDB();
		new TaskPlanningSolver().runSolver();
	}
	/*@Test
	@Ignore
	public  void testKieServiceApi() {
		KieServicesConfiguration kieServicesConfiguration = new KieServicesConfigurationImpl("http://localhost:8080/kie-server/services/rest/server", "kieserver", "kieserver", 100000000000000l);
		KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(kieServicesConfiguration);
	}*/

}
