package com.kairos.planning.domain;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class EmployeeStrengthComparator implements  Comparator<Employee> {
    @Override
    public int compare(Employee a, Employee b) {
        return new CompareToBuilder()
                .append(a.getAvailableMinutes(), b.getAvailableMinutes())
                .toComparison();
    }
}
