package com.kairos.planning.domain;

import java.util.Comparator;

public class VehicleComparator implements Comparator<Vehicle> {


    @Override
    public int compare(Vehicle v1, Vehicle v2) {
       // return v1.getSpeedKmpm().compareTo(v2.getSpeedKmpm());
        return v1.getId().compareTo(v2.getId());
    }
}
