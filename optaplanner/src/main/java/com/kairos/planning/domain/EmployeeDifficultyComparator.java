package com.kairos.planning.domain;

import java.util.Comparator;

public class EmployeeDifficultyComparator implements  Comparator<Employee> {
    @Override
    public int compare(Employee o1, Employee o2) {

        //return o1.getAvailableMinutes().compareTo(o2.getAvailableMinutes());
        return o1.getAvailableMinutesAfterPlanning().compareTo(o2.getAvailableMinutesAfterPlanning());
    }
}
