package com.kairos.planning.domain;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class TaskDifficultyComparator implements Comparator<Task>{
		 @Override
		    public int compare(Task a, Task b) {
		        return new CompareToBuilder()
						.append(a.getInitialStartTime(), b.getInitialStartTime())
		                .append(a.getPriority(), b.getPriority())
		                //.append(a.getTaskType().getRequiredSkillList().size(), b.getTaskType().getRequiredSkillList().size())

		                .toComparison();
		    }
	}
