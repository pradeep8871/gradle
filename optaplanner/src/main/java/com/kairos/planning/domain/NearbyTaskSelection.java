package com.kairos.planning.domain;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public class NearbyTaskSelection implements NearbyDistanceMeter<Task, TaskOrEmployee> {
	
	@Override
    public double getNearbyDistance(Task task, TaskOrEmployee prevTaskOrEmployee) {
        long distance = task.getDistanceFrom(prevTaskOrEmployee);
        return distance;
    }
}
